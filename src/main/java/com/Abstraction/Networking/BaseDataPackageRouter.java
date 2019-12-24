package com.Abstraction.Networking;

import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Networking.Readers.BaseReader;

import java.io.IOException;

/**
 * Reads data packages and route them to given Processor
 */

public class BaseDataPackageRouter {

    /**
     * From which will fetch Data Packages
     */

    protected final BaseReader reader;


    public BaseDataPackageRouter(BaseReader reader) {
        this.reader = reader;
    }

    /**
     * Action that will happen each time in a loop
     * until process() return false
     *
     * @param processor consumes packages
     * @return false if processor can't handle given package
     * @throws IOException if network fails
     */

    public boolean handleDataPackageRouting(Processable processor) throws IOException {
        AbstractDataPackage read = reader.read();
        if (!processor.process(read)) {
            DataPackagePool.returnPackage(read);
            return false;
        }
        DataPackagePool.returnPackage(read);
        return true;
    }
}
