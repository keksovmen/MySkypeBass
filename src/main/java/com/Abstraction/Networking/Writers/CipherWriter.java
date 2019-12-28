package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Utility.Users.BaseUser;

import javax.crypto.*;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.function.Function;

/**
 * In each method encode data with AES
 * <p>
 * Basically only 1 thread can write at same time with others
 * So why not make permanent byte buffer for cipher data
 * Problem is in its size, need to be dynamically expendable
 */

public class CipherWriter extends PlainWriter {

    private static final int BUFFER_SIZE_MULTIPLIER = 2;

    /**
     * Work only in ENCRYPT_MODE
     */

    private final Cipher encoder;

    /**
     * Command, could return null, Integer is {@link BaseUser#getId()}
     */

    private final Function<Integer, BaseUser> userFetcher;

    /**
     * Dynamically changes its size, through it's allocate static method
     * Lazy initialisation
     * Approximate max size is {@link com.Abstraction.Networking.Protocol.ProtocolBitMap#MAX_VALUE} * {@link #BUFFER_SIZE_MULTIPLIER}
     */

    private ByteBuffer cipherBuffer;


    public CipherWriter(OutputStream outputStream, int bufferSize, Function<Integer, BaseUser> userFetcher) throws NoSuchPaddingException, NoSuchAlgorithmException {
        super(outputStream, bufferSize);
        this.userFetcher = userFetcher;

        encoder = Cipher.getInstance("AES/CBC/PKCS5Padding");
//        encoder.init(Cipher.ENCRYPT_MODE, sharedKey, parameterSpec);
    }


    @Override
    public synchronized void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        super.writeWithoutReturnToPool(encryptGivenPackage(dataPackage));
    }

    protected AbstractDataPackage encryptGivenPackage(AbstractDataPackage dataPackage) {
        if (dataPackage.getData().length == 0)
            return dataPackage;

        ByteBuffer plaintData = ByteBuffer.wrap(dataPackage.getData());
        if (!initCipher(dataPackage.getHeader().getTo())){
            /*
            Handle missing dude in underlying model
            Maybe just ignore message sending
            Or send it anyway, dude probably disconnected from server, won't do much harm
            */
            return dataPackage;
        }
        int size = encode(plaintData);
        byte[] data = new byte[size];
        cipherBuffer.flip();
        cipherBuffer.get(data);
        dataPackage.setData(data);

        return dataPackage;
    }

    private int encode(ByteBuffer input) {
        validateBuffer(input);
        try {
            return encoder.doFinal(input, cipherBuffer);
        } catch (ShortBufferException | IllegalBlockSizeException e) {
            //Should not drop any of this exception because of {@link #validateBuffer(ByteBuffer)} method in head
            e.printStackTrace();
        } catch (BadPaddingException e) {
            //Seems like it will never be thrown with ENCODE_MODE of Cipher only for DECODE
            e.printStackTrace();
        }
        return -1;//just to indicate something fucking wrong in my life
    }

    private void validateBuffer(ByteBuffer input) {
        if (cipherBuffer == null) {
            cipherBuffer = ByteBuffer.allocate(calculateMinCapacity(input));
        } else {
            increaseBufferSize(input);
        }
        cipherBuffer.clear();
    }

    private void increaseBufferSize(ByteBuffer input) {
        int minSize = calculateMinCapacity(input);
        if (cipherBuffer.capacity() < minSize) {
            cipherBuffer = ByteBuffer.allocate(minSize * BUFFER_SIZE_MULTIPLIER);
        }
    }

    private int calculateMinCapacity(ByteBuffer input) {
        return input.capacity() + encoder.getBlockSize();
    }

    private boolean initCipher(int id){
        BaseUser user = userFetcher.apply(id);
        if (user == null){
            //handle not existence of user in underlying model
            return false;
        }
        try {
            encoder.init(Cipher.ENCRYPT_MODE, user.getSharedKey(), user.getAlgorithmParameterSpec());
        } catch (InvalidKeyException | InvalidAlgorithmParameterException e) {
            e.printStackTrace();
            return false;
        }
        return true;
    }
}
