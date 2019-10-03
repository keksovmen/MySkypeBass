package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.Protocol.AbstractDataPackagePool;

/**
 * Server implementation of the processor
 * Not uses an Executor like ClientResponder version
 */

public class ServerProcessor extends BaseProcessor {

    public ServerProcessor() {
        super();
    }

//    @Override
    public void process(AbstractDataPackage dataPackage) {
        if (dataPackage != null) {
            listeners.forEach(abstractDataPackageConsumer -> abstractDataPackageConsumer.accept(dataPackage));
            AbstractDataPackagePool.returnPackage(dataPackage);
        }
    }
}
