package com.Networking.Utility;

import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.serverLogger;

/**
 * Server version of a user
 *
 * Lock policy:
 * Want to do something with thi dude
 * first call lock()
 * then do whatever you want
 * last call release()
 */

public class ServerUser extends BaseUser {

    /**
     * Shows in conversation you are or not
     */

    private volatile Conversation conversation;

    /**
     * Work as long term lock
     */

    private final Semaphore semaphore;

    public ServerUser(String name, int id) {
        super(name, id);
        semaphore = new Semaphore(1);
//        this.controller = controller;
    }

    /**
     * Lock for sync purposes
     */

    public void lock(){
        try {
            semaphore.acquire();
        } catch (InterruptedException ignored) {// is not used
        }
    }

    /**
     * Release lock
     */

    public void release(){
        semaphore.release();
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "setConversation",
                "Set conversation for - " + this + ", from - " + this.conversation + ", to - " + conversation);
        this.conversation = conversation;
    }

    public boolean inConv() {
        return conversation != null;
    }

}
