package be.nfm.rs2.server;

import be.nfm.rs2.client.Client;
import be.nfm.rs2.client.ClientPool;
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
import java.nio.channels.SocketChannel;
import java.util.HashMap;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
@ConfigurationProperties(prefix = "rs2.server")
public final class Server implements Runnable {

    private final ConcurrentHashMap<SelectionKey, Client> clientMap;
    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ExecutorService executorService;
    private final ClientPool clientPool;
    private final Timer acceptTimer;
    private final long acceptDelay;
    private final int acceptBatch;
    private final ServerContext ctx;
    private final ThreadLocal<ByteBuffer> localBuffer;
    private final ArrayWrapper<Client> activeClients;
    private final ArrayWrapper<Client> loginQueue;

    public Server(@Value("port") int port,
                  @Value("host") String host,
                  @Value("accept-delay") long acceptDelay,
                  @Value("accept-batch") int acceptBatch,
                  @Value("workers") int workers,
                  @Value("buffer-size") int bufferSize,
                  @Value("clients.buffer-size") int clientBufferSize,
                  @Value("clients.capacity") int capacity,
                  @Value("clients.login-queue-capacity") int loginQueueCapacity) throws IOException {
        clientMap = new ConcurrentHashMap<>();
        acceptTimer = new Timer(System::currentTimeMillis);
        this.acceptDelay = acceptDelay;
        this.acceptBatch = acceptBatch;
        this.clientPool = new ClientPool(capacity + loginQueueCapacity, clientBufferSize);
        executorService = Executors.newFixedThreadPool(workers);

        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        localBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(bufferSize));

        activeClients = ArrayWrapper.wrap(new Client[capacity]);
        loginQueue = ArrayWrapper.wrap(new Client[loginQueueCapacity]);

        ctx = new ServerContext(serverSocketChannel,
                selector,
                clientPool,
                executorService,
                acceptBatch,
                localBuffer,
                clientMap,
                activeClients,
                loginQueue);
    }

    @Override
    public void run() {
        try {
            selector.selectNow();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (key.isAcceptable() && acceptTimer.elapsed(acceptDelay).value()) {
                    acceptTimer.reset();
                    executorService.submit(ServerEventContainer.newAcceptEvent(ctx));
                }
                if (key.isReadable()) executorService.submit(ServerEventContainer.newReadEvent(ctx, key));
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
