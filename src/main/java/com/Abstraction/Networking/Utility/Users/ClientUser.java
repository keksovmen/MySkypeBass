package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Networking.Readers.Reader;
import com.Abstraction.Networking.Writers.ClientWriter;

import java.security.AlgorithmParameters;
import java.security.Key;

/**
 * Represent client side user
 */

public class ClientUser implements UserWithLock {

    /**
     * Indicate that you are not calling anyone
     */

    public static final int NO_ONE = -1;


    private final UserWithLock user;

    private final ClientWriter writer;
    private final Reader readerTCP;
    private final Reader readerUDP;

    private int whoCalling = NO_ONE;


    public ClientUser(UserWithLock user, ClientWriter writer, Reader readerTCP, Reader readerUDP) {
        this.user = user;
        this.writer = writer;
        this.readerTCP = readerTCP;
        this.readerUDP = readerUDP;
    }

    @Override
    public void lock() {
        user.lock();
    }

    @Override
    public void unlock() {
        user.unlock();
    }

    @Override
    public String getName() {
        return user.getName();
    }

    @Override
    public int getId() {
        return user.getId();
    }

    @Override
    public Key getSharedKey() {
        return user.getSharedKey();
    }

    @Override
    public AlgorithmParameters getAlgorithmParameters() {
        return user.getAlgorithmParameters();
    }

    @Override
    public String toNetworkFormat() {
        return user.toNetworkFormat();
    }

    @Override
    public String toString() {
        return user.toString();
    }

    @Override
    public boolean equals(Object obj) {
        return user.equals(obj);
    }

    @Override
    public int hashCode() {
        return user.hashCode();
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
