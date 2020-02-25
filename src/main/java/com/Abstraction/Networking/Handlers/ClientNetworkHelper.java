package com.Abstraction.Networking.Handlers;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Networking.BaseDataPackageRouter;
import com.Abstraction.Networking.ClientDataPackageRouter;
import com.Abstraction.Networking.Processors.ClientProcessor;
import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Readers.Reader;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Util.Algorithms;
import com.Abstraction.Util.Interfaces.Starting;

import java.io.IOException;
import java.net.DatagramSocket;
import java.net.Socket;

public class ClientNetworkHelper implements Starting {

    protected final AbstractClient client;

    protected final Socket socket;
    protected final Processable processor;
    protected final BaseDataPackageRouter packageRouter;
    protected final DatagramSocket datagramSocket;
//    protected final BaseDataPackageRouter packageRouterUDP;

    protected volatile boolean isWorking;

    public ClientNetworkHelper(AbstractClient client, Socket socket, DatagramSocket datagramSocket) {
        this.client = client;
        this.socket = socket;
        processor = createProcessor();
//        packageRouter = createPackageRouter(createReader());
        packageRouter = createPackageRouter();
        this.datagramSocket = datagramSocket;
//        packageRouterUDP = createPackageRouter(new UDPReader(datagramSocket,
//                ProtocolBitMap.PACKET_SIZE + AudioSupplier.getInstance().getDefaultAudioFormat().getMicCaptureSize()));
    }

//    /**
//     * @param name your desired name on server
//     * @param socket with connection
//     * @return true if connected to server, false if already connected
//     * @throws IOException if network connection failed
//     */
//
//    public boolean start(String name, Socket socket) throws IOException {
//        if (isWorking)
//            return false;
//        this.socket = socket;
//        BaseReader reader = createReader();
//        ClientWriter writer = createWriter();
//        ClientUser user = client.authenticate(reader, writer, name);
//        if (user == null)
//            throw new IOException();
//        processor = createProcessor(user);
//        packageRouter = createPackageRouter(reader);
//
//        isWorking = true;
//        new Thread(this::handleLoopTCP, name + " Reader").start();
//
//        return true;
//    }


    @Override
    public boolean start(String name) {
        if (isWorking) return false;

        isWorking = true;
        new Thread(this::handleLoopTCP, name + " TCP").start();
        new Thread(this::handleLoopUDP, name + " UDP").start();
        return true;
    }

    @Override
    public void close() {
        if (!isWorking)
            return;
        isWorking = false;
        processor.close();
        if (socket.isConnected()) {
            Algorithms.closeSocketThatCouldBeClosed(socket);
        }
        datagramSocket.close();
    }

    public boolean isWorking() {
        return isWorking;
    }

//    protected BaseReader createReader() throws IOException {
//        return new BaseReader(socket.getInputStream(), Resources.getInstance().getBufferSize());
//    }

//    protected ClientWriter createWriter() throws IOException {
//        return new ClientWriter(socket.getOutputStream(), Resources.getInstance().getBufferSize());
//    }

//    protected Processable createProcessor(ClientUser clientUser) {
//        return new ClientProcessor(client.getModel(), clientUser, client);
//    }

    protected Processable createProcessor() {
        return new ClientProcessor(client.getModel(), client);
    }

    protected BaseDataPackageRouter createPackageRouter() {
        return new ClientDataPackageRouter();
    }

    private void handleLoopTCP() {
        Reader readerTCP = client.getModel().getMyself().getReaderTCP();
        while (isWorking) {
            try {
                if (!packageRouter.handleDataPackageRouting(readerTCP, processor)) {
                    //if router did route but processor didn't handle it
                    close();
                }
            } catch (IOException e) {
                //if router didn't route due to network failure
                if (isWorking) {
                    client.notifyObservers(ACTIONS.CONNECTION_TO_SERVER_FAILED, null);
                    close();
                }
            }
        }
    }

    private void handleLoopUDP(){
        Reader readerUDP = client.getModel().getMyself().getReaderUDP();
        while (isWorking) {
            try {
                if (!packageRouter.handleDataPackageRouting(readerUDP, processor)) {
                    //if router did route but processor didn't handle it
                    close();
                }
            } catch (IOException e) {
                //if router didn't route due to network failure
                //tcp will handle all errors
            }
        }
    }

}
