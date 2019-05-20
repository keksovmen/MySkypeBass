package Bin.Networking.Readers;

import Bin.Networking.DataParser.Package.BaseDataPackage;
import Bin.Networking.DataParser.Package.DataPackageHeader;
import Bin.Networking.DataParser.Package.DataPackagePool;
import Bin.Networking.Processors.Processor;
import Bin.Networking.Startable;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

public abstract class BaseReader implements Processor, Startable {

    private final static int BUFFER_SIZE = 16384;

    protected DataInputStream inputStream;
    protected boolean work;

    public BaseReader(InputStream inputStream) {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE));
        work = true;
    }

    protected BaseDataPackage read() throws IOException {
        BaseDataPackage aPackage = DataPackagePool.getPackage();

        byte[] header = new byte[DataPackageHeader.INITIAL_SIZE];
        inputStream.readFully(header);
        aPackage.getHeader().init(header);

        int length = aPackage.getHeader().getLength();
        if (length == 0) return aPackage;

        byte[] body = new byte[length];
        inputStream.readFully(body);
        aPackage.setData(body);
        return aPackage;
    }

    @Override
    public void start() {
        new Thread(() -> {
            while (work){
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                    work = false;
                }
            }
        }).start();
    }

    @Override
    public void close() {
        work = false;
    }

    //    public abstract void createServerSocket();


}
