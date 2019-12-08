package com.Abstraction.Networking.Processors;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Servers.AbstractServer;
import com.Abstraction.Networking.Utility.Conversation;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Networking.Utility.WHO;

import java.io.IOException;

public class ServerProcessor implements Processable {

    private final ServerUser correspondUser;
    private final AbstractServer server;

    public ServerProcessor(ServerUser correspondUser, AbstractServer server) {
        this.correspondUser = correspondUser;
        this.server = server;
    }

    /**
     * Handle the data package command
     *
     * @param dataPackage to handleRequest
     * @return true if handled request without network failures
     */

    @Override
    public boolean process(AbstractDataPackage dataPackage) {
        if (dataPackage == null)
            throw new NullPointerException("Data package can't be null, ruins invariant");
        switch (dataPackage.getHeader().getCode()) {
            case SEND_USERS: {
                return sendUsers(dataPackage);
            }
            case SEND_MESSAGE: {
                return onTransferData(dataPackage);
            }
            case SEND_CALL: {
                return onCall(dataPackage);
            }
            case SEND_DISCONNECT: {
                return false; //indicate end of loop for a holder
            }
            case SEND_ACCEPT_CALL: {
                return onCallAccept(dataPackage);
            }
            case SEND_DENY_CALL: {
                return onTransferData(dataPackage);
            }
            case SEND_CANCEL_CALL: {
                return onTransferData(dataPackage);
            }
            case SEND_DISCONNECT_FROM_CONV: {
                return onExitConversation(dataPackage);
            }
            case SEND_SOUND: {
                return onSendSound(dataPackage);
            }
            //And so on
        }
        System.err.println("There is no such handler for given CODE - " + dataPackage.getHeader().getCode());
        return false;
    }

    @Override
    public void close() {
        server.removeUser(correspondUser.getId());
        if (correspondUser.inConversation()) {
            correspondUser.getConversation().removeDude(correspondUser);
        }
    }

    /**
     * Grabs every correspondUser on the server except you and send it to you
     *
     * @param dataPackage contain info
     * @return true if handled without connection failure
     */

    protected boolean sendUsers(AbstractDataPackage dataPackage) {
        String users = server.getUsersExceptYou(correspondUser.getId());
        try {
            correspondUser.getWriter().writeUsers(correspondUser.getId(), users);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Transfer given dataPackage to correspond correspondUser or conversation
     * Without creating new ones
     *
     * @param dataPackage contain message and receiver id
     * @return true if handled without network failure
     */

    protected boolean onTransferData(AbstractDataPackage dataPackage) {
        int to = dataPackage.getHeader().getTo();
        //To conversation
        if (to == WHO.CONFERENCE.getCode()) {
            Conversation conversation = correspondUser.getConversation();
            if (conversation != null) {
                conversation.sendMessage(dataPackage, correspondUser);
            } else {
                //tell him that he is not in a conversation
                try {
                    correspondUser.getWriter().writeStopConv(correspondUser.getId());
                } catch (IOException e) {
                    return false;
                }
            }
            return true;
        }

        //To other dude
        ServerUser receiver = server.getUser(to);
        if (receiver == null) {
            return onDudeIsMissing(to);
        }
        try {
            receiver.getWriter().transferPacket(dataPackage);
        } catch (IOException e) {
            //tell that dude is offline
            onDudeIsMissing(to);
        }
        return true;
    }

    /**
     * Short cut to notifyObservers dude that last message he sent
     * didn't get to the receiver
     *
     * @param dudeId who he wanted to send a message
     * @return true if handled without connection failure
     */

    private boolean onDudeIsMissing(int dudeId) {
        try {
            correspondUser.getWriter().writeRemoveFromUserList(correspondUser.getId(), dudeId);
        } catch (IOException e) {
            return false;
        }
        return true;
    }

    /**
     * Transfer call request
     * If you are in conversation then will put all dudes as string data
     * of given package
     *
     * @param dataPackage contain receiver and may be changed to contain other in a conversation
     * @return true if handled without connection failure
     */

    protected boolean onCall(AbstractDataPackage dataPackage) {
        int to = dataPackage.getHeader().getTo();
        ServerUser receiver = server.getUser(to);
        if (receiver == null) {
            return onDudeIsMissing(to);
        }

        //check if we both in conversations
        Runnable release = doubleLock(correspondUser, receiver);
        try {
            if (correspondUser.inConversation() && receiver.inConversation()) {
                try {
                    correspondUser.getWriter().writeBothInConversations(correspondUser.getId(), receiver.getId());
                } catch (IOException e) {
                    return false;
                }
                try {
                    receiver.getWriter().writeBothInConversations(receiver.getId(), correspondUser.getId());
                } catch (IOException ignored) {
                    //His thread will handleRequest shit
                }
                return true;
            }
            try {
                if (correspondUser.inConversation()) {
                    dataPackage.setData(correspondUser.getConversation().getAllToString(correspondUser));
                }
                receiver.getWriter().transferPacket(dataPackage);
            } catch (IOException e) {
                //tell that dude is offline
                onDudeIsMissing(to);
            }
        } finally {
            release.run();
        }
        return true;
    }

    /**
     * Create new or updates already existing conversation
     *
     * @param dataPackage contains receiver
     * @return true if handled without connection failure
     */

    protected boolean onCallAccept(AbstractDataPackage dataPackage) {
        int to = dataPackage.getHeader().getTo();
        ServerUser receiver = server.getUser(to);
        if (receiver == null) {
            return onDudeIsMissing(to);
        }

        Runnable release = doubleLock(correspondUser, receiver);
        //here goes atomic code
        Conversation conversation;
        if (correspondUser.inConversation()) {
            //add dude to conv and put all dudes from conf to notifyObservers him
            conversation = correspondUser.getConversation();
            dataPackage.setData(conversation.getAllToString(correspondUser));
            conversation.addDude(receiver, correspondUser);
        } else if (receiver.inConversation()) {
            //add me to dude's conv I already know about dudes in conf
            conversation = receiver.getConversation();
            conversation.addDude(correspondUser, receiver);
        } else {
            //create conv for us
            conversation = new Conversation(correspondUser, receiver);
            correspondUser.setConversation(conversation);
            receiver.setConversation(conversation);
        }
        release.run();

        try {
            receiver.getWriter().transferPacket(dataPackage);
        } catch (IOException ignored) {
            //Dude disconnected before so it's thread will handleRequest
        }
        return true;
    }

    /**
     * Remove you from conversation
     *
     * @param dataPackage just to indicate a desire to leave the conversation
     * @return true if handled without connection failure
     */

    protected boolean onExitConversation(AbstractDataPackage dataPackage) {
        Conversation conversation = correspondUser.getConversation();
        //check if it is null because some other thread could make you leave
        if (conversation == null)
            return true;
        conversation.removeDude(correspondUser);
        return true;
    }

    /**
     * Delegate sound sending to conversation object
     *
     * @param dataPackage contain sound as plain data
     * @return true if handled without connection failure
     */

    protected boolean onSendSound(AbstractDataPackage dataPackage) {
        Conversation conversation = correspondUser.getConversation();
        if (conversation == null)
            return true;
        conversation.sendSound(dataPackage, correspondUser.getId());
        return true;
    }

    /**
     * Short cut to lock both dudes at appropriate order
     * to avoid dead locks
     * USERS MUST HAVE UNIQUE ID
     *
     * @param me   first correspondUser
     * @param dude second correspondUser
     * @return anonymous function to unlock the boys
     */

    private static Runnable doubleLock(ServerUser me, ServerUser dude) {
        if (me.getId() > dude.getId()) {
            me.lock();
            dude.lock();
        } else {
            dude.lock();
            me.lock();
        }
        return () -> {
            me.unlock();
            dude.unlock();
        };
    }
}
