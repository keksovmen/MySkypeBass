package Com.Networking.Readers;

import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;
import Com.Networking.Protocol.ProtocolBitMap;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Base reader for all the readers
 */

public class BaseReader {

//    /**
//     * Define buffer size of inputStream
//     */

//    private final int BUFFER_SIZE;
//    private final static int BUFFER_SIZE;

//    static {
//        Integer size = Integer.valueOf(Server.serverProp.getProperty("bufferSize"));
//        BUFFER_SIZE = size * 1024;
//    }

    /**
     * DataInputStream because it can readFully()
     */

    final DataInputStream inputStream;

    /**
     * Using this constructor allows usage only of read() method
     *
     * @param inputStream which you will read
     */

    public BaseReader(InputStream inputStream, int bufferSize) {
        this.inputStream = new DataInputStream(new BufferedInputStream(inputStream, bufferSize));
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

        byte[] header = new byte[ProtocolBitMap.PACKET_SIZE];
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

//    /**
//     * Your main action in thread
//     * must contain on of the read methods
//     * and then do something with the data
//     * <p>
//     * Problem! it doesn't throw an Exception
//     * and if you use read() instead
//     * it will be the only place where you can handle it
//     */
//
//    void process() throws IOException {
//        handler.process(read());
//    }

//    /**
//     * STARTS NEW THREAD
//     * That will handle reading
//     *
//     * @param threadName name for debugging
//     */
//
//    @Override
//    public void start(String threadName) {
//        new Thread(() -> {
//            while (work) {
//                try {
//                    process();
//                } catch (IOException e) {
//                    e.printStackTrace();
//                    errorCase();
//                }
//            }
//        }, threadName).start();
//    }

}
