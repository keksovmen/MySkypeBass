package com.Abstraction.Networking.Handlers;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Networking.BaseDataPackageRouter;
import com.Abstraction.Networking.ClientDataPackageRouter;
import com.Abstraction.Networking.Processors.ClientProcessor;
import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Util.Interfaces.Starting;

import java.io.IOException;
import java.net.Socket;

public class ClientNetworkHelper implements Starting {

    protected final AbstractClient client;

    protected final Socket socket;
    protected final Processable processor;
    protected final BaseDataPackageRouter packageRouter;

    protected volatile boolean isWorking;

    public ClientNetworkHelper(AbstractClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
        processor = createProcessor();
//        packageRouter = createPackageRouter(createReader());
        packageRouter = createPackageRouter(client.getModel().getMyself().getReader());
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
//        new Thread(this::handleLoop, name + " Reader").start();
//
//        return true;
//    }


    @Override
    public boolean start(String name) {
        if (isWorking) return false;

        isWorking = true;
        new Thread(this::handleLoop, name).start();
        return true;
    }

    @Override
    public void close() {
        if (!isWorking)
            return;
        isWorking = false;
        processor.close();
        if (socket.isConnected()) {
            try {
                socket.close();
            } catch (IOException ignored) {
                //already closed
            }
        }
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

    protected BaseDataPackageRouter createPackageRouter(BaseReader reader) {
        return new ClientDataPackageRouter(reader);
    }

    private void handleLoop() {
        while (isWorking) {
            try {
                if (!packageRouter.handleDataPackageRouting(processor)) {
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

}
