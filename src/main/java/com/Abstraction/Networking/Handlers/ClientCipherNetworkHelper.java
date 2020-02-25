package com.Abstraction.Networking.Handlers;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Networking.Processors.ClientDecoderProcessor;
import com.Abstraction.Networking.Processors.Processable;

import java.net.DatagramSocket;
import java.net.Socket;

public class ClientCipherNetworkHelper extends ClientNetworkHelper {

    public ClientCipherNetworkHelper(AbstractClient client, Socket socket, DatagramSocket datagramSocket) {
        super(client, socket, datagramSocket);
    }

    @Override
    protected Processable createProcessor() {
        return new ClientDecoderProcessor(client.getModel(), client);
    }
}
