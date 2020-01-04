package com.Abstraction.Util.Cryptographics;


/**
 * Work like
 * Client send you his {@link CommonCryptoHelper#getPublicKeyEncoded()}
 * You call {@link #initialiseKeyGenerator(byte[])}
 * Then you send him {@link #getAlgorithmParametersEncoded()}
 * That's it now you can call for key and parameters
 */

public interface ServerCryptoHelper extends CommonCryptoHelper {

    void initialiseKeyGenerator(byte[] clientEncodedPublicKey);

    byte[] getAlgorithmParametersEncoded();

}
