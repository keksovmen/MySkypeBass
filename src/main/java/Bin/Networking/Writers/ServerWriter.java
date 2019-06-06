package Bin.Networking.Writers;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackagePool;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

public class ServerWriter extends BaseWriter {

    private Lock lock;
    private static final int LOCK_TIME = 300;       //in millis

    public ServerWriter(OutputStream outputStream) {
        super(outputStream);
        lock = new ReentrantLock();
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
        outputStream.write(dataPackage.getHeader().getRaw());//     uses already calculated header
        if (dataPackage.getHeader().getLength() != 0)
            outputStream.write(dataPackage.getData());
        outputStream.flush();
        DataPackagePool.returnPackage(dataPackage);
    }

    public void transferAudio(BaseDataPackage dataPackage) throws IOException {
        try {
            if (lock.tryLock(LOCK_TIME, TimeUnit.MILLISECONDS)) {
                try {
                    outputStream.write(dataPackage.getHeader().getRaw());//     uses already calculated header
                    if (dataPackage.getHeader().getLength() != 0) {
                        outputStream.write(dataPackage.getData());
                    }
                    outputStream.flush();
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeAddToConv(int whoToAdd, int to) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_ADD, whoToAdd, to));
    }

    public void writeRemoveFromConv(int whoToRemove, int to) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_REMOVE, whoToRemove, to));
    }

    public void writeStopConv(int to) throws IOException {
        write(DataPackagePool.getPackage().init(CODE.SEND_STOP_CONV, WHO.CONFERENCE.getCode(), to));
    }
}
