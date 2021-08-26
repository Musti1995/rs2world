package be.nfm.rs2.server.events;

import be.nfm.rs2.server.client.Client;
import be.nfm.rs2.server.ServerContext;
import be.nfm.rs2.server.client.ClientState;
import be.nfm.rs2.server.packets.login.ConnectionPacket;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

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
        switch(client.state()) {
            case CONNECTING:
                client.queuePacket(ConnectionPacket.build(buffer, ctx.randomGenerator().nextLong()));
                break;
        }
    }
}
