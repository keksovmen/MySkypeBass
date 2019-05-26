package Bin.Networking.Writers;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;

import java.io.IOException;
import java.io.OutputStream;

public class ServerWriter extends BaseWriter {

    public ServerWriter(OutputStream outputStream) {
        super(outputStream);
    }

    public void writeId(int id) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_ID, WHO.SERVER.getCode(), id));
    }

    public void writeAudioFormat(int id, String format) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_AUDIO_FORMAT, WHO.SERVER.getCode(), id, format));
    }

    public void writeUsers(int id, String users) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_USERS, WHO.SERVER.getCode(), id, users));
    }

    public void writeDisconnect(int id) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_DISCONNECT, WHO.SERVER.getCode(), id));
    }
    /*
    check this peace of garbage
     */
    public synchronized void transferData(BaseDataPackage dataPackage) throws IOException {
//        try {
        outputStream.write(dataPackage.getHeader().getRaw());//uses already calculated header
        if (dataPackage.getHeader().getLength() != 0)
            outputStream.write(dataPackage.getData());
        outputStream.flush();
        DataPackagePool.returnPackage(dataPackage);
//            write(dataPackage);//cashe somehow header cause he is already calculated
//        } catch (IOException e) {
//            e.printStackTrace();
//
//        }
    }
}
