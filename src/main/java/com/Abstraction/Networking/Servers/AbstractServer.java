package com.Abstraction.Networking.Servers;

import com.Abstraction.Networking.Handlers.ServerHandler;
import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Networking.Writers.ServerWriter;
import com.Abstraction.Networking.Writers.Writer;
import com.Abstraction.Util.Interfaces.Starting;

import java.io.IOException;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;

/**
 * Represent server abstraction
 * And basic operation for handlers
 */

public abstract class AbstractServer implements Starting {

    protected final ServerSocket serverSocket;

    /**
     * Does all dirty work, for server thread
     */

    protected final ExecutorService executorService;

    protected final boolean isCipherMode;

    /**
     * Indicator of activity state
     */

    private volatile boolean isWorking;


    public AbstractServer(int port, boolean isCipherMode) throws IOException {
        serverSocket = new ServerSocket(port);
        this.isCipherMode = isCipherMode;
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

    /**
     * Method for deciding add user on this server or not
     *
     * @param reader to read input
     * @param writer to write commands
     * @return null if user is not good enough to join our community
     */

    public abstract ServerUser authenticate(BaseReader reader, ServerWriter writer);

    /**
     * Basically adds user in an underlying collection
     *
     * @param user to add
     */

    public abstract void registerUser(ServerUser user);

    /**
     * Remove user from an underlying collection
     *
     * @param user_id
     */

    public abstract void removeUser(int user_id);

    /**
     * Same as removeUser(int)
     *
     * @param user to remove
     */

    public void removeUser(ServerUser user) {
        removeUser(user.getId());
    }

    /**
     * @return audio format as string, ready to be sent to a user
     */

    public abstract String getAudioFormat();

    /**
     * Instead of serialisation send as string
     * @param exclusiveID your id
     * @return all users on this server as toString(), except for you
     */

    public abstract String getUsersExceptYou(int exclusiveID);

    /**
     * same as above
     * @param exclusiveUser you
     * @return all users on this server as toString() except, for you
     */

    public String getUsersExceptYou(ServerUser exclusiveUser) {
        return getUsersExceptYou(exclusiveUser.getId());
    }

    /**
     *
     * @param id of desired user
     * @return user on this server or null if not present
     */

    public abstract ServerUser getUser(int id);

    /**
     * Factory method
     * @return executor service for dirty work
     */

    protected abstract ExecutorService createService();

    /**
     * Template method
     * Called when you need to accept the socket after connection establishment
     * before authenticate
     *
     * @param socket who connected
     */

    protected abstract void acceptSocket(Socket socket);

    protected abstract ServerHandler createServerHandler(Socket socket, ServerUser user);

    protected abstract Writer createWriterForUser(OutputStream outputStream, BaseUser cipherInfo);

    /**
     * Server loop for it's main thread
     */

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
