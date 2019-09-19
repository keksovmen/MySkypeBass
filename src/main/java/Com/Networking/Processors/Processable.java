package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;

public interface Processable {

    boolean process(AbstractDataPackage dataPackage);
}
