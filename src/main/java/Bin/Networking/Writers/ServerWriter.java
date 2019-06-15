package Bin.Networking.Writers;

import Bin.Main;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Utility.ErrorHandler;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Properties;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Contain not all possible server write actions
 */

public class ServerWriter extends BaseWriter {

    /**
     * Need for conference writing
     */

    private Lock lock;

    /**
     * If internet of one of the users is garbage
     * it will skip him through this time
     * <p>
     * in millis, not calculated,
     * but half a second is sound package so need to make it more reasonable
     */

    private static final int LOCK_TIME; //default 300

    /*
        Reads LOCK_TIME from property map
        You can change it there
     */

    static {
        Properties defaultProp = new Properties();
        defaultProp.setProperty("lock_time", "300");
        Properties loadedProp = new Properties(defaultProp);
        InputStream resourceAsStream = Main.class.getResourceAsStream("properties/Server.properties.properties");
        if (resourceAsStream != null) {
            try {
                loadedProp.load(resourceAsStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        LOCK_TIME = Integer.parseInt(loadedProp.getProperty("lock_time"));
    }

    public ServerWriter(OutputStream outputStream) {
        super(outputStream);
        lock = new ReentrantLock();
    }

    public ServerWriter(OutputStream outputStream, ErrorHandler mainErrorHandler) {
        super(outputStream, mainErrorHandler);
        lock = new ReentrantLock();
    }


    public void writeAudioFormat(int id, String format) {
        writeA(AbstractDataPackagePool.getPackage().init(CODE.SEND_AUDIO_FORMAT, WHO.SERVER.getCode(), id, format));
    }

    public void writeUsers(int id, String users) {
        writeA(AbstractDataPackagePool.getPackage().init(CODE.SEND_USERS, WHO.SERVER.getCode(), id, users));
    }

    public void writeDisconnect(int id) {
        writeA(AbstractDataPackagePool.getPackage().init(CODE.SEND_DISCONNECT, WHO.SERVER.getCode(), id));
    }

    /**
     * Method for transferring data from one user to another one
     *
     * @param dataPackage to be transferred
     */

    public synchronized void transferData(AbstractDataPackage dataPackage) {
        try {
            outputStream.write(dataPackage.getHeader().getRaw());//     uses already calculated header, when you read it
            if (dataPackage.getHeader().getLength() != 0)
                outputStream.write(dataPackage.getData());
            outputStream.flush();
            AbstractDataPackagePool.returnPackage(dataPackage);
        } catch (IOException e) {
            e.printStackTrace();
            mainErrorHandler.errorCase();
        }
    }

    /**
     * Method for writing data in conversation mode
     * It tries to get lock if can't just pass this dude
     *
     * @param dataPackage to be sanded
     * @throws IOException if networking fails
     */

    public void transferAudio(AbstractDataPackage dataPackage) throws IOException {
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

    public void writeAddToConv(int whoToAdd, int to) {
        writeA(AbstractDataPackagePool.getPackage().init(CODE.SEND_ADD, whoToAdd, to));
    }

    public void writeRemoveFromConv(int whoToRemove, int to) {
        writeA(AbstractDataPackagePool.getPackage().init(CODE.SEND_REMOVE, whoToRemove, to));
    }

    public void writeStopConv(int to) {
        writeA(AbstractDataPackagePool.getPackage().init(CODE.SEND_STOP_CONV, WHO.CONFERENCE.getCode(), to));
    }
}
