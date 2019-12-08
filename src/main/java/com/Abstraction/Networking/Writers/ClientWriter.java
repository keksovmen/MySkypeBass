package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Contain not all possible write actions for a client
 * Each method basically ask pool for carcase and init it
 */

public class ClientWriter extends BaseWriter {

    public ClientWriter(OutputStream outputStream, int bufferSize) {
        super(outputStream, bufferSize);
    }

    public void writeName(String name) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_NAME, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), name));
    }

    public void writeUsersRequest(int from) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_USERS, from, WHO.SERVER.getCode()));
    }

    /**
     * Might accept to as WHO.CONFERENCE.getId()
     *
     * @param from    who writes
     * @param to      who gonna receive it
     * @param message what you typed
     */

    public void writeMessage(int from, int to, String message) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_MESSAGE, from, to, message));
    }

    public void writeCall(int from, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CALL, from, to));
    }

    public void writeApproveAudioFormat(int from, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_APPROVE, from, to));
    }

    public void writeAccept(int from, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ACCEPT_CALL, from, to));
    }

    public void writeDeny(int from, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DENY_CALL, from, to));
    }

    public void writeCancel(int from, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CANCEL_CALL, from, to));
    }

    public void writeSound(int from, byte[] data) throws IOException {
        write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, from, WHO.CONFERENCE.getCode(), data));
    }

    public void writeDisconnect(int from) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT, from, WHO.SERVER.getCode()));
    }

    public void writeDisconnectFromConv(int from) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT_FROM_CONV, from, WHO.CONFERENCE.getCode()));
    }

}
