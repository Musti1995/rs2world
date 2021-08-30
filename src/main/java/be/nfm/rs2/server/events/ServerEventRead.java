package be.nfm.rs2.server.events;

import be.nfm.rs2.server.client.Client;
import be.nfm.rs2.server.ServerContext;
import be.nfm.rs2.server.client.ClientEvent;
import be.nfm.rs2.server.client.ClientState;
import be.nfm.rs2.server.packets.login.ConnectionPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.security.SecureRandom;

public class ServerEventRead implements ServerEvent {
    @Override
    public void execute(ServerContext ctx, SelectionKey key) {
        ByteBuffer buffer = ctx.localBuffer().get();
        buffer.clear();

        Client client = ctx.clientMap().get(key);
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
        while ((packet = decodePacket(client.state(), buffer, ctx.randomGenerator())) != null) {
            client.queuePacket(packet);
        }
    }

    private static ClientEvent decodePacket(ClientState state, ByteBuffer buffer, SecureRandom rng) {
        switch(state) {
            case CONNECTING:
                return ConnectionPacket.build(buffer, rng.nextLong());
            case LOGGING_IN:
                return null;
            case LOGGED_IN:
                return null;
        }
        return null;
    }
}
