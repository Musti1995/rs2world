package be.nfm.rs2.client;

import be.nfm.rs2.util.ArrayWrapper;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Musa Kapan
 */
@Component
public class ClientService {

    private final ArrayWrapper<Client> activeClients;

    public ClientService(@Qualifier("activeClients") ArrayWrapper<Client> activeClients) {
        this.activeClients = activeClients;
    }


}
