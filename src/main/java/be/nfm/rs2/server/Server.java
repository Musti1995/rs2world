package be.nfm.rs2.server;

import be.nfm.rs2.client.Client;
import be.nfm.rs2.client.ClientPool;
import be.nfm.rs2.server.events.ServerEventContainer;
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

    public Server(@Value("port") int port,
                  @Value("host") String host,
                  @Value("accept-delay") long acceptDelay,
                  @Value("accept-batch") int acceptBatch,
                  @Value("workers") int workers,
                  @Value("buffer-size") int bufferSize,
                  ClientPool clientPool) throws IOException {
        clientMap = new ConcurrentHashMap<>();
        acceptTimer = new Timer(System::currentTimeMillis);
        this.acceptDelay = acceptDelay;
        this.acceptBatch = acceptBatch;
        this.clientPool = clientPool;
        executorService = Executors.newFixedThreadPool(workers);

        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        serverSocketChannel.configureBlocking(false);

        localBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(bufferSize));

        ctx = new ServerContext(serverSocketChannel,
                selector,
                clientPool,
                executorService,
                acceptBatch,
                localBuffer,
                clientMap);
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

    private void accept() {
        acceptTimer.reset();
        try {
            for (int i = 0; i < acceptBatch; i++) {
                SocketChannel channel = serverSocketChannel.accept();
                if (channel == null) return;
                channel.configureBlocking(false);

                Client client = clientPool.request();
                if (client == null) {
                    channel.write(ServerResponse.PREMADE_WORLD_FULL);
                    channel.close();
                    return;
                }

                SelectionKey key = channel.register(selector, SelectionKey.OP_READ);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
