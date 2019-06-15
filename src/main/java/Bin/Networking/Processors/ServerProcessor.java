package Bin.Networking.Processors;

import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;

/**
 * Server implementation of the processor
 * Not uses an Executor like Client version
 */

public class ServerProcessor extends BaseProcessor {

    public ServerProcessor() {
        super();
    }

    @Override
    public void process(AbstractDataPackage dataPackage) {
        if (dataPackage != null){
            listeners.forEach(abstractDataPackageConsumer -> abstractDataPackageConsumer.accept(dataPackage));
            AbstractDataPackagePool.returnPackage(dataPackage);
        }
    }
}
