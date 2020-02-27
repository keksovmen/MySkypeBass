package com.Abstraction.Networking.Handlers;

import com.Abstraction.Networking.BaseDataPackageRouter;
import com.Abstraction.Networking.Processors.Processable;
import com.Abstraction.Networking.Processors.ServerProcessor;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Readers.Reader;
import com.Abstraction.Networking.Servers.AbstractServer;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Util.Interfaces.Starting;

import java.io.IOException;
import java.net.Socket;

public class ServerHandler implements Starting {

    protected final AbstractServer server;
    protected final Socket socket;
    protected final Processable processor;
    protected final BaseDataPackageRouter controller;
    protected final Reader reader;

    protected volatile boolean isWorking;

    public ServerHandler(AbstractServer server, Socket socket, ServerUser user) {
        this.server = server;
        this.socket = socket;
        processor = createProcessor(user);
        controller = createController();
        reader = user.getReader();
    }

    @Override
    public final boolean start(String name) {
        if (isWorking) return false;
        isWorking = true;
        new Thread(this::handleLoop, name).start();

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

    protected Processable createProcessor(ServerUser serverUser) {
        return new ServerProcessor(serverUser, server);
    }

    protected BaseDataPackageRouter createController() {
        return new BaseDataPackageRouter();
    }

    private void handleLoop() {
        while (isWorking) {
            try {
                if (!controller.handleDataPackageRouting(reader, processor)) {
                    //if processor can't handleDataPackageRouting a request
                    close();
                }
            } catch (IOException e) {
                close();
            }
        }
    }
}
