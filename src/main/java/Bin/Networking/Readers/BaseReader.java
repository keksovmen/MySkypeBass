package Bin.Networking.Readers;

import Bin.Networking.DataParser.Package.BaseDataPackage;
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
        int length = inputStream.readInt();
//        System.out.println("lemgth = " + length);
        byte[] data = new byte[length];

        inputStream.readFully(data, 0, length);
        return BaseDataPackage.getObject().init(data);
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
