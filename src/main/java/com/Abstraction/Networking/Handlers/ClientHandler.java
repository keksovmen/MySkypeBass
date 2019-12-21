package com.Abstraction.Networking.Handlers;

import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Networking.BaseController;
import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Processors.ClientProcessor;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.ClientController;
import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Networking.Writers.ClientWriter;
import com.Abstraction.Pipeline.ACTIONS;
import com.Abstraction.Util.Interfaces.Starting;
import com.Abstraction.Util.Resources;

import java.io.Closeable;
import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Closeable {

    protected final AbstractClient client;
    protected Socket socket;
    protected Processable processor;
    protected BaseController controller;

    protected volatile boolean isWorking;

    public ClientHandler(AbstractClient client) {
        this.client = client;
    }

    /**
     * @param name your desired name on server
     * @param socket with connection
     * @return true if connected to server, false if already connected
     * @throws IOException if network connection failed
     */

    public boolean start(String name, Socket socket) throws IOException {
        if (isWorking)
            return false;
        this.socket = socket;
        BaseReader reader = createReader();
        ClientWriter writer = createWriter();
        ClientUser user = client.authenticate(reader, writer, name);
        if (user == null)
            throw new IOException();
        controller = createController(reader);
        processor = createProcessor(user);

        isWorking = true;
        new Thread(this::handleLoop, name + " Reader").start();

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
            }
        }
    }

    public boolean isWorking() {
        return isWorking;
    }

    protected BaseReader createReader() throws IOException {
        return new BaseReader(socket.getInputStream(), Resources.getInstance().getBufferSize());
    }

    protected ClientWriter createWriter() throws IOException {
        return new ClientWriter(socket.getOutputStream(), Resources.getInstance().getBufferSize());
    }

    protected Processable createProcessor(ClientUser clientUser) {
        return new ClientProcessor(client.getModel(), clientUser, client);
    }

    protected BaseController createController(BaseReader reader) {
        return new ClientController(reader);
    }

    private void handleLoop() {
        while (isWorking) {
            try {
                if (!controller.handleRequest(processor)) {
                    //if processor can't handleRequest a request
                    close();
                }
            } catch (IOException e) {
                if (isWorking) {
                    client.notifyObservers(ACTIONS.CONNECTION_TO_SERVER_FAILED, null);
                    close();
                }
            }
        }
    }

}
