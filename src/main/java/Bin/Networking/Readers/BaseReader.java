package Bin.Networking.Readers;

import Bin.Networking.Processors.Processable;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Protocol.AbstractHeader;
import Bin.Networking.Server;
import Bin.Networking.Utility.ErrorHandler;
import Bin.Networking.Utility.Starting;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base reader for all the readers
 */

public class BaseReader implements Starting, ErrorHandler {

    /**
     * Define buffer size of inputStream
     */

    private final static int BUFFER_SIZE;

    static {
        Integer size = Integer.valueOf(Server.serverProp.getProperty("bufferSize"));
        BUFFER_SIZE = size * 1024;
    }

    /**
     * DataInputStream because it can readFully()
     */

    final DataInputStream inputStream;

    /**
     * Define live of the thread
     */

    volatile boolean work;

    /**
     * Just put here your dataPackages
     * It must handle them
     */

    final Processable handler;

    /**
     * Using this constructor allows usage only of read() method
     *
     * @param inputStream which you will read
     */

    public BaseReader(InputStream inputStream, Processable handler) {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream, BUFFER_SIZE));
        this.handler = handler;
        work = true;
    }

    /**
     * Base read method
     * Firstly read INITIAL_SIZE of the package
     * then define is there any length
     * and if so read body of the package
     *
     * @return package with at least header info
     * @throws IOException if networking fails
     */

    public AbstractDataPackage read() throws IOException {
        AbstractDataPackage aPackage = AbstractDataPackagePool.getPackage();

        byte[] header = new byte[AbstractHeader.getInitialSize()];
        inputStream.readFully(header);
        aPackage.getHeader().init(header);

        int length = aPackage.getHeader().getLength();
        if (length == 0) {
            return aPackage;
        }

        byte[] body = new byte[length];
        inputStream.readFully(body);
        aPackage.setData(body);

        return aPackage;
    }

    /**
     * Your main action in thread
     * must contain on of the read methods
     * and then do something with the data
     * <p>
     * Problem! it doesn't throw an Exception
     * and if you use read() instead
     * it will be the only place where you can handle it
     */

    void process() throws IOException {
        handler.process(read());
    }

    /**
     * STARTS NEW THREAD
     * That will handle reading
     *
     * @param threadName name for debugging
     */

    @Override
    public void start(String threadName) {
        new Thread(() -> {
            while (work) {
                try {
                    process();
                } catch (IOException e) {
                    e.printStackTrace();
                    errorCase();
                }
            }
        }, threadName).start();
    }

    /**
     * Eliminate the thread
     */

    @Override
    public void close() {
        work = false;
    }

    /**
     * Basically strops the thread
     */

    @Override
    public void errorCase() {
        close();
        iterate();
    }

    @Override
    public ErrorHandler[] getNext() {
        return null;
    }
}
