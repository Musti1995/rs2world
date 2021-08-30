package be.nfm.rs2.server.client;

import be.nfm.rs2.util.Timer;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.ConcurrentLinkedQueue;

public class Client {

    private static final long TIMEOUT_DELAY = 10000;

    private final Timer timeoutTimer;
    private final ConcurrentLinkedQueue<ClientEvent> packetQueue;
    private final ByteBuffer outBuffer;
    private final ClientPool clientPool;
    private SelectionKey selectionKey;
    private ClientState state;

    protected Client(ByteBuffer outBuffer, ClientPool clientPool) {
        this.timeoutTimer = new Timer(System::currentTimeMillis);
        this.packetQueue = new ConcurrentLinkedQueue<>();
        this.outBuffer = outBuffer;
        this.clientPool = clientPool;
        this.state = ClientState.CONNECTING;
    }

    public void cleanAndReturn() {
        outBuffer.clear();
        selectionKey = null;
        state = ClientState.CONNECTING;
        packetQueue.clear();
        clientPool.reclaim(this);
    }

    public void queuePacket(ClientEvent packet) {
        packetQueue.add(packet);
        timeoutTimer.reset();
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
}
