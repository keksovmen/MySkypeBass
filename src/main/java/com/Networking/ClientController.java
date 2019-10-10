package com.Networking;

import com.Audio.AudioSupplier;
import com.Model.ChangeableModel;
import com.Networking.Processors.ClientProcessor;
import com.Networking.Processors.Processable;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Readers.BaseReader;
import com.Networking.Utility.BaseUser;
import com.Networking.Utility.ClientUser;
import com.Networking.Utility.ProtocolValueException;
import com.Networking.Utility.WHO;
import com.Networking.Writers.ClientWriter;
import com.Pipeline.ACTIONS;
import com.Pipeline.ActionableLogic;
import com.Pipeline.ActionsHandler;
import com.Pipeline.BUTTONS;
import com.Util.FormatWorker;
import com.Util.Interfaces.Registration;
import com.Util.Resources;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

/**
 * Uses only as holder of network stuff
 * and handle networking
 * Changes model
 */

public class ClientController extends BaseController implements Registration<ActionsHandler>, ActionsHandler {

    private final ClientProcessor processor;
    private final ChangeableModel model;
    private final ClientResponder clientResponder;

    private final List<ActionsHandler> handlerList;

    private ClientWriter writer;
    private ClientUser me;


    public ClientController(ChangeableModel model) {
        this.model = model;
        processor = new ClientProcessor();
        clientResponder = new ClientResponder();
        handlerList = new ArrayList<>();
    }

    /**
     * Try to establish a TCP connection
     *
     * @param hostName ip address
     * @param port     to connect
     * @return true if connected to the server
     */

    public boolean connect(String myName, final String hostName, final int port) {
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
            socket.connect(new InetSocketAddress(hostName, port), Resources.getTimeOut() * 1000);
            int bufferSize = Resources.getBufferSize() * 1024;
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

    public boolean connect(String myName, final String hostName, final String port) {
        return connect(
                myName,
                hostName,
                Integer.parseInt(port)
        );
    }


    private int getId() {
        return me.getId();
    }

    public ActionableLogic getController() {
        return clientResponder;
    }

    public Consumer<byte[]> getSendSoundFunction() {
        return bytes -> {
            try {
                writer.writeSound(me.getId(), bytes);
            } catch (IOException ignored) {
                //Mic thread has nothing to do with anything here
            }
        };
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
            int micCaptureSize = FormatWorker.parseMicCaptureSize(read.getDataAsString());
            DataPackagePool.returnPackage(read);

            //sets audio format and tell the server can speaker play format or not
            if (!AudioSupplier.setAudioFormat(audioFormat, micCaptureSize)) {
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
            getProcessor().process(reader.read());
        } catch (IOException e) {
            onNetworkException();
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
        processor.getOnSendSound().setListener(this::onSendSound);
    }

    @Override
    void cleanUp() {
        //Need to notify the whole system
        plainRespond(ACTIONS.DISCONNECTED);
        me.drop();
    }

    @Override
    Processable getProcessor() {
        return processor;
    }

    void onUsers(AbstractDataPackage dataPackage) {
        String users = dataPackage.getDataAsString();
        model.addToModel(BaseUser.parseUsers(users));
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
        synchronized (me) {
            if (me.isCalling() != ClientUser.NO_ONE) {
                //Auto deny because you already calling and send some shit
                //that will tell that you wre called
                try {
                    writer.writeDeny(getId(), sender.getId());
                    dudeRespond(ACTIONS.CALLED_BUT_BUSY, sender);
                } catch (IOException ignored) {
                }
                return;
            }
            me.call(sender.getId());
        }
        String dudesInConv = dataPackage.getDataAsString();
        //Use BaseUser.parse(dudesInConv)
        handle(ACTIONS.INCOMING_CALL, sender, dudesInConv, null, -1);
    }

    void onCallCanceled(AbstractDataPackage dataPackage) {
        me.drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        dudeRespond(ACTIONS.CALL_CANCELLED, baseUser);
    }

    void onCallDenied(AbstractDataPackage dataPackage) {
        me.drop();
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        dudeRespond(ACTIONS.CALL_DENIED, baseUser);
    }

    void onCallAccepted(AbstractDataPackage dataPackage) {
        me.drop();
        BaseUser dude = model.getUserMap().get(dataPackage.getHeader().getFrom());

        callAcceptRoutine(dude, dataPackage.getDataAsString());
    }

    void onBothInConversation(AbstractDataPackage dataPackage) {
        if (me.isCalling() == dataPackage.getHeader().getFrom())
            me.drop();
        dudeRespond(ACTIONS.BOTH_IN_CONVERSATION,
                model.getUserMap().get(dataPackage.getHeader().getFrom()));
    }

    void onExitConversation(AbstractDataPackage dataPackage) {
        plainRespond(ACTIONS.EXITED_CONVERSATION);
        model.clearConversation();
        AbstractDataPackagePool.clearStorage();
    }

    void onRemoveDudeFromConversation(AbstractDataPackage dataPackage) {
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        model.removeFromConversation(baseUser);
    }

    void onAddDudeToConversation(AbstractDataPackage dataPackage) {
        BaseUser baseUser = model.getUserMap().get(dataPackage.getHeader().getFrom());
        model.addToConversation(baseUser);
    }

    void onSendSound(AbstractDataPackage dataPackage) {
        int from = dataPackage.getHeader().getFrom();
        handle(ACTIONS.INCOMING_SOUND,
                model.getUserMap().get(from),
                null,
                dataPackage.getData(),
                from
        );
    }

    void onNetworkException() {
        plainRespond(ACTIONS.CONNECTION_TO_SERVER_FAILED);
    }

    private void plainRespond(ACTIONS action) {
        handle(action, null, null, null, -1);
    }

    private void stringRespond(ACTIONS action, String data) {
        handle(action, null, data, null, -1);
    }

    private void dudeRespond(ACTIONS action, BaseUser dude) {
        handle(action, dude, null, null, dude.getId());
    }

    private void callAcceptRoutine(BaseUser dude, String others) {
        plainRespond(ACTIONS.CALL_ACCEPTED);
        model.addToConversation(dude);
        for (BaseUser baseUser : BaseUser.parseUsers(others)) {
            model.addToConversation(baseUser);
        }
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
            boolean connect = connect(name, hostName, port);//Load buffer size from properties
            if (!connect) {
                //tell gui to show that can't connect to the server
                plainRespond(ACTIONS.CONNECT_FAILED);
                return;
            }
            boolean audioFormatAccepted = start("Client Reader");
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
                stringRespond(ACTIONS.PORT_ALREADY_BUSY, port);
                return;
            } catch (ProtocolValueException e) {
                stringRespond(ACTIONS.INVALID_AUDIO_FORMAT, e.getMessage());
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
            if (!FormatWorker.verifyOnlyDigits(port)) {
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
                writer.writeUsersRequest(getId());
            } catch (IOException e) {
                //reader will fix all problems
            }
        }

        private void onMessageSend(int to, String message) {
            try {
                writer.writeMessage(getId(), to, message);
            } catch (IOException e) {
                //reader will fix all problems
            }
        }

        private void onDisconnect() {
            try {
                writer.writeDisconnect(getId());
            } catch (IOException ignored) {
            }
            close();
            model.clear();
            plainRespond(ACTIONS.DISCONNECTED);
        }

        private void onCallSomeOne(BaseUser baseUser) {
            synchronized (me) {
                if (me.isCalling() != ClientUser.NO_ONE) {
                    plainRespond(ACTIONS.ALREADY_CALLING_SOMEONE);
                    return;
                }
                if (model.inConversationWith(baseUser))
                    return;
                me.call(baseUser.getId());
            }
            try {
                writer.writeCall(getId(), baseUser.getId());
                dudeRespond(ACTIONS.OUT_CALL, baseUser);
            } catch (IOException e) {
                //reader will fix all problems
            }
        }

        private void onDenyCall(BaseUser user) {
            me.drop();
            try {
                writer.writeDeny(getId(), user.getId());
            } catch (IOException e) {
                //reader will fix all problems
            }
        }

        private void onCancelCall(BaseUser user) {
            me.drop();
            try {
                writer.writeCancel(getId(), user.getId());
            } catch (IOException e) {
                //reader will fix all problems
            }
        }

        private void onCallAccepted(BaseUser dude, String others) {
            me.drop();
            try {
                writer.writeAccept(getId(), dude.getId());
            } catch (IOException e) {
                //reader will fix all problems
                return;
            }
            callAcceptRoutine(dude, others);

        }

        private void onExitConference() {
            try {
                writer.writeDisconnectFromConv(getId());
                model.clearConversation();
                plainRespond(ACTIONS.EXITED_CONVERSATION);
            } catch (IOException e) {
                //reader will fix all problems
            }
            AbstractDataPackagePool.clearStorage();
        }

    }
}
