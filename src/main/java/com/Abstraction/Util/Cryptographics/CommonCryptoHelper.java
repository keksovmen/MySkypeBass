package com.Abstraction.Util.Cryptographics;

import java.security.AlgorithmParameters;
import java.security.Key;

public interface CommonCryptoHelper {

    byte[] getPublicKeyEncoded();

    AlgorithmParameters getParameters();

    Key getKey();
}
