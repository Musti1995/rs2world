package be.nfm.rs2.server.events;

import be.nfm.rs2.server.ServerContext;

import java.nio.channels.SelectionKey;

public class ServerEventContainer implements Runnable {

    private static final ServerEvent ACCEPT_EVENT = new ServerEventAccept();
    private static final ServerEvent READ_EVENT = new ServerEventRead();

    private ServerEvent event;
    private ServerContext context;
    private SelectionKey selectionKey;

    private ServerEventContainer(ServerEvent event, ServerContext ctx, SelectionKey key) {
        this.event = event;
        this.context = ctx;
        this.selectionKey = key;
    }

    @Override
    public void run() {
        event.execute(context, selectionKey);
    }

    public static Runnable newAcceptEvent(ServerContext ctx) {
        return new ServerEventContainer(ACCEPT_EVENT, ctx, null);
    }

    public static Runnable newReadEvent(ServerContext ctx, SelectionKey key) {
        return new ServerEventContainer(READ_EVENT, ctx, key);
    }
}