package Bin;

import Bin.Audio.AudioCapture;
import Bin.Audio.AudioClient;
import Bin.GUI.Forms.MainFrame;
import Bin.Networking.ClientController;
import Bin.Networking.DataParser.BaseDataPackage;
import Bin.Networking.Processors.ClientProcessor;
import Bin.Networking.Readers.ClientReader;
import Bin.Networking.Server;
import Bin.Networking.Writers.BaseWriter;
import Bin.Networking.Writers.ClientWriter;
import Bin.Networking.Utility.BaseUser;
import Bin.Networking.Utility.ClientUser;

import javax.sound.sampled.AudioFormat;
import java.awt.*;
import java.io.IOException;
import java.net.Socket;
import java.util.*;
import java.util.function.BiConsumer;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class Main {

    /*
     * SingleTone
     */

//    private static final Main main = new Main();

    //    private ClientWriter writer;
//    private ClientProcessor clientProcessor;
//    private ClientReader reader;
//    private BaseUser me;
//    private MainFrame mainFrame;
//    private Socket socket;
//    private final Map<Integer, ClientUser> users;
//    private AudioCapture audioCapture;
    private MainFrame mainFrame;
    private ClientController controller;
    private Server server;
    private final Map<Integer, BaseUser> users;
    private final AudioClient audioClient = AudioClient.getInstance();

    private Main() {
//        users = new HashMap<>();
//        mainFrame = new MainFrame();
        users = new HashMap<>();
        EventQueue.invokeLater(() -> {
            controller = new ClientController();
            mainFrame = new MainFrame(connect(), createServer(), nameAndId(), disconnect(), callForUsers(), sendMessage());
            controller.getProcessor().addTaskListener(usersIncome());
            controller.getProcessor().addTaskListener(showMessage());
        });
    }

    public static void main(String[] args) {
        Main main = new Main();
    }

    private Function<String[], Boolean> connect() {
        return strings -> {
            try {
                return controller.connect(strings[0], strings[1], strings[2]);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    private Function<String[], Boolean> createServer() {
        return strings -> {
            try {
                if (server != null) server.close();
                server = new Server(strings[0], strings[1], strings[2]);
                return server.start();
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        };
    }

    private Supplier<String> nameAndId() {
        return () -> controller.getMe().toString();
    }

    private Runnable disconnect() {
        return () -> controller.disconnect();
    }

    private Runnable callForUsers() {
        return () -> {
            try {
                controller.getWriter().writeUsersRequest(controller.getMe().getId());
            } catch (IOException e) {
                e.printStackTrace();
                error();
//                controller.disconnect();
//                mainFrame.errorCase();
            }
        };
    }

    private Consumer<BaseDataPackage> usersIncome() {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_USERS)) {
                BaseUser[] baseUsers = ClientController.parseUsers(baseDataPackage.getDataAsString());
                users.clear();
                Arrays.stream(baseUsers).forEach(baseUser -> users.put(baseUser.getId(), baseUser));
                mainFrame.updateUsers(baseUsers);
            }
        };
    }

    private BiConsumer<Integer, String> sendMessage() {
        return (integer, s) -> {
            try {
                controller.getWriter().writeMessage(controller.getMe().getId(), integer, s);
            } catch (IOException e) {
                e.printStackTrace();
                error();
            }
        };
    }

    private void error() {
        controller.disconnect();
        mainFrame.errorCase();
    }

    private Consumer<BaseDataPackage> showMessage() {
        return baseDataPackage -> {
            if (baseDataPackage.getHeader().getCode().equals(BaseWriter.CODE.SEND_MESSAGE)) {
                mainFrame.showMessage(users.get(baseDataPackage.getHeader().getFrom()), baseDataPackage.getDataAsString());
                audioClient.playMessageSound();
            }
        };
    }
//    public static Main getInstance() {
//        return main;
//    }

//    public boolean connect(String name, String ip, int port){
//        try {
//            socket = new Socket(ip, port);
//            writer = new ClientWriter(socket.getOutputStream());
//            clientProcessor = new ClientProcessor();
////            clientProcessor.start();
//            reader = new ClientReader(socket.getInputStream(), clientProcessor);
//            reader.start();
//            writer.writeName(name);
//        } catch (IOException e) {
//            e.printStackTrace();
//            return false;
//        }
//        return true;
//    }

//    public boolean startServer(int port, int sampleRate, int sampleSize){
////        Server server = Server.getInstance();
////        server.init(port, sampleRate, sampleSize);
////        try {
////            server.createServerSocket();
////        } catch (IOException e) {
////            e.printStackTrace();
////            return false;
////        }
////        server.start();
//        return true;
//    }

//    public void setAudioFormat(AudioFormat format){
//        if (!AudioClient.getInstance().setAudioFormat(format))
//            mainFrame.showDialog("Something doesn't work on this settings (" + format.toString() +"), check it if false " +
//                    "microphone = " + AudioClient.getInstance().isMic() + " speaker = " + AudioClient.getInstance().isSpeaker());
//        mainFrame.setUserName(me.toString());
//        mainFrame.changeFirstToSecond();
//    }

//    public void disconnect(){
//        try {
//            writer.writeDisconnect(me.getId());
//        } catch (IOException e) {
//            e.printStackTrace();
//        }finally {
//            try {
//                socket.close();
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//            mainFrame.changeSecondToFirst();
////            clientProcessor.close();
//            mainFrame.showDialog("Disconnected from the server, may be its died or your connection was lost");
//        }
//
//    }

//    public boolean writeMessage(String message, int to){
//        try {
//            writer.writeMessage(me.getId(), to, message);
//        } catch (IOException e) {
//            e.printStackTrace();
//            disconnect();
//            return false;
//        }
//        return true;
//    }
//
//    public void writeRefresh(){
//        try {
//            writer.writeUsersRequest(me.getId());
//        } catch (IOException e) {
//            e.printStackTrace();
//            disconnect();
//        }
//    }
//
//    public void call(ClientUser user){
//        if (users.containsValue(user)){
//            try {
//                writer.writeCall(me.getId(), user.getId());
//                mainFrame.showCallingDialog(user);
//            } catch (IOException e) {
//                e.printStackTrace();
//                disconnect();
//            }
//        }
//    }

//    public void cancelCall(ClientUser user){
//        if (users.containsValue(user)){
//            try {
//                writer.writeCancel(me.getId(), user.getId());
//            } catch (IOException e) {
//                e.printStackTrace();
//                disconnect();
//            }
//        }
//    }

//    public void denyCall(ClientUser who){
//        if (users.containsValue(who)){
//            try {
//                writer.writeDeny(me.getId(), who.getId());
//            } catch (IOException e) {
//                e.printStackTrace();
//                disconnect();
//            }
//        }
//    }

//    public void acceptCall(ClientUser who){
//        if (users.containsValue(who)){
//            try {
//                writer.writeAccept(me.getId(), who.getId());
//                mainFrame.acceptReceived(who.getId());
////                writer.writeAdd(me.getId());
////                AudioClient.getInstance().add(who.getId());
////                    AudioClient.getInstance().add(who.getId());
////                audioCapture.start();
////                mainFrame.addUserToConversation(audioCapture, who);
//            } catch (IOException e) {
//                e.printStackTrace();
//                disconnect();
//            }
//        }
//    }

//    public void endCall(){
//
//    }
//
//    public void setMe(int id) {
//        me = new BaseUser(mainFrame.getMyName(), id);
//        audioCapture = new AudioCapture(writer, id);
//    }

//    public String[] getListUsers(){
//        synchronized (users) {
//            if (users.size() == 0) return new String[0];
//            return users.stream().map(BaseUser::toString).toArray(String[]::new);
//        }
//    }

//    public Collection<ClientUser> getUsers(){
//        synchronized (users) {
//            return users.values();
//        }
//    }
//
//    public ClientUser getUserById(int who){
//        synchronized (users) {
//            return users.get(who);
//        }
//    }

//    public void resetUsers(ClientUser[] users){
//        synchronized (this.users) {
//            ClientUser[] clientUsers = this.users.values().stream().filter(dude -> {
//                for (ClientUser user : users) {
//                    if (user.getId() == dude.getId()) return true;
//                }
//                return false;
//            }).toArray(ClientUser[]::new);
//            this.users.clear();
//
//            Arrays.stream(users).forEach(baseUser -> this.users.put(baseUser.getId(), baseUser));
//            Arrays.stream(clientUsers).forEach(user -> this.users.put(user.getId(), user));
//        }
//        mainFrame.updateList();
//    }
//
//    public MainFrame getMainFrame() {
//        return mainFrame;
//    }

//    public ClientWriter getWriter() {
//        return writer;
//    }
//
//    public int getMeId() {
//        return me.getId();
//    }

//    public AudioCapture getAudioCapture() {
//        return audioCapture;
//    }
}