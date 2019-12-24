package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base writer that only can write AbstractDataPackage or its children
 * Thread safe can call write methods then will be sent sequentially
 */

public class BaseWriter implements Writer {

    /**
     * Where to write
     */

    protected final DataOutputStream outputStream;


    public BaseWriter(OutputStream outputStream, int bufferSize) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream, bufferSize));
    }

    /**
     * Thread safe method writes given package
     *
     * @param dataPackage to be written
     * @throws IOException if network failing occurs
     */

    @Override
    public synchronized void write(AbstractDataPackage dataPackage) throws IOException {
        writeWithoutReturnToPool(dataPackage);
        AbstractDataPackagePool.returnPackage(dataPackage);
    }

    /**
     * Same as above but doesn't return data package in to pool
     * for cash purposes
     *
     * @param dataPackage to write
     * @throws IOException if network fails
     */

    @Override
    public synchronized void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        outputStream.write(dataPackage.getHeader().getRawHeader());// cashed in other implementation @see serverWriter
        if (dataPackage.getHeader().getLength() != 0) {
            outputStream.write(dataPackage.getData());
        }
        outputStream.flush();
    }

}
