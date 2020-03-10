package com.Abstraction.Networking.Writers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.ProtocolBitMap;

import java.io.BufferedOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.net.InetAddress;

/**
 * Base writer that only can write AbstractDataPackage or its children
 * Thread safe can call write methods then will be sent sequentially
 */

public class PlainWriter implements Writer {

    /**
     * Where to write
     */

    protected final DataOutputStream outputStream;
    protected final DatagramSocket socket;

    /**
     * For only TCP protocol
     *
     * @param outputStream opened from socket
     * @param bufferSize stream buffer size
     */

    public PlainWriter(OutputStream outputStream, int bufferSize) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream, bufferSize));
        socket = null;
    }

    /**
     * For both TCP and UDP
     *
     * @param outputStream where to write
     * @param bufferSize   for TCP stream
     * @param socket       could be null if you don't want to use UDP protocol
     */


    public PlainWriter(OutputStream outputStream, int bufferSize, DatagramSocket socket) {
        this.outputStream = new DataOutputStream(new BufferedOutputStream(outputStream, bufferSize));
        this.socket = socket;
    }

    /**
     * Thread safe method writes given package
     *
     * @param dataPackage to be written
     * @throws IOException if network failing occurs
     */

    @Override
    public synchronized void write(AbstractDataPackage dataPackage) throws IOException {
        writeWithoutReturnToPool(dataPackage);
        AbstractDataPackagePool.returnPackage(dataPackage);
    }

    /**
     * Same as above but doesn't return data package in to pool
     * for cash purposes
     *
     * @param dataPackage to write
     * @throws IOException if network fails
     */

    @Override
    public synchronized void writeWithoutReturnToPool(AbstractDataPackage dataPackage) throws IOException {
        outputStream.write(dataPackage.getHeader().getRawHeader());// cashed in other implementation @see serverWriter
        if (dataPackage.getHeader().getLength() != 0) {
            outputStream.write(dataPackage.getData());
        }
        outputStream.flush();
    }

    @Override
    public synchronized void writeUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        if (socket == null)
            throw new RuntimeException("This writer is only for TCP, datagram socket is null");
        writeWithoutReturnToPoolUDP(dataPackage, address, port);
        AbstractDataPackagePool.returnPackage(dataPackage);
    }

    @Override
    public synchronized void writeWithoutReturnToPoolUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        if (socket == null)
            throw new RuntimeException("This writer is only for TCP, datagram socket is null");
        DatagramPacket datagramPacket = fillPacket(dataPackage);
        datagramPacket.setAddress(address);
        datagramPacket.setPort(port);
        socket.send(datagramPacket);
    }

    private DatagramPacket fillPacket(AbstractDataPackage dataPackage) {
        final int packetSize = ProtocolBitMap.PACKET_SIZE;
        final int length = dataPackage.getHeader().getLength();
        byte[] tmp = new byte[packetSize + length];
        System.arraycopy(dataPackage.getHeader().getRawHeader(), 0, tmp, 0, packetSize);
        System.arraycopy(dataPackage.getData(), 0, tmp, packetSize, length);
        return new DatagramPacket(tmp, 0, tmp.length);
    }
}
