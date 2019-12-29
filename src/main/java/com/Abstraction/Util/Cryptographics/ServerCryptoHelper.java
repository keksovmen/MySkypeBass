package com.Abstraction.Util.Cryptographics;

public interface ServerCryptoHelper extends CommonCryptoHelper {

    void initialiseKeyGenerator(byte[] clientEncodedPublicKey);

//    void fnishExchange();

    byte[] getAlgorithmParametersEncoded();

}
