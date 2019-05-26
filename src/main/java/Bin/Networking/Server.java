package Bin.Networking;

import Bin.Networking.Utility.ServerUser;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Startable {

    private ServerSocket serverSocket;
    private volatile boolean work;
    private AudioFormat audioFormat;
    private AtomicInteger id;
    private Map<Integer, ServerUser> users;
    private Executor executor;

//    static {
    /*
     * initial value grater than 0 because of problem with transporting negative byte
     */
//        id = new AtomicInteger(3);
//    }

    public Server(final int port, final int sampleRate, final int sampleSize) throws IOException {
        serverSocket = new ServerSocket(port);
        audioFormat = new AudioFormat(sampleRate, sampleSize, 1, true, true);
        id = new AtomicInteger(3);//because 0 1 2 already in use @see BaseWriter enum WHO
        users = new HashMap<>();//change to one of concurent maps
        executor = Executors.newFixedThreadPool(3);
    }

    public Server(final String port, final String sampleRate, final String sampleSize) throws IOException {
        serverSocket = new ServerSocket(Integer.parseInt(port));
        audioFormat = new AudioFormat(Integer.parseInt(sampleRate), Integer.parseInt(sampleSize), 1, true, true);
        id = new AtomicInteger(3);//because 0 1 2 already in use @see BaseWriter enum WHO
        users = new HashMap<>();//change to one of concurent maps
        executor = Executors.newFixedThreadPool(3);

    }

    protected int getIdAndIncrement() {
        return id.getAndIncrement();
    }

    protected synchronized void addUser(ServerUser serverUser) {
        users.put(serverUser.getId(), serverUser);
//        System.out.println(users);
    }


    @Override
    public boolean start() {
        if (work) return false;
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
        }, "Server").start();
        return true;
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

    String getAudioFormat() {
        return "Sample rate = " + audioFormat.getSampleRate() + "\n" +
                "Sample size = " + audioFormat.getSampleSizeInBits();
    }

    synchronized String getUsers(final int exclusiveId) {
        StringBuilder stringBuilder = new StringBuilder(50);
        users.forEach((integer, serverUser) -> {
            if (integer != exclusiveId) {
                stringBuilder.append(serverUser.toString()).append("\n");
            }
        });
        return stringBuilder.toString();
    }

    synchronized void removeUser(int id) {
        users.remove(id);
    }

    public synchronized ServerController getController(int who) {
//        System.out.println(users);
        ServerUser serverUser = users.get(who);
        if (serverUser != null) return serverUser.getController();
        return null;
    }

    synchronized void sendUpdateUsers() {
        final String users = getUsers(-1);//-1 isn't possible value so should be everyone
        this.users.values().forEach(serverUser -> {
//                serverUser.getController().getWriter().writeUsers(serverUser.getId(), getUsers(serverUser.getId()));
            executor.execute(() -> {
                try {
                    serverUser.getController().getWriter().writeUsers(serverUser.getId(), users.replaceFirst(serverUser.toString() + "\n", ""));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            });
        });
    }

}
