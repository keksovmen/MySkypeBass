package Bin.Networking.Writers;

import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Protocol.CODE;
import Bin.Networking.Server;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Utility.WHO;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Contain not all possible server write actions
 */

public class ServerWriter extends WriterWithHandler {

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
        LOCK_TIME = Integer.parseInt(Server.serverProp.getProperty("lock_time"));
    }

    public ServerWriter(OutputStream outputStream, ErrorHandler mainErrorHandler) {
        super(outputStream, mainErrorHandler);
        lock = new ReentrantLock();
    }


    public void writeAudioFormat(int id, String format) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_AUDIO_FORMAT,
                WHO.SERVER.getCode(),
                id,
                format));
    }

    public void writeUsers(int id, String users) {
        writeWithHandler(AbstractDataPackagePool.getPackage().initString(CODE.SEND_USERS, WHO.SERVER.getCode(), id, users));
    }

    public void writeDisconnect(int id) {
        writeWithHandler(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT, WHO.SERVER.getCode(), id));
    }

    /**
     * Method for transferring data from one user to another one
     * <p>
     * Don't return package back to the pool
     *
     * @param dataPackage to be transferred
     */

    public synchronized void transferData(AbstractDataPackage dataPackage) {
        try {
            outputStream.write(dataPackage.getHeader().getRawHeader());//     uses already calculated header, when you read it
            if (dataPackage.getHeader().getLength() != 0) {
                outputStream.write(dataPackage.getData());
            }
            outputStream.flush();
//            AbstractDataPackagePool.returnPackage(dataPackage);
        } catch (IOException e) {
            e.printStackTrace();
            mainErrorHandler.errorCase();
        }
    }

    /**
     * Method for writing data in conversation mode
     * It tries to get lock if can't just pass this dude
     * <p>
     * Don't return package back to the pool
     *
     * @param dataPackage to be sanded
     * @throws IOException if networking fails
     */

    public void transferAudio(AbstractDataPackage dataPackage) throws IOException {
        try {
            if (lock.tryLock(LOCK_TIME, TimeUnit.MILLISECONDS)) {
                try {
                    outputStream.write(dataPackage.getHeader().getRawHeader());//     uses already calculated header
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

    /**
     * Transfer a message from conference
     * Doesn't return package back to the pool
     *
     * @param dataPackage contains the message
     * @throws IOException if networking fails
     */

    public synchronized void transferMessage(AbstractDataPackage dataPackage) throws IOException {
        outputStream.write(dataPackage.getHeader().getRawHeader());//     uses already calculated header, when you read it
        if (dataPackage.getHeader().getLength() != 0) {
            outputStream.write(dataPackage.getData());
        }
        outputStream.flush();
    }

    public void writeAddToConv(int whoToAdd, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ADD, whoToAdd, to));
    }

    public void writeRemoveFromConv(int whoToRemove, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_REMOVE, whoToRemove, to));
    }

    public void writeStopConv(int to) {
        writeWithHandler(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_STOP_CONV, WHO.CONFERENCE.getCode(), to));
    }

    public void writeId(int id) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_ID,
                WHO.SERVER.getCode(),
                id
        ));
    }
}
