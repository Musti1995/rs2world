package be.nfm.rs2.login.exceptions;

import be.nfm.rs2.login.LoginStatus;

/**
 * @author Musa Kapan
 */
public class WorldFullException extends LoginException {
    public WorldFullException() {
        super(LoginStatus.WORLD_FULL);
    }
}
