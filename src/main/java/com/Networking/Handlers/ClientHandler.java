package com.Networking.Handlers;

import com.Client.AbstractClient;
import com.Networking.BaseController;
import com.Networking.Processors.Processable;
import com.Networking.Processors.TestProcessor;
import com.Networking.Readers.BaseReader;
import com.Networking.TestController;
import com.Networking.Utility.Users.ClientUser;
import com.Networking.Writers.ClientWriter;
import com.Util.Interfaces.Starting;
import com.Util.Resources;

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
     *
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
        return new TestProcessor(client.getModel(), clientUser, client);
    }

    protected BaseController createController(BaseReader reader) {
        return new TestController(reader);
    }

    private void handleLoop() {
        while (isWorking) {
            try {
                if (!controller.handleRequest(processor)) {
                    //if processor can't handleRequest a request
                    close();
                }
            } catch (IOException e) {
                close();
            }
        }
    }

}
