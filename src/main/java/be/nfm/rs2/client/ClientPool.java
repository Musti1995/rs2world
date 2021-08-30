package be.nfm.rs2.client;

import be.nfm.rs2.login.exceptions.WorldFullException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.NoSuchElementException;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
public class ClientPool {

    private final ConcurrentLinkedQueue<Client> availableClients;
    private final int clientBufferSize;

    public ClientPool(@Value("${rs2.client.capacity}") int capacity,
                      @Value("${rs2.client.login-queue-capacity}") int clientBufferSize) {
        this.availableClients = new ConcurrentLinkedQueue<>();
        this.clientBufferSize = clientBufferSize;
        for (int i = 0; i < capacity; i++) availableClients.add(create());
    }

    private Client create() {
        return new Client(ByteBuffer.allocate(clientBufferSize), this);
    }

    public Client request() {
        try {
            return availableClients.remove();
        } catch(NoSuchElementException nsee) {
            throw new WorldFullException();
        }
    }

    public void reclaim(Client client) {
        availableClients.add(client);
    }

}