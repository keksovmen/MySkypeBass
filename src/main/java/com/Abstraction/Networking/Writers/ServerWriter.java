package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;
import com.Abstraction.Util.Logging.LogManagerHelper;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Contain not all possible server write actions
 */

public class ServerWriter {

    private final BaseLogger serverLogger = LogManagerHelper.getInstance().getServerLogger();


    private final Writer writer;


    public ServerWriter(Writer writer) {
        this.writer = writer;
    }


    protected void write(AbstractDataPackage dataPackage) throws IOException {
        writer.write(dataPackage);
    }

    protected void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        writer.writeWithoutReturnToPool(dataPackage);
    }


    protected void writeWithoutReturnToPoolUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        if (address == null)
            writeWithoutReturnToPool(dataPackage);
        else
            writer.writeWithoutReturnToPoolUDP(dataPackage, address, port);
    }


    public void writeAudioFormat(int id, String format) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_AUDIO_FORMAT,
                WHO.SERVER.getCode(),
                id,
                format));
    }

    public void writeUsers(int id, String users) throws IOException {
        serverLogger.logp(this.getClass().getName(), "writeUsers",
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
     * @param address could be null if full TCP
     * @param port to send to
     * @throws IOException if networking fails
     */

    public void transferAudio(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        writeWithoutReturnToPoolUDP(dataPackage, address, port);
    }

    public void writeAddToConv(int whoToAdd, int to) throws IOException {
        serverLogger.logp(this.getClass().getName(), "writeAddToConv", "Trying to add this - " + whoToAdd + ", message is for - " + to);
        write(AbstractDataPackagePool.getPackage().initZeroLength(CODE.SEND_ADD_TO_CONVERSATION, whoToAdd, to));
    }

    public void writeRemoveFromConv(int whoToRemove, int to) throws IOException {
        serverLogger.logp(this.getClass().getName(), "writeRemoveFromConv", "Try to remove this dude from conversation - " + whoToRemove + ", message is for - " + to);
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
        serverLogger.logp(this.getClass().getName(), "writeRemoveFromUserList",
                "Sending message to remove this dude - " + dudeToRemove + ", to - " + to);
        write(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_REMOVE_FROM_USER_LIST,
                WHO.SERVER.getCode(),
                to,
                String.valueOf(dudeToRemove)
        ));
    }

    public void transferPacket(AbstractDataPackage dataPackage) throws IOException {
        serverLogger.logp(this.getClass().getName(), "transferPacket",
                "Transferring a package - " + dataPackage.getHeader().toString());
        writeWithoutReturnToPool(dataPackage);
    }

    public void writeBothInConversations(int me, int from) throws IOException {
        serverLogger.logp(this.getClass().getName(), "writeBothInConversations",
                "Write both in conversation to  - " + me + ", from - " + from);
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

    public void writeSizeOfUDP(int sizeUDP) throws IOException {
        write(AbstractDataPackagePool.getPackage().initString(CODE.SEND_UDP_PACKAGE_SIZE, WHO.SERVER.getCode(), WHO.NO_NAME.getCode(), String.valueOf(sizeUDP)));
    }

    public void writeIsFullTCPConnection(boolean isFullTCP) throws IOException {
        write(AbstractDataPackagePool.getPackage().initZeroLength(
                isFullTCP ? CODE.SEND_FULL_TCP_CONNECTION : CODE.SEND_MIXED_CONNECTION,
                WHO.SERVER.getCode(),
                WHO.NO_NAME.getCode()
        ));
    }
}
