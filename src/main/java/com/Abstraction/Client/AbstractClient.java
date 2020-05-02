package com.Abstraction.Client;

import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Handlers.ClientCipherNetworkHelper;
import com.Abstraction.Networking.Handlers.ClientNetworkHelper;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Readers.UDPReader;
import com.Abstraction.Networking.Utility.Authenticator;
import com.Abstraction.Networking.Utility.Users.*;
import com.Abstraction.Networking.Writers.CipherWriter;
import com.Abstraction.Networking.Writers.ClientWriter;
import com.Abstraction.Networking.Writers.PlainWriter;
import com.Abstraction.Networking.Writers.Writer;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.Algorithms;
import com.Abstraction.Util.Cryptographics.Crypto;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Monitors.SpeedMonitor;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.net.SocketException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Represent client logic part
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

    /**
     * For back end threads
     */

    protected final Executor asyncTasksExecutor;

    /**
     * For handling connection on client and server sides
     */

    protected final Authenticator authenticator;

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
        executor = Executors.newSingleThreadExecutor(r -> new Thread(r, "Buttons handler"));
        asyncTasksExecutor = Executors.newFixedThreadPool(3, r -> new Thread(r, "Client Async Helper"));
//        executor = new ThreadPoolExecutor(1, 1, 30, TimeUnit.SECONDS, new ArrayBlockingQueueWithWait<>(12), r -> new Thread(r, "Buttons handler"));
        authenticator = createAuthenticator();

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

    public void asyncTask(Runnable runnable){
        asyncTasksExecutor.execute(runnable);
    }

    /**
     * Override to put more BUTTON cases in to {@link #handleRequest(BUTTONS, Object[])}
     *
     * @param buttons to handle
     * @param data    to use
     */

    protected abstract void additionalCases(BUTTONS buttons, Object[] data);

    protected abstract String createDefaultName();

    protected Authenticator createAuthenticator() {
        return new Authenticator();
    }

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
        if (strings == null) return;

        Socket socket = new Socket();
        DatagramSocket datagramSocket;
        InputStream inputStream;
        OutputStream outputStream;
        InetSocketAddress socketAddress = new InetSocketAddress(strings[1], Integer.parseInt(strings[2]));
        try {
            socket.connect(socketAddress, Resources.getInstance().getTimeOut() * 1000);
            inputStream = socket.getInputStream();
            outputStream = socket.getOutputStream();
        } catch (IOException e) {
            plainNotify(ACTIONS.CONNECT_FAILED);
            Algorithms.closeSocketThatCouldBeClosed(socket);
            return;
        }

        try {
            datagramSocket = new DatagramSocket();
        } catch (SocketException e) {
            plainNotify(ACTIONS.CONNECT_FAILED);
            stringNotify(ACTIONS.UDP_SOCKET_NOT_BINDED, "Your UDP socket is already in use by other software!");
            Algorithms.closeSocketThatCouldBeClosed(socket);
            return;
        }

        Authenticator.ClientStorage storage = authenticator.clientAuthentication(inputStream, outputStream, strings[0], datagramSocket.getLocalPort());
        if (!handleAuthenticationResults(storage)) {
            Algorithms.closeSocketThatCouldBeClosed(socket);
            Algorithms.closeSocketThatCouldBeClosed(datagramSocket);
            return;
        }

        if (storage.isFullTCP) {
            Algorithms.closeSocketThatCouldBeClosed(datagramSocket);
            datagramSocket = null;
        }

        finishSucceededConnection(createClientUser(storage,
                outputStream, inputStream, datagramSocket,
                storage.isFullTCP ? null : new InetSocketAddress(socket.getInetAddress(),
                        socket.getPort())),
                createNetworkHelper(socket, datagramSocket)
        );
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
        User dude = (User) data[0];
        ClientUser myself = model.getMyself();
        myself.lock();
        if (myself.isCalling() != ClientUser.NO_ONE) {
            plainNotify(ACTIONS.ALREADY_CALLING_SOMEONE);
            myself.unlock();
            return;
        }
        if (model.inConversationWith(dude)) {
            myself.unlock();
            return;
        }
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
        User dude = (User) data[0];

        ClientUser myself = model.getMyself();
        myself.drop();
        try {
            myself.getWriter().writeAccept(dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
            return;
        }
        callAcceptRoutine(this, model, dude);
    }

    protected void onCallDenied(Object[] data) {
        User dude = (User) data[0];
        ClientUser myself = model.getMyself();

        myself.drop();
        try {
            myself.getWriter().writeDeny(dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onCallCanceled(Object[] data) {
        User dude = (User) data[0];
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
//            long beforeNano = System.nanoTime();
            getWriter().writeSound((byte[]) data[0]);
//            long timeMicro = (System.nanoTime() - beforeNano) / 1000;
//            LogManagerHelper.getInstance().getClientLogger().logp(this.getClass().getName(), "onSendSound",
//                    "Time - " + timeMicro);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected ClientNetworkHelper createNetworkHelper(Socket socket, DatagramSocket datagramSocket) {
        if (isSecureConnection) {
            return new ClientCipherNetworkHelper(this, socket, datagramSocket);
        } else {
            return new ClientNetworkHelper(this, socket, datagramSocket);
        }
    }

    protected Writer createWriterForClient(OutputStream outputStream, Authenticator.ClientStorage storage, DatagramSocket datagramSocket) {
        Writer writer = new PlainWriter(outputStream, Resources.getInstance().getBufferSize(), datagramSocket);
        if (storage.isSecureConnection) {
            return new CipherWriter(writer, storage.cryptoHelper.getKey(), storage.cryptoHelper.getParameters());
        } else {
            return writer;
        }
    }

    /**
     * @param storage        meta info
     * @param outputStream   opened
     * @param inputStream    opened
     * @param datagramSocket could be null if so than full TCP connection
     * @param address        where to send UDP, also could be null
     * @return appropriate client user for given situation
     */

    protected ClientUser createClientUser(Authenticator.ClientStorage storage, OutputStream outputStream, InputStream inputStream, DatagramSocket datagramSocket, InetSocketAddress address) {
        final ClientWriter writer = new ClientWriter(createWriterForClient(outputStream, storage, datagramSocket), storage.myID, address);
        writer.setSpeedMonitor(new SpeedMonitor(Algorithms.calculatePartOfAudioUnitDuration(), this::asyncTask));
        final BaseReader readerTCP = new BaseReader(inputStream, Resources.getInstance().getBufferSize());
        final UDPReader readerUDP;
        if (datagramSocket == null)
            readerUDP = null;
        else
            readerUDP = new UDPReader(datagramSocket, storage.sizeUDP);
        final User user;
        if (storage.isSecureConnection) {
            user = new CipherUser(
                    storage.name,
                    storage.myID,
                    storage.cryptoHelper.getKey(),
                    storage.cryptoHelper.getParameters()
            );
        } else {
            user = new PlainUser(storage.name, storage.myID);
        }
        return new ClientUser(new BaseUserWithLock(user), writer, readerTCP, readerUDP);
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
     * Sends notifies about particular states of authentication
     *
     * @param storage contain flags
     * @return false if you can't connect to server for some reason
     */

    protected boolean handleAuthenticationResults(Authenticator.ClientStorage storage) {
        if (storage.isNetworkFailure) {
            plainNotify(ACTIONS.CONNECT_FAILED);
            return false;
        }
        if (!storage.isAudioFormatAccepted) {
            stringNotify(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED, storage.audioFormat.toString());
            return false;
        }
        stringNotify(ACTIONS.AUDIO_FORMAT_ACCEPTED, storage.audioFormat.toString());

        isSecureConnection = storage.isSecureConnection;
        if (storage.isSecureConnection) {
            if (!storage.isSecureConnectionAccepted) {
                stringNotify(ACTIONS.CIPHER_FORMAT_IS_NOT_ACCEPTED, "Can't handle given format - " + Crypto.STANDARD_CIPHER_FORMAT);
                return false;
            }
        }
        return true;
    }

    /**
     * Handle succeeded connection, send some notifies and changes model state
     * @param user me
     * @param networkHelper to start
     */

    protected void finishSucceededConnection(ClientUser user, ClientNetworkHelper networkHelper) {
        model.setMyself(user);
        this.networkHelper = networkHelper;
        networkHelper.start("Client network helper / reader");
        stringNotify(ACTIONS.CONNECT_SUCCEEDED, user.toString());
        try {
            user.getWriter().writeUsersRequest();
        } catch (IOException ignored) {
            //networkHelper exception handler will handle it
        }
    }


    /**
     * Short cut for gaining writer from user
     *
     * @return my writer
     */

    protected final ClientWriter getWriter() {
        return model.getMyself().getWriter();
    }

    public static void callAcceptRoutine(Logic logic, ChangeableModel model, User user) {
        logic.notifyObservers(ACTIONS.CALL_ACCEPTED, null);
        model.addToConversation(user);
    }
}
