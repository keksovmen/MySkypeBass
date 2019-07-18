package Bin.Networking.Writers;

import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;

/**
 * Base writer that only can write AbstractDataPackage or its children
 */

public abstract class BaseWriter {

    /**
     * Where to write
     */

    final DataOutputStream outputStream;

    /**
     * You can use only write() method
     *
     * @param outputStream where to write
     */

    BaseWriter(OutputStream outputStream) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream));
    }

    /**
     * Thread safe method writes given package
     *
     * @param dataPackage to be written
     * @throws IOException if network failing occurs
     */

    synchronized void write(AbstractDataPackage dataPackage) throws IOException {
        outputStream.write(dataPackage.getHeader().getRawHeader());// cashed in other implementation @see serverWriter
        if (dataPackage.getHeader().getLength() != 0) {
            outputStream.write(dataPackage.getData());
        }
        outputStream.flush();
//        System.out.println(dataPackage + " " + Thread.currentThread().getName());
        AbstractDataPackagePool.returnPackage(dataPackage);
    }

}
