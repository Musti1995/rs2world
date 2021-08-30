package be.nfm.rs2.server;

import be.nfm.rs2.server.client.Client;
import be.nfm.rs2.server.client.ClientPool;
import be.nfm.rs2.server.events.ServerEventContainer;
import be.nfm.rs2.util.ArrayWrapper;
import be.nfm.rs2.util.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ConfigurationProperties(prefix = "rs2.server")
public final class Server implements Runnable {

    private final Timer acceptTimer;
    private final long acceptDelay;
    private final ServerContext ctx;

    public Server(@Value("port") int port,
                  @Value("host") String host,
                  @Value("accept-delay") long acceptDelay,
                  @Value("accept-batch") int acceptBatch,
                  @Value("workers") int workers,
                  @Value("buffer-size") int bufferSize,
                  @Value("clients.buffer-size") int clientBufferSize,
                  @Value("clients.capacity") int capacity,
                  @Value("clients.login-queue-capacity") int loginQueueCapacity,
                  SecureRandom rng) throws IOException {
        ConcurrentHashMap<SelectionKey, Client> clientMap = new ConcurrentHashMap<>();
        acceptTimer = new Timer(System::currentTimeMillis);
        this.acceptDelay = acceptDelay;
        ClientPool clientPool = new ClientPool(capacity + loginQueueCapacity, clientBufferSize);
        ExecutorService executorService = Executors.newFixedThreadPool(workers);

        Selector selector = Selector.open();
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        ThreadLocal<ByteBuffer> localBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(bufferSize));

        ArrayWrapper<Client> activeClients = ArrayWrapper.wrap(new Client[capacity]);
        ArrayWrapper<Client> loginQueue = ArrayWrapper.wrap(new Client[loginQueueCapacity]);

        ctx = new ServerContext(serverSocketChannel,
                selector,
                clientPool,
                executorService,
                acceptBatch,
                localBuffer,
                clientMap,
                activeClients,
                loginQueue,
                rng);
    }

    @Override
    public void run() {
        try {
            ctx.selector().selectNow();
            Iterator<SelectionKey> keys = ctx.selector().selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (key.isAcceptable() && acceptTimer.elapsed(acceptDelay)) {
                    acceptTimer.reset();
                    ctx.executor().submit(ServerEventContainer.newAcceptEvent(ctx));
                }
                if (key.isReadable()) ctx.executor().submit(ServerEventContainer.newReadEvent(ctx, key));
                keys.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public ServerContext context() {
        return ctx;
    }

}
