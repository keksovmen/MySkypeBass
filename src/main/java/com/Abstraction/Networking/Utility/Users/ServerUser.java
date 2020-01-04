package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Utility.Conversation;
import com.Abstraction.Networking.Writers.ServerWriter;

import java.security.AlgorithmParameters;
import java.security.Key;

/**
 * SimpleServer version of a user
 *
 * Lock policy:
 * Want to do something with this dude
 * first call lock()
 * then do whatever you want
 * last call unlock()
 */

public class ServerUser extends UserWithLock {


    private final ServerWriter writer;
    private final BaseReader reader;

    /**
     * Shows in conversation you are or not
     */

    private volatile Conversation conversation;


    public ServerUser(String name, int id, ServerWriter writer, BaseReader reader) {
        super(name, id);
        this.writer = writer;
        this.reader = reader;
    }

    public ServerUser(String name, int id, Key sharedKey, AlgorithmParameters algorithmParameters, ServerWriter writer, BaseReader reader) {
        super(name, id, sharedKey, algorithmParameters);
        this.writer = writer;
        this.reader = reader;
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

    public BaseReader getReader() {
        return reader;
    }
}
