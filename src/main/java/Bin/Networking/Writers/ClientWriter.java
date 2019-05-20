package Bin.Networking.Writers;

import Bin.Networking.DataParser.Package.BaseDataPackage;

import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class ClientWriter extends BaseWriter {

    public ClientWriter(OutputStream outputStream) {
        super(outputStream);
    }

    public void writeName(String name) throws IOException {
        write(BaseDataPackage.getObject().init(name, NO_NAME, SERVER, SEND_NAME));
    }

    public void writeUsersRequest(int from) throws IOException {
        write(BaseDataPackage.getObject().init(from, SERVER, SEND_USERS, null));
    }

    public void writeMessage(int from, int to, String message) throws IOException {
        write(BaseDataPackage.getObject().init(message, from, to, SEND_MESSAGE));
    }

    public void writeCall(int from, int to) throws IOException {
        write(BaseDataPackage.getObject().init(from, to, SEND_CALL, null));
    }

    public void writeAccept(int from, int to) throws IOException {
        write(BaseDataPackage.getObject().init(from, to, SEND_APPROVE, null));
    }

    public void writeDenay(int from, int to) throws IOException {
        write(BaseDataPackage.getObject().init(from, to, SEND_DENY, null));
    }

    public void writeCancel(int from, int to) throws IOException {
        write(BaseDataPackage.getObject().init(from, to, SEND_CANCEL, null));
    }

    public void writeSound(int from, byte[] data) throws IOException {
        write(BaseDataPackage.getObject().init(from, CONFERENCE, SEND_SOUND, data));
    }

    public void writeDisconnect(int from) throws IOException {
        write(BaseDataPackage.getObject().init(from, SERVER, SEND_DISCONNECT, null));
    }

    public void writeAdd(int from) throws IOException {
        write(BaseDataPackage.getObject().init(from, CONFERENCE, SEND_ADD, null));
    }

    public void writeRemove(int from) throws IOException {
        write(BaseDataPackage.getObject().init(from, CONFERENCE, SEND_REMOVE, null));
    }

}
