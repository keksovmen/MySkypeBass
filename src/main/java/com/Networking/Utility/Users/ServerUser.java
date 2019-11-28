package com.Networking.Utility.Users;

import com.Networking.Utility.Conversation;
import com.Networking.Writers.ServerWriter;

import java.util.concurrent.Semaphore;

/**
 * SimpleServer version of a user
 *
 * Lock policy:
 * Want to do something with thi dude
 * first call lock()
 * then do whatever you want
 * last call unlock()
 */

public class ServerUser extends UserWithLock {


    private final ServerWriter writer;

    /**
     * Shows in conversation you are or not
     */

    private volatile Conversation conversation;


    public ServerUser(String name, int id, ServerWriter writer) {
        super(name, id);
        this.writer = writer;
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
}
