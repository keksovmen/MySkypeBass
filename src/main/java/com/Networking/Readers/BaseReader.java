package com.Networking.Readers;

import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.ProtocolBitMap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base reader for all the readers
 */

public class BaseReader {

    /**
     * DataInputStream because it can readFully()
     */

    final DataInputStream inputStream;


    public BaseReader(InputStream inputStream, int bufferSize) {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream, bufferSize));
    }

    /**
     * Base read method
     * Firstly read header of the package
     * then define is there any length
     * and if so read body of the package
     *
     * @return package with at least header info
     * @throws IOException if networking fails
     */

    public AbstractDataPackage read() throws IOException {
        AbstractDataPackage aPackage = AbstractDataPackagePool.getPackage();

        readHeader(aPackage);

        if (aPackage.getHeader().getLength() == 0)
            return aPackage;

        readBody(aPackage);

        return aPackage;
    }

    private void readHeader(AbstractDataPackage dataPackage) throws IOException {
        byte[] header = new byte[ProtocolBitMap.PACKET_SIZE];
        inputStream.readFully(header);
        dataPackage.getHeader().init(header);
    }

    private void readBody(AbstractDataPackage dataPackage) throws IOException {
        byte[] body = new byte[dataPackage.getHeader().getLength()];
        inputStream.readFully(body);
        dataPackage.setData(body);
    }

}
