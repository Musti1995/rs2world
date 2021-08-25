package be.nfm.rs2.client;

import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

public class Client {

    private final ByteBuffer outBuffer;
    private final ClientPool clientPool;
    private SelectionKey selectionKey;

    protected Client(ByteBuffer outBuffer, ClientPool clientPool) {
        this.outBuffer = outBuffer;
        this.clientPool = clientPool;
    }



}
