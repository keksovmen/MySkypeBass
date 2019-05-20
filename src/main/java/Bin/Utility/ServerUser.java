package Bin.Utility;

import Bin.Networking.Controller;

//import java.net.Socket;

public class ServerUser extends BaseUser {

//    private Socket socket;
    private Controller controller;
    private volatile Conversation conversation;

    public ServerUser(String name, int id, Controller controller) {
        super(name, id);
        this.controller = controller;
//        this.socket = socket;
    }

    public Controller getController() {
        return controller;
    }

    public synchronized Conversation getConversation() {
        return conversation;
    }

    public synchronized void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
