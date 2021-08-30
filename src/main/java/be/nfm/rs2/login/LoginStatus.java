package be.nfm.rs2.login;

public class LoginStatus {

    public static final int WAIT_TRY_AGAIN_COUNT = -1;
    public static final int CONNECTED = 0;
    public static final int WAIT_TRY_AGAIN = 1;
    public static final int LOGGED_IN = 2;
    public static final int INVALID_LOGIN_DETAILS = 3;
    public static final int ACCOUNT_DISABLED = 4;
    public static final int ALREADY_LOGGED_IN = 5;
    public static final int WORLD_UPDATED = 6;
    public static final int WORLD_FULL = 7;
    public static final int LOGIN_SERVER_OFFLINE = 8;
    public static final int LOGIN_LIMIT_EXCEEDED = 9;
    public static final int BAD_SESSION_ID = 10;
    public static final int REJECTED_SESSION = 11;
    public static final int NEED_MEMBERS = 12;
    public static final int INCOMPLETE_LOGIN = 13;
    public static final int WORLD_BEING_UPDATED = 14;
    public static final int RECONNECTED = 15;
    public static final int LOGIN_ATTEMPTS_EXCEEDED = 16;
    public static final int STANDING_IN_MEMBERS_AREA = 17;
    public static final int INVALID_LOGIN_SERVER = 20;
    public static final int JUST_LEFT_OTHER_WORLD = 21;

}
