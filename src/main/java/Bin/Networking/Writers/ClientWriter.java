package Bin.Networking.Writers;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Utility.ErrorHandler;

import java.io.IOException;
import java.io.OutputStream;

public class ClientWriter extends BaseWriter {

    public ClientWriter(OutputStream outputStream, ErrorHandler mainErrorHandler) {
        super(outputStream, mainErrorHandler);
    }

    public void writeName(String name) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_NAME, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), name));
    }

    public void writeUsersRequest(int from) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_USERS, from, WHO.SERVER.getCode()));
    }

    public void writeMessage(int from, int to, String message) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_MESSAGE, from, to, message));
    }

    public void writeCall(int from, int to) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_CALL, from, to));
    }

    public void writeAccept(int from, int to) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_APPROVE, from, to));
    }

    public void writeDeny(int from, int to) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_DENY, from, to));
    }

    public void writeCancel(int from, int to) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_CANCEL, from, to));
    }

    public void writeSound(int from, byte[] data) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_SOUND, from, WHO.CONFERENCE.getCode(), data));
    }

    public void writeDisconnect(int from) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_DISCONNECT, from, WHO.SERVER.getCode()));
    }

    public void writeDisconnectFromConv(int from) {
        writeA(DataPackagePool.getPackage().init(CODE.SEND_DISCONNECT_FROM_CONV, from, WHO.CONFERENCE.getCode()));
    }

}
