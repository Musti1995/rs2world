package be.nfm.rs2.login;

import be.nfm.rs2.client.Client;
import be.nfm.rs2.login.exceptions.WorldFullException;
import be.nfm.rs2.util.ArrayWrapper;
import be.nfm.rs2.util.Bool;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * @author Musa Kapan
 */
@Component
public class LoginService {

    private final ArrayWrapper<Client> loginQueue;

    public LoginService(@Qualifier("loginQueue") ArrayWrapper<Client> loginQueue) {
        this.loginQueue = loginQueue;
    }

    public void addToLoginQueue(Client client) {
        Bool.throwIfTrue(loginQueue.add(client) == -1, WorldFullException::new);
    }

}
