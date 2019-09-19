package Com.Networking.Utility;

/**
 * Server version of a user
 */

public class ServerUser extends BaseUser {

//    /**
//     * Who created you
//     */

//    private final ServerController controller;

    /**
     * Shows in conversation you are or not
     */

    private volatile Conversation conversation;

    public ServerUser(String name, int id) {
        super(name, id);
//        this.controller = controller;
    }

//    public ServerController getController() {
//        return controller;
//    }

    public Conversation getConversation() {
        return conversation;
    }

    public void setConversation(Conversation conversation) {
        this.conversation = conversation;
    }

    public boolean inConv() {
        return conversation != null;
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
