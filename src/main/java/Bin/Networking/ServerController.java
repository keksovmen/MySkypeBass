package Bin.Networking;

import Bin.Networking.Processors.ServerProcessor;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Readers.BaseReader;
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
    private BaseReader reader;
    private ServerProcessor serverProcessor;
    private ServerUser me;
    private final Server server;

    public ServerController(Socket socket, Server server) throws IOException {
        this.socket = socket;
        this.server = server;
        writer = new ServerWriter(socket.getOutputStream(), this);
        serverProcessor = new ServerProcessor();
        reader = new BaseReader(socket.getInputStream(), serverProcessor, this);
    }

    /**
     * Start authenticate procedure
     * if success starts a new thread to handle the user
     * otherwise disconnect him
     */

    void start() {
        try {
            if (authenticate())
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
     * @return true if only audio format is accepted
     * @throws IOException if connection get ruined or disconnected
     */

    private boolean authenticate() throws IOException {
        AbstractDataPackage dataPackage = reader.read();
        String name = dataPackage.getDataAsString();
        setUser(name);

        final int id = me.getId();

        writer.writeAudioFormat(id, server.getAudioFormat());
        AbstractDataPackagePool.returnPackage(dataPackage);
        dataPackage = reader.read();
        if (dataPackage.getHeader().getCode() != BaseWriter.CODE.SEND_APPROVE) {
            writer.writeDisconnect(id);
            errorCase();
//            disconnect();
            AbstractDataPackagePool.returnPackage(dataPackage);
            return false;
        }
        AbstractDataPackagePool.returnPackage(dataPackage);

        server.addUser(me);

        return true;
    }

    private void launch() {
        serverProcessor.addListener(createUsersRequestListener(this));
        serverProcessor.addListener(createConvHandler(this));
        serverProcessor.addListener(createTransferHandler(this));
        reader.start("Server reader - " + getId());
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
     * MAIN PROBLEM THAT I HAVEN'T TESTED:
     * When auto accept occurs what will happen?
     * Both clients sendSound approve that triers create conversation handler
     * Maybe change Conversation to static synchronised factory method
     *
     * @param controller basically this if it wasn't static
     * @return ready to work listener
     */

    private static Consumer<AbstractDataPackage> createConvHandler(ServerController controller) {
        return dataPackage -> {

            switch (dataPackage.getHeader().getCode()) {
                case SEND_APPROVE: {
                    ServerController receiver = controller.server.getController(dataPackage.getHeader().getTo());
                    //case when dude disconnected before approve was received
                    if (receiver == null) {
                        int id = controller.getId();
                        controller.writer.writeStopConv(id);
                        controller.writer.writeUsers(id, controller.server.getUsers(id));
                        break;
                    }
                    ServerUser receiverUser = receiver.me;
                    Conversation conversation;
                    Conversation receiverConversation;

                    if (controller.me.inConv()) {
                        if (receiverUser.inConv()) {
                            conversation = controller.me.getConversation();
                            receiverConversation = receiverUser.getConversation();
                            new Conversation(conversation.getAll(), receiverConversation.getAll());
                        } else {
                            conversation = controller.me.getConversation();
                            dataPackage.setData(conversation.getAllToString(controller.me));
                            conversation.addDude(controller.me, receiverUser);
                        }
                    } else {
                        if (receiverUser.inConv()) {
                            conversation = receiverUser.getConversation();
                            conversation.addDude(receiverUser, controller.me);
                        } else {
                            new Conversation(controller.me, receiverUser);
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
                        controllerReceiver.writer.transferData(dataPackage);//test it
                    } else {
                        controller.writer.writeUsers(controller.getId(), controller.server.getUsers(controller.getId()));
                    }
                }
            }
        };
    }

    private void setUser(String name) {
        me = new ServerUser(name, server.getIdAndIncrement(), this);
    }

    public ServerWriter getWriter() {
        return writer;
    }

    /**
     * Short cut
     *
     * @return id
     */

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

}
