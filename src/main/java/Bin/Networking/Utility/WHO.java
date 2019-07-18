package Bin.Networking.Utility;

/**
 * Uses as default representation of main users
 */

public enum WHO {
    NO_NAME(0), //your id when first connect to the server
    SERVER(1), //package for the server
    CONFERENCE(2);  //package for the conference

    /**
     * Unique id
     */

    private final int code;

    WHO(int code) {
        this.code = code;
    }

    public int getCode() {
        return code;
    }

    /**
     * Indicates start of unique users id
     * Must be greater than max value of WHO enum
     */

    public static final int SIZE = WHO.values().length;
}
