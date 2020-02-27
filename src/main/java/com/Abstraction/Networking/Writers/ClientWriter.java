package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;

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

    private final Writer bridgeImplementor;
    private final int myID;

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
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_NAME, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), name));
    }

    public void writeUsersRequest() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_USERS, myID, WHO.SERVER.getCode()));
    }

    /**
     * Might accept to as WHO.CONFERENCE.getId()
     *
     * @param to      who gonna receive it
     * @param message what you typed
     */

    public void writeMessage(int to, String message) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_MESSAGE, myID, to, message));
    }

    public void writeCall(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CALL, myID, to));
    }

    public void writeApproveAudioFormat() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_AUDIO_FORMAT_ACCEPT, myID, WHO.SERVER.getCode()));
    }

    public void writeDeclineAudioFormat() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_AUDIO_FORMAT_DENY, myID, WHO.SERVER.getCode()));
    }

    public void writeAccept(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ACCEPT_CALL, myID, to));
    }

    public void writeDeny(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DENY_CALL, myID, to));
    }

    public void writeCancel(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CANCEL_CALL, myID, to));
    }

    public void writeSound(byte[] data) throws IOException {
        writeUDP(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data), address.getAddress(), address.getPort());
//        write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data));
    }

    public void writeDisconnect() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT, myID, WHO.SERVER.getCode()));
    }

    public void writeDisconnectFromConv() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT_FROM_CONVERSATION, myID, WHO.CONFERENCE.getCode()));
    }

    public void writePublicKeyEncoded(byte[] encodedPubKey) throws IOException {
        write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_PUBLIC_ENCODED_KEY, myID, WHO.SERVER.getCode(),encodedPubKey));
    }

    public void writeCipherModeAccepted() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CIPHER_MODE_ACCEPTED, myID, WHO.SERVER.getCode()));
    }

    public void writeCipherModeDenied() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CIPHER_MODE_DENIED, myID, WHO.SERVER.getCode()));
    }

    public void writeMyPortUDP(int port) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_UDP_PORT, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), String.valueOf(port)));
    }

}
