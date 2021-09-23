package be.nfm.rs2.client;

import be.nfm.rs2.login.exceptions.WorldFullException;
import be.nfm.rs2.util.ArrayWrapper;
import org.springframework.stereotype.Component;

import java.nio.channels.SelectionKey;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Musa Kapan
 */
@Component
public class ClientService {

    private final ClientPool clientPool;
    private final ArrayWrapper<Client> activeClients;
    private final ConcurrentHashMap<SelectionKey, Client> clientMap;

    public ClientService(ClientPool clientPool, ArrayWrapper<Client> activeClients) {
        this.clientPool = clientPool;
        this.activeClients = activeClients;
        this.clientMap = new ConcurrentHashMap<>();
    }

    public Client getClient(SelectionKey key) {
        return clientMap.get(key);
    }

    public Client register(SelectionKey key) {
        Client client = clientPool.request();
        if (client == null) throw new WorldFullException();

        client.initialize(key);
        client.setState(ClientState.CONNECTING);
        clientMap.put(key, client);
        return client;
    }


    public void deregister(SelectionKey key) {
        
    }
}
