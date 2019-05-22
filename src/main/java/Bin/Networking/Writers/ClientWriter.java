package Bin.Networking.Writers;

import Bin.Networking.DataParser.DataPackagePool;

import java.io.IOException;
import java.io.OutputStream;

public class ClientWriter extends BaseWriter {

    public ClientWriter(OutputStream outputStream) {
        super(outputStream);
    }

    public void writeName(String name) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_NAME, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), name));
    }

    public void writeUsersRequest(int from) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_USERS, from, WHO.SERVER.getCode()));
    }

    public void writeMessage(int from, int to, String message) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_MESSAGE, from, to, message));
    }

    public void writeCall(int from, int to) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_CALL, from, to));
    }

    public void writeAccept(int from, int to) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_APPROVE, from, to));
    }

    public void writeDeny(int from, int to) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_DENY, from, to));
    }

    public void writeCancel(int from, int to) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_CANCEL, from, to));
    }

    public void writeSound(int from, byte[] data) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_SOUND, from, WHO.CONFERENCE.getCode(), data));
    }

    public void writeDisconnect(int from) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_DISCONNECT, from, WHO.SERVER.getCode()));
    }

//    public void writeAdd(int from) throws IOException {
//        write(DataPackagePool.getPackage().init(CODE.SEND_ADD, from, to));
//    }

//    public void writeRemove(int from) throws IOException {
//        write(BaseDataPackage.getObject().init(from, CONFERENCE, SEND_REMOVE, null));
//    }

}
