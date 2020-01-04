package com.Abstraction.Util.Cryptographics;

/**
 * Work like
 * You call {@link #initialiseKeyGenerator()}
 * You send to a server {@link CommonCryptoHelper#getPublicKeyEncoded()}
 * You call {@link #finishExchange(byte[])} with data from server {@link ServerCryptoHelper#getPublicKeyEncoded()}
 * You call {@link #setAlgorithmParametersEncoded(byte[])} with data from server {@link ServerCryptoHelper#getAlgorithmParametersEncoded()}
 * That's it now you can call for key and parameters
 */

public interface ClientCryptoHelper extends CommonCryptoHelper {

    void initialiseKeyGenerator();

    void finishExchange(byte[] serverPublicKeyEncoded);

    void setAlgorithmParametersEncoded(byte[] parametersEncoded);

}
