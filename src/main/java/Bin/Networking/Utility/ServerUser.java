package Bin.Networking.Utility;

import Bin.Networking.ServerController;

//import java.net.Socket;

public class ServerUser extends BaseUser {

//    private Socket socket;
    private ServerController controller;
    private volatile Conversation conversation;

    public ServerUser(String name, int id, ServerController controller) {
        super(name, id);
        this.controller = controller;
//        this.socket = socket;
    }

    public ServerController getController() {
        return controller;
    }

    public synchronized Conversation getConversation() {
        return conversation;
    }

    public synchronized void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }
}
