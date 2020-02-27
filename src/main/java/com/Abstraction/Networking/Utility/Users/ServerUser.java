package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Networking.Readers.Reader;
import com.Abstraction.Networking.Utility.Conversation;
import com.Abstraction.Networking.Writers.ServerWriter;

import java.net.InetAddress;
import java.security.AlgorithmParameters;
import java.security.Key;

/**
 * SimpleServer version of a user
 * <p>
 * Lock policy:
 * Want to do something with this dude
 * first call lock()
 * then do whatever you want
 * last call unlock()
 */

public class ServerUser implements UserWithLock {


    private final UserWithLock user;

    private final ServerWriter writer;
    private final Reader reader;

    /**
     * Shows in conversation you are or not
     */

    private volatile Conversation conversation;


    /**
     * UDP specific fields
     */

    private final InetAddress address;
    private final int port;

    public ServerUser(UserWithLock user, ServerWriter writer, Reader reader, InetAddress address, int port) {
        this.user = user;
        this.writer = writer;
        this.reader = reader;
        this.address = address;
        this.port = port;
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

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public boolean inConversation() {
        return conversation != null;
    }

    public ServerWriter getWriter() {
        return writer;
    }

    public Reader getReader() {
        return reader;
    }

    public InetAddress getAddress() {
        return address;
    }

    public int getPort() {
        return port;
    }

}
