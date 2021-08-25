package be.nfm.rs2.server.events;

import be.nfm.rs2.server.client.Client;
import be.nfm.rs2.server.ServerContext;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;

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

        try {
            client.decodePackets(buffer);
        } catch (IOException e) {

        }
    }
}
