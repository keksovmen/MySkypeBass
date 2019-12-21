package com.Abstraction.Networking.Servers;

import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Networking.Writers.ServerWriter;
import com.Abstraction.Util.Interfaces.Starting;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

public abstract class AbstractServer implements Starting {

    protected final ServerSocket serverSocket;
    protected final ExecutorService executorService;

    private volatile boolean isWorking;

    public AbstractServer(int port) throws IOException {
        serverSocket = new ServerSocket(port);
        executorService = createService();

        isWorking = false;
    }

    @Override
    public final boolean start(String name) {
        if (isWorking)
            return false;

        isWorking = true;
        new Thread(this::workLoop, name).start();
        return true;
    }

    @Override
    public void close() {
        if (!isWorking)
            return;
        isWorking = false;
        executorService.shutdown();
        try {
            serverSocket.close();
        } catch (IOException ignored) {
        }
    }

    public abstract ServerUser authenticate(BaseReader reader, ServerWriter writer);

    public abstract void registerUser(ServerUser user);

    public abstract void removeUser(int user_id);

    public void removeUser(ServerUser user) {
        removeUser(user.getId());
    }

    public abstract String getAudioFormat();

    public abstract String getUsersExceptYou(int exclusiveID);

    public String getUsersExceptYou(ServerUser exclusiveUser) {
        return getUsersExceptYou(exclusiveUser.getId());
    }

    public abstract ServerUser getUser(int id);

    protected abstract ExecutorService createService();

    /**
     * Template method
     * Called when you need to accept the socket after connection establishment
     * before authenticate
     * @param socket who connected
     */

    protected abstract void acceptSocket(Socket socket);

    private void workLoop() {
        while (isWorking) {
            try {
                Socket socket = serverSocket.accept();
                executorService.execute(() -> acceptSocket(socket));
            } catch (IOException e) {
                close();
            }
        }
    }
}
