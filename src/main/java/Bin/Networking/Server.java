package Bin.Networking;

import Bin.Utility.ServerUser;

import javax.sound.sampled.AudioFormat;
import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class Server implements Startable{

    private ServerSocket serverSocket;
//    private int port;
    private boolean work;
    private AudioFormat audioFormat;
    private AtomicInteger id;
    private Map<Integer, ServerUser> users;

//    static {
        /*
        * initial value grater than 0 because of problem with transporting negative byte
         */
//        id = new AtomicInteger(3);
//    }

    public Server(final int port, final int sampleRate, final int sampleSize) throws IOException {
        serverSocket = new ServerSocket(port);
        audioFormat = new AudioFormat(sampleRate, sampleSize, 1, true, true);
        id = new AtomicInteger(1);
        users = new HashMap<>();
    }

//    public static Server getInstance(){
//        return server == null ? new Server() : server;
//    }

//    public void init(int port, int sampleRate, int sampleSize){
//        if (init) return;
//        this.port = port;
//        audioFormat = new AudioFormat(sampleRate, sampleSize, 1, true, true);
//        init = true;
//    }

    protected int getIdAndIncrement() {
        return id.getAndIncrement();
    }

    protected synchronized void addUser(ServerUser serverUser){
        users.put(serverUser.getId(), serverUser);
//        System.out.println(users);
    }


    @Override
    public void start() {
        if (work) return;
        work = true;
        new Thread(() -> {
            while (work) {
                try {
                    Socket socket = serverSocket.accept();
                    new Controller(socket, this).start();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }, "Server").start();
    }

    @Override
    public void close() {
        work = false;
//        serverSocket.close();
    }

    public String getAudioFormat(){
        return "Sample rate = " + audioFormat.getSampleRate() + "\n" +
                "Sample size = " + audioFormat.getSampleSizeInBits();
    }

    public synchronized String getUsers(int exclusiveId){
        StringBuilder stringBuilder = new StringBuilder(50);
        users.forEach((integer, serverUser) -> {
            if (integer != exclusiveId) {
                stringBuilder.append(serverUser.toString()).append("\n");
            }
        });
        return stringBuilder.toString();
    }

    public synchronized void removeUser(int id){
        users.remove(id);
    }

    public synchronized Controller getController(int who){
//        System.out.println(users);
        ServerUser serverUser = users.get(who);
        if (serverUser != null) return serverUser.getController();
        return null;
    }

    public synchronized void sendUpdateUsers(){
        users.values().forEach(serverUser -> {
            try {
                serverUser.getController().getWriter().writeUsers(serverUser.getId());
            } catch (IOException e) {
                e.printStackTrace();
            }
        });
    }

}
