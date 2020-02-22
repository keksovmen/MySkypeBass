package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Utility.Conversation;
import com.Abstraction.Networking.Writers.ServerWriter;

import java.util.concurrent.atomic.AtomicBoolean;

public class ConversationServerUser implements AbstractServerUser {

    private final ServerUser user;

    private AtomicBoolean isSuspended;


    public ConversationServerUser(ServerUser user) {
        this.user = user;
        isSuspended = new AtomicBoolean(false);
    }

    @Override
    public Conversation getConversation() {
        return user.getConversation();
    }

    @Override
    public void setConversation(Conversation conversation) {
        user.setConversation(conversation);
    }

    @Override
    public boolean inConversation() {
        return user.inConversation();
    }

    @Override
    public ServerWriter getWriter() {
        return user.getWriter();
    }

    @Override
    public BaseReader getReader() {
        return user.getReader();
    }

    @Override
    public boolean equals(Object obj) {
        return user.equals(obj);
    }

    @Override
    public String toString() {
        return user.toString();
    }

    public int getId(){
        return user.getId();
    }

    public boolean IsSuspended() {
        return isSuspended.get();
    }

    public boolean suspend(){
        return isSuspended.compareAndSet(false, true);
    }

    public void unSuspend() {
        isSuspended.set(false);
    }
}
