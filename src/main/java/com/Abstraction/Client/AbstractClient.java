package com.Abstraction.Client;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Handlers.ClientNetworkHelper;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Networking.Writers.ClientWriter;
import com.Abstraction.Networking.Writers.PlainWriter;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Cryptographics.Crypto;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represent client logic part
 * Contain ClientUser, ClientHandler -> ClientProcessor -> ClientController
 */

public abstract class AbstractClient implements Logic {


    protected final ChangeableModel model;
    protected final List<LogicObserver> observerList;

    /**
     * Not ExecutorService because this object suppose to live
     * from beginning till the end of program, so no need to
     * shutdown etc.
     */

    protected final Executor executor;

//    protected ClientUser user;

    /**
     * Help with server incoming messages
     * Will be changed each time when you connect to a server
     * Will be 2 factory methods plain and ciphered version
     */

    protected ClientNetworkHelper networkHelper;

    /**
     * Indicates connection type
     * plain or cipher
     */

    protected boolean isSecureConnection = false;


    public AbstractClient(ChangeableModel model) {
        this.model = model;
        observerList = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();
//        networkHelper = createNetworkHelper();

    }

    /**
     * Authenticate procedure {@link com.Abstraction.Networking.Servers.AbstractServer} has similar
     * In future there will be flag indicating is connection ciphered or not
     * Default implementation
     *
     * @param reader to read data
     * @param writer to send to the server
     * @param myName to send for authenticate
     * @return my id or {@link WHO#NO_NAME}
     */

    public BaseUser authenticate(BaseReader reader, ClientWriter writer, String myName) throws IOException {
        writer.writeName(myName);

        AbstractDataPackage read = reader.read();
        String formatAndCaptureSizeAsString = read.getDataAsString();
        DataPackagePool.returnPackage(read);

        //sets audio format and tell the server can speaker play format or not
        if (!AudioSupplier.getInstance().isFormatSupported(formatAndCaptureSizeAsString)) {
            writer.writeDeny(WHO.SERVER.getCode());
            stringNotify(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED, formatAndCaptureSizeAsString);
            throw new IOException("Audio format not accepted");
        }
        writer.writeApproveAudioFormat(WHO.SERVER.getCode());
        stringNotify(ACTIONS.AUDIO_FORMAT_ACCEPTED, formatAndCaptureSizeAsString);

        read = reader.read();
        int myID = read.getHeader().getTo();
        AbstractDataPackagePool.returnPackage(read);

        read = reader.read();
        if (read.getHeader().getCode().equals(CODE.SEND_SERVER_PLAIN_MODE)) {
            isSecureConnection = false;
            return new BaseUser(myName, myID);
        } else if (read.getHeader().getCode().equals(CODE.SEND_SERVER_CIPHER_MODE)) {
            if (!Crypto.isCipherAcceptable(Crypto.STANDARD_CIPHER_FORMAT)){
                stringNotify(ACTIONS.CIPHER_FORMAT_IS_NOT_ACCEPTED, "Can't handle given format - " + Crypto.STANDARD_CIPHER_FORMAT);
                throw new IOException("Your system can't handle given cipher algorithm - " + Crypto.STANDARD_CIPHER_FORMAT);
            }
            isSecureConnection = true;


            return null;
        }else {
            //error
            return null;
        }

    }

    @Override
    public void notifyObservers(ACTIONS action, Object[] data) {
        observerList.forEach(logicObserver -> logicObserver.observe(action, data));
    }

    @Override
    public void attach(LogicObserver listener) {
        if (!observerList.contains(listener)) {
            observerList.add(listener);
        }
    }

    @Override
    public void detach(LogicObserver listener) {
        observerList.remove(listener);
    }

    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        executor.execute(() -> {
            switch (button) {
                case CONNECT:
                    onConnect(data);
                    return;
                case DISCONNECT:
                    onDisconnect();
                    return;
                case SEND_MESSAGE:
                    onMessageSend(data);
                    return;
                case CALL:
                    onCall(data);
                    return;
                case EXIT_CONFERENCE:
                    onExitConference();
                    return;
                case ASC_FOR_USERS:
                    onUserRequest();
                    return;
                case CALL_ACCEPTED:
                    onCallAccepted(data);
                    return;
                case CALL_DENIED:
                    onCallDenied(data);
                    return;
                case CALL_CANCELLED:
                    onCallCanceled(data);
                    return;
                case SEND_SOUND:
                    onSendSound(data);
                    return;
            }
            additionalCases(button, data);
        });
    }

    public ChangeableModel getModel() {
        return model;
    }

    /**
     * Override to put more BUTTON cases to networkHelper
     *
     * @param buttons to handle
     * @param data    to use
     */

    protected abstract void additionalCases(BUTTONS buttons, Object[] data);

    protected abstract String createDefaultName();

    protected final void plainNotify(ACTIONS actions) {
        notifyObservers(actions, null);
    }

    protected final void stringNotify(ACTIONS actions, String data) {
        notifyObservers(actions, new Object[]{data});
    }

    protected void onConnect(Object[] data) {
        if (networkHelper != null && networkHelper.isWorking()) {
            stringNotify(ACTIONS.ALREADY_CONNECTED_TO_SERVER, model.getMyself().toString());
            return;
        }

        String[] strings = validateConnectData(data);
        if (strings == null) {
            return;
        }

        Socket socket = new Socket();
        String myName = strings[0];

        try {
            socket.connect(new InetSocketAddress(strings[1], Integer.parseInt(strings[2])), Resources.getInstance().getTimeOut() * 1000);
            BaseReader reader = new BaseReader(socket.getInputStream(), Resources.getInstance().getBufferSize());
            OutputStream outputStream = socket.getOutputStream();

            BaseUser authenticate = authenticate(
                    reader,
                    new ClientWriter(new PlainWriter(outputStream, Resources.getInstance().getBufferSize())),
                    myName
            );

            ClientUser me = new ClientUser(authenticate, new ClientWriter(createWriterForClient(outputStream), authenticate.getId()), reader);
            model.setMyself(me);
            networkHelper = createNetworkHelper(socket);
            networkHelper.start("Client network helper / reader");
            stringNotify(ACTIONS.CONNECT_SUCCEEDED, me.toString());
        } catch (IOException e) {
            plainNotify(ACTIONS.CONNECT_FAILED);
            try {
                socket.close();
            } catch (IOException ignored) {
                //already closed
            }
            return;
        }

        /*
        Check cipher flag
        Create ClientUser and set it to model
        Create ClientNetworkHandler which in future can be 2 types cipher and plain, so need some creation patterns
        */


//        try {
//            if (networkHelper.start(strings[0], socket)) {
//                stringNotify(ACTIONS.CONNECT_SUCCEEDED, user.toString());
//            }else {
//                stringNotify(ACTIONS.ALREADY_CONNECTED_TO_SERVER, user.toString());
//            }
//        } catch (IOException e) {
//
//            networkHelper.close();
//        }
    }

    protected void onDisconnect() {
        try {
            getWriter().writeDisconnect();
        } catch (IOException ignored) {
        }
        networkHelper.close();
    }

    protected void onMessageSend(Object[] data) {
        String message = (String) data[0];
        int to = (int) data[1];
        try {
            getWriter().writeMessage(to, message);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onCall(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        ClientUser myself = model.getMyself();
        myself.lock();
        if (myself.isCalling() != ClientUser.NO_ONE) {
            plainNotify(ACTIONS.ALREADY_CALLING_SOMEONE);
            return;
        }
        if (model.inConversationWith(dude))
            return;
        myself.call(dude.getId());
        myself.unlock();

        try {
            myself.getWriter().writeCall(dude.getId());
            notifyObservers(ACTIONS.OUT_CALL, new Object[]{dude});
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onExitConference() {
        try {
            getWriter().writeDisconnectFromConv();
            model.clearConversation();
            plainNotify(ACTIONS.EXITED_CONVERSATION);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
        AbstractDataPackagePool.clearStorage();
    }

    protected void onUserRequest() {
        try {
            getWriter().writeUsersRequest();
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onCallAccepted(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        String others = (String) data[1];

        ClientUser myself = model.getMyself();
        myself.drop();
        try {
            myself.getWriter().writeAccept(dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
            return;
        }
        callAcceptRoutine(dude, others, this, model);
    }

    protected void onCallDenied(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        ClientUser myself = model.getMyself();

        myself.drop();
        try {
            myself.getWriter().writeDeny(dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onCallCanceled(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        ClientUser myself = model.getMyself();

        myself.drop();
        try {
            myself.getWriter().writeCancel(dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onSendSound(Object[] data) {
        try {
            getWriter().writeSound((byte[]) data[0]);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected ClientNetworkHelper createNetworkHelper(Socket socket) {
        if (isSecureConnection) {
            return null;
        } else {
            return new ClientNetworkHelper(this, socket);
        }
    }

    protected PlainWriter createWriterForClient(OutputStream outputStream) {
        if (isSecureConnection) {
            return null;
        } else {
            return new PlainWriter(outputStream, Resources.getInstance().getBufferSize());
        }
    }


    /**
     * Check argument and modify them if needed
     *
     * @param data contain 3 string name, hostName, port
     * @return null if data is invalid or 3 Strings name, hostName, port
     */

    protected final String[] validateConnectData(Object[] data) {
        String name = (String) data[0];
        if (FormatWorker.checkZeroLength(name))
            name = createDefaultName(); // or get from property map

        String hostName = (String) data[1];
        if (FormatWorker.checkZeroLength(hostName))
            hostName = "127.0.0.1"; // or get from property
        if (!FormatWorker.isHostNameCorrect(hostName)) {
            stringNotify(ACTIONS.WRONG_HOST_NAME_FORMAT, hostName);
            return null;
        }

        String port = (String) data[2];
        if (FormatWorker.checkZeroLength(port))
            port = "8188"; // or get from property
        if (!checkPort(port))
            return null;
        return new String[]{name, hostName, port};
    }

    protected final boolean checkPort(String port) {
        if (!FormatWorker.verifyOnlyDigits(port)) {
            stringNotify(ACTIONS.WRONG_PORT_FORMAT, port);
            return false;
        }
        int portAsInt = Integer.parseInt(port);
        if (!FormatWorker.portInRange(portAsInt)) {
            notifyObservers(ACTIONS.PORT_OUT_OF_RANGE, new Object[]{
                            "0 < port < " + 0xFFFF,
                            portAsInt
                    }
            );
            return false;
        }
        return true;
    }

    /**
     * Short cut for gaining writer from user
     *
     * @return my writer
     */

    protected final ClientWriter getWriter() {
        return model.getMyself().getWriter();
    }


    /**
     * For client side
     *
     * @param dude   who to add in a conversation
     * @param others who may present in the conversation
     * @param logic  what to notifyObservers about progress
     * @param model  to modelObservation about progress
     */

    public static void callAcceptRoutine(BaseUser dude, String others, Logic logic, ChangeableModel model) {
        logic.notifyObservers(ACTIONS.CALL_ACCEPTED, null);
        model.addToConversation(dude);
        for (BaseUser baseUser : BaseUser.parseUsers(others)) {
            model.addToConversation(baseUser);
        }
    }
}
