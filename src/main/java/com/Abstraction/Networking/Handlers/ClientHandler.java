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

import java.io.IOException;
import java.net.Socket;

public class ClientHandler implements Starting {

    protected final AbstractClient client;
    protected final Socket socket;
    protected Processable processor;
    protected BaseController controller;

    protected volatile boolean isWorking;

    public ClientHandler(AbstractClient client, Socket socket) {
        this.client = client;
        this.socket = socket;
    }

    /**
     * @param name your desired name on server <- totally retarded code
     * @return true if new reader thread started
     */

    @Override
    public boolean start(String name) {
        if (isWorking)
            return false;
        BaseReader reader = null;
        ClientWriter writer = null;
        try {
            reader = createReader();
            writer = createWriter();
        } catch (IOException e) {
            return false;
        }
        ClientUser user = client.authenticate(reader, writer, name);
        if (user == null)
            return false;
        controller = createController(reader);
        processor = createProcessor(user);

        isWorking = true;
        new Thread(this::handleLoop, name + " Reader").start();

        return true;
    }

    @Override
    public void close() {
        isWorking = false;
        processor.close();
        if (!socket.isClosed()) {
            try {
                socket.close();
            } catch (IOException ignored) {
            }
        }
    }

    protected BaseReader createReader() throws IOException {
        return new BaseReader(socket.getInputStream(), Resources.getBufferSize());
    }

    protected ClientWriter createWriter() throws IOException {
        return new ClientWriter(socket.getOutputStream(), Resources.getBufferSize());
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
                if (isWorking)
                    client.notifyObservers(ACTIONS.CONNECTION_TO_SERVER_FAILED, null);
                close();
            }
        }
    }

}
