package be.nfm.rs2.server.events;

import be.nfm.rs2.client.Client;
import be.nfm.rs2.server.ServerContext;
import be.nfm.rs2.server.ServerResponse;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ServerEventAccept implements ServerEvent {
    @Override
    public void execute(ServerContext ctx, SelectionKey key) {
        try {
            for (int i = 0; i < ctx.acceptBatch(); i++) {
                SocketChannel channel = ctx.channel().accept();
                if (channel == null) return;
                channel.configureBlocking(false);

                ByteBuffer buffer = ctx.localBuffer().get();
                buffer.clear();

                Client client = ctx.clientPool().request();
                if (client == null) {
                    buffer.put((byte) ServerResponse.WORLD_FULL);
                    buffer.flip();
                    channel.write(buffer);
                    return;
                }

                SelectionKey newKey = channel.register(ctx.selector(), SelectionKey.OP_READ);
                ctx.clientMap().put(newKey, client);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
