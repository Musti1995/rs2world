package be.nfm.rs2.client;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

public final class ClientPool {

    private final ConcurrentLinkedQueue<Client> availableClients;
    private final int clientBufferSize;

    public ClientPool(int capacity,
                      int clientBufferSize) {
        this.availableClients = new ConcurrentLinkedQueue<>();
        this.clientBufferSize = clientBufferSize;
        for (int i = 0; i < capacity; i++) availableClients.add(create());
    }

    private Client create() {
        return new Client(ByteBuffer.allocate(clientBufferSize), this);
    }

    public Client request() {
        return availableClients.poll();
    }

    public void reclaim(Client client) {
        availableClients.add(client);
    }

}