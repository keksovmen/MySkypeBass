package com.Abstraction.Networking;

import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Networking.Readers.BaseReader;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ClientController extends BaseController {

    private final ExecutorService executorService;


    public ClientController(BaseReader reader) {
        super(reader);
        executorService = Executors.newSingleThreadExecutor();
    }

    @Override
    public boolean handleRequest(Processable processor) throws IOException {
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
