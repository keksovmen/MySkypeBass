package com.Abstraction.Util.Cryptographics;

import com.Abstraction.Util.Algorithms;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.SecretKeySpec;
import java.io.IOException;
import java.security.AlgorithmParameters;
import java.security.Key;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Crypto {

    public static final String STANDARD_KEY_EXCHANGE_ALGORITHM = "DH";
    public static final String STANDARD_CIPHER_ALGORITHM = "AES";
    public static final String STANDARD_CIPHER_FORMAT = "AES/CBC/PKCS5Padding";
    public static final int STANDARD_PADDING = 16;

    private static final List<String> formats = new ArrayList<>();

    private Crypto() {
    }

    /**
     * @param cipher algorithm/mode/padding
     * @return true if can create such Cipher
     */

    public static boolean isCipherAcceptable(String cipher) {
        if (formats.contains(cipher)) return true;
        try {
            Cipher.getInstance(cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
            return false;
        }
        formats.add(cipher);
        return true;
    }

    /**
     * Must call when 100% sure you will get such object
     * Better first check {@link #isCipherAcceptable(String)}
     *
     * @param cipher algorithm/mode/padding
     * @return Cipher object but never null
     */

    public static Cipher getCipherWithoutExceptions(String cipher) {
        if (!isCipherAcceptable(cipher))
            throw new IllegalArgumentException("Such format " + cipher + " is not appropriate");
        try {
            return Cipher.getInstance(cipher);
        } catch (NoSuchAlgorithmException | NoSuchPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Create particular key
     *
     * @param key as {@link Key#getEncoded()}
     * @return the key
     */

    public static Key createCipherKey(byte[] key) {
        return new SecretKeySpec(key, STANDARD_CIPHER_ALGORITHM);
    }

    /**
     * @param key as {@link Key#getEncoded()} then {@link Algorithms#byteArrayToString(byte[])}
     * @return the key
     */

    public static Key createCipherKey(String key) {
        return createCipherKey(Algorithms.stringToByteArray(key));
    }

    /**
     * Create particular parameters for my implementation
     *
     * @param initializer as {@link AlgorithmParameters#getEncoded()}
     * @return initialised parameters or null if input is ill formatted
     */

    public static AlgorithmParameters createParameters(byte[] initializer) {
        try {
            AlgorithmParameters instance = AlgorithmParameters.getInstance(STANDARD_CIPHER_ALGORITHM);
            instance.init(initializer);
            return instance;
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            //Won't be thrown because Java specification swear to implement AES algorithm
            throw new RuntimeException("AES algorithm is not implemented on your machine!");
        } catch (IOException e) {
            e.printStackTrace();
            throw new RuntimeException("Input data is wrong! - " + Arrays.toString(initializer));
            //If input data is wrong
        }
    }

    /**
     * @param initializer as {@link AlgorithmParameters#getEncoded()} then {@link Algorithms#byteArrayToString(byte[])}
     * @return initialised parameters
     */

    public static AlgorithmParameters createParameters(String initializer) {
        return createParameters(Algorithms.stringToByteArray(initializer));
    }
}
