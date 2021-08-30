package be.nfm.rs2.server;

import be.nfm.rs2.client.*;
import be.nfm.rs2.login.LoginService;
import be.nfm.rs2.login.LoginStatus;
import be.nfm.rs2.login.exceptions.LoginException;
import be.nfm.rs2.packets.PacketDecoderService;
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
import java.security.SecureRandom;
import java.util.Iterator;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Component
public final class Server implements Runnable {

    private final ServerSocketChannel serverSocketChannel;
    private final Selector selector;
    private final ClientPool clientPool;
    private final ExecutorService executor;
    private final ThreadLocal<ByteBuffer> localBuffer;
    private final SecureRandom rng;

    private final ConcurrentHashMap<SelectionKey, Client> clientMap;
    private final PacketDecoderService packetDecoderService;
    private final LoginService loginService;

    private final Timer acceptTimer;
    private final long acceptDelay;
    private final int acceptBatch;

    public Server(ClientPool clientPool,
                  SecureRandom rng,
                  LoginService loginService,
                  PacketDecoderService packetDecoderService,
                  @Value("${rs2.server.port}") int port,
                  @Value("${rs2.server.host}") String host,
                  @Value("${rs2.server.workers}") int workers,
                  @Value("${rs2.server.buffer-size}") int bufferSize,
                  @Value("${rs2.server.accept-delay}") long acceptDelay,
                  @Value("${rs2.server.accept-batch}") int acceptBatch) throws IOException {
        this.acceptBatch = acceptBatch;
        this.acceptDelay = acceptDelay;

        selector = Selector.open();
        serverSocketChannel = ServerSocketChannel.open();

        serverSocketChannel.configureBlocking(false);
        serverSocketChannel.socket().bind(new InetSocketAddress(host, port));
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);

        clientMap = new ConcurrentHashMap<>();
        acceptTimer = new Timer(System::currentTimeMillis);
        executor = Executors.newFixedThreadPool(workers);
        localBuffer = ThreadLocal.withInitial(() -> ByteBuffer.allocate(bufferSize));
        this.packetDecoderService = packetDecoderService;
        this.loginService = loginService;
        this.clientPool = clientPool;
        this.rng = rng;
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
        ByteBuffer buffer = localBuffer.get();
        try {
            for (int i = 0; i < acceptBatch; i++) {
                SocketChannel channel = serverSocketChannel.accept();
                if (channel == null) return;
                buffer.clear();
                configureConnection(channel, buffer);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }

    private void configureConnection(SocketChannel channel, ByteBuffer buffer) throws IOException {
        Client client = null;
        SelectionKey newKey = null;
        try {
            channel.configureBlocking(false);
            newKey = channel.register(selector, SelectionKey.OP_READ);

            client = clientPool.request();
            client.initialize(newKey);
            loginService.addToLoginQueue(client);
            clientMap.put(newKey, client);

            sendStatus(channel, buffer, LoginStatus.CONNECTED);
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
        ByteBuffer buffer = localBuffer.get();
        buffer.clear();

        Client client = clientMap.get(key);
        if (client == null) {
            key.cancel();
            return;
        }

        SocketChannel channel = (SocketChannel) key.channel();
        try {
            int readBytes = channel.read(buffer);
            if (readBytes == -1) return;
        } catch(IOException ioe) {
            client.setState(ClientState.AWAITING_CLEANUP);
            return;
        }

        buffer.flip();
        ClientEvent packet;
        while ((packet = packetDecoderService.decodePacket(client.state(), buffer)) != null) {
            client.queuePacket(packet);
        }
    }

}
