package Com.Networking.Writers;

import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Protocol.CODE;
import Com.Networking.Utility.WHO;

import java.io.IOException;
import java.io.OutputStream;
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

    private static final int LOCK_TIME = 300; //default 300

    /*
        Reads LOCK_TIME from property map
        You can change it there
     */

//    static {
//        LOCK_TIME = Integer.parseInt(Server.serverProp.getProperty("lock_time"));
//    }

    public ServerWriter(OutputStream outputStream, int bufferSize) {
        super(outputStream, bufferSize);
        lock = new ReentrantLock();
    }


    public void writeAudioFormat(int id, String format) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_AUDIO_FORMAT,
                WHO.SERVER.getCode(),
                id,
                format));
    }

    public void writeUsers(int id, String users) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_USERS, WHO.SERVER.getCode(), id, users));
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

    public void writeAddToConv(int whoToAdd, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ADD_TO_CONVERSATION, whoToAdd, to));
    }

    public void writeRemoveFromConv(int whoToRemove, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_REMOVE_FROM_CONVERSATION, whoToRemove, to));
    }

    public void writeStopConv(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_DISCONNECT_FROM_CONV,
                WHO.CONFERENCE.getCode(),
                to
        ));
    }

    public void writeId(int id) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_ID,
                WHO.SERVER.getCode(),
                id
        ));
    }

    public void writeAddToUserList(int to, String dudeToAdd) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_ADD_TO_USER_LIST,
                WHO.SERVER.getCode(),
                to,
                dudeToAdd
        ));
    }

    public void writeRemoveFromUserList(int to, int dudeToRemove) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_REMOVE_FROM_USER_LIST,
                WHO.SERVER.getCode(),
                to,
                String.valueOf(dudeToRemove)
        ));
    }

    public void transferPacket(AbstractDataPackage dataPackage) throws IOException {
        writeWithoutReturnToPool(dataPackage);
    }

    public void writeDudeIsOffline(int from, int to, String whoIsMissing) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_DUDE_IS_OFFLINE,
                from,
                to,
                whoIsMissing
        ));
    }

    public void writeBothInConversations(int me, int from) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_BOTH_IN_CONVERSATIONS,
                from,
                me
        ));
    }
}
