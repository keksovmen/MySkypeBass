package Com.Networking.Utility;


import Com.Networking.Protocol.AbstractDataPackage;
import Com.Networking.ServerController;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles all conversation actions
 * Transfer sound, soon messages, adds and removes dudes
 */

public class Conversation {

    /**
     * Must be concurrent
     * CopyOnWriteArrayList works well
     */

    private final List<ServerController> users;

    /**
     * Creates conversation for two dudes that know about each other
     *
     * @param first  initiator
     * @param second dude
     */

    public Conversation(ServerController first, ServerController second) {
        users = new CopyOnWriteArrayList<>();
        users.add(first);
        users.add(second);
    }

    /**
     * Default method for sending sound data for multiple targets
     *
     * @param dataPackage contains sound
     * @param from        who called this method
     */

    public void sendSound(AbstractDataPackage dataPackage, int from) {
//        for (ServerUser user : users) {
//            if (user.getId() != from /*&& user.isCanHear()*/) {
////                ServerController controller = user.getController();
////                try {
////                    controller.getWriter().transferAudio(dataPackage);
////                } catch (IOException e) {
////                    removeDude(user);
////                }
//            }
//        }
    }

    /**
     * Default method for sending a message to every one in this conf
     *
     * @param dataPackage contains message data
     * @param from        who sent it
     */

    public void sendMessage(AbstractDataPackage dataPackage, int from) {
//        for (ServerUser user : users) {
//            if (user.getId() != from) {
//                ServerController controller = user.getController();
//                try {
//                    controller.getWriter().transferMessage(dataPackage);
//                } catch (IOException e) {
//                    removeDude(user);
//                }
//            }
//        }
    }

    /**
     * Add new user(s) to this conference
     */

    public synchronized void addDude(ServerController dude, ServerController me) {
        users.forEach(serverController -> {
            if (serverController.getId() == me.getId())
                return;
            try {
                serverController.getWriter().writeAddToConv(dude.getId(), serverController.getId());
            } catch (IOException ignored) {
                //who you send message is offline ignore it. His thread will handle shit
            }
        });
        users.add(dude);
    }

    /**
     * Remove user(s) from this conference
     * Also when there is last dude, it tells him that conversation is ended
     * and terminate all links
     *
     * @param user to be removed
     */

    public synchronized void removeDude(ServerController user) {
        users.remove(user);
        users.forEach(serverController -> {
            try {
                serverController.getWriter().writeRemoveFromConv(user.getId(), serverController.getId());
            } catch (IOException ignored) {
                //Ignore this dude's thread will handle the mess
            }
        });
        if (users.size() == 1){
            ServerController last = users.get(0);
            try {
                last.getWriter().writeStopConv(last.getId());
                last.getMe().setConversation(null);
            } catch (IOException ignored) {
                //Ignore this dude's thread will handle the mess
            }
        }
    }

    /**
     * Collect info about users
     *
     * @return all users that is in the conversation
     */

    public synchronized ServerUser[] getAll() {
        return users.toArray(new ServerUser[0]);
    }

    /**
     * Sae as previous but
     * Gets all as string except you
     *
     * @param exclusive who called this method
     * @return all except you
     */

    public synchronized String getAllToString(ServerController exclusive) {
        StringBuilder result = new StringBuilder();
        users.forEach(serverController -> {
            if (serverController.getId() == exclusive.getId())
                return;
            result.append(serverController.getMe().toString()).append("\n");
        });
        return result.toString();
    }
}
