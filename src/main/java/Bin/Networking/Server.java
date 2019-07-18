package Bin.Networking;

import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.Utility.ServerUser;
import Bin.Networking.Utility.Starting;
import Bin.Networking.Utility.WHO;
import Bin.Util.Checker;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.io.InputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Implementation of server with my protocol
 * Basic TCP, but best way is made this was use TCP for everything
 * except sound data for that should be better UDP
 * but I too lazy to implement it
 */

public class Server implements Starting {

    public static final Properties serverProp;

    /*
      Load of default properties for server parts
     */

    static {
        Properties defaultProp = new Properties();
        defaultProp.setProperty("lock_time", "300");
        defaultProp.setProperty("bufferSize", "32");
        serverProp = new Properties(defaultProp);
        try {
            InputStream resourceAsStream = Checker.getCheckedInput("/properties/Server.properties.properties");
            serverProp.load(resourceAsStream);
        } catch (IOException e) {
            e.printStackTrace();
            //Ignore because you already have default one
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

    private final ExecutorService executor;

    private static final int AMOUNT_OF_HELPER_THREADS = 10;

//    private static final Logger logger = Logger.getLogger("MyLogger");

//    static {
//        logger.setLevel(Level.FINE);
//    }


    /**
     * Creates server with give parameters
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    private Server(final int port, final int sampleRate, final int sampleSizeInBits) throws IOException {
        serverSocket = new ServerSocket(port);
        audioFormat = new AudioFormat(sampleRate, sampleSizeInBits, 1, true, true);
        id = new AtomicInteger(WHO.SIZE);//because some ids already in use @see BaseWriter enum WHO
        users = new HashMap<>();//change to one of concurrent maps
        executor = new ThreadPoolExecutor(0, AMOUNT_OF_HELPER_THREADS,
                30, TimeUnit.SECONDS, new LinkedBlockingQueue<>());
    }

    /**
     * Creates server from integers
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public static Server getFromIntegers(final int port, final int sampleRate, final int sampleSizeInBits) throws IOException {
        return new Server(port, sampleRate, sampleSizeInBits);
    }

    /**
     * Creates server from strings
     *
     * @param port             for the server
     * @param sampleRate       any acceptable one
     * @param sampleSizeInBits must be dividable by 8
     * @throws IOException if port already in use
     */

    public static Server getFromStrings(final String port, final String sampleRate, final String sampleSizeInBits) throws IOException {
        return new Server(Integer.valueOf(port), Integer.valueOf(sampleRate), Integer.parseInt(sampleSizeInBits));
    }

    /**
     * Starts a new thread
     * that will accept new connections
     *
     * @param name with given name
     */

    @Override
    public void start(String name) {
        if (work) {
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
    }

    /**
     * For ServerController usages
     *
     * @return unique id for a user
     */

    int getIdAndIncrement() {
        return id.getAndIncrement();
    }

    /**
     * Put new user for all observation
     * and updates others with new dude on
     *
     * @param serverUser to add
     */

    synchronized void addUser(ServerUser serverUser) {
        users.put(serverUser.getId(), serverUser);
//        logger.fine("Added - " + serverUser + "\nCONTAINS " + users.toString());
        sendUpdateUsers();
    }

    /**
     * Remove a user from server
     * and sendSound others update on it
     * clear any existed data packages from pool
     *
     * @param id of user to remove
     */

    synchronized void removeUser(int id) {
        users.remove(id);
//        logger.fine("Removed - " + id + "\nCONTAINS " + users.toString());
        sendUpdateUsers();
        AbstractDataPackagePool.clearStorage();
    }

    /**
     * Format for transferring audio data
     *
     * @return data enough to represent audio format for client
     */

    String getAudioFormat() {
        return "Sample rate = " + audioFormat.getSampleRate() + "\n" +
                "Sample size = " + audioFormat.getSampleSizeInBits();
    }

    /**
     * Parse string like this Sample rate = 01...n\nSample size = 01....n
     * retrieve from them digits
     * <p>
     * MUST MATCH getAudioFormat() !
     *
     * @param data got from the server
     * @return default audio format
     */

    public static AudioFormat parseAudioFormat(String data) {
        String[] strings = data.split("\n");
        Pattern pattern = Pattern.compile("\\d+?\\b");
        Matcher matcher = pattern.matcher(strings[0]);
        matcher.find();
        int sampleRate = Integer.valueOf(matcher.group());
        matcher = pattern.matcher(strings[1]);
        matcher.find();
        int sampleSize = Integer.valueOf(matcher.group());
        return new AudioFormat(sampleRate, sampleSize, 1, true, true);
    }

    /**
     * Method for obtaining all except you users
     *
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
     *
     * @param who to get
     * @return null if there is no such dude
     */

    synchronized ServerController getController(int who) {
        ServerUser serverUser = users.get(who);
        if (serverUser != null) {
            return serverUser.getController();
        }
        return null;
    }

    /**
     * Update each user with new users
     */

    private synchronized void sendUpdateUsers() {
        final String users = getUsers(-1);//-1 isn't possible value so should be everyone
        this.users.values().forEach(serverUser -> executor.execute(() ->
                serverUser.getController().getWriter().writeUsers(serverUser.getId(),
                        users.replaceFirst(serverUser.toString() + "\n", ""))));
    }
}
