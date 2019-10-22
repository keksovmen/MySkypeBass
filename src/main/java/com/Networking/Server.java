package com.Networking;

import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.ProtocolBitMap;
import com.Networking.Utility.ProtocolValueException;
import com.Networking.Utility.WHO;
import com.Util.FormatWorker;
import com.Util.Interfaces.Starting;
import com.Util.Resources;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.serverLogger;

/**
 * Implementation of server with my protocol
 * Basic TCP, but best way is made this was use TCP for everything
 * except sound data for that should be better UDP
 * but I too lazy to implement it
 */

public class Server implements Starting {

    /**
     * The socket
     */

    private final ServerSocket serverSocket;

    /**
     * Display thread activity
     */

    private volatile boolean work;

    /**
     * Format for audio data, if client can't handle it on mic or speaker
     * he must be disconnected, but i will rewrite this
     */

    private final AudioFormat audioFormat;

    /**
     * Must be less or equal ProtocolBitMap.MAX_VALUE
     */

    private final int MIC_CAPTURE_SIZE;

    /**
     * Place where you get your unique id
     * Must starts from WHO. last index + 1
     */

    private final AtomicInteger id;

    /**
     * Where me and the boys are flexing
     */

    private final ConcurrentHashMap<Integer, ServerController> users;

    /**
     * For utility purposes
     */

    private final ExecutorService executor;

    private final int BUFFER_SIZE_FOR_IO;

    /**
     * Creates server with give parameters
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     * @throws ProtocolValueException if mic capture size is grater than possible length of the protocol
     */

    private Server(final int port, final int sampleRate, final int sampleSizeInBits /*final int usersAmount*/) throws IOException, ProtocolValueException {
        serverSocket = new ServerSocket(port);
        audioFormat = new AudioFormat(
                sampleRate,
                sampleSizeInBits,
                1,
                true,
                true);
        try {
            MIC_CAPTURE_SIZE = calculateMicCaptureSize(sampleRate, sampleSizeInBits);
        } catch (ProtocolValueException e) {
            serverSocket.close();
            throw e;
        }
        id = new AtomicInteger(WHO.SIZE);//because some ids already in use @see BaseWriter enum WHO
        users = new ConcurrentHashMap<>();//change to one of concurrent maps
        executor = new ThreadPoolExecutor(
                0,
                8,
                30,
                TimeUnit.SECONDS,
                new LinkedBlockingQueue<>());

        BUFFER_SIZE_FOR_IO = Resources.getBufferSize() * 1024;
    }

    /**
     * Creates server from integers
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public static Server getFromIntegers(final int port, final int sampleRate, final int sampleSizeInBits) throws IOException, ProtocolValueException {
        return new Server(
                port,
                sampleRate,
                sampleSizeInBits
        );
    }

    /**
     * Creates server from strings
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public static Server getFromStrings(final String port, final String sampleRate, final String sampleSizeInBits) throws IOException, ProtocolValueException {
        return new Server(
                Integer.valueOf(port),
                Integer.valueOf(sampleRate),
                Integer.parseInt(sampleSizeInBits)
        );
    }

    /**
     * Starts a new thread
     * that will accept new connections
     *
     * @param name with given name
     */

    @Override
    public boolean start(String name) {
        if (work) {
            return false;
        }
        work = true;
        new Thread(() -> {
            while (work) {
                try {
                    Socket socket = serverSocket.accept();
                    executor.execute(() -> {
                        try {
                            Starting serverController = new ServerController(
                                    socket,
                                    this,
                                    BUFFER_SIZE_FOR_IO
                            );
                            serverController.start("Server controller - ");
                        } catch (IOException e) {
                            e.printStackTrace();
                            try {
                                socket.close();
                            } catch (IOException ignored) {
                            }
                        }
                    });
                } catch (IOException e) {
                    close();
                }
            }
        }, name).start();
        return true;
    }

    @Override
    public void close() {
        if (!work)
            return;
        work = false;
        executor.shutdown();
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();

        }
    }

    /**
     * For ServerController usages
     *
     * @return unique id for a user
     */

    int getIdAndIncrement() {
        int i = id.getAndIncrement();
        if (ProtocolBitMap.MAX_VALUE < i) {
            System.err.println("Max limit of IDs is exceeded! System shutting down!");
            System.exit(1);
        }
        return i;
    }

    /**
     * Put new user
     * And notifies others with new dude on
     *
     * @param serverController to add
     */

    public void registerController(ServerController serverController) {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "registerController",
                "Controller is registered - " + serverController.getMe());
        users.put(serverController.getId(), serverController);
        if (work)
            sendAddDude(serverController);
    }

    /**
     * Remove a user from server
     * Send notification about it
     * Clears any existed data packages in the pool
     *
     * @param id of user to remove
     */

    public void removeController(int id) {
        users.remove(id);
        if (work)
            sendRemoveDude(id);
        AbstractDataPackagePool.clearStorage();
    }

    /**
     * Format for transferring audio data
     * and mic capture size for not violating protocol length
     *
     * @return data enough to represent audio format for client
     */

    public String getAudioFormat() {
        return FormatWorker.getFullAudioPackage(audioFormat, MIC_CAPTURE_SIZE);
    }


    /**
     * Method for obtaining all except you users
     *
     * @param exclusiveId you
     * @return all others users
     */

    public String getUsers(final int exclusiveId) {
        StringBuilder stringBuilder = new StringBuilder(50);
        users.forEach((integer, serverController) -> {
            if (integer != exclusiveId) {
                stringBuilder.append(serverController.getMe().toString()).append("\n");
            }
        });
        return stringBuilder.toString();
    }

    public int getControllersSize() {
        return users.size();
    }

    /**
     * Get controller for other usages
     *
     * @param who to get id
     * @return null if there is no such dude
     */

    public ServerController getController(int who) {
        return users.get(who);
    }

    /**
     * Update each user with new users
     */

    public void sendAddDude(ServerController dudesController) {
        serverLogger.logp(Level.FINER, this.getClass().getName(), "sendAddDude",
                "All others dudes are notified about this user - " + dudesController.getMe());
        users.forEach((integer, controller) ->
                {
                    if (integer == dudesController.getId())
                        return;
                    executor.execute(() ->
                    {
                        try {
                            controller.getWriter().writeAddToUserList(
                                    controller.getId(),
                                    dudesController.getMe().toString()
                            );
                        } catch (IOException ignored) {
                            //If exception with io, is must be handled by corresponding thread not yours
                        }
                    });
                }
        );
    }

    /**
     * Send notification to each user that some dude disconnected
     *
     * @param dudesId of disconnected
     */

    public void sendRemoveDude(int dudesId) {
        users.forEach((integer, controller) ->
                executor.execute(() ->
                {
                    try {
                        controller.getWriter().writeRemoveFromUserList(
                                controller.getId(),
                                dudesId
                        );
                    } catch (IOException ignored) {
                        //If exception with io, is must be handled by corresponding thread not yours
                    }
                })
        );
    }

    private int calculateMicCaptureSize(int sampleRate, int sampleSizeInBits) throws ProtocolValueException {
        int i = (sampleRate / Resources.getMiCaptureSizeDivider()) * (sampleSizeInBits / 8);
        i = i - i % (sampleSizeInBits / 8);
        if (ProtocolBitMap.MAX_VALUE < i)
            throw new ProtocolValueException("Audio capture size is larger than length of the protocol! " +
                    i + " must be " + "<= " + ProtocolBitMap.MAX_VALUE);
        return i;
    }

}
