package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;

import java.io.IOException;

/**
 * Contain all possible write actions for a client
 * Each method basically ask pool for carcase and init it
 * <p>
 * Part of Bridge pattern it's abstraction
 */

public class ClientWriter implements Writer {

    private final Writer bridgeImplementor;
    private final int myID;


    /**
     * Base client writer with {@link #myID} = {@link WHO#NO_NAME}
     *
     * @param bridgeImplementor contain vital methods for network writing
     */

    public ClientWriter(Writer bridgeImplementor) {
        this.bridgeImplementor = bridgeImplementor;
        myID = WHO.NO_NAME.getCode();
    }

    /**
     * Base client writer with server id
     *
     * @param bridgeImplementor contain vital methods for network writing
     * @param myID              received from server
     */

    public ClientWriter(Writer bridgeImplementor, int myID) {
        this.bridgeImplementor = bridgeImplementor;
        this.myID = myID;
    }


    @Override
    public void write(AbstractDataPackage dataPackage) throws IOException {
        bridgeImplementor.write(dataPackage);
    }

    @Override
    public void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        bridgeImplementor.writeWithoutReturnToPool(dataPackage);
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

    public void writeApproveAudioFormat(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_APPROVE, myID, to));
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
        write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data));
    }

    public void writeDisconnect() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT, myID, WHO.SERVER.getCode()));
    }

    public void writeDisconnectFromConv() throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT_FROM_CONV, myID, WHO.CONFERENCE.getCode()));
    }

}
