package com.Abstraction.Util.Cryptographics;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.KeyPair;

public class BaseCommonCryptoHelper implements CommonCryptoHelper{

    protected KeyPair keyPair;
    protected KeyAgreement keyAgreement;

    protected SecretKeySpec keyForAlgorithm;
    protected AlgorithmParameters algorithmParameters;


    @Override
    public byte[] getPublicKeyEncoded() {
        return keyPair.getPublic().getEncoded();
    }

    @Override
    public AlgorithmParameters getParameters() {
        if (algorithmParameters == null)
            throw new IllegalStateException("Wrong steps, algorithm parameters are null! " +
                    "Steps must be initialiseKeyGenerator() -> getPublicKeyEncoded() -> " +
                    "finishExchange(byte[]) -> setAlgorithmParametersEncoded(byte[]) -> " +
                    "getParameters()");
        return algorithmParameters;
    }

    @Override
    public SecretKeySpec getKey() {
        if (keyForAlgorithm == null)
            throw new IllegalStateException("Wrong steps, key is null! " +
                    "Steps must be initialiseKeyGenerator() -> getPublicKeyEncoded() -> " +
                    "finishExchange(byte[]) -> setAlgorithmParametersEncoded(byte[]) -> " +
                    "getParameters()");
        return keyForAlgorithm;
    }
}
