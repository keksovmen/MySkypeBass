package Bin.Networking.Writers;

import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Protocol.CODE;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Utility.WHO;

import java.io.IOException;
import java.io.OutputStream;

/**
 * Contain not all possible write actions for a client
 * Each method basically ask pool for carcase and init it
 */

public class ClientWriter extends WriterWithHandler {

    public ClientWriter(OutputStream outputStream, ErrorHandler mainErrorHandler) {
        super(outputStream, mainErrorHandler);
    }

    public void writeName(String name) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_NAME, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), name));
    }

    public void writeUsersRequest(int from) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_USERS, from, WHO.SERVER.getCode()));
    }

    /**
     * Might accept to as WHO.CONFERENCE.getId()
     *
     * @param from    who writes
     * @param to      who gonna receive it
     * @param message what you typed
     */

    public void writeMessage(int from, int to, String message) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_MESSAGE, from, to, message));
    }

    public void writeCall(int from, int to) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_CALL, from, to));
    }

    public void writeAccept(int from, int to) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_APPROVE, from, to));
    }

    public void writeDeny(int from, int to) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_DENY, from, to));
    }

    public void writeCancel(int from, int to) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_CANCEL, from, to));
    }

    public void writeSound(int from, byte[] data) throws IOException {
        write(AbstractDataPackagePool.getPackage().init(CODE.SEND_SOUND, from, WHO.CONFERENCE.getCode(), data));
    }

    public void writeDisconnect(int from) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_DISCONNECT, from, WHO.SERVER.getCode()));
    }

    public void writeDisconnectFromConv(int from) {
        writeWithHandler(AbstractDataPackagePool.getPackage().init(CODE.SEND_DISCONNECT_FROM_CONV, from, WHO.CONFERENCE.getCode()));
    }

}
