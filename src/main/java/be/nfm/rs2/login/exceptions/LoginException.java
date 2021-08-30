package be.nfm.rs2.login.exceptions;

/**
 * @author Musa Kapan
 */
public class LoginException extends RuntimeException {

    private final int status;

    public LoginException(int status) {
        this.status = status;
    }

    public int getStatus() {
        return status;
    }

}
