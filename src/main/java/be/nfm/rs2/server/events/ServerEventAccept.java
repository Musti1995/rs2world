package be.nfm.rs2.server.events;

import be.nfm.rs2.server.client.Client;
import be.nfm.rs2.server.ServerContext;
import be.nfm.rs2.server.ServerResponse;
import be.nfm.rs2.server.client.ClientState;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;

public class ServerEventAccept implements ServerEvent {
    @Override
    public void execute(ServerContext ctx, SelectionKey key) {
        ByteBuffer buffer = ctx.localBuffer().get();
        try {
            for (int i = 0; i < ctx.acceptBatch(); i++) {
                buffer.clear();

                SocketChannel channel = ctx.channel().accept();
                if (channel == null) return;
                channel.configureBlocking(false);

                Client client = ctx.clientPool().request();
                if (client == null) {
                    buffer.put((byte) ServerResponse.WORLD_FULL);
                    buffer.flip();
                    channel.write(buffer);
                    return;
                }

                int position = ctx.loginQueue().add(client);
                if (position == -1) {
                    buffer.put((byte) ServerResponse.WORLD_FULL);
                    buffer.flip();
                    channel.write(buffer);
                    client.cleanAndReturn();
                    return;
                }

                SelectionKey newKey = channel.register(ctx.selector(), SelectionKey.OP_READ);
                client.initialize(newKey);
                ctx.clientMap().put(newKey, client);
                buffer.put((byte) ServerResponse.CONNECTED);
                buffer.flip();
                channel.write(buffer);
                client.setState(ClientState.CONNECTING);
            }
        } catch(IOException ioe) {
            ioe.printStackTrace();
        }
    }
}
