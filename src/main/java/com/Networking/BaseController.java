package com.Networking;

import com.Networking.Processors.Processable;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Readers.BaseReader;

import java.io.IOException;

/**
 * Template method
 * Start() is main method
 */

public class BaseController {

    protected final BaseReader reader;

    public BaseController(BaseReader reader) {
        this.reader = reader;
    }

    /**
     * Action that will happen each time in a loop
     * until close() or error or process() return false
     *
     * @throws IOException if network fails
     */

    public boolean handleRequest(Processable processor) throws IOException {
        AbstractDataPackage read = reader.read();
        if (!processor.process(read)) {
            DataPackagePool.returnPackage(read);
            return false;
        }
        DataPackagePool.returnPackage(read);
        return true;
    }
}
