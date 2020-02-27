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

public class ServerUser extends UserWithLock {


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


    public ServerUser(String name, int id, ServerWriter writer, Reader reader, InetAddress address, int port) {
        super(name, id);
        this.writer = writer;
        this.reader = reader;
        this.address = address;
        this.port = port;
    }

    public ServerUser(String name, int id, Key sharedKey, AlgorithmParameters algorithmParameters, ServerWriter writer, Reader reader, InetAddress address, int port) {
        super(name, id, sharedKey, algorithmParameters);
        this.writer = writer;
        this.reader = reader;
        this.address = address;
        this.port = port;
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
