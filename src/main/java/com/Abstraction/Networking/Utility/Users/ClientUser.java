package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Networking.Readers.Reader;
import com.Abstraction.Networking.Writers.ClientWriter;

import java.security.AlgorithmParameters;
import java.security.Key;

/**
 * Represent client side user
 */

public class ClientUser extends UserWithLock {

    /**
     * Indicate that you are not calling anyone
     */

    public static final int NO_ONE = -1;


    private final ClientWriter writer;
    private final Reader readerTCP;
    private final Reader readerUDP;


    private int whoCalling = NO_ONE;


    public ClientUser(String name, int id, ClientWriter writer, Reader readerTCP, Reader readerUDP) {
        super(name, id);
        this.writer = writer;
        this.readerTCP = readerTCP;
        this.readerUDP = readerUDP;
    }

    public ClientUser(String name, int id, Key sharedKey, AlgorithmParameters algorithmParameters, ClientWriter writer, Reader readerTCP, Reader readerUDP) {
        super(name, id, sharedKey, algorithmParameters);
        this.writer = writer;
        this.readerTCP = readerTCP;
        this.readerUDP = readerUDP;
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

    public Reader getReaderTCP() {
        return readerTCP;
    }

    public Reader getReaderUDP() {
        return readerUDP;
    }

    public int isCalling(){
        return whoCalling;
    }
}
