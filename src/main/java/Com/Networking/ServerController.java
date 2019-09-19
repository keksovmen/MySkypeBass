package Com.Networking;

import Com.Networking.Processors.Processable;
import Com.Networking.Processors.Processor;
import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Protocol.CODE;
import Com.Networking.Readers.BaseReader;
import Com.Networking.Utility.*;
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

    /**
     * Start authenticate procedure
     * if success starts a new thread to handle the user
     * otherwise disconnect him
     */

//    @Override
//    public boolean start(String name) {
//        if (work)
//            return false;
//        work = true;
//
//        if (!authenticate()) {
//            close();
//            return false;
//        }
//        launch();   //Init data here
//
//        new Thread(() -> {
//            mainLoop();
//            cleanUp();
//        }, name).start();
//        return true;
//    }
//    @Override
//    void mainLoopAction() throws IOException {
////        while (work) {
////            try {
//                AbstractDataPackage read = reader.read();
//                processor.process(read);  //Here all possible cases of CODE
////            } catch (IOException e) {   //Case when a dude just ruined his connection
////                    e.printStackTrace();
////                close();
////            }
////        }
//    }

    @Override
    void cleanUp() {
        //Clean up code
        server.removeController(getId());
    }

//    @Override
//    public void close() {
//        work = false;
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

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
//        processor.setListener(ServerHandlerProvider.createUsersRequestListener(this));
//        processor.setListener(ServerHandlerProvider.createConvHandler(this));
//        processor.setListener(ServerHandlerProvider.createTransferHandler(this));
//        this.processor = processor;
        server.registerController(this);
//        reader.start("Server reader - " + getId());
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
                if (baseDataPackage.getHeader().getCode().equals(CODE.SEND_USERS)) {
                    final int id = controller.getId();
                    controller.writer.writeUsers(id, controller.server.getUsers(id));
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

                        /*
                          To reduce code amount
                         */

                        class Helper {

                            private void registerConversation(ServerUser me, ServerUser receiverUser) {
                                Conversation conversation;
                                if (me.inConv()) {
                                    if (receiverUser.inConv()) {

                                        /*
                                        Looks like poison but should work well
                                         */
                                        final int i = System.identityHashCode(me.getConversation());
                                        final int j = System.identityHashCode(receiverUser.getConversation());
                                        if (i > j) {
                                            synchronized (me.getConversation()) {
                                                synchronized (receiverUser.getConversation()) {
                                                    Conversation.registerComplexConversation(me.getConversation().getAll(), receiverUser.getConversation().getAll());
                                                }
                                            }
                                        } else if (i < j) {
                                            synchronized (receiverUser.getConversation()) {
                                                synchronized (me.getConversation()) {
                                                    Conversation.registerComplexConversation(me.getConversation().getAll(), receiverUser.getConversation().getAll());
                                                }
                                            }
                                        } else {
                                            synchronized (Helper.class) {
                                                synchronized (me.getConversation()) {
                                                    synchronized (receiverUser.getConversation()) {
                                                        Conversation.registerComplexConversation(me.getConversation().getAll(), receiverUser.getConversation().getAll());
                                                    }
                                                }
                                            }
                                        }

                                        /*
                                         * Why no just addDude()?
                                         * Because of some retarded synchronisation when multiple auto accepts occur
                                         * It happens that two dudes * already in both conferences indicated as --
                                         * * - * -- * - *
                                         * |            |
                                         * *            *
                                         * But others not, because of that you need to clear both sides when colliding them
                                         */

//                                        Conversation.registerComplexConversation(conversation.getAll(), receiverConversation.getAll());
                                    } else {
//                                        conversation = me.getConversation();
                                        synchronized (me.getConversation()) {
                                            conversation = me.getConversation();
                                            dataPackage.setData(conversation.getAllToString(me));
                                            conversation.addDude(me, receiverUser);
                                        }
                                    }
                                } else {
                                    if (receiverUser.inConv()) {
                                        synchronized (receiverUser.getConversation()) {
                                            conversation = receiverUser.getConversation();
                                            conversation.addDude(receiverUser, me);
                                        }
                                    } else {
                                        Conversation.registerSimpleConversation(me, receiverUser);
                                    }
                                }
                            }
                        }

                        final ServerController receiver = controller.server.getController(dataPackage.getHeader().getTo());
                        //case when dude disconnected before approve was received
                        if (receiver == null) {
                            /*
                            You just ignore it because of you don't know in a conference you are or the dude
                            It'll be handled in transfer handler or could be in conversation
                             */
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
            return new Consumer<AbstractDataPackage>() {
                private int counter;
                private static final int BOUNDARY = 5;

                /**
                 * Idea is each your pocket with sound or text to conference
                 * That can't reach conference because you don't have one
                 * will increase counter when it hit BOUNDARY server will tell you
                 * to stop the conversation
                 *
                 * Needed for synchronise purposes
                 * @return false if you have to stop
                 */

                private boolean increaseCounter() {
                    counter++;
                    if (counter > BOUNDARY) {
                        counter = 0;
                        controller.getWriter().writeStopConv(controller.getId());
                        return false;
                    }
                    return true;
                }

                @Override
                public void accept(AbstractDataPackage dataPackage) {
                    final int to = dataPackage.getHeader().getTo();
                    if (to != WHO.SERVER.getCode()) {
                        /*All that belong to conversation*/
                        if (to == WHO.CONFERENCE.getCode()) {
                            if (!controller.me.inConv()) {
                                increaseCounter();
                                return;
                            }
                            counter = 0;
                            Conversation myConv = controller.me.getConversation();
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
                            ServerController controllerReceiver = controller.server.getController(to);
                            if (controllerReceiver != null) {
                                controllerReceiver.writer.transferData(dataPackage);
                            } else {
                                controller.writer.writeUsers(controller.getId(), controller.server.getUsers(controller.getId()));
                            }
                        }
                    }
                }
            };
        }

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
                String users = current.server.getUsers(current.getId());
                current.writer.writeUsers(current.getId(), users);
            };
        }
    }
}
