package Com.Networking;

import Com.Networking.Processors.Processable;
import Com.Networking.Processors.Processor;
import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Protocol.CODE;
import Com.Networking.Readers.BaseReader;
import Com.Networking.Utility.Conversation;
import Com.Networking.Utility.ServerUser;
import Com.Networking.Utility.WHO;
import Com.Networking.Writers.ServerWriter;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

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
        writer = new ServerWriter(socket.getOutputStream());
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
            me.setConversation(null);
        }
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
//            server.registerController(me);

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
//        Thread.currentThread().setName(Thread.currentThread().getName() + me.getId());
        //Add listeners here
//        Processor processor = new Processor();
        processor.getOnUsers().setListener(Handlers.onUsersRequest(this));
        processor.getOnMessage().setListener(Handlers.onTransfer(this));
        processor.getOnDisconnect().setListener(Handlers.onDisconnect(this));
        processor.getOnCall().setListener(Handlers.onCall(this));
        processor.getOnCallAccept().setListener(Handlers.onCallAccept(this));
        processor.getOnCallCancel().setListener(Handlers.onTransfer(this));
        processor.getOnCallDeny().setListener(Handlers.onTransfer(this));

//        processor.setListener(ServerHandlerProvider.createUsersRequestListener(this));
//        processor.setListener(ServerHandlerProvider.createConvHandler(this));
//        processor.setListener(ServerHandlerProvider.createTransferHandler(this));
//        this.processor = processor;
        server.registerController(this);
        sendUsers();
        //        reader.start("Server reader - " + getId());
    }

    private void sendUsers() {
        String users = server.getUsers(getId());
        try {
            writer.writeUsers(getId(), users);
        } catch (IOException e) {
            //dude was disconnected
            close();
        }
    }

    private void onDudeIsMissing(int whoIsMissing) {
        try {
            writer.writeDudeIsOffline(WHO.SERVER.getCode(), getId(), String.valueOf(whoIsMissing));
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

    private static class Handlers {

        /**
         * Get user list as string
         * Send it
         *
         * @return handler for users
         */

        private static Consumer<AbstractDataPackage> onUsersRequest(ServerController current) {
            return dataPackage -> current.sendUsers();
        }

        private static Consumer<AbstractDataPackage> onTransfer(ServerController current) {
            return dataPackage -> {
                int to = dataPackage.getHeader().getTo();
                if (to == WHO.CONFERENCE.getCode()) {
                    Conversation conversation = current.getMe().getConversation();
                    if (conversation != null) {
                        conversation.sendMessage(dataPackage, current);
                    }else {
                        //tell him that hi is not in conversation
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
                    current.onDudeIsMissing(to);
                }
            };
        }

        private static Consumer<AbstractDataPackage> onCall(ServerController current) {
            return dataPackage -> {
                int to = dataPackage.getHeader().getTo();
                ServerController receiver = checkedGet(current, to);
                if (receiver == null)
                    return;

                //check if we both in conversations
                ServerUser me = current.me;
                ServerUser dude = receiver.me;
                Runnable release = doubleLock(me, dude);
                try {
                    if (me.inConv() && dude.inConv()) {
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
                            dataPackage.setData(me.getConversation().getAllToString(current));
                        }
                        receiver.writer.transferPacket(dataPackage);
                    } catch (IOException e) {
                        //tell that dude is offline
                        current.onDudeIsMissing(to);
                    }
                } finally {
                    release.run();
                }
            };
        }

        private static Consumer<AbstractDataPackage> onDisconnect(ServerController current) {
            return dataPackage -> {
                current.close();
                //check conversation cleanup
            };
        }

        private static Consumer<AbstractDataPackage> onCallAccept(ServerController current) {
            return dataPackage -> {
                int to = dataPackage.getHeader().getTo();
                ServerController receiver = checkedGet(current, to);
                if (receiver == null)
                    return;

                ServerUser me = current.me;
                ServerUser dude = receiver.me;
                Runnable release = doubleLock(me, dude);
                //here goes atomic code
                Conversation conversation;
                if (me.inConv()) {
                    //add dude to conv
                    conversation = me.getConversation();
                    dataPackage.setData(conversation.getAllToString(current));
                    conversation.addDude(receiver, current);
                    dude.setConversation(conversation);
                } else if (dude.inConv()) {
                    //add me to dude's conv
                    conversation = dude.getConversation();
                    dataPackage.setData(conversation.getAllToString(receiver));
                    conversation.addDude(current, receiver);
                    me.setConversation(conversation);
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
                    conversation = new Conversation(current, receiver);
                    me.setConversation(conversation);
                    dude.setConversation(conversation);
                }
                release.run();

                try {
                    receiver.getWriter().writeCallAccepted(
                            me.getId(),
                            dude.getId(),
                            conversation.getAllToString(receiver)
                    );
                } catch (IOException ignored) {
                    //Dude disconnected before so it's thread will handle
                }

            };
        }

        private static ServerController checkedGet(ServerController current, int who) {
            ServerController receiver = current.server.getController(who);
            if (receiver == null) {
                current.onDudeIsMissing(who);
            }
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
            return () -> {
                me.release();
                dude.release();
            };
        }
    }
}
