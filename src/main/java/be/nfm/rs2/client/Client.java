package be.nfm.rs2.client;

import be.nfm.rs2.util.Timer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {

    private static final long TIMEOUT_DELAY = 10000;

    private final Timer timeoutTimer;
    private final ByteBuffer outBuffer;
    private final ByteBuffer inBuffer;
    private final ClientPool clientPool;
    private SelectionKey selectionKey;
    private ClientState state;

    protected Client(ByteBuffer outBuffer, ByteBuffer inBuffer, ClientPool clientPool) {
        this.timeoutTimer = new Timer(System::currentTimeMillis);
        this.outBuffer = outBuffer;
        this.inBuffer = inBuffer;
        this.clientPool = clientPool;
        this.state = ClientState.CONNECTING;
    }

    public void cleanAndReturn() {
        outBuffer.clear();
        inBuffer.clear();
        selectionKey = null;
        state = ClientState.CONNECTING;
        clientPool.reclaim(this);
    }

    public void initialize(SelectionKey key) {
        this.selectionKey = key;
        timeoutTimer.reset();
    }

    public boolean isDisconnected() {
        return timeoutTimer.elapsed(TIMEOUT_DELAY);
    }

    public SelectionKey selectionKey() {
        return selectionKey;
    }

    public void setState(ClientState state) {
        this.state = state;
    }

    public ClientState state() {
        return state;
    }

    public ByteBuffer outBuffer() {
        return outBuffer;
    }

    public ByteBuffer inBuffer() {
        return inBuffer;
    }

    public void flushOutBuffer() {
        if (outBuffer.position() == 0) return;
        try {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            outBuffer.flip();
            channel.write(outBuffer);
            outBuffer.clear();
        } catch(IOException ignored) {

        }
    }

    public void readIncomingPackets() throws IOException {
        synchronized (inBuffer) {
            SocketChannel channel = (SocketChannel) selectionKey.channel();
            channel.read(inBuffer);
        }
    }
}
