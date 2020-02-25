package com.Abstraction.Networking.Readers;

import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.ProtocolBitMap;

import java.io.IOException;
import java.net.DatagramPacket;
import java.net.DatagramSocket;
import java.util.Arrays;

public class UDPReader implements Reader {

    private final DatagramSocket socket;
//    private final int packetSize;
    private final DatagramPacket packet;


    public UDPReader(DatagramSocket socket, int packetSize) {
        this.socket = socket;
//        this.packetSize = packetSize;
        packet = new DatagramPacket(new byte[packetSize], 0, packetSize);
    }

    @Override
    public AbstractDataPackage read() throws IOException {
        socket.receive(packet);
        byte[] data = packet.getData();
        AbstractDataPackage dataPackage = AbstractDataPackagePool.getPackage();
        dataPackage.getHeader().init(data);
        dataPackage.setData(Arrays.copyOfRange(data, ProtocolBitMap.PACKET_SIZE, data.length));
        return dataPackage;
    }
}
