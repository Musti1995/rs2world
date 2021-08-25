package be.nfm.rs2.server;

import be.nfm.rs2.client.Client;
import be.nfm.rs2.client.ClientPool;
import be.nfm.rs2.util.Timer;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.HashMap;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;

public final class ServerContext {

    private final ServerSocketChannel channel;
    private final Selector selector;
    private final ClientPool clientPool;
    private final ExecutorService executor;
    private final int acceptBatch;
    private final ThreadLocal<ByteBuffer> localBuffer;
    private final ConcurrentHashMap<SelectionKey, Client> clientMap;

    public ServerContext(ServerSocketChannel channel,
                         Selector selector,
                         ClientPool clientPool,
                         ExecutorService executor,
                         int acceptBatch,
                         ThreadLocal<ByteBuffer> localBuffer,
                         ConcurrentHashMap<SelectionKey, Client> clientMap) {
        this.channel = channel;
        this.selector = selector;
        this.clientPool = clientPool;
        this.executor = executor;
        this.acceptBatch = acceptBatch;
        this.localBuffer = localBuffer;
        this.clientMap = clientMap;
    }

    public ServerSocketChannel channel() {
        return channel;
    }

    public Selector selector() {
        return selector;
    }

    public ClientPool clientPool() {
        return clientPool;
    }

    public ExecutorService executor() {
        return executor;
    }

    public int acceptBatch() {
        return acceptBatch;
    }

    public ThreadLocal<ByteBuffer> localBuffer() {
        return localBuffer;
    }

    public ConcurrentHashMap<SelectionKey, Client> clientMap() {
        return clientMap;
    }
}
