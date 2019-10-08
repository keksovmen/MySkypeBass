package Com.Networking.Processors;

import Com.Networking.Protocol.AbstractDataPackage;

/**
 * Handle Abstract Data Package instance in any way possible
 */

public interface Processable {

    boolean process(AbstractDataPackage dataPackage);
}
