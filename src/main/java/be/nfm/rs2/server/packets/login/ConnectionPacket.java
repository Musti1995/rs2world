package be.nfm.rs2.server.packets.login;

import be.nfm.rs2.server.ServerResponse;
import be.nfm.rs2.server.client.Client;
import be.nfm.rs2.server.client.ClientEvent;
import be.nfm.rs2.server.client.ClientState;

import java.nio.ByteBuffer;

public class ConnectionPacket implements ClientEvent {

    private final long serverSessionKey;
    private final int confirm14;
    private final int nameHash;

    private ConnectionPacket(long serverSessionKey, int confirm14, int nameHash) {
        this.serverSessionKey = serverSessionKey;
        this.confirm14 = confirm14;
        this.nameHash = nameHash;
    }

    public static ConnectionPacket build(ByteBuffer buffer, long serverSessionKey) {
        try {
            return new ConnectionPacket(serverSessionKey, buffer.get(), buffer.get());
        } catch(Exception e) {
            return null;
        }
    }

    @Override
    public void execute(Client client) {
        client.outBuffer().clear();
        if (confirm14 == 14) {
            client.outBuffer().putLong(0L);
            client.outBuffer().put((byte)ServerResponse.CONNECTED);
            client.outBuffer().putLong(serverSessionKey);
            client.flushOutBuffer();
            client.setState(ClientState.LOGGING_IN);
        } else {
            client.outBuffer().put((byte)ServerResponse.REJECTED_SESSION);
            client.flushOutBuffer();
            client.setState(ClientState.AWAITING_CLEANUP);
        }
    }
}