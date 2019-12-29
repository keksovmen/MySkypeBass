package com.Abstraction.Util.Cryptographics;

public interface ClientCryptoHelper extends CommonCryptoHelper {

    void initialiseKeyGenerator();

    void finishExchange(byte[] serverPublicKeyEncoded);

    void setAlgorithmParametersEncoded(byte[] parametersEncoded);

}
