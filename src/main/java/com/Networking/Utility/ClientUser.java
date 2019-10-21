package com.Networking.Utility;

import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.clientLogger;

/**
 * Represent client side user
 */

public class ClientUser extends BaseUser {

    public static final int NO_ONE = -1;
    private int whoCalling = NO_ONE;

    public ClientUser(String name, int id) {
        super(name, id);
    }

    /**
     * When you calling some one
     */

    public synchronized void call(int id){
        clientLogger.logp(Level.FINER, this.getClass().getName(),
                "call", "Call this dude - " + id);
        whoCalling = id;
    }

    /**
     * When you stop calling
     */

    public synchronized void drop(){
        clientLogger.logp(Level.FINER, this.getClass().getName(),
                "drop", "Drop this dude - " + whoCalling);
        whoCalling = NO_ONE;
    }

    public int isCalling(){
        return whoCalling;
    }
}
