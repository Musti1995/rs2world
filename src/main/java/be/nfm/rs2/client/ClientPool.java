package be.nfm.rs2.client;

import be.nfm.rs2.client.Client;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.nio.ByteBuffer;
import java.util.concurrent.ConcurrentLinkedQueue;

@Component
@ConfigurationProperties(prefix = "rs2.clients")
public final class ClientPool {

    private final ConcurrentLinkedQueue<Client> availableClients;
    private final int clientBufferSize;

    protected ClientPool(@Value("capacity") int clientCapacity,
                         @Value("login-queue-capacity") int loginQueueCapacity,
                         @Value("buffer-size") int clientBufferSize) {
        final int totalCapacity = clientCapacity + loginQueueCapacity;
        this.availableClients = new ConcurrentLinkedQueue<>();
        this.clientBufferSize = clientBufferSize;
        for (int i = 0; i < totalCapacity; i++) availableClients.add(create());
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