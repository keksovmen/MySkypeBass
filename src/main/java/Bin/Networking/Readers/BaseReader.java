package Bin.Networking.Readers;

import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.DataParser.DataPackageHeader;
import Bin.Networking.DataParser.DataPackagePool;
import Bin.Networking.Processors.Processor;
import Bin.Networking.Startable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

public abstract class BaseReader implements Processor, Startable {

    private final static int BUFFER_SIZE = 16384;

    protected DataInputStream inputStream;
    protected volatile boolean work;

    public BaseReader(InputStream inputStream) {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE));
        work = true;
    }

    public BaseDataPackage read() throws IOException {
        BaseDataPackage aPackage = DataPackagePool.getPackage();

        byte[] header = new byte[DataPackageHeader.INITIAL_SIZE];
//        System.out.println(Thread.currentThread().getName() + " " + inputStream.available());
//        System.out.println(aPackage);
        inputStream.readFully(header);
        aPackage.getHeader().init(header);


        int length = aPackage.getHeader().getLength();
        if (length == 0) return aPackage;

        byte[] body = new byte[length];
        inputStream.readFully(body);
//        System.out.println(Arrays.toString(body));
        aPackage.setData(body);

        return aPackage;
    }

    @Override
    public void close() {
        work = false;
    }


}
