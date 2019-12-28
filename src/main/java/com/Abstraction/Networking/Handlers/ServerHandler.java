package com.Abstraction.Networking.Handlers;

import com.Abstraction.Networking.BaseDataPackageRouter;
import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Processors.ServerProcessor;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Servers.AbstractServer;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Networking.Writers.PlainWriter;
import com.Abstraction.Networking.Writers.ServerWriter;
import com.Abstraction.Util.Interfaces.Starting;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.net.Socket;

public class ServerHandler implements Starting {

    protected final AbstractServer server;
    protected final Socket socket;
    protected Processable processor;
    protected BaseDataPackageRouter controller;

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
        return new BaseReader(socket.getInputStream(), Resources.getInstance().getBufferSize());
    }

    protected ServerWriter createWriter() throws IOException {
        return new ServerWriter(new PlainWriter(socket.getOutputStream(), Resources.getInstance().getBufferSize()));
    }

    protected Processable createProcessor(ServerUser serverUser) {
        return new ServerProcessor(serverUser, server);
    }

    protected BaseDataPackageRouter createController(BaseReader reader) {
        return new BaseDataPackageRouter(reader);
    }

    private void handleLoop() {
        while (isWorking) {
            try {
                if (!controller.handleDataPackageRouting(processor)) {
                    //if processor can't handleDataPackageRouting a request
                    close();
                }
            } catch (IOException e) {
                close();
            }
        }
    }
}
