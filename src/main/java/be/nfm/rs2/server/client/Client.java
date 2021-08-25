package be.nfm.rs2.server.client;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class Client {

    private final ByteBuffer outBuffer;
    private final ClientPool clientPool;
    private SelectionKey selectionKey;
    private ClientState state;

    protected Client(ByteBuffer outBuffer, ClientPool clientPool) {
        this.outBuffer = outBuffer;
        this.clientPool = clientPool;
        this.state = ClientState.CONNECTING;
    }

    public void cleanAndReturn() {
        outBuffer.clear();
        selectionKey = null;
        state = ClientState.CONNECTING;
        clientPool.reclaim(this);
    }

    public void decodePackets(ByteBuffer buffer) throws IOException {
        SocketChannel channel = (SocketChannel) selectionKey.channel();
        channel.read(buffer);
        buffer.flip();
        switch(state) {
            case CONNECTING:
                break;
            case CONNECTED:
                break;
            case LOGGED_IN:
                break;
        }
    }

    public void assignKey(SelectionKey key) {
        this.selectionKey = key;
    }

    public SelectionKey selectionKey() {
        return selectionKey;
    }

    public void setState(ClientState state) {
        this.state = state;
    }
}
