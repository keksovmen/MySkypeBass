package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Algorithms;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;

import java.io.IOException;
import java.net.InetAddress;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * High level object for network write methods
 * Contains all possible server write actions
 */

public class ServerWriter extends AbstractWriter {

    /**
     * for optimising internet connection on write sound
     */

    protected final Lock optimiserLock;

    protected final int lockDuration;

    /**
     * @param bridgeImplementation plaint ot cipher, will delegate to him
     */


    public ServerWriter(Writer bridgeImplementation) {
        super(bridgeImplementation);
        optimiserLock = new ReentrantLock();
        lockDuration = Algorithms.calculatePartOfAudioUnitDuration();
    }

    @Override
    protected BaseLogger createLogger() {
        return LogManagerHelper.getInstance().getServerLogger();
    }

    protected void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        bridgeImplementation.writeWithoutReturnToPool(dataPackage);
    }


    protected void writeWithoutReturnToPoolUDPorTCP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        if (address == null)
            writeWithoutReturnToPool(dataPackage);
        else
            bridgeImplementation.writeWithoutReturnToPoolUDP(dataPackage, address, port);
    }


    public void writeUsers(int id, String users) throws IOException {
        logger.logp(this.getClass().getName(), "writeUsers",
                "Write all users to  - " + id + ", users - " + users);
        writeTCP(AbstractDataPackagePool.getPackage().initString(CODE.SEND_USERS, WHO.SERVER.getCode(), id, users));
    }

    /**
     * Method for writing data in conversation mode
     * If user is lagging it will detect it handle some how
     * <p>
     * Don't return package back to the pool
     *
     * @param dataPackage to be sanded
     * @param address     could be null if full TCP
     * @param port        to send to
     * @throws IOException if networking fails
     */

    public void transferAudio(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        if (speedMonitor == null) {
            writeSoundTryNoMonitor(dataPackage, address, port);
        } else {
            writeSoundTryWithMonitor(dataPackage, address, port);
        }


    }

    public void writeAddToConv(int whoToAdd, int to) throws IOException {
        logger.logp(this.getClass().getName(), "writeAddToConv", "Trying to add this - " + whoToAdd + ", message is for - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_ADD_TO_CONVERSATION,
                whoToAdd,
                to)
        );
    }

    public void writeRemoveFromConv(int whoToRemove, int to) throws IOException {
        logger.logp(this.getClass().getName(), "writeRemoveFromConv", "Try to remove this dude from conversation - " + whoToRemove + ", message is for - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_REMOVE_FROM_CONVERSATION,
                whoToRemove,
                to)
        );
    }

    public void writeStopConv(int to) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_DISCONNECT_FROM_CONVERSATION,
                WHO.CONFERENCE.getCode(),
                to
        ));
    }

    public void writeAddToUserList(int to, String dudeToAdd) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_ADD_TO_USER_LIST,
                WHO.SERVER.getCode(),
                to, dudeToAdd
        ));
    }

    public void writeRemoveFromUserList(int to, int dudeToRemove) throws IOException {
        logger.logp(this.getClass().getName(), "writeRemoveFromUserList",
                "Sending message to remove this dude - " + dudeToRemove + ", to - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_REMOVE_FROM_USER_LIST,
                WHO.SERVER.getCode(),
                to, String.valueOf(dudeToRemove)
        ));
    }

    public void transferPacket(AbstractDataPackage dataPackage) throws IOException {
        logger.logp(this.getClass().getName(), "transferPacket",
                "Transferring a package - " + dataPackage.getHeader().toString());
        writeWithoutReturnToPool(dataPackage);
    }

    public void writeBothInConversations(int me, int from) throws IOException {
        logger.logp(this.getClass().getName(), "writeBothInConversations",
                "Write both in conversation to  - " + me + ", from - " + from);
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_BOTH_IN_CONVERSATIONS,
                from,
                me
        ));
    }


    public void writeAddWholeConversation(int to, String dudes) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_ADD_WHOLE_CONVERSATION,
                WHO.SERVER.getCode(),
                to, dudes)
        );
    }


    public void writePing(int to) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_PING,
                WHO.SERVER.getCode(),
                to));
    }

    /**
     * Will prevent new entered thread from waiting too much time
     * on lagging connection
     *
     * @param dataPackage not null
     * @param address     could be null for TCP connection
     * @param port        could be -1 for TCP connection
     * @throws IOException if network fails
     */

    protected void writeSoundTryNoMonitor(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        try {
            if (optimiserLock.tryLock(lockDuration, TimeUnit.MICROSECONDS)) {
                try {
                    writeWithoutReturnToPoolUDPorTCP(dataPackage, address, port);
                } finally {
                    optimiserLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            //shouldn't happen because not using interruptions
            e.printStackTrace();
            logger.loge(this.getClass().getName(), "writeSoundTryNoMonitor", e);
            optimiserLock.unlock();
        }
    }

    /**
     * Will work as {@link #writeSoundTryNoMonitor(AbstractDataPackage, InetAddress, int)}
     * And will not send audio to this dude if his network is lagging
     * for some time period
     *
     * @param dataPackage not null
     * @param address     could be null for TCP connection
     * @param port        could be -1 for TCP connection
     * @throws IOException if network fails
     */

    protected void writeSoundTryWithMonitor(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        if (!speedMonitor.isAllowed())
            return;
        try {
            if (optimiserLock.tryLock(speedMonitor.getMinBoundary(), TimeUnit.MICROSECONDS)) {
                try {
                    if (!speedMonitor.isAllowed())
                        return;//   finally unlocks
                    long beforeNano = System.nanoTime();
                    writeWithoutReturnToPoolUDPorTCP(dataPackage, address, port);
                    int deltaMicro = (int) (System.nanoTime() - beforeNano) / 1000;
                    speedMonitor.feedValue(deltaMicro);
                } finally {
                    optimiserLock.unlock();
                }
            }
        } catch (InterruptedException e) {
            //shouldn't happen because not using interruptions
            e.printStackTrace();
            logger.loge(this.getClass().getName(), "writeSoundTryWithMonitor", e);
            optimiserLock.unlock();
        }
    }
}
