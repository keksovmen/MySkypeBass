package com.Abstraction.Util.Cryptographics;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyAgreement;
import javax.crypto.interfaces.DHPublicKey;
import javax.crypto.spec.DHParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.*;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.X509EncodedKeySpec;

public class BaseServerCryptoHelper extends BaseCommonCryptoHelper implements ServerCryptoHelper {

    @Override
    public void initialiseKeyGenerator(byte[] clientEncodedPublicKey) {
        KeyFactory keyFactory = null;
        try {
            keyFactory = KeyFactory.getInstance(Crypto.STANDARD_KEY_EXCHANGE_ALGORITHM);
            X509EncodedKeySpec keySpec = new X509EncodedKeySpec(clientEncodedPublicKey);

            PublicKey clientPublicKey = keyFactory.generatePublic(keySpec);
            DHParameterSpec params = ((DHPublicKey) clientPublicKey).getParams();

            //Server creates his own key pair
            KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(Crypto.STANDARD_KEY_EXCHANGE_ALGORITHM);
            keyPairGenerator.initialize(params);
            keyPair = keyPairGenerator.generateKeyPair();

            //Server init his Agreement object
            keyAgreement = KeyAgreement.getInstance(Crypto.STANDARD_KEY_EXCHANGE_ALGORITHM);
            keyAgreement.init(keyPair.getPrivate());
            keyAgreement.doPhase(clientPublicKey, true);

            keyForAlgorithm = new SecretKeySpec(keyAgreement.generateSecret(), 0, 16, Crypto.STANDARD_CIPHEER_ALGORITM);
            Cipher cipher = Crypto.getCipherWithoutExceptions(Crypto.STANDARD_CIPHER_FORMAT);
            cipher.init(Cipher.ENCRYPT_MODE, keyForAlgorithm);
            cipher.doFinal(new byte[cipher.getBlockSize()]);

            algorithmParameters = cipher.getParameters();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeySpecException e) {
            e.printStackTrace();
        }
    }

//    @Override
//    public void fnishExchange() {
//
//    }

    @Override
    public byte[] getAlgorithmParametersEncoded() {
        try {
            return algorithmParameters.getEncoded();
        } catch (IOException e) {
            e.printStackTrace();
            //won't happen cause initialised
        }
        return null;
    }

}
