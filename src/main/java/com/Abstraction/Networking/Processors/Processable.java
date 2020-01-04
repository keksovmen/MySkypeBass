package com.Abstraction.Networking.Processors;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;

/**
 * Handle Abstract Data Package instance in any possible way
 */

public interface Processable {

    boolean process(AbstractDataPackage dataPackage);

    void close();
}
