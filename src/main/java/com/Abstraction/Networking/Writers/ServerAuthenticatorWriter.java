package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;

import java.io.IOException;

/**
 * Use only for server authentication part
 */

public class ServerAuthenticatorWriter extends AbstractWriter {


    public ServerAuthenticatorWriter(Writer bridgeImplementation) {
        super(bridgeImplementation);
    }

    @Override
    protected BaseLogger createLogger() {
        return LogManagerHelper.getInstance().getServerLogger();
    }

    public void writeAudioFormat(int id, String format) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_AUDIO_FORMAT,
                WHO.SERVER.getCode(),
                id, format));
    }

    public void writeIsFullTCPConnection(boolean isFullTCP) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                isFullTCP ? CODE.SEND_FULL_TCP_CONNECTION : CODE.SEND_MIXED_CONNECTION,
                WHO.SERVER.getCode(),
                WHO.NO_NAME.getCode()
        ));
    }

    public void writeSizeOfUDP(int sizeUDP) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_UDP_PACKAGE_SIZE,
                WHO.SERVER.getCode(),
                WHO.NO_NAME.getCode(), String.valueOf(sizeUDP))
        );
    }

    public void writeCipherMode(int to) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_SERVER_CIPHER_MODE,
                WHO.SERVER.getCode(),
                to)
        );
    }

    public void writePublicKeyEncoded(int to, byte[] key) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initRaw(
                CODE.SEND_PUBLIC_ENCODED_KEY,
                WHO.SERVER.getCode(),
                to, key)
        );
    }

    public void writeAlgorithmParams(int to, byte[] params) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initRaw(
                CODE.SEND_ALGORITHM_PARAMETERS_ENCODED,
                WHO.SERVER.getCode(),
                to, params)
        );
    }

    public void writePlainMode(int to) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_SERVER_PLAIN_MODE,
                WHO.SERVER.getCode(), to)
        );
    }
}
