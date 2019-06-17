package Bin.Networking;

import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Utility.ServerUser;
import Bin.Networking.Utility.Starting;
import Bin.Networking.Writers.BaseWriter;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Implementation of server with my protocol
 * Basic TCP, but best way is made this was use TCP for everything
 * except sound data for that should be better UDP
 * but I too lazy to implement it
 */

public class Server implements Starting {

    public static final Properties serverProp;

    static {
        Properties defaultProp = new Properties();
        defaultProp.setProperty("lock_time", "300");
        defaultProp.setProperty("bufferSize", "32");
        serverProp = new Properties(defaultProp);
        InputStream resourceAsStream = Server.class.getResourceAsStream("properties/Server.properties.properties");
        if (resourceAsStream != null){
            try {
                serverProp.load(resourceAsStream);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

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
     * Place where you get your unique id
     * Must starts from WHO. last index + 1
     */

    private final AtomicInteger id;

    /**
     * Where me and the boys are flexing
     */

    private final Map<Integer, ServerUser> users;

    /**
     * For utility purposes
     */

    private final Executor executor;

//    private static final Logger logger = Logger.getLogger("MyLogger");

//    static {
//        logger.setLevel(Level.FINE);
//    }


//    static {
    /*
     * initial value grater than 0 because of problem with transporting negative byte
     */
//        id = new AtomicInteger(3);
//    }


    /**
     * Creates server from integers
     * @param port for the server
     * @param sampleRate any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public Server(final int port, final int sampleRate, final int sampleSizeInBits) throws IOException {
        serverSocket = new ServerSocket(port);
        audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, 1, true, true);
        id = new AtomicInteger(BaseWriter.START_OF_USERS);//because 0 1 2 already in use @see BaseWriter enum WHO
        users = new HashMap<>();//change to one of concurrent maps
        executor = Executors.newFixedThreadPool(3);
    }

    /**
     * Creates server from strings
     * @param port for the server
     * @param sampleRate any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public Server(final String port, final String sampleRate, final String sampleSizeInBits) throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(port));
        audioFormat = new AudioFormat(Integer.parseInt(sampleRate), Integer.parseInt(sampleSizeInBits), 1, true, true);
        id = new AtomicInteger(BaseWriter.START_OF_USERS);//because 0 1 2 already in use @see BaseWriter enum WHO
        users = new HashMap<>();//change to one of concurrent maps
        executor = Executors.newFixedThreadPool(3);

    }

    /**
     * Starts a new thread
     * that will accept new connections
     * @param name with given name
     */

    @Override
    public void start(String name) {
        if (work){
            throw new IllegalStateException("Already started");
        }
        work = true;
        new Thread(() -> {
            while (work) {
                try {
                    Socket socket = serverSocket.accept();
                    new ServerController(socket, this).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, name).start();
    }

    @Override
    public void close() {
        work = false;
        try {
            serverSocket.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
//        serverSocket.close();
    }

    /**
     * For ServerController usages
     * @return unique id for a user
     */

    protected int getIdAndIncrement() {
        return id.getAndIncrement();
    }

    /**
     * Put new user for all observation
     * and updates others with new dude on
     * @param serverUser to add
     */

    protected synchronized void addUser(ServerUser serverUser) {
        users.put(serverUser.getId(), serverUser);
//        logger.fine("Added - " + serverUser + "\nCONTAINS " + users.toString());
        sendUpdateUsers();
    }

    /**
     * Remove a user from server
     * and send others update on it
     * clear any existed data packages from pool
     * @param id of user to remove
     */

    protected synchronized void removeUser(int id) {
        users.remove(id);
//        logger.fine("Removed - " + id + "\nCONTAINS " + users.toString());
        sendUpdateUsers();
        AbstractDataPackagePool.clearStorage();
    }

    /**
     * Format for transferring audio data
     * @return data enough to represent audio format for client
     */

    String getAudioFormat() {
        return "Sample rate = " + audioFormat.getSampleRate() + "\n" +
                "Sample size = " + audioFormat.getSampleSizeInBits();
    }

    /**
     * Method for obtaining all except you users
     * @param exclusiveId you
     * @return all others users
     */

    synchronized String getUsers(final int exclusiveId) {
        StringBuilder stringBuilder = new StringBuilder(50);
        users.forEach((integer, serverUser) -> {
            if (integer != exclusiveId) {
                stringBuilder.append(serverUser.toString()).append("\n");
            }
        });
        return stringBuilder.toString();
    }

    /**
     * Get controller for other usages
     * @param who to get
     * @return null if there is no such dude
     */

    public synchronized ServerController getController(int who) {
        ServerUser serverUser = users.get(who);
        if (serverUser != null) {
            return serverUser.getController();
        }
        return null;
    }

    /**
     * Update each user with new users
     */

    synchronized void sendUpdateUsers() {
        final String users = getUsers(-1);//-1 isn't possible value so should be everyone
        this.users.values().forEach(serverUser -> executor.execute(() ->
                serverUser.getController().getWriter().writeUsers(serverUser.getId(),
                        users.replaceFirst(serverUser.toString() + "\n", ""))));
    }

}
