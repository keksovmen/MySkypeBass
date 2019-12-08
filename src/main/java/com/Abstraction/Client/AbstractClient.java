package com.Abstraction.Client;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Model.ChangeableModel;
import com.Abstraction.Networking.Handlers.ClientHandler;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Networking.Writers.ClientWriter;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Pipeline.BUTTONS;
import com.Abstraction.Util.FormatWorker;
import com.Abstraction.Util.Resources;

import java.io.IOException;
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

    protected ClientUser user;
    protected ClientHandler handler;

    public AbstractClient(ChangeableModel model) {
        this.model = model;
        observerList = new ArrayList<>();
        executor = Executors.newSingleThreadExecutor();

    }

    /**
     * Authenticate procedure Server has similar
     * Default implementation
     *
     * @param reader to read data
     * @param writer to send to the server
     * @param myName to send for authenticate
     * @return me or null if failed
     */

    public ClientUser authenticate(BaseReader reader, ClientWriter writer, String myName) {
        try {
            writer.writeName(myName);

            AbstractDataPackage read = reader.read();
            String formatAndCaptureSizeAsString = read.getDataAsString();
            DataPackagePool.returnPackage(read);

            //sets audio format and tell the server can speaker play format or not
            if (!AudioSupplier.getInstance().isFormatSupported(formatAndCaptureSizeAsString)) {
                writer.writeDeny(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
                stringNotify(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED, formatAndCaptureSizeAsString);
                return null;
            }
            writer.writeApproveAudioFormat(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
            stringNotify(ACTIONS.AUDIO_FORMAT_ACCEPTED, formatAndCaptureSizeAsString);

            read = reader.read();
            user = new ClientUser(myName, read.getHeader().getTo(), writer);
            AbstractDataPackagePool.returnPackage(read);
        } catch (IOException e) {
            return null;
        }
        return user;
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
     * Override to put more BUTTON cases to handler
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
        String[] strings = validateConnectData(data);
        if (strings == null) {
            return;
        }
        Socket socket = new Socket();

        try {
            socket.connect(new InetSocketAddress(strings[1], Integer.parseInt(strings[2])), Resources.getTimeOut() * 1000);
        } catch (IOException e) {
            plainNotify(ACTIONS.CONNECT_FAILED);
            try {
                socket.close();
            } catch (IOException ignored) {
            }
            return;
        }
        handler = createHandler(socket);
        if (handler.start(strings[0])) {
            stringNotify(ACTIONS.CONNECT_SUCCEEDED, user.toString());
        } else {
            handler.close();
        }
    }

    protected void onDisconnect() {
        try {
            user.getWriter().writeDisconnect(user.getId());
        } catch (IOException ignored) {
        }
        handler.close();
    }

    protected void onMessageSend(Object[] data) {
        String message = (String) data[0];
        int to = (int) data[1];
        try {
            user.getWriter().writeMessage(user.getId(), to, message);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onCall(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        user.lock();
        if (user.isCalling() != ClientUser.NO_ONE) {
            plainNotify(ACTIONS.ALREADY_CALLING_SOMEONE);
            return;
        }
        if (model.inConversationWith(dude))
            return;
        user.call(dude.getId());
        user.unlock();

        try {
            user.getWriter().writeCall(user.getId(), dude.getId());
            notifyObservers(ACTIONS.OUT_CALL, new Object[]{dude});
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onExitConference() {
        try {
            user.getWriter().writeDisconnectFromConv(user.getId());
            model.clearConversation();
            plainNotify(ACTIONS.EXITED_CONVERSATION);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
        AbstractDataPackagePool.clearStorage();
    }

    protected void onUserRequest() {
        try {
            user.getWriter().writeUsersRequest(user.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onCallAccepted(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        String others = (String) data[1];

        user.drop();
        try {
            user.getWriter().writeAccept(user.getId(), dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
            return;
        }
        callAcceptRoutine(dude, others, this, model);
    }

    protected void onCallDenied(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        user.drop();
        try {
            user.getWriter().writeAccept(user.getId(), dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onCallCanceled(Object[] data) {
        BaseUser dude = (BaseUser) data[0];
        user.drop();
        try {
            user.getWriter().writeAccept(user.getId(), dude.getId());
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected void onSendSound(Object[] data) {
        try {
            user.getWriter().writeSound(user.getId(), (byte[]) data[0]);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    protected ClientHandler createHandler(Socket socket) {
        return new ClientHandler(this, socket);
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
     * For client side
     *
     * @param dude   who to add in a conversation
     * @param others who may present in the conversation
     * @param logic  what to notifyObservers about progress
     * @param model  to update about progress
     */

    public static void callAcceptRoutine(BaseUser dude, String others, Logic logic, ChangeableModel model) {
        logic.notifyObservers(ACTIONS.CALL_ACCEPTED, null);
        model.addToConversation(dude);
        for (BaseUser baseUser : BaseUser.parseUsers(others)) {
            model.addToConversation(baseUser);
        }
    }
}
