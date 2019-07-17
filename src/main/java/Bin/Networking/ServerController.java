package Bin.Networking;

import Bin.Networking.Processors.Processable;
import Bin.Networking.Processors.ServerProcessor;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Readers.ReaderWithHandler;
import Bin.Networking.Utility.Conversation;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Utility.ServerUser;
import Bin.Networking.Writers.BaseWriter;
import Bin.Networking.Writers.ServerWriter;

import java.io.IOException;
import java.net.Socket;
import java.util.function.Consumer;

/**
 * Handles all server actions for the connected user
 */

public class ServerController implements ErrorHandler {

    private final Socket socket;
    private ServerWriter writer;
    private ReaderWithHandler reader;
    private Processable serverProcessor;
    private ServerUser me;
    private final Server server;

    public ServerController(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        writer = new ServerWriter(socket.getOutputStream(), this);
        serverProcessor = new ServerProcessor();
        reader = new ReaderWithHandler(socket.getInputStream(), serverProcessor, this);
    }

    /**
     * Start authenticate procedure
     * if success starts a new thread to handle the user
     * otherwise disconnect him
     */

    void start() {
        try {
            authenticate();
            launch();
        } catch (IOException e) {
            e.printStackTrace();
            errorCase();
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
     * @throws IOException if connection get ruined or disconnected
     */

    private void authenticate() throws IOException {
        AbstractDataPackage dataPackage = reader.read();

        final String name = dataPackage.getDataAsString();
        final int id = server.getIdAndIncrement();
        boolean canHear = false;

        writer.writeAudioFormat(id, server.getAudioFormat());
        AbstractDataPackagePool.returnPackage(dataPackage);
        dataPackage = reader.read();

        if (dataPackage.getHeader().getCode() == BaseWriter.CODE.SEND_APPROVE) {
            canHear = true;
        }
        me = new ServerUser(name, id, this, canHear);
        server.addUser(me);

        AbstractDataPackagePool.returnPackage(dataPackage);
    }

    private void launch() {
        serverProcessor.addListener(ServerHandlerProvider.createUsersRequestListener(this));
        serverProcessor.addListener(ServerHandlerProvider.createConvHandler(this));
        serverProcessor.addListener(ServerHandlerProvider.createTransferHandler(this));
        reader.start("Server reader - " + getId());
    }

    public ServerWriter getWriter() {
        return writer;
    }

    private int getId() {
        return me.getId();
    }


    @Override
    public void errorCase() {
        if (me.inConv()) {
            me.getConversation().removeDude(me);
        }
        server.removeUser(getId());
        try {
            socket.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            iterate();
        }
    }

    @Override
    public ErrorHandler[] getNext() {
        return new ErrorHandler[]{reader};
    }


    private static class ServerHandlerProvider {

        private ServerHandlerProvider() {
        }

        /**
         * Sends all <s>nudes</s> users except you to you
         *
         * @param controller basically this if it wasn't static
         * @return ready to work listener
         */

        private static Consumer<AbstractDataPackage> createUsersRequestListener(ServerController controller) {
            return baseDataPackage -> {
                if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_USERS)) {
                    controller.writer.writeUsers(controller.getId(), controller.server.getUsers(controller.getId()));
                }
            };
        }

        /**
         * Handles all conversation actions
         * <p>
         * MAIN PROBLEM THAT I HAVEN'T TESTED: @see Test
         * When auto accept occurs what will happen?
         * Both clients sendSound approve that triers create conversation handler
         * Maybe change Conversation to static synchronised factory method
         * <p>
         * Tried to make it handle auto accept by locking on this class
         *
         * @param controller basically this if it wasn't static
         * @return ready to work listener
         */

        private static Consumer<AbstractDataPackage> createConvHandler(final ServerController controller) {
            return dataPackage -> {

                switch (dataPackage.getHeader().getCode()) {
                    case SEND_APPROVE: {

                        /**
                         * To reduce code amount
                         */

                        class Helper {

                            private void registerConversation(ServerUser me, ServerUser receiverUser) {
                                Conversation conversation;
                                if (me.inConv()) {
                                    if (receiverUser.inConv()) {
                                        conversation = me.getConversation();
                                        Conversation receiverConversation = receiverUser.getConversation();
                                        /*
                                         * Why no just addDude()?
                                         * Because of some retarded synchronisation when multiple auto accepts occur
                                         * It happens that two dudes * already in both conferences indicated as --
                                         * * - * -- * - *
                                         * |            |
                                         * *            *
                                         * But others not, because of that you need to clear both sides when colliding them
                                         */
                                        Conversation.registerComplexConversation(conversation.getAll(), receiverConversation.getAll());
                                    } else {
                                        conversation = me.getConversation();
                                        dataPackage.setData(conversation.getAllToString(me));
                                        conversation.addDude(me, receiverUser);
                                    }
                                } else {
                                    if (receiverUser.inConv()) {
                                        conversation = receiverUser.getConversation();
                                        conversation.addDude(receiverUser, me);
                                    } else {
                                        Conversation.registerSimpleConversation(me, receiverUser);
                                    }
                                }
                            }
                        }

                        final ServerController receiver = controller.server.getController(dataPackage.getHeader().getTo());
                        //case when dude disconnected before approve was received
                        if (receiver == null) {
                            int id = controller.getId();
                            controller.writer.writeStopConv(id);
                            controller.writer.writeUsers(id, controller.server.getUsers(id));
                            break;
                        }

                    /*
                    Has to be atomic operation for auto accept purposes
                    The best I can do
                    Main problem is when you try get intrinsic lock on any server user
                    you will end up with dead lock
                    So you need something that can be a bridge between two threads
                    Initially though to use server executor, but it is not single thread

                    Solved thanks to Java Concurrency In Practice 10.1.2 Dynamic Lock Order Deadlocks
                     */
                        final int myHash = System.identityHashCode(controller.me);
                        final int yourHash = System.identityHashCode(receiver.me);
                        if (myHash > yourHash) {
                            synchronized (controller.me) {
                                synchronized (receiver.me) {
//                                    System.out.println("my > your \t" + Integer.toHexString(myHash).toUpperCase() + "\t"
//                                            + controller.me + " " + receiver.me + "\t" + Integer.toHexString(yourHash).toUpperCase());
                                    new Helper().registerConversation(controller.me, receiver.me);
                                }
                            }
                        } else if (myHash < yourHash) {
                            synchronized (receiver.me) {
                                synchronized (controller.me) {
//                                    System.out.println("my < your \t" + Integer.toHexString(myHash).toUpperCase() + "\t" +
//                                            controller.me + " " + receiver.me + "\t" + Integer.toHexString(yourHash).toUpperCase());
                                    new Helper().registerConversation(controller.me, receiver.me);
                                }
                            }
                        } else {
                            synchronized (ServerController.class) {
                                synchronized (controller.me) {
                                    synchronized (receiver.me) {
                                        new Helper().registerConversation(controller.me, receiver.me);
                                    }
                                }
                            }
                        }
                        break;
                    }
                    case SEND_DISCONNECT_FROM_CONV: {
                        Conversation myConv = controller.me.getConversation();
                        if (myConv != null) {
                            myConv.removeDude(controller.me);
                        }
                        break;
                    }
                    case SEND_CALL: {
                        Conversation myConv = controller.me.getConversation();
                        if (myConv != null) {
                            dataPackage.setData(myConv.getAllToString(controller.me));
                        }
                        break;
                    }
                }
            };
        }

        /**
         * Handles all staff that is not for conversation and not for server
         * Just sendSound messages and some control instructions
         *
         * @param controller basically this if it wasn't static
         * @return ready to work handler
         */

        private static Consumer<AbstractDataPackage> createTransferHandler(ServerController controller) {
            return dataPackage -> {
                if (dataPackage.getHeader().getTo() != BaseWriter.WHO.SERVER.getCode()) {
                    /*All that belong to conversation*/
                    if (dataPackage.getHeader().getTo() == BaseWriter.WHO.CONFERENCE.getCode()) {
                        Conversation myConv = controller.me.getConversation();
                        if (myConv == null) {
                            return;
                        }
                        switch (dataPackage.getHeader().getCode()) {
                            case SEND_SOUND: {
                                myConv.sendSound(dataPackage, controller.getId());
                                break;
                            }
                            case SEND_MESSAGE: {
                                myConv.sendMessage(dataPackage, controller.getId());
                                break;
                            }
                        }
                    } else {
                        /*All that belong to direct transition*/
                        ServerController controllerReceiver = controller.server.getController(dataPackage.getHeader().getTo());
                        if (controllerReceiver != null) {
                            controllerReceiver.writer.transferData(dataPackage);
                        } else {
                            controller.writer.writeUsers(controller.getId(), controller.server.getUsers(controller.getId()));
                        }
                    }
                }
            };
        }
    }

}
