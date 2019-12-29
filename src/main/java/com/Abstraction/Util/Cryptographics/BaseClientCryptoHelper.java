package com.Abstraction.Util.Cryptographics;

import javax.crypto.KeyAgreement;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class BaseClientCryptoHelper extends BaseCommonCryptoHelper implements ClientCryptoHelper {

    @Override
    public void initialiseKeyGenerator() {
        try {
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(Crypto.STANDARD_KEY_EXCHANGE_ALGORITHM);
            keyPairGenerator.initialize(1024);
            keyPair = keyPairGenerator.generateKeyPair();
            keyAgreement = KeyAgreement.getInstance(Crypto.STANDARD_KEY_EXCHANGE_ALGORITHM);
            keyAgreement.init(keyPair.getPrivate());
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //Won't be thrown because every java platform must implement it DiffieHellman (1024)
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            //won't be thrown because I am using not magic constants but single static final field
        }
    }

    @Override
    public void finishExchange(byte[] serverPublicKeyEncoded) {
        try {
            KeyFactory keyFactory = KeyFactory.getInstance(Crypto.STANDARD_KEY_EXCHANGE_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(serverPublicKeyEncoded);
            PublicKey serverPublicKey = keyFactory.generatePublic(keySpec);
            keyAgreement.doPhase(serverPublicKey, true);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
            //only if server fucked up
        } catch (InvalidKeyException e) {
            e.printStackTrace();
            //only if server fucked up
        }
    }

    @Override
    public void setAlgorithmParametersEncoded(byte[] parametersEncoded) {
        try {
            algorithmParameters = AlgorithmParameters.getInstance(Crypto.STANDARD_CIPHEER_ALGORITM);
            algorithmParameters.init(parametersEncoded);
            keyForAlgorithm = new SecretKeySpec(keyAgreement.generateSecret(), 0, 16, Crypto.STANDARD_CIPHEER_ALGORITM);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //won't happen cause java must implement it, that is what api says
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


}
