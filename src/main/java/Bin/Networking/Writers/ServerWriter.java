package Bin.Networking.Writers;

import Bin.Networking.DataParser.Package.BaseDataPackage;
import Bin.Networking.Server;

import java.io.IOException;
import java.io.OutputStream;

public class ServerWriter extends BaseWriter {

    public ServerWriter(OutputStream outputStream) {
        super(outputStream);
    }

    public void writeId(int id) throws IOException {
        write(BaseDataPackage.getObject().init(SERVER, id, SEND_ID, null));
    }

    public void writeAudioFormat(int id, String format) throws IOException {
        write(BaseDataPackage.getObject().init(format, SERVER, id, SEND_AUDIO_FORMAT));
    }

    public void writeUsers(int id) throws IOException {
        write(BaseDataPackage.getObject().init(Server.getInstance().getUsers(id), SERVER, id, SEND_USERS));
    }
    /*
    check this peace of garbage
     */
    public void transferData(BaseDataPackage dataPackage){
        try {
            write(dataPackage);
        } catch (IOException e) {
            e.printStackTrace();

        }
    }
}
