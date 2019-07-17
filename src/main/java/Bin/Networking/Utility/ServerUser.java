package Bin.Networking.Utility;

import Bin.Networking.ServerController;

/**
 * Server version of a user
 */

public class ServerUser extends BaseUser {

    /**
     * Who created you
     */

    private final ServerController controller;

    /**
     * Shows in conversation you are or not
     */

    private Conversation conversation;

    /**
     * Need for indicating a possibility to play audio
     * basically AudioClient speaker field
     */

    private final boolean canHear;

    public ServerUser(String name, int id, ServerController controller, boolean canHear) {
        super(name, id);
        this.controller = controller;
        this.canHear = canHear;
    }

    public ServerController getController() {
        return controller;
    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public boolean inConv() {
        return conversation != null;
    }

    public boolean isCanHear() {
        return canHear;
    }

    /*
    For debug only
     */
//    public String getString() {
//        return "ServerUser{" +
//                "controller=" + controller +
//                ", conversation=" + conversation +
//                '}';
//    }


}
