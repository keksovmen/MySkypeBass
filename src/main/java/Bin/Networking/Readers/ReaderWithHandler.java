package Bin.Networking.Readers;

import Bin.Networking.Processors.Processable;
import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Utility.ErrorHandler;

import java.io.IOException;
import java.io.InputStream;

public class ReaderWithHandler extends BaseReader {

    /**
     * If you use second constructor you can
     * read data without throwing exception
     * because if there will be it will handle
     * shutdown of the system
     */

    final ErrorHandler mainErrorHandler;

    /**
     * Allow usage of both read() with or without exception methods
     *
     * @param inputStream      which you will read
     * @param mainErrorHandler handler calls when exception in networking occurs
     */

    public ReaderWithHandler(InputStream inputStream, Processable handler, ErrorHandler mainErrorHandler) {
        super(inputStream, handler);
        this.mainErrorHandler = mainErrorHandler;
    }

    /**
     * Improved version that can immediately handle exception
     *
     * @return package with at least header info
     */

    public AbstractDataPackage readAndHandle() {
        try {
            return read();
        } catch (IOException e) {
            e.printStackTrace();
            if (work) {
                mainErrorHandler.errorCase();
            }
            return null;
        }
    }

    /**
     * If ruins read action will call main handler to deal with it
     */

    @Override
    void process() {
        handler.process(readAndHandle());
    }

    @Override
    public void start(String threadName) {
        new Thread(() -> {
            while (work) {
                process();
            }
        }, threadName).start();
    }
}
