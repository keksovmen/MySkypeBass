package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Contain not all possible server write actions
 */

public class ServerWriter implements Writer {

    private final Writer writer;

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

    private final int LOCK_TIME; //default 50


    public ServerWriter(Writer writer) {
//        super(outputStream, bufferSize);
        this.writer = writer;
        lock = new ReentrantLock();
        LOCK_TIME = Resources.getInstance().getLockTime();
    }

    @Override
    public void write(AbstractDataPackage dataPackage) throws IOException {
        writer.write(dataPackage);
    }

    @Override
    public void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        writer.writeWithoutReturnToPool(dataPackage);
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
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ADD_TO_CONVERSATION, whoToAdd, to));
    }

    public void writeRemoveFromConv(int whoToRemove, int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_REMOVE_FROM_CONVERSATION, whoToRemove, to));
    }

    public void writeStopConv(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_DISCONNECT_FROM_CONVERSATION,
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

    public void writeBothInConversations(int me, int from) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_BOTH_IN_CONVERSATIONS,
                from,
                me
        ));
    }

    public void writeCipherMode(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_SERVER_CIPHER_MODE, WHO.SERVER.getCode(), to));
    }

    public void writePlainMode(int to) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_SERVER_PLAIN_MODE, WHO.SERVER.getCode(), to));
    }

    public void writePublicKeyEncoded(int to, byte[] key) throws IOException {
        write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_PUBLIC_ENCODED_KEY, WHO.SERVER.getCode(), to, key));
    }

    public void writeAlgorithmParams(int to, byte[] params) throws IOException {
        write(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_ALGORITHM_PARAMETERS_ENCODED, WHO.SERVER.getCode(), to, params));
    }

    public void writeAddWholeConversation(int to, String dudes) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_ADD_WHOLE_CONVERSATION, WHO.SERVER.getCode(), to, dudes));
    }
}
