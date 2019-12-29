package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Writers.ClientWriter;

/**
 * Represent client side user
 */

public class ClientUser extends UserWithLock {

    /**
     * Indicate that you are not calling anyone
     */

    public static final int NO_ONE = -1;


    private final ClientWriter writer;
    private final BaseReader reader;


    private int whoCalling = NO_ONE;


    public ClientUser(String name, int id, ClientWriter writer, BaseReader reader) {
        super(name, id);
        this.writer = writer;
        this.reader = reader;
    }

    public ClientUser(BaseUser user, ClientWriter writer, BaseReader reader) {
        super(user.getName(), user.getId(), user.getSharedKey(), user.getAlgorithmParameters());
        this.writer = writer;
        this.reader = reader;
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



    public ClientWriter getWriter() {
        return writer;
    }

    public BaseReader getReader() {
        return reader;
    }

    public int isCalling(){
        return whoCalling;
    }
}
