package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;

import java.io.IOException;
import java.net.InetAddress;

/**
 * Represents basic writer, needed for bridge pattern implementation
 */

public interface Writer {

    /**
     * Thread safe method writes given package
     *
     * @param dataPackage to be written
     * @throws IOException if network failing occurs
     */

    void write(AbstractDataPackage dataPackage) throws IOException;

    /**
     * Same as above but doesn't return data package in to the pool
     * for cash purposes
     *
     * @param dataPackage to write
     * @throws IOException if network fails
     */

    void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException;

    void writeUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException;

    void writeWithoutReturnToPoolUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException;

}
