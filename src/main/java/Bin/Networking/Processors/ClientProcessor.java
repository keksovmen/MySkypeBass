package Bin.Networking.Processors;

import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

/**
 * Main purpose is to have registered listeners
 * and feed them with dataPackages
 * Also return to home the packages
 */

public class ClientProcessor extends BaseProcessor{

    /**
     * Instead of its own thread you have
     * SINGLE THREAD EXECUTOR for not prone purposes
     */

    private final Executor executor;

    public ClientProcessor() {
        super();
        executor = Executors.newSingleThreadExecutor();
    }

    /**
     * Call when you have a package to act on it
     * Simply put in executor queue
     *
     * @param dataPackage valid package
     */

    @Override
    public void process(AbstractDataPackage dataPackage) {
        if (dataPackage == null) {
            return;
        }
        executor.execute(() -> {
            listeners.forEach(baseDataPackageConsumer -> baseDataPackageConsumer.accept(dataPackage));
            AbstractDataPackagePool.returnPackage(dataPackage);
        });
    }
}
