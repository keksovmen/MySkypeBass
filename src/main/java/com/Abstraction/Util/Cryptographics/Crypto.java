package com.Abstraction.Util.Cryptographics;

import javax.crypto.Cipher;
import javax.crypto.NoSuchPaddingException;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

public class Crypto {

    public static final String STANDARD_KEY_EXCHANGE_ALGORITHM = "DH";
    public static final String STANDARD_CIPHEER_ALGORITM = "AES";
    public static final String STANDARD_CIPHER_FORMAT = "AES/CBC/PKCS5Padding";

    private static final List<String> formats = new ArrayList<>();

    private Crypto() {
    }

    /**
     *
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

}
