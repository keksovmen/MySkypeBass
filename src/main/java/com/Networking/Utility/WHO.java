package com.Networking.Utility;

import com.Networking.Protocol.ProtocolBitMap;
import com.Util.Algorithms;

import java.util.Comparator;

/**
 * Uses as default representation of main users
 */

public enum WHO {
    NO_NAME(0), //your id when first connect to the server
    SERVER(1), //package for the server
    CONFERENCE(2);  //package for the conference

    private static boolean checked = false;

    /**
     * Indicates start of unique users id
     * Must be greater than max value of WHO enum
     */

    public static final int SIZE = WHO.values().length;
    /**
     * Unique id
     */

    private final int code;


    /**
     * Must call before using this enum
     * <p>
     * Check for identical IDs
     */

    public static void uniqueIdCheck() {
        if (checked)
            return;
        boolean b = Algorithms.searchForIdentities(values(), Comparator.comparingInt(WHO::getCode));
        if (b) {
            throw new IllegalArgumentException("There is already pair of equal IDs ");
        }
        checked = true;
    }

    WHO(int code) {
        if (code < 0 || ProtocolBitMap.MAX_VALUE < code) {
            throw new IllegalArgumentException("Unique id {" + code + "} is out of range");
        }
        this.code = code;
    }

    public int getCode() {
        return code;
    }
}
