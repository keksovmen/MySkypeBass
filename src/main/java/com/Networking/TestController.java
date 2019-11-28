package com.Networking;

import com.Networking.Processors.Processable;
import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Readers.BaseReader;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TestController extends BaseController {

    private final ExecutorService executorService;


    public TestController(BaseReader reader) {
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
