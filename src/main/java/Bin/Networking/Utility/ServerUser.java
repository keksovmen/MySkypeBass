package Bin.Networking.Utility;

import Bin.Networking.ServerController;

public class ServerUser extends BaseUser {

    private ServerController controller;
    private volatile Conversation conversation;

    public ServerUser(String name, int id, ServerController controller) {
        super(name, id);
        this.controller = controller;
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

    public boolean inConv(){
        return conversation != null;
    }

    public String getString() {
        return "ServerUser{" +
                "controller=" + controller +
                ", conversation=" + conversation +
                '}';
    }


}
