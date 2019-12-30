package com.Abstraction.Networking;

import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Networking.Readers.BaseReader;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Contain executor service for non blocking reader thread with processor work
 */

public class ClientDataPackageRouter extends BaseDataPackageRouter {

    /**
     * May be unnecessary, 'cause context switching take more cycles than
     * average action in {@link com.Abstraction.Networking.Processors.ClientProcessor}
     * But when cryptographic will be added it will be handled by same thread there may be problems
     *
     * Not need to close explicitly cause in any case network exception will be thrown
     */

    private final ExecutorService executorService;


    public ClientDataPackageRouter(BaseReader reader) {
        super(reader);
        executorService = Executors.newSingleThreadExecutor(r -> new Thread(r, "Client processor"));
    }

    /**
     * Action that will happen each time in a loop
     * until process() return false
     *
     * @param processor consumes packages
     * @return false in 1 case when network has failed and executor service is dead
     * @throws IOException if network fails
     */

    @Override
    public boolean handleDataPackageRouting(Processable processor) throws IOException {
        if (executorService.isShutdown())
            return false;
        try {
            AbstractDataPackage read = reader.read();
            executorService.execute(() -> {
                if (!processor.process(read)) {
                    executorService.shutdown();
                }
                DataPackagePool.returnPackage(read);
            });
        } catch (IOException e) {
            executorService.shutdown();
            throw e;
        }
        return true;
    }
}
