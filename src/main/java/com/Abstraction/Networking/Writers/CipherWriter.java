package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Util.Cryptographics.Crypto;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.ShortBufferException;
import java.io.IOException;
import java.net.InetAddress;
import java.nio.ByteBuffer;
import java.security.AlgorithmParameters;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.Key;

/**
 * In each method encode data with AES
 * <p>
 * Basically only 1 thread can write at same time with others
 * So why not make permanent byte buffer for cipher data
 * Problem is in its size, need to be dynamically expendable
 *
 * Decorator pattern
 */

public class CipherWriter implements Writer {

    private static final int BUFFER_SIZE_MULTIPLIER = 2;

    /**
     * Underlying writer
     * To which calls are delegated
     */

    private final Writer writer;

    /**
     * Work only in ENCRYPT_MODE
     */

    private final Cipher encoder;


    /**
     * Dynamically changes its size, through it's allocate static method
     * Lazy initialisation
     * Approximate max size is {@link com.Abstraction.Networking.Protocol.ProtocolBitMap#MAX_VALUE} * {@link #BUFFER_SIZE_MULTIPLIER}
     */

    private ByteBuffer cipherBuffer;


    public CipherWriter(Writer writer, Key key, AlgorithmParameters parameters) {
        this.writer = writer;

        encoder = Crypto.getCipherWithoutExceptions(Crypto.STANDARD_CIPHER_FORMAT);
        try {
            encoder.init(Cipher.ENCRYPT_MODE, key, parameters);
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        }
    }

    @Override
    public synchronized void write(AbstractDataPackage dataPackage) throws IOException {
        writer.write(encryptGivenPackage(dataPackage));
    }

    @Override
    public synchronized void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        writer.writeWithoutReturnToPool(encryptGivenPackage(dataPackage));
    }


    @Override
    public void writeUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        writer.writeUDP(encryptGivenPackage(dataPackage), address, port);
    }


    @Override
    public void writeWithoutReturnToPoolUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        writer.writeWithoutReturnToPoolUDP(encryptGivenPackage(dataPackage), address, port);
    }

    protected AbstractDataPackage encryptGivenPackage(AbstractDataPackage dataPackage) {
        if (!checkPackageForEncoding(dataPackage))
            return dataPackage;

        ByteBuffer plaintData = ByteBuffer.wrap(dataPackage.getData());
        int size = encode(plaintData);
        byte[] data = new byte[size];
        cipherBuffer.flip();
        cipherBuffer.get(data);
        dataPackage.setData(data);
        return dataPackage;
    }

    protected boolean checkPackageForEncoding(AbstractDataPackage dataPackage) {
        return dataPackage.getHeader().getLength() != 0;
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

}
