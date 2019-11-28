package com.Client;

import com.Audio.AudioSupplier;
import com.Model.ChangeableModel;
import com.Networking.Handlers.ClientHandler;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Readers.BaseReader;
import com.Networking.Servers.AbstractServer;
import com.Networking.Servers.SimpleServer;
import com.Networking.Utility.ProtocolValueException;
import com.Networking.Utility.Users.BaseUser;
import com.Networking.Utility.Users.ClientUser;
import com.Networking.Utility.WHO;
import com.Networking.Writers.ClientWriter;
import com.Pipeline.ACTIONS;
import com.Pipeline.BUTTONS;
import com.Util.Algorithms;
import com.Util.FormatWorker;
import com.Util.Resources;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

public class Client extends AbstractClient {


    private ClientUser user;
    private ClientHandler handler;

    private AbstractServer server;

    public Client(ChangeableModel model) {
        super(model);
    }


    @Override
    public void handleRequest(BUTTONS button, Object[] data) {
        //remember which thread will call it through chain of responsibility
        switch (button) {
            case CONNECT:
                onConnect(data);
                break;
            case DISCONNECT:
                onDisconnect();
                break;
            case CREATE_SERVER:
                onServerCreate(data);
                break;
            case SEND_MESSAGE:
                onMessageSend(data);
                break;
            case CALL:
                onCall(data);
                break;
            case EXIT_CONFERENCE:
                onExitConference();
                break;
            case ASC_FOR_USERS:
                onUserRequest();
                break;
            case CALL_ACCEPTED:
                onCallAccepted(data);
                break;
            case CALL_DENIED:
                onCallDenied(data);
                break;
            case CALL_CANCELLED:
                onCallCanceled(data);
                break;
            case SEND_SOUND:
                onSendSound(data);
                break;
        }
    }


    @Override
    public ClientUser authenticate(BaseReader reader, ClientWriter writer, String myName) {
        try {
            writer.writeName(myName);

            AbstractDataPackage read = reader.read();
            AudioFormat audioFormat = FormatWorker.parseAudioFormat(read.getDataAsString());
            int micCaptureSize = FormatWorker.parseMicCaptureSize(read.getDataAsString());
            DataPackagePool.returnPackage(read);

            //sets audio format and tell the server can speaker play format or not
            if (!AudioSupplier.setAudioFormat(audioFormat, micCaptureSize)) {
                writer.writeDeny(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
                stringNotify(ACTIONS.AUDIO_FORMAT_NOT_ACCEPTED, audioFormat.toString());
                return null;
            }
            writer.writeApproveAudioFormat(WHO.NO_NAME.getCode(), WHO.SERVER.getCode());
            stringNotify(ACTIONS.AUDIO_FORMAT_ACCEPTED, audioFormat.toString());

            read = reader.read();
            user = new ClientUser(myName, read.getHeader().getTo(), writer);
            AbstractDataPackagePool.returnPackage(read);
        } catch (IOException e) {
            return null;
        }
        return user;
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
        handler = new ClientHandler(this, socket);
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
        plainNotify(ACTIONS.DISCONNECTED);
    }

    protected void onServerCreate(Object[] data) {
        String[] strings = validateServerCreateData(data);
        if (strings == null) {
            return;
        }
        try {
            server = SimpleServer.getFromStrings(strings[0], strings[1], strings[2]);
        } catch (IOException e) {
            stringNotify(ACTIONS.PORT_ALREADY_BUSY, strings[0]);
            return;
        } catch (ProtocolValueException e) {
            stringNotify(ACTIONS.INVALID_AUDIO_FORMAT, e.getMessage());
            return;
        }
        if (server.start("Simple Server")) {
            plainNotify(ACTIONS.SERVER_CREATED);
        } else {
            plainNotify(ACTIONS.SERVER_CREATED_ALREADY);
        }

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
            notify(ACTIONS.OUT_CALL, new Object[]{dude});
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
        Algorithms.callAcceptRoutine(dude, others, this, model);
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

    protected void onSendSound(Object[] data){
        try {
            user.getWriter().writeSound(user.getId(), (byte[]) data[0]);
        } catch (IOException ignored) {
            //Handler and its reader thread will close connection on failure
        }
    }

    /**
     * Check argument and modify them if needed
     *
     * @param data contain 3 string name, hostName, port
     * @return null if data is invalid or 3 Strings name, hostName, port
     */

    private String[] validateConnectData(Object[] data) {
        String name = (String) data[0];
        if (FormatWorker.checkZeroLength(name))
            name = System.getProperty("user.name"); // or get from property map

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

    private String[] validateServerCreateData(Object[] data) {
        String port = (String) data[0];
        if (FormatWorker.checkZeroLength(port))
            port = "8188"; // or get from property map
        if (!checkPort(port))
            return null;

        String sampleRate = (String) data[1];
        if (FormatWorker.checkZeroLength(sampleRate)
                || !FormatWorker.verifyOnlyDigits(sampleRate)) {
            stringNotify(ACTIONS.WRONG_SAMPLE_RATE_FORMAT, sampleRate);
            return null;
        }

        String sampleSize = (String) data[2];
        if (FormatWorker.checkZeroLength(sampleSize)
                || !FormatWorker.verifyOnlyDigits(sampleSize)) {
            stringNotify(ACTIONS.WRONG_SAMPLE_SIZE_FORMAT, sampleSize);
            return null;
        }

        return new String[]{port, sampleRate, sampleSize};
    }

    private boolean checkPort(String port) {
        if (!FormatWorker.verifyOnlyDigits(port)) {
            stringNotify(ACTIONS.WRONG_PORT_FORMAT, port);
            return false;
        }
        int portAsInt = Integer.parseInt(port);
        if (!FormatWorker.portInRange(portAsInt)) {
            notify(ACTIONS.PORT_OUT_OF_RANGE, new Object[]{
                            "0 < port < " + 0xFFFF,
                            portAsInt
                    }
            );
            return false;
        }
        return true;
    }

    private void plainNotify(ACTIONS actions) {
        notify(actions, null);
    }

    private void stringNotify(ACTIONS actions, String data) {
        notify(actions, new Object[]{data});
    }

}
