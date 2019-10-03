package Com.Networking;

import Com.Audio.AudioClient;
import Com.Model.ClientModel;
import Com.Model.Registration;
import Com.Networking.Processors.ClientProcessor;
import Com.Networking.Processors.Processable;
import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Protocol.DataPackagePool;
import Com.Networking.Readers.BaseReader;
import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.ClientUser;
import Com.Networking.Utility.WHO;
import Com.Networking.Writers.ClientWriter;
import Com.Pipeline.ACTIONS;
import Com.Pipeline.ActionableLogic;
import Com.Pipeline.ActionsHandler;
import Com.Pipeline.BUTTONS;
import Com.Util.FormatWorker;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;

public class ClientController extends BaseController implements Registration<ActionsHandler>, ActionsHandler {

    private final ClientProcessor processor;
    private final ClientModel model;
    private final ClientResponder clientResponder;

    private final List<ActionsHandler> handlerList;

    private ClientWriter writer;
    private ClientUser me;


    /**
     * Uses only for holder of network stuff
     * and handle networking
     */

    public ClientController(ClientModel model) {
        this.model = model;
        processor = new ClientProcessor();
        clientResponder = new ClientResponder();
        handlerList = new ArrayList<>();
    }

    /**
     * Try to establish a TCP connection
     *
     * @param hostName   ip address
     * @param port       to connect
     * @param bufferSize buffer size for reader and writer
     * @return true if connected to the server
     */

    public boolean connect(String myName, final String hostName,
                           final int port, final int bufferSize) {
        if (socket != null &&
                !socket.isClosed()) {
            throw new IllegalStateException("ClientResponder's socket is already opened. " +
                    "Close it before connecting again");
        }

        if (!FormatWorker.isHostNameCorrect(hostName))
            throw new IllegalArgumentException(
                    "Host name is in wrong format - " + hostName);

        socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(hostName, port), 7_000); // timeOut as property
            writer = new ClientWriter(socket.getOutputStream(), bufferSize);
            reader = new BaseReader(socket.getInputStream(), bufferSize);
        } catch (IOException e) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            return false;
        }
        me = new ClientUser(myName, WHO.NO_NAME.getCode());

        return true;
    }

    public boolean connect(String myName, final String hostName,
                           final String port, final String bufferSize) {
        return connect(
                myName,
                hostName,
                Integer.parseInt(port),
                Integer.parseInt(bufferSize)
        );
    }

    public ClientWriter getWriter() {
        return writer;
    }

    public BaseUser getMe() {
        return me;
    }

    public int getId() {
        return me.getId();
    }

    public ActionableLogic getController() {
        return clientResponder;
    }

    @Override
    public boolean registerListener(ActionsHandler listener) {
        return handlerList.add(listener);
    }

    @Override
    public boolean removeListener(ActionsHandler listener) {
        return handlerList.remove(listener);
    }

    @Override
    public void handle(ACTIONS action, BaseUser from, String stringData, byte[] bytesData, int intData) {
        handlerList.forEach(handler ->
                handler.handle(action, from, stringData, bytesData, intData));
    }

    /**
     * Trying to authenticate first writes your name
     * second read audio format and checks it if supported
     * send can use it or not
     * third creates clientResponder user with unique id from the server
     */

    @Override
    boolean authenticate() {
        try {
            writer.writeName(me.getName());

            AbstractDataPackage read = reader.read();
            AudioFormat audioFormat = FormatWorker.parseAudioFormat(read.getDataAsString());
            DataPackagePool.returnPackage(read);

            //sets audio format and tell the server can speaker play format or not
            if (!AudioClient.getInstance().setAudioFormat(audioFormat)) {
                writer.writeDeny(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
                return false;
            }
            writer.writeApproveAudioFormat(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());

            read = reader.read();
            me = new ClientUser(
                    me.getName(),
                    read.getHeader().getTo()
            );
            AbstractDataPackagePool.returnPackage(read);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }

    /**
     * You don't want to return package because it is processed in another thread
     *
     * @throws IOException if network fails
     */

    @Override
    void mainLoopAction() throws IOException {
        try {
            processor.process(reader.read());
        } catch (IOException e) {
            plainRespond(ACTIONS.CONNECTION_TO_SERVER_FAILED);
            throw e;
        }
    }

    @Override
    void dataInitialisation() {
        //Add all your possible action handler
        processor.getOnUsers().setListener(this::onUsers);
        processor.getOnAddUserToList().setListener(this::onAddUserToList);
        processor.getOnRemoveUserFromList().setListener(this::onRemoveUserFromList);
        processor.getOnMessage().setListener(this::onIncomingMessage);
        processor.getOnCall().setListener(this::onIncomingCall);
        processor.getOnCallCancel().setListener(this::onCallCanceled);
        processor.getOnCallDeny().setListener(this::onCallDenied);
        processor.getOnCallAccept().setListener(this::onCallAccepted);
        processor.getOnBothInConversation().setListener(this::onBothInConversation);
        processor.getOnExitConference().setListener(this::onExitConversation);
        processor.getOnRemoveDudeFromConversation().setListener(this::onRemoveDudeFromConversation);
        processor.getOnAddDudeToConversation().setListener(this::onAddDudeToConversation);
    }

    @Override
    void cleanUp() {
        //Need to notify the whole system
        plainRespond(ACTIONS.DISCONNECTED);
    }

    @Override
    Processable getProcessor() {
        return processor;
    }

    void onUsers(AbstractDataPackage dataPackage) {
        String users = dataPackage.getDataAsString();
        model.updateModel(BaseUser.parseUsers(users));
    }

    void onAddUserToList(AbstractDataPackage dataPackage) {
        String user = dataPackage.getDataAsString();
        model.addToModel(BaseUser.parse(user));
    }

    void onRemoveUserFromList(AbstractDataPackage dataPackage) {
        int user = dataPackage.getDataAsInt();
        model.removeFromModel(user);
    }

    void onIncomingMessage(AbstractDataPackage dataPackage) {
        BaseUser sender = model.getUserMap().get(dataPackage.getHeader().getFrom());
        handle(
                ACTIONS.INCOMING_MESSAGE,
                sender,
                dataPackage.getDataAsString(),
                null,
                dataPackage.getHeader().getTo() == WHO.CONFERENCE.getCode() ? 1 : 0
        );
    }

    void onIncomingCall(AbstractDataPackage dataPackage) {
        BaseUser sender = model.getUserMap().get(dataPackage.getHeader().getFrom());
        if (model.getMe().isCalling()) {
            //Auto deny because you already calling and send some shit
            //that will tell that you wre called
            try {
                writer.writeDeny(model.getMe().getId(), sender.getId());
                dudeRespond(ACTIONS.CALLED_BUT_BUSY, sender);
            } catch (IOException e) {
                onNetworkException();
            }
            return;
        }
        model.getMe().call();
        String dudesInConv = dataPackage.getDataAsString();
        //Use BaseUser.parse(dudesInConv)
        handle(ACTIONS.INCOMING_CALL, sender, dudesInConv, null, -1);
    }

    void onCallCanceled(AbstractDataPackage dataPackage) {
        model.getMe().drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        dudeRespond(ACTIONS.CALL_CANCELLED, baseUser);
    }

    void onCallDenied(AbstractDataPackage dataPackage) {
        model.getMe().drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        dudeRespond(ACTIONS.CALL_DENIED, baseUser);
    }

    void onCallAccepted(AbstractDataPackage dataPackage) {
        model.getMe().drop();
        BaseUser dude = model.getUserMap().get(dataPackage.getHeader().getFrom());
        handle(
                ACTIONS.CALL_ACCEPTED,
                dude,
                dataPackage.getDataAsString(),
                null,
                -1
        );
    }

    void onBothInConversation(AbstractDataPackage dataPackage) {
        dudeRespond(ACTIONS.BOTH_IN_CONVERSATION,
                model.getUserMap().get(dataPackage.getHeader().getFrom()));
    }

    void onExitConversation(AbstractDataPackage dataPackage) {
        plainRespond(ACTIONS.EXITED_CONVERSATION);
    }

    void onRemoveDudeFromConversation(AbstractDataPackage dataPackage) {
        dudeRespond(
                ACTIONS.REMOVE_DUDE_FROM_CONVERSATION,
                model.getUserMap().get(dataPackage.getHeader().getFrom())
        );
    }

    void onAddDudeToConversation(AbstractDataPackage dataPackage) {
        dudeRespond(
                ACTIONS.ADD_DUDE_TO_CONVERSATION,
                model.getUserMap().get(dataPackage.getHeader().getFrom())
        );
    }

    private void onNetworkException() {
        plainRespond(ACTIONS.CONNECTION_TO_SERVER_FAILED);
        close();
    }

    private void plainRespond(ACTIONS action) {
        handle(action, null, null, null, -1);
    }

    private void stringRespond(ACTIONS action, String data) {
        handle(action, null, data, null, -1);
    }

    private void dudeRespond(ACTIONS action, BaseUser dude) {
        handle(action, dude, null, null, -1);
    }


    /**
     * Helps handle all shit that comes from UI
     */

    private class ClientResponder implements ActionableLogic {

        private Server server; // made final

        private ClientResponder() {
        }


        @Override
        public void act(BUTTONS button, Object plainData, String stringData, int integerData) {
            switch (button) { //there probably will be swing thread
                case CONNECT: {
                    processor.execute(() -> onConnect(plainData));
                    return;
                }
                case CREATE_SERVER: {
                    onServerCreate(plainData);
                    return;
                }
                case ASC_FOR_USERS: {
                    onUsersRequest();
                    return;
                }
                case SEND_MESSAGE: {
                    onMessageSend(integerData, stringData);
                    return;
                }
                case DISCONNECT: {
                    onDisconnect();
                    return;
                }
                case CALL: {
                    onCallSomeOne((BaseUser) plainData);
                    return;
                }
                case CALL_DENIED: {
                    onDenyCall((BaseUser) plainData);
                    return;
                }
                case CALL_CANCELLED: {
                    onCancelCall((BaseUser) plainData);
                    return;
                }
                case CALL_ACCEPTED: {
                    onCallAccepted((BaseUser) plainData, stringData);
                    return;
                }
                case EXIT_CONFERENCE: {
                    onExitConference();
                    return;
                }

            }
        }

        /* Action for UI here */

        void onConnect(Object plainData) {
            String[] data = (String[]) plainData;

            String name = data[0];
            if (FormatWorker.checkZeroLength(name))
                name = System.getProperty("user.name"); // or get from property map

            String hostName = data[1];
            if (FormatWorker.checkZeroLength(hostName))
                hostName = "127.0.0.1"; // or get from property
            if (!FormatWorker.isHostNameCorrect(hostName)) {
                stringRespond(ACTIONS.WRONG_HOST_NAME_FORMAT, hostName);
                return;
            }

            String port = data[2];
            if (FormatWorker.checkZeroLength(port))
                port = "8188"; // or get from property
            if (!checkPort(port))
                return;
            onConnect(hostName, Integer.parseInt(port), name);
        }

        void onConnect(String hostName, int port, String name) {
//        model.setMe(new ClientUser(name, WHO.NO_NAME.getCode()));
            boolean connect = connect(name, hostName, port, 8192);//Load buffer size from properties
            if (!connect) {
                //tell gui to show that can't connect to the server
                plainRespond(ACTIONS.CONNECT_FAILED);
                return;
            }
            boolean audioFormatAccepted = start("ClientResponder Reader");
            if (!audioFormatAccepted) {
                //tell gui to show that audio format can't be set
                stringRespond(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED,
                        "HERE GOES AUDIO FORMAT AS STRING");
                return;
            }
            stringRespond(ACTIONS.AUDIO_FORMAT_ACCEPTED,
                    "HERE GOES AUDIO FORMAT AS STRING");

            stringRespond(ACTIONS.CONNECT_SUCCEEDED, me.toString());
        }

        private void onServerCreate(Object plainData) {
            String[] data = (String[]) plainData;
            String port = data[0];
            if (FormatWorker.checkZeroLength(port))
                port = "8188"; // or get from property map
            if (!checkPort(port))
                return;

            String sampleRate = data[1];
            if (FormatWorker.checkZeroLength(sampleRate)
                    || !FormatWorker.verifyOnlyDigits(sampleRate)) {
                stringRespond(ACTIONS.WRONG_SAMPLE_RATE_FORMAT, sampleRate);
                return;
            }

            String sampleSize = data[2];
            if (FormatWorker.checkZeroLength(sampleSize)
                    || !FormatWorker.verifyOnlyDigits(sampleSize)) {
                stringRespond(ACTIONS.WRONG_SAMPLE_SIZE_FORMAT, sampleSize);
                return;
            }

            onServerCreate(port, sampleRate, sampleSize);
        }

        private void onServerCreate(String port, String sampleRate, String sampleSize) {
            try {
                server = Server.getFromStrings(port, sampleRate, sampleSize);
            } catch (IOException e) {
                server = null;
                stringRespond(ACTIONS.PORT_ALREADY_BUSY, port);
                return;
            }
            boolean start = this.server.start("Server");
            if (start) {
                plainRespond(ACTIONS.SERVER_CREATED);
            } else {
                plainRespond(ACTIONS.SERVER_CREATED_ALREADY);
            }
        }

        private boolean checkPort(String port) {
            if (!FormatWorker.verifyPortFormat(port)) {
                stringRespond(ACTIONS.WRONG_PORT_FORMAT, port);
                return false;
            }
            int portAsInt = Integer.parseInt(port);
            if (!FormatWorker.portInRange(portAsInt)) {
                handle(
                        ACTIONS.PORT_OUT_OF_RANGE,
                        null,
                        "0 < port < " + 0xFFFF,
                        null,
                        portAsInt
                );
                return false;
            }
            return true;
        }

        private void onUsersRequest() {
            try {
                getWriter().writeUsersRequest(getId());
            } catch (IOException e) {
                onNetworkException();
            }
        }

        private void onMessageSend(int to, String message) {
            try {
                writer.writeMessage(getId(), to, message);
            } catch (IOException e) {
//            e.printStackTrace();//disconnected act
                onNetworkException();
//            plainRespond(ACTIONS.CONNECTION_TO_SERVER_FAILED);

            }
        }

        private void onNetworkException() {
            plainRespond(ACTIONS.CONNECTION_TO_SERVER_FAILED);
            close();
        }

        private void onDisconnect() {
            try {
                writer.writeDisconnect(getId());
            } catch (IOException ignored) {
            }
            close();
            model.clear();
            plainRespond(ACTIONS.DISCONNECTED);
            //audio close too

        }

        private void onCallSomeOne(BaseUser baseUser) {
            if (me.isCalling()) {
                plainRespond(ACTIONS.ALREADY_CALLING_SOMEONE);
                return;
            }
            me.call();
            try {
                writer.writeCall(getId(), baseUser.getId());
                dudeRespond(ACTIONS.OUT_CALL, baseUser);
            } catch (IOException e) {
                onNetworkException();
            }
        }

        private void onDenyCall(BaseUser user) {
            model.getMe().drop();
            try {
                writer.writeDeny(model.getMe().getId(), user.getId());
//            dudeRespond(ACTIONS.CALL_DENIED, user);
            } catch (IOException e) {
                onNetworkException();
            }
        }

        private void onCancelCall(BaseUser user) {
            model.getMe().drop();
            try {
                writer.writeCancel(model.getMe().getId(), user.getId());
//            dudeRespond(ACTIONS.CALL_CANCELLED, user);
            } catch (IOException e) {
                onNetworkException();
            }
        }

        private void onCallAccepted(BaseUser dude, String others) {
            me.drop();
            try {
                writer.writeAccept(model.getMe().getId(), dude.getId());
            } catch (IOException e) {
                onNetworkException();
                return;
            }
            //Audio tell to add corresponding outputs
            //tell gui to act
            handle(ACTIONS.CALL_ACCEPTED, dude, others, null, -1);

        }

        private void onExitConference() {
            try {
                writer.writeDisconnectFromConv(model.getMe().getId());
                plainRespond(ACTIONS.EXITED_CONVERSATION);
            } catch (IOException e) {
                onNetworkException();
            }
        }
    }
}
