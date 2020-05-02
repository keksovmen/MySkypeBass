package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;
import com.Abstraction.Util.Monitors.SpeedMonitor;

import java.io.IOException;
import java.net.InetAddress;
import java.net.InetSocketAddress;

/**
 * Contain all possible write actions for a client
 * Each method basically ask pool for carcase and init it
 */

public class ClientWriter extends AbstractWriter {

    /**
     * Received from server, or {@link WHO#NO_NAME} as default
     */

    private final int myID;

    /**
     * If null mean full TCP connection
     * Needed for UDP connection
     */

    private final InetSocketAddress address;

    /**
     * Base client writer with id
     *
     * @param bridgeImplementation contain vital methods for network writing
     * @param myID              received from server, unique id
     * @param address           for UDP session, might be null
     */

    public ClientWriter(Writer bridgeImplementation, int myID, InetSocketAddress address) {
        super(bridgeImplementation);
        this.myID = myID;
        this.address = address;
    }



    @Override
    protected BaseLogger createLogger() {
        return LogManagerHelper.getInstance().getClientLogger();
    }

    protected void writeUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        bridgeImplementation.writeUDP(dataPackage, address, port);
    }


    public void writeUsersRequest() throws IOException {
        logger.logp(this.getClass().getName(), "writeUserRequest",
                "Writing user request");
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_USERS, myID, WHO.SERVER.getCode()));
    }

    /**
     * Might accept to as WHO.CONFERENCE.getId()
     *
     * @param to      who gonna receive it
     * @param message what you typed
     * @throws IOException if network fails
     */

    public void writeMessage(int to, String message) throws IOException {
        logger.logp(this.getClass().getName(), "writeMessage",
                "Writing message to - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initString(CODE.SEND_MESSAGE, myID, to, message));
    }

    public void writeCall(int to) throws IOException {
        logger.logp(this.getClass().getName(), "writeCall",
                "Writing call to - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CALL, myID, to));
    }

    public void writeAccept(int to) throws IOException {
        logger.logp(this.getClass().getName(), "writeAcceptCall",
                "Writing accept call to - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ACCEPT_CALL, myID, to));
    }

    public void writeDeny(int to) throws IOException {
        logger.logp(this.getClass().getName(), "writeDenyCall",
                "Writing deny call - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DENY_CALL, myID, to));
    }

    public void writeCancel(int to) throws IOException {
        logger.logp(this.getClass().getName(), "writeCancelCall",
                "Writing cancel call - " + to);
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_CANCEL_CALL, myID, to));
    }

    public void writeSound(byte[] data) throws IOException {
        if (address == null) {
            if (speedMonitor == null) {
                writeTCP(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data));
            }else {
                writeSoundWithMonitor(data);
            }
        } else {
            //UDP won't lag as TCP on very bed internet connection so no need for monitor
            writeUDP(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data), address.getAddress(), address.getPort());
        }
    }

    public void writeSoundWithMonitor(byte[] data) throws IOException {
        if (!speedMonitor.isAllowed())
            return;
        long beforeNano = System.nanoTime();
        writeTCP(AbstractDataPackagePool.getPackage().initRaw(CODE.SEND_SOUND, myID, WHO.CONFERENCE.getCode(), data));
        int deltaMicro = (int) (System.nanoTime() - beforeNano) / 1000;
        speedMonitor.feedValue(deltaMicro);
    }

    public void writeDisconnect() throws IOException {
        logger.logp(this.getClass().getName(), "writeDisconnect",
                "Writing disconnect call");
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT, myID, WHO.SERVER.getCode()));
    }

    public void writeDisconnectFromConv() throws IOException {
        logger.logp(this.getClass().getName(), "writeDisconnectFromConversation",
                "Writing disconnect from conversation");
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_DISCONNECT_FROM_CONVERSATION, myID, WHO.CONFERENCE.getCode()));
    }

    public void writePong() throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_PONG, myID, WHO.SERVER.getCode()));
    }
}
