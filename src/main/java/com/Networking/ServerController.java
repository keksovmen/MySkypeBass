package com.Networking;

import com.Networking.Processors.Processable;
import com.Networking.Processors.Processor;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.CODE;
import com.Networking.Readers.BaseReader;
import com.Networking.Utility.Conversation;
import com.Networking.Utility.ServerUser;
import com.Networking.Utility.WHO;
import com.Networking.Writers.ServerWriter;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;
import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.serverLogger;

/**
 * Handles all server actions for the connected user
 */

public class ServerController extends BaseController {

    private ServerWriter writer;
    private ServerUser me;
    private final Server server;
    private final Processor processor;

    public ServerController(Socket socket, Server server, int bufferSize) throws IOException {
        reader = new BaseReader(socket.getInputStream(), bufferSize);
        writer = new ServerWriter(socket.getOutputStream(), bufferSize);
        processor = new Processor();
        this.socket = socket;
        this.server = server;
    }

    @Override
    void cleanUp() {
        //Clean up code
        server.removeController(getId());
        if (me.inConv()) {
            me.getConversation().removeDude(this);
        }
        serverLogger.logp(Level.FINER, this.getClass().getName(), "cleanUp",
                "Thread is almost finished - " + me);
    }

    /**
     * Trying to register a new user for the server
     * first read name from the user
     * second writes audio format
     * third gets true or false on the audio format
     * than add user or disconnect him
     * after write all users on server to him
     * and notify all other users
     *
     * @return true only if you are able to use this audio format
     */

    @Override
    boolean authenticate() {
        try {
            AbstractDataPackage dataPackage = reader.read();
            final String name = dataPackage.getDataAsString();
            AbstractDataPackagePool.returnPackage(dataPackage);

            writer.writeAudioFormat(WHO.NO_NAME.getCode(),
                    server.getAudioFormat());
            dataPackage = reader.read();

            if (dataPackage.getHeader().getCode() != CODE.SEND_APPROVE) {
                //Then dude just disconnects so do we
                AbstractDataPackagePool.returnPackage(dataPackage);
                return false;
            }
            AbstractDataPackagePool.returnPackage(dataPackage);

            final int id = server.getIdAndIncrement();
            writer.writeId(id);

            me = new ServerUser(name, id);
            Thread.currentThread().setName(Thread.currentThread().getName() + id);
            serverLogger.logp(Level.FINER, this.getClass().getName(), "authenticate",
                    "New thread is running for - " + me);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * Uses as initialisation before entering the main loop
     */

    @Override
    void dataInitialisation() {
        //Add listeners here
        processor.getOnUsers().setListener(Handlers.onUsersRequest(this));
        processor.getOnMessage().setListener(Handlers.onTransfer(this));
        processor.getOnDisconnect().setListener(Handlers.onDisconnect(this));
        processor.getOnCall().setListener(Handlers.onCall(this));
        processor.getOnCallAccept().setListener(Handlers.onCallAccept(this));
        processor.getOnCallCancel().setListener(Handlers.onTransfer(this));
        processor.getOnCallDeny().setListener(Handlers.onTransfer(this));
        processor.getOnExitConference().setListener(Handlers.onExitConversation(this));
        processor.getOnSendSound().setListener(Handlers.onSendSound(this));

        serverLogger.logp(Level.FINER, this.getClass().getName(), "dataInitialisation",
                "Authenticate finished good, so now adding you to server model - " + me);

        server.registerController(this);

        serverLogger.logp(Level.FINER, this.getClass().getName(), "dataInitialisation",
                "Sending all users for you for first time - " + me);
        sendUsers();
    }

    private void sendUsers() {
        String users = server.getUsers(getId());
        try {
            writer.writeUsers(getId(), users);
        } catch (IOException e) {
            close();
        }
    }

    private void onDudeIsMissing() {
        try {
            writer.writeRemoveFromUserList(WHO.SERVER.getCode(), getId());
        } catch (IOException e) {
            close();
        }
    }

    @Override
    Processable getProcessor() {
        return processor;
    }

    public ServerWriter getWriter() {
        return writer;
    }

    public int getId() {
        return me.getId();
    }

    public ServerUser getMe() {
        return me;
    }

    @Override
    public String toString() {
        return "ServerController{" +
                "me=" + me +
                '}';
    }

    private static class Handlers {

        /**
         * Get user list as string
         * Send it
         *
         * @return handler for users
         */

        private static Consumer<AbstractDataPackage> onUsersRequest(ServerController current) {
            return dataPackage -> {
                serverLogger.exiting(current.getClass().getName(), "onUserRequest",
                        "Dude Ask for users - " + current.getMe() );
                current.sendUsers();
            };
        }

        private static Consumer<AbstractDataPackage> onTransfer(ServerController current) {
            return dataPackage -> {
                int to = dataPackage.getHeader().getTo();
                serverLogger.logp(Level.FINER, current.getClass().getName(), "onTransfer",
                        "Transferring code/message to - " + to + ", from - " +
                                dataPackage.getHeader().getFrom() + ", CODE is - " +
                                dataPackage.getHeader().getCode());
                if (to == WHO.CONFERENCE.getCode()) {
                    Conversation conversation = current.getMe().getConversation();
                    if (conversation != null) {
                        conversation.sendMessage(dataPackage, current);
                    }else {
                        //tell him that hi is not in conversation
                        try {
                            current.writer.writeStopConv(current.getId());
                        } catch (IOException e) {
                            current.close();
                        }
                    }
                    return;
                }

                ServerController receiver = checkedGet(current, to);
                if (receiver == null)
                    return;
                try {
                    receiver.writer.transferPacket(dataPackage);
                } catch (IOException e) {
                    //tell that dude is offline
                    current.onDudeIsMissing();
                }
            };
        }

        private static Consumer<AbstractDataPackage> onCall(ServerController current) {
            return dataPackage -> {
                serverLogger.entering(current.getClass().getName(), "onCall", "Call initiator is - " + current.me);
                int to = dataPackage.getHeader().getTo();
                ServerController receiver = checkedGet(current, to);
                if (receiver == null) {
                    serverLogger.exiting(current.getClass().getName(), "onCall", "Receiver is not on server");
                    return;
                }

                //check if we both in conversations
                ServerUser me = current.me;
                ServerUser dude = receiver.me;
                Runnable release = doubleLock(me, dude);
                try {
                    if (me.inConv() && dude.inConv()) {
                        serverLogger.logp(Level.FINER, current.getClass().getName(), "onCall",
                                "Me and dude in conversation");
                        try {
                            current.writer.writeBothInConversations(current.getId(), receiver.getId());
                        } catch (IOException e) {
                            current.close();
                        }
                        try {
                            receiver.writer.writeBothInConversations(receiver.getId(), current.getId());
                        } catch (IOException ignored) {
                            //His thread will handle shit
                        }
                        return;
                    }
                    try {
                        if (me.inConv()) {
                            String allToString = me.getConversation().getAllToString(current);
                            serverLogger.logp(Level.FINER, current.getClass().getName(), "onCall",
                                    "Me in conversation adding dudes in conversation as data - " + allToString);
                            dataPackage.setData(allToString);
                        }
                        receiver.writer.transferPacket(dataPackage);
                    } catch (IOException e) {
                        //tell that dude is offline
                        current.onDudeIsMissing();
                    }
                } finally {
                    release.run();
                    serverLogger.exiting(current.getClass().getName(), "onCall");
                }
            };
        }

        private static Consumer<AbstractDataPackage> onDisconnect(ServerController current) {
            return dataPackage -> current.close();
        }

        private static Consumer<AbstractDataPackage> onCallAccept(ServerController current) {
            return dataPackage -> {
                serverLogger.entering(current.getClass().getName(), "onCallAccepted",
                        "Dude who accepted is - " + current.getMe());

                int to = dataPackage.getHeader().getTo();
                ServerController receiver = checkedGet(current, to);
                if (receiver == null) {
                    serverLogger.exiting(current.getClass().getName(), "onCallAccepted",
                            "Dude who's call you trying to accept is disconnected - " + to);
                    return;
                }

                ServerUser me = current.me;
                ServerUser dude = receiver.me;
                Runnable release = doubleLock(me, dude);
                //here goes atomic code
                Conversation conversation;
                if (me.inConv()) {
                    serverLogger.logp(Level.FINER, current.getClass().getName(), "onCallAccepted",
                            "Me in conversation so trying to add dude in it and add others as data - " + dude);

                    //add dude to conv and put all dudes from conf to notify him
                    conversation = me.getConversation();
                    dataPackage.setData(conversation.getAllToString(current));
                    conversation.addDude(receiver, current);
                } else if (dude.inConv()) {
                    //add me to dude's conv I already know about dudes in conf
                    serverLogger.logp(Level.FINER, current.getClass().getName(), "onCallAccepted",
                            "Dude in conversation so trying to add me in it - " + me);
                    conversation = dude.getConversation();
                    conversation.addDude(current, receiver);
//                }else if (me.inConv() && dude.inConv()){
                    //merge conversations
                    /*
                    I am too weak to sync this shit
                    Problems start when some one from any conversation
                    calling some one else or even another conversation

                    Tried to think for each conversation to have it's own semaphore
                    but won't work because you need to set conversation in ServerUser
                    so it will be changed while it was waiting for obsolete semaphore

                    So it will be deprecated some time maybe always,
                    and handled on call level which will check if both in conversation
                    will tell caller that dude is already in a conversion
                    */

                } else {
                    //create conv for us
                    serverLogger.logp(Level.FINER, current.getClass().getName(), "onCallAccepted",
                            "Me and dude isn't in any conversation so trying to create new one - " + dude);
                    conversation = new Conversation(current, receiver);
                    me.setConversation(conversation);
                    dude.setConversation(conversation);
                }
                release.run();

                try {
                    receiver.getWriter().transferPacket(dataPackage);
                } catch (IOException ignored) {
                    //Dude disconnected before so it's thread will handle
                }finally {
                    serverLogger.exiting(current.getClass().getName(), "onCallAccepted");
                }

            };
        }

        private static Consumer<AbstractDataPackage> onExitConversation(ServerController current) {
            return dataPackage -> {
                serverLogger.logp(Level.FINER, current.getClass().getName(), "onExitConversation",
                        "I send remove me from conversation - " + current.getMe());
                Conversation conversation = current.getMe().getConversation();
                //check if it is null because some other thread could make you leave
                if (conversation == null)
                    return;
                conversation.removeDude(current);
            };
        }

        private static Consumer<AbstractDataPackage> onSendSound(ServerController current){
            return dataPackage -> {
                Conversation conversation = current.getMe().getConversation();
                if (conversation == null)
                    return;
                conversation.sendSound(dataPackage, current.getId());
            };
        }

        private static ServerController checkedGet(ServerController current, int who) {
            serverLogger.entering(current.getClass().getName(), "checkedGet", "Trying to get this dude from storage - " + who);
            ServerController receiver = current.server.getController(who);
            if (receiver == null) {
                serverLogger.exiting(current.getClass().getName(), "checkedGet", "There is no such dude with such id - " + who);
                current.onDudeIsMissing();
                return null;
            }
            serverLogger.exiting(current.getClass().getName(), "checkedGet", "Find dude with such id - " + receiver.me);
            return receiver;
        }

        private static Runnable doubleLock(ServerUser me, ServerUser dude) {
            if (me.getId() > dude.getId()) {
                me.lock();
                dude.lock();
            } else {
                dude.lock();
                me.lock();
            }
            serverLogger.logp(Level.FINER, ServerController.class.getName(), "doubleLock",
                    "Acquired double lock on me - " + me + ", and - " + dude);
            return () -> {
                me.release();
                dude.release();
                serverLogger.logp(Level.FINER, ServerController.class.getName(), "doubleLock",
                        "Released double lock on me - " + me + ", and - " + dude);
            };
        }
    }
}
