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
import java.net.InetSocketAddress;
import java.util.Arrays;

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
    public synchronized void writeUDP(AbstractDataPackage dataPackage) throws IOException {
        writeWithoutReturnToPoolUDP(dataPackage);
        AbstractDataPackagePool.returnPackage(dataPackage);

    }

    @Override
    public synchronized void writeUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        writeWithoutReturnToPoolUDP(dataPackage, address, port);
        AbstractDataPackagePool.returnPackage(dataPackage);
    }

    @Override
    public synchronized void writeWithoutReturnToPoolUDP(AbstractDataPackage dataPackage) throws IOException {
        socket.send(fillPacket(dataPackage));
    }

    @Override
    public synchronized void writeWithoutReturnToPoolUDP(AbstractDataPackage dataPackage, InetAddress address, int port) throws IOException {
        DatagramPacket datagramPacket = fillPacket(dataPackage);
        datagramPacket.setAddress(address);
        datagramPacket.setPort(port);
        socket.send(datagramPacket);
    }

    private DatagramPacket fillPacket(AbstractDataPackage dataPackage){
        final int packetSize = ProtocolBitMap.PACKET_SIZE;
        final int length = dataPackage.getHeader().getLength();
        byte[] tmp = new byte[packetSize + length];
        fillBuffer(tmp, 0, dataPackage.getHeader().getRawHeader(), 0, packetSize);
        fillBuffer(tmp, packetSize, dataPackage.getData(), 0, length);
        return new DatagramPacket(tmp, 0, tmp.length);
    }

    private static void fillBuffer(byte[] buffer, int offset1, byte[] filler, int offset2, int count){
        for (int i = 0; i < count; i++) {
            buffer[offset1 + i] = filler[offset2 + i];
        }
    }
}
