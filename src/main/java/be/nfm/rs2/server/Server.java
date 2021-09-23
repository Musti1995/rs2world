package be.nfm.rs2.server;

import be.nfm.rs2.client.*;
import be.nfm.rs2.login.LoginStatus;
import be.nfm.rs2.login.exceptions.LoginException;
import be.nfm.rs2.util.Timer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class Server implements Runnable {

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ExecutorService executor;

    private final ClientService clientService;

    private final Timer acceptTimer;
    private final long acceptDelay;
    private final int acceptBatch;

    public Server(ClientService clientService,
                  @Value("${rs2.server.port}") int port,
                  @Value("${rs2.server.host}") String host,
                  @Value("${rs2.server.workers}") int workers,
                  @Value("${rs2.server.accept-delay}") long acceptDelay,
                  @Value("${rs2.server.accept-batch}") int acceptBatch) throws IOException {
        this.clientService = clientService;
        this.acceptBatch = acceptBatch;
        this.acceptDelay = acceptDelay;

        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        acceptTimer = new Timer(System::currentTimeMillis);
        executor = Executors.newFixedThreadPool(workers);
    }

    @Override
    public void run() {
        try {
            selector.selectNow();
            Iterator<SelectionKey> keys = selector.selectedKeys().iterator();
            while (keys.hasNext()) {
                SelectionKey key = keys.next();
                if (key.isAcceptable() && acceptTimer.elapsed(acceptDelay)) {
                    acceptTimer.reset();
                    executor.submit(this::batchAcceptConnections);
                }
                if (key.isReadable()) executor.submit(() -> read(key));
                keys.remove();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void batchAcceptConnections() {
        try {
            for (int i = 0; i < acceptBatch; i++) {
                SocketChannel channel = serverSocketChannel.accept();
                if (channel == null) return;
                configureConnection(channel);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void configureConnection(SocketChannel channel) throws IOException {
        Client client = null;
        SelectionKey newKey = null;
        try {
            channel.configureBlocking(false);
            newKey = channel.register(selector, SelectionKey.OP_READ);

            client = clientService.register(newKey);

            sendStatus(channel, client.outBuffer(), LoginStatus.CONNECTED);
            client.setState(ClientState.CONNECTING);
        } catch(LoginException le) {
            sendStatus(channel, buffer, le.getStatus());
            if (client != null) client.cleanAndReturn();
            if (newKey != null) {
                clientMap.remove(newKey);
                newKey.cancel();
            }
        }
    }

    private void sendStatus(SocketChannel channel, ByteBuffer buffer, int status) throws IOException {
        buffer.put((byte) status);
        buffer.flip();
        channel.write(buffer);
    }

    private void read(SelectionKey key) {
        try {
            Client client = clientService.getClient(key);
            if (client == null) {
                key.cancel();
                return;
            }
            client.readIncomingPackets();
        } catch(IOException ioe) {
            clientService.deregister(key);
        }
    }

}