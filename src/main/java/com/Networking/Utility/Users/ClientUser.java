package com.Networking.Utility.Users;

import com.Networking.Writers.ClientWriter;

/**
 * Represent client side user
 */

public class ClientUser extends UserWithLock {

    public static final int NO_ONE = -1;
    private int whoCalling = NO_ONE;

    private final ClientWriter writer;

    public ClientUser(String name, int id, ClientWriter writer) {
        super(name, id);
        this.writer = writer;
    }

    /**
     * When you calling some one
     */

    public void call(int id){
        lock();
        whoCalling = id;
        unlock();
    }

    /**
     * When you stop calling
     */

    public void drop(){
        lock();
        whoCalling = NO_ONE;
        unlock();
    }

    public int isCalling(){
        return whoCalling;
    }

    public ClientWriter getWriter() {
        return writer;
    }
}
