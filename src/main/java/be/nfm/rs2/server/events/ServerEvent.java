package be.nfm.rs2.server.events;

import be.nfm.rs2.server.ServerContext;

import java.nio.channels.SelectionKey;

public interface ServerEvent {

    void execute(ServerContext ctx, SelectionKey key);

}
