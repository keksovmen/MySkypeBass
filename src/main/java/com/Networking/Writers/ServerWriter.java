package com.Networking.Writers;

import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.CODE;
import com.Networking.Utility.WHO;
import com.Util.Resources;

import java.io.IOException;
import java.io.OutputStream;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.serverLogger;

/**
 * Contain not all possible server write actions
 */

public class ServerWriter extends BaseWriter {

    /**
     * Need for conference writing
     */

    private final Lock lock;

    /**
     * If internet of one of the users is garbage
     * it will skip him through this time
     * <p>
     * in millis, not calculated
     */

    private final int LOCK_TIME; //default 300

    public ServerWriter(OutputStream outputStream, int bufferSize) {
        super(outputStream, bufferSize);
        lock = new ReentrantLock();
        LOCK_TIME = Resources.getLockTime();
    }


    public void writeAudioFormat(int id, String format) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_AUDIO_FORMAT,
                WHO.SERVER.getCode(),
                id,
                format));
    }

    public void writeUsers(int id, String users) throws IOException {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "writeUsers",
                "Write all users to  - " + id + ", users - " + users);
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
                    writeWithoutReturnToPool(dataPackage);
                } finally {
                    lock.unlock();
                }
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void writeAddToConv(int whoToAdd, int to) throws IOException {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "writeAddToConv", "Trying to add this - " + whoToAdd + ", message is for - " + to);
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ADD_TO_CONVERSATION, whoToAdd, to));
    }

    public void writeRemoveFromConv(int whoToRemove, int to) throws IOException {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "writeRemoveFromConv", "Try to remove this dude from conversation - " + whoToRemove + ", message is for - " + to);
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
        serverLogger.logp(Level.FINER, this.getClass().getName(), "writeRemoveFromUserList",
                "Sending message to remove this dude - " + dudeToRemove + ", to - " + to);
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_REMOVE_FROM_USER_LIST,
                WHO.SERVER.getCode(),
                to,
                String.valueOf(dudeToRemove)
        ));
    }

    public void transferPacket(AbstractDataPackage dataPackage) throws IOException {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "transferPacket",
                "Transferring a package - " + dataPackage.getHeader().toString());
        writeWithoutReturnToPool(dataPackage);
    }

    public void writeBothInConversations(int me, int from) throws IOException {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "writeBothInConversations",
                "Write both in conversation to  - " + me + ", from - " + from);
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_BOTH_IN_CONVERSATIONS,
                from,
                me
        ));
    }
}
