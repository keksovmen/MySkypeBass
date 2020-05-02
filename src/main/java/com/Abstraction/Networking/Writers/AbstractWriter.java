package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;
import com.Abstraction.Util.Monitors.SpeedMonitor;

import java.io.IOException;

/**
 * Children must provide more specific methods to write
 * Higher level of network writer
 * <p>
 * Part of Bridge pattern it's abstraction
 */

public abstract class AbstractWriter {

    protected final BaseLogger logger;

    /**
     * Handles low level network writings
     */

    protected final Writer bridgeImplementation;

    /**
     * Might be null don't relay on it
     * Helps in optimising network transmissions
     */

    protected SpeedMonitor speedMonitor;


    public AbstractWriter(Writer bridgeImplementation) {
        this.bridgeImplementation = bridgeImplementation;
        logger = createLogger();
    }


    public void setSpeedMonitor(SpeedMonitor speedMonitor) {
        this.speedMonitor = speedMonitor;
    }

    /**
     * Short cut for children
     *
     * @param dataPackage not null
     * @throws IOException if network fails
     */

    protected void writeTCP(AbstractDataPackage dataPackage) throws IOException {
        bridgeImplementation.write(dataPackage);
    }

    /**
     * Factory method
     *
     * @return not null particular logger
     */

    protected abstract BaseLogger createLogger();


}
