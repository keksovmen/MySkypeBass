package Com.Networking.Utility;

import java.util.concurrent.Semaphore;

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

    private final Semaphore semaphore;

    public ServerUser(String name, int id) {
        super(name, id);
        semaphore = new Semaphore(1);
//        this.controller = controller;
    }

    public void lock(){
        try {
            semaphore.acquire();
        } catch (InterruptedException ignored) {// interaction is not used
        }
    }

    public void release(){
        semaphore.release();
        if (conversation != null){
            //release semaphore
        }
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
