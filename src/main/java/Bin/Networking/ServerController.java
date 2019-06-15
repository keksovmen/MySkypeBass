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

    private void launch() {
        serverProcessor.addListener(createUsersRequestListener(this));
        serverProcessor.addListener(createConvHandler(this));
        serverProcessor.addListener(createTransferHandler(this));
        reader.start("Server reader - " + getId());
    }

    private static Consumer<AbstractDataPackage> createUsersRequestListener(ServerController controller) {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_USERS)) {
                controller.writer.writeUsers(controller.getId(), controller.server.getUsers(controller.getId()));
            }
        };
    }

    private static Consumer<AbstractDataPackage> createConvHandler(ServerController controller) {
        return dataPackage -> {

            switch (dataPackage.getHeader().getCode()) {
                case SEND_APPROVE: {
                    ServerController receiver = controller.server.getController(dataPackage.getHeader().getTo());
                    //case when dude disconnected before approve was received
                    if (receiver == null){
                        int id = controller.getId();
                        controller.writer.writeStopConv(id);
                        controller.writer.writeUsers(id, controller.server.getUsers(id));
                        return;
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
                    controller.me.getConversation().removeDude(controller.me);
                    break;
                }
                case SEND_SOUND: {
                    if (controller.me.getConversation() != null) {
                        controller.me.getConversation().send(dataPackage, controller.getId());
                    }
                    break;
                }
                case SEND_CALL: {
                    if (controller.me.inConv()) {
                        dataPackage.setData(controller.me.getConversation().getAllToString(controller.me));
                    }
                    break;
                }
            }
        };
    }

    private static Consumer<AbstractDataPackage> createTransferHandler(ServerController controller) {
        return baseDataPackage -> {
            if ((baseDataPackage.getHeader().getTo() != BaseWriter.WHO.SERVER.getCode())
                    && (baseDataPackage.getHeader().getTo() != BaseWriter.WHO.CONFERENCE.getCode())) {        //add to conversation condition
                ServerController controllerReceiver = controller.server.getController(baseDataPackage.getHeader().getTo());
                if (controllerReceiver != null) {
                    controllerReceiver.writer.transferData(baseDataPackage);//test it
                }else {
                    controller.writer.writeUsers(controller.getId(), controller.server.getUsers(controller.getId()));
                }
            }
        };
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

//    public void disconnect() {
//        server.removeUser(me.getId());
//        try {
//            socket.close();
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//    }

    private void setUser(String name) {
        me = new ServerUser(name, server.getIdAndIncrement(), this);
    }

    public ServerWriter getWriter() {
        return writer;
    }

    /**
     * Short cut
     * @return id
     */

    private int getId() {
        return me.getId();
    }


    @Override
    public void errorCase() {
        if (me.inConv()) {
            me.getConversation().removeDude(me);
//            me.setConversation(null);
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
