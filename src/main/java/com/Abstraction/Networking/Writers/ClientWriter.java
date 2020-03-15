package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Contain all possible write actions for a client
 * Each method basically ask pool for carcase and init it
 * <p>
 * Part of Bridge pattern it's abstraction
 */

public class ClientWriter {

    private final BaseLogger clientLogger = LogManagerHelper.getInstance().getClientLogger();

    private final Writer bridgeImplementor;
    private final int myID;

    /**
     * If null mean full TCP connection
     * Needed for UDP connection
     */

    private final InetSocketAddress address;

    /**
     * Base client writer with {@link #myID} = {@link WHO#NO_NAME}
     *
     * @param bridgeImplementor contain vital methods for network writing
     */

    public ClientWriter(Writer bridgeImplementor) {
        this.bridgeImplementor = bridgeImplementor;
        myID = WHO.NO_NAME.getCode();
        address = null;
    }

    /**
     * Base client writer with server id
     *
     * @param bridgeImplementor contain vital methods for network writing
     * @param myID              received from server
     * @param address           for UDP session
     */

    public ClientWriter(Writer bridgeImplementor, int myID, InetSocketAddress address) {
        this.bridgeImplementor = bridgeImplementor;
        this.myID = myID;
        this.address = address;
    }


    protected void write(AbstractDataPackage dataPackage) throws IOException {
        bridgeImplementor.write(dataPackage);
    }

    protected void writeUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        bridgeImplementor.writeUDP(dataPackage, address, port);
    }


    public void writeName(String name) throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeName",
                "Writing my name - " + name);
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_NAME, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), name));
    }

    public void writeUsersRequest() throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeUserRequest",
                "Writing user request");
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_USERS, myID, WHO.SERVER.getCode()));
    }

    /**
     * Might accept to as WHO.CONFERENCE.getId()
     *
     * @param to      who gonna receive it
     * @param message what you typed
     * @throws IOException if network fails
     */

    public void writeMessage(int to, String message) throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeMessage",
                "Writing message to - " + to);
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_MESSAGE, myID, to, message));
    }

    public void writeCall(int to) throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeCall",
                "Writing call to - " + to);
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CALL, myID, to));
    }

    public void writeApproveAudioFormat() throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeApproveAudioFormat",
                "Writing approve audio format");
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_AUDIO_FORMAT_ACCEPT, myID, WHO.SERVER.getCode()));
    }

    public void writeDeclineAudioFormat() throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeDeclineAudioFormat",
                "Writing decline audio format");
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_AUDIO_FORMAT_DENY, myID, WHO.SERVER.getCode()));
    }

    public void writeAccept(int to) throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeAcceptCall",
                "Writing accept call to - " + to);
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ACCEPT_CALL, myID, to));
    }

    public void writeDeny(int to) throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeDenyCall",
                "Writing deny call - " + to);
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DENY_CALL, myID, to));
    }

    public void writeCancel(int to) throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeCancelCall",
                "Writing cancel call - " + to);
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CANCEL_CALL, myID, to));
    }

    public void writeSound(byte[] data) throws IOException {
        if (address == null) {
            write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data));
        } else {
            writeUDP(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data), address.getAddress(), address.getPort());
        }
    }

    public void writeDisconnect() throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeDisconnect",
                "Writing disconnect call");
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT, myID, WHO.SERVER.getCode()));
    }

    public void writeDisconnectFromConv() throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeDisconnectFromConversation",
                "Writing disconnect from conversation");
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT_FROM_CONVERSATION, myID, WHO.CONFERENCE.getCode()));
    }

    public void writePublicKeyEncoded(byte[] encodedPubKey) throws IOException {
        write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_PUBLIC_ENCODED_KEY, myID, WHO.SERVER.getCode(), encodedPubKey));
    }

    public void writeCipherModeAccepted() throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeCipherModeAccepted",
                "Writing approve cipher mode");
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CIPHER_MODE_ACCEPTED, myID, WHO.SERVER.getCode()));
    }

    public void writeCipherModeDenied() throws IOException {
        clientLogger.logp(this.getClass().getName(), "writeCipherModeDenied",
                "Writing deny cipher mode");
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CIPHER_MODE_DENIED, myID, WHO.SERVER.getCode()));
    }

    public void writeMyPortUDP(int port) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_UDP_PORT, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), String.valueOf(port)));
    }

    public void writePong() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_PONG, myID, WHO.SERVER.getCode()));
    }

}
