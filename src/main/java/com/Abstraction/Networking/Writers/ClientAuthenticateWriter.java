package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;

import java.io.IOException;

/**
 * Used for authentication part only
 */

public class ClientAuthenticateWriter extends AbstractWriter {


    public ClientAuthenticateWriter(Writer bridgeImplementation) {
        super(bridgeImplementation);
    }

    @Override
    protected BaseLogger createLogger() {
        return LogManagerHelper.getInstance().getClientLogger();
    }

    public void writeName(String name) throws IOException {
        logger.logp(this.getClass().getName(), "writeName",
                "Writing my name - " + name);
        writeTCP(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_NAME, WHO.NO_NAME.getCode(), WHO.SERVER.getCode(), name)
        );
    }

    public void writeApproveAudioFormat() throws IOException {
        logger.logp(this.getClass().getName(), "writeApproveAudioFormat",
                "Writing approve audio format");
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_AUDIO_FORMAT_ACCEPT, WHO.NO_NAME.getCode(), WHO.SERVER.getCode())
        );
    }

    public void writeDeclineAudioFormat() throws IOException {
        logger.logp(this.getClass().getName(), "writeDeclineAudioFormat",
                "Writing decline audio format");
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_AUDIO_FORMAT_DENY, WHO.NO_NAME.getCode(), WHO.SERVER.getCode())
        );
    }

    public void writeMyPortUDP(int port) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initString(
                CODE.SEND_UDP_PORT, WHO.NO_NAME.getCode(),
                WHO.SERVER.getCode(), String.valueOf(port))
        );
    }

    public void writeCipherModeAccepted() throws IOException {
        logger.logp(this.getClass().getName(), "writeCipherModeAccepted",
                "Writing approve cipher mode");
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_CIPHER_MODE_ACCEPTED, WHO.NO_NAME.getCode(), WHO.SERVER.getCode())
        );
    }

    public void writeCipherModeDenied() throws IOException {
        logger.logp(this.getClass().getName(), "writeCipherModeDenied",
                "Writing deny cipher mode");
        writeTCP(AbstractDataPackagePool.getPackage().initZeroLength(
                CODE.SEND_CIPHER_MODE_DENIED, WHO.NO_NAME.getCode(), WHO.SERVER.getCode())
        );
    }

    public void writePublicKeyEncoded(byte[] encodedPubKey) throws IOException {
        writeTCP(AbstractDataPackagePool.getPackage().initRaw(
                CODE.SEND_PUBLIC_ENCODED_KEY, WHO.NO_NAME.getCode(),
                WHO.SERVER.getCode(), encodedPubKey)
        );
    }
}
