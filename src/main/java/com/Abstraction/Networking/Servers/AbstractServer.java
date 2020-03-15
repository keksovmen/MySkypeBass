package com.Abstraction.Networking.Servers;

import com.Abstraction.Networking.Handlers.ServerHandler;
import com.Abstraction.Networking.Utility.Authenticator;
import com.Abstraction.Networking.Utility.ServerSocketProxy;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Networking.Writers.Writer;
import com.Abstraction.Util.Algorithms;
import com.Abstraction.Util.Interfaces.Starting;
import com.Abstraction.Util.Logging.LogManagerHelper;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.DatagramSocket;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutorService;

/**
 * Represent server abstraction
 * And basic operation for handlers
 */

public abstract class AbstractServer implements Starting {

    protected final ServerSocket serverSocket;
    protected final DatagramSocket serverSocketUDP;

    /**
     * Does all dirty work, for server thread
     */

    protected final ExecutorService executorService;

    protected final boolean isCipherMode;

    protected final Authenticator authenticator;

    protected final boolean isFullTCP;

    /**
     * For ping and pong actions
     */

    protected final Timer timer;

    protected final BaseLogger serverLogger = LogManagerHelper.getInstance().getServerLogger();


    /**
     * Indicator of activity state
     */

    protected volatile boolean isWorking;


    public AbstractServer(int port, boolean isCipherMode, Authenticator authenticator, boolean isFullTCP) throws IOException {
        serverSocket = new ServerSocketProxy(port);
        if (!isFullTCP)
            serverSocketUDP = new DatagramSocket(port);
        else
            serverSocketUDP = null;
        executorService = createService();
        this.isCipherMode = isCipherMode;
        this.authenticator = authenticator;
        this.isFullTCP = isFullTCP;

        timer = new Timer("Ping Timer");

        isWorking = false;
    }

    @Override
    public final boolean start(String name) {
        if (isWorking)
            return false;

        isWorking = true;
        new Thread(this::workLoopTCP, name + " TCP").start();
        if (!isFullTCP)
            new Thread(this::workLoopUDP, name + " UDP").start();
        final long period = Algorithms.minToMillis(Resources.getInstance().getPingPeriod());
        timer.schedule(new TimerTask() {
            @Override
            public void run() {
                serverLogger.logp(getClass().getName(), "run", "Sending pings");
                pingAction();
            }
        }, period, period);
        return true;
    }

    @Override
    public void close() {
        if (!isWorking)
            return;
        isWorking = false;
        executorService.shutdown();
        Algorithms.closeSocketThatCouldBeClosed(serverSocket);
        Algorithms.closeSocketThatCouldBeClosed(serverSocketUDP);
        timer.cancel();
        //need to kill already established connections
    }

    public void asyncTusk(Runnable task) {
        executorService.execute(task);
    }


    /**
     * Basically adds user in an underlying collection
     *
     * @param user to add
     */

    public abstract void registerUser(ServerUser user);

    /**
     * Remove user from an underlying collection
     *
     * @param user_id to remove
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
     *
     * @param exclusiveID your id
     * @return all users on this server as toString(), except for you
     */

    public abstract String getUsersExceptYou(int exclusiveID);

    /**
     * same as above
     *
     * @param exclusiveUser you
     * @return all users on this server as toString() except, for you
     */

    public String getUsersExceptYou(ServerUser exclusiveUser) {
        return getUsersExceptYou(exclusiveUser.getId());
    }

    /**
     * @param id of desired user
     * @return user on this server or null if not present
     */

    public abstract ServerUser getUser(int id);

    /**
     * Factory method
     *
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

    protected abstract Writer createWriterForUser(Authenticator.CommonStorage storage, OutputStream outputStream);

    protected abstract ServerUser createUser(Authenticator.CommonStorage storage, InputStream inputStream, OutputStream outputStream, InetAddress address);

    /**
     * Server loop for it's main thread
     */

    private void workLoopTCP() {
        while (isWorking) {
            try {
                Socket socket = serverSocket.accept();
                executorService.execute(() -> acceptSocket(socket));
            } catch (IOException e) {
                close();
            }
        }
    }

    protected abstract void workLoopUDP();

    /**
     * Must send {@link com.Abstraction.Networking.Protocol.CODE#SEND_PING} to everyone on server each time period
     */

    protected abstract void pingAction();
}
