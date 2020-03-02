package com.Abstraction.Networking.Utility;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Audio.Misc.AbstractAudioFormatWithMic;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Writers.ClientWriter;
import com.Abstraction.Networking.Writers.PlainWriter;
import com.Abstraction.Networking.Writers.ServerWriter;
import com.Abstraction.Networking.Writers.Writer;
import com.Abstraction.Util.Cryptographics.BaseClientCryptoHelper;
import com.Abstraction.Util.Cryptographics.BaseServerCryptoHelper;
import com.Abstraction.Util.Cryptographics.CommonCryptoHelper;
import com.Abstraction.Util.Cryptographics.Crypto;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * 2 main methods must work with each other
 */

public class Authenticator {

    /**
     * 1 - write my name
     * 2 - read audio format and my id
     * 3 - write response to audio format
     * 4 - write my UDP port
     * 5 - read UDP package size
     * 6 - read cipher mode
     * 7 - write response to cipher mode
     * 8 - discuss cipher details
     *
     * @param inputStream  of connected socket
     * @param outputStream of connected socket
     * @param desiredName  on server
     * @return structure with flags
     */

    public ClientStorage clientAuthentication(InputStream inputStream, OutputStream outputStream, String desiredName, int portUDP) {
        BaseReader reader = createClientReader(inputStream);
        ClientWriter writer = createClientWriter(outputStream);

        try {
            writer.writeName(desiredName);
            AbstractDataPackage dataPackage = reader.read();
            final int myID = dataPackage.getHeader().getTo();
            final AbstractAudioFormatWithMic format = AbstractAudioFormatWithMic.fromString(dataPackage.getDataAsString());
            if (AudioSupplier.getInstance().isFormatSupported(format)) {
                writer.writeApproveAudioFormat();
            } else {
                writer.writeDeclineAudioFormat();
                return createClientAudioNotAccepted(format);
            }
            writer.writeMyPortUDP(portUDP);
            final int sizeUDP = reader.read().getDataAsInt();

            if (reader.read().getHeader().getCode().equals(CODE.SEND_SERVER_CIPHER_MODE)) {
                if (Crypto.isCipherAcceptable(Crypto.STANDARD_CIPHER_FORMAT)) {
                    writer.writeCipherModeAccepted();

                    BaseClientCryptoHelper cryptoHelper = new BaseClientCryptoHelper();
                    cryptoHelper.initialiseKeyGenerator();
                    writer.writePublicKeyEncoded(cryptoHelper.getPublicKeyEncoded());
                    cryptoHelper.finishExchange(reader.read().getData());
                    cryptoHelper.setAlgorithmParametersEncoded(reader.read().getData());
                    return createClientAudioAcceptedCipherAccepted(myID, portUDP, format, cryptoHelper, desiredName, sizeUDP);
                } else {
                    writer.writeCipherModeDenied();
                    return createClientAudioAcceptedCipherNotAccepted(format);
                }
            } else {
                return createClientAudioAcceptedPlain(myID, portUDP, format, desiredName, sizeUDP);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return createClientNetworkFailure();
        }
    }

    /**
     * 1 - read his name
     * 2 - write audio format
     * 3 - read response on audio format
     * 4 - read UDP port
     * 5 - write UDP package size
     * 6 - write cipher mode
     * 7 - read cipher response
     * 8 - discuss cipher details
     *
     * @param inputStream        of connected socket
     * @param outputStream       of connected socket
     * @param audioFormat        that server proposes
     * @param hisID              unique for server identification
     * @param isSecureConnection server option
     * @return structure with flags
     */

    public CommonStorage serverAuthentication(InputStream inputStream, OutputStream outputStream, String audioFormat, int hisID, boolean isSecureConnection, int sizeUDP) {
        BaseReader reader = createServerReader(inputStream);
        ServerWriter writer = createServerWriter(outputStream);

        try {
            final String name = reader.read().getDataAsString();
            writer.writeAudioFormat(hisID, audioFormat);
            if (reader.read().getHeader().getCode().equals(CODE.SEND_AUDIO_FORMAT_ACCEPT)) {
                final int portUDP = reader.read().getDataAsInt();
                writer.writeSizeOfUDP(sizeUDP);
                if (isSecureConnection) {
                    writer.writeCipherMode(hisID);
                    if (reader.read().getHeader().getCode().equals(CODE.SEND_CIPHER_MODE_ACCEPTED)) {
                        BaseServerCryptoHelper cryptoHelper = new BaseServerCryptoHelper();
                        cryptoHelper.initialiseKeyGenerator(reader.read().getData());
                        writer.writePublicKeyEncoded(hisID, cryptoHelper.getPublicKeyEncoded());
                        writer.writeAlgorithmParams(hisID, cryptoHelper.getAlgorithmParametersEncoded());

                        return createServerAudioAcceptedCipherAccepted(hisID, portUDP, name, cryptoHelper);
                    } else {
                        return createServerAudioAcceptedCipherNotAccepted();
                    }
                } else {
                    writer.writePlainMode(hisID);
                    return createServerAudioAcceptedPlain(hisID, portUDP, name);
                }
            } else {
                return createServerAudioNotAccepted();
            }
        } catch (IOException e) {
            e.printStackTrace();
            return createServerNetworkFailure();
        }

    }


    protected BaseReader createClientReader(InputStream inputStream) {
        return new BaseReader(inputStream, Resources.getInstance().getBufferSize());
    }

    protected BaseReader createServerReader(InputStream inputStream) {
        return new BaseReader(inputStream, Resources.getInstance().getBufferSize());
    }

    protected ClientWriter createClientWriter(OutputStream outputStream) {
        return new ClientWriter(createPlainWriter(outputStream));
    }

    protected ServerWriter createServerWriter(OutputStream outputStream) {
        return new ServerWriter(createPlainWriter(outputStream));
    }


    private Writer createPlainWriter(OutputStream stream) {
        return new PlainWriter(stream, Resources.getInstance().getBufferSize());
    }

    private static ClientStorage createClientNetworkFailure() {
        return new ClientStorage(false, true, false, false, -1, -1, null, null, null, -1);
    }

    private static ClientStorage createClientAudioNotAccepted(AbstractAudioFormatWithMic format) {
        return new ClientStorage(true, false, false, false, -1, -1, null, null, format, -1);
    }

    private static ClientStorage createClientAudioAcceptedPlain(int id, int portUDP, AbstractAudioFormatWithMic format, String name, int sizeUDP) {
        return new ClientStorage(true, false, false, false, id, portUDP, name, null, format, sizeUDP);
    }

    private static ClientStorage createClientAudioAcceptedCipherNotAccepted(AbstractAudioFormatWithMic format) {
        return new ClientStorage(true, false, true, false, -1, -1, null, null, format, -1);
    }

    private static ClientStorage createClientAudioAcceptedCipherAccepted(int id, int portUDP, AbstractAudioFormatWithMic format, CommonCryptoHelper helper, String name, int sizeUDP) {
        return new ClientStorage(true, false, true, true, id, portUDP, name, helper, format, sizeUDP);
    }


    private static CommonStorage createServerNetworkFailure() {
        return new CommonStorage(false, true, false, false, -1, -1, null, null);
    }

    private static CommonStorage createServerAudioNotAccepted() {
        return new CommonStorage(false, false, false, false, -1, -1, null, null);
    }

    private static CommonStorage createServerAudioAcceptedPlain(int id, int portUDP, String name) {
        return new CommonStorage(true, false, false, false, id, portUDP, name, null);
    }

    private static CommonStorage createServerAudioAcceptedCipherNotAccepted() {
        return new CommonStorage(true, false, true, false, -1, -1, null, null);
    }

    private static CommonStorage createServerAudioAcceptedCipherAccepted(int id, int portUDP, String name, CommonCryptoHelper helper) {
        return new CommonStorage(true, false, true, true, id, portUDP, name, helper);
    }

    /**
     * Contain common flags
     * Could be in 3 states
     * 1 - Network failure where {@link #isNetworkFailure} false
     * 2 - Audio not accepted where {@link #isAudioFormatAccepted} false
     * 3 - Connected where all flags are properly initialised
     */

    public static class CommonStorage {

        public final boolean isAudioFormatAccepted;
        public final boolean isNetworkFailure;
        public final boolean isSecureConnection;
        public final boolean isSecureConnectionAccepted;

        public final int myID;
        public final int portUDP;

        public final String name;
        public final CommonCryptoHelper cryptoHelper;


        public CommonStorage(boolean isAudioFormatAccepted, boolean isNetworkFailure, boolean isSecureConnection, boolean isSecureConnectionAccepted, int myID, int portUDP, String name, CommonCryptoHelper cryptoHelper) {
            this.isAudioFormatAccepted = isAudioFormatAccepted;
            this.isNetworkFailure = isNetworkFailure;
            this.isSecureConnection = isSecureConnection;
            this.isSecureConnectionAccepted = isSecureConnectionAccepted;
            this.myID = myID;
            this.portUDP = portUDP;
            this.name = name;
            this.cryptoHelper = cryptoHelper;
        }
    }

    public static class ClientStorage extends CommonStorage {


        public final AbstractAudioFormatWithMic audioFormat;
        public final int sizeUDP;

        public ClientStorage(boolean isAudioFormatAccepted, boolean isNetworkFailure, boolean isSecureConnection, boolean isSecureConnectionAccepted, int myID, int portUDP, String name, CommonCryptoHelper cryptoHelper, AbstractAudioFormatWithMic audioFormat, int sizeUDP) {
            super(isAudioFormatAccepted, isNetworkFailure, isSecureConnection, isSecureConnectionAccepted, myID, portUDP, name, cryptoHelper);
            this.audioFormat = audioFormat;
            this.sizeUDP = sizeUDP;
        }
    }

}
