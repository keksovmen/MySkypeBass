package com.Networking.Handlers;

import com.Networking.BaseController;
import com.Networking.Processors.Processable;
import com.Networking.Processors.ServerProcessor;
import com.Networking.Readers.BaseReader;
import com.Networking.Servers.AbstractServer;
import com.Networking.Utility.Users.ServerUser;
import com.Networking.Writers.ServerWriter;
import com.Util.Interfaces.Starting;
import com.Util.Resources;

import java.io.IOException;
import java.net.Socket;

public class ServerHandler implements Starting {

    protected final AbstractServer server;
    protected final Socket socket;
    protected Processable processor;
    protected BaseController controller;

    protected volatile boolean isWorking;

    public ServerHandler(AbstractServer server, Socket socket) {
        this.server = server;
        this.socket = socket;
    }

    @Override
    public final boolean start(String name) {
        if (isWorking)
            return false;
        BaseReader reader = null;
        ServerWriter writer = null;
        try {
            reader = createReader();
            writer = createWriter();
        } catch (IOException e) {
            return false;
        }

        ServerUser user = server.authenticate(reader, writer);
        if (user == null)
            return false;

        server.registerUser(user);
        try {
            user.getWriter().writeUsers(user.getId(), server.getUsersExceptYou(user));
        } catch (IOException e) {
            server.removeUser(user);
            return false;
        }

        controller = createController(reader);
        processor = createProcessor(user);

        isWorking = true;
        new Thread(this::handleLoop, name + user.getId()).start();

        return true;
    }

    @Override
    public final void close() {
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

    protected ServerWriter createWriter() throws IOException {
        return new ServerWriter(socket.getOutputStream(), Resources.getBufferSize());
    }

    protected Processable createProcessor(ServerUser serverUser) {
        return new ServerProcessor(serverUser, server);
    }

    protected BaseController createController(BaseReader reader) {
        return new BaseController(reader);
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
