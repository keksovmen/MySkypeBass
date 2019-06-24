package Bin.Networking.Utility;


import Bin.Networking.Protocol.AbstractDataPackage;
import Bin.Networking.Protocol.AbstractDataPackagePool;
import Bin.Networking.ServerController;

import java.io.IOException;
import java.util.Arrays;
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

    private final List<ServerUser> users;

    /**
     * Create conversation for users that don't have a conversation already
     *
     * @param user should be 2+ users
     */

    public Conversation(ServerUser... user) {
        users = new CopyOnWriteArrayList<>();
        for (ServerUser serverUser : user) {
            serverUser.setConversation(this);
        }
        users.addAll(Arrays.asList(user));

    }

    /**
     * For those who already in conversation
     *
     * @param rightSide dude that in conversation and his boys, caller or receiver
     * @param leftSide  dude that in conversation and his boys, caller or receiver
     */

    public Conversation(ServerUser[] rightSide, ServerUser[] leftSide) {
        users = new CopyOnWriteArrayList<>();
        for (ServerUser right : rightSide) {
            right.setConversation(this);
            for (ServerUser left : leftSide) {
                right.getController().getWriter().writeAddToConv(left.getId(), right.getId());
            }
        }
        for (ServerUser left : leftSide) {
            left.setConversation(this);
            for (ServerUser right : rightSide) {
                left.getController().getWriter().writeAddToConv(right.getId(), left.getId());
            }
        }

        users.addAll(Arrays.asList(rightSide));
        users.addAll(Arrays.asList(leftSide));

    }

    /**
     * Default method for sending sound data for multiple targets
     *
     * @param dataPackage contains sound
     * @param from        who called this method
     */

    public void sendSound(AbstractDataPackage dataPackage, int from) {
        for (ServerUser user : users) {
            if (user.getId() != from && user.isCanHear()) {
                ServerController controller = user.getController();
                try {
                    controller.getWriter().transferAudio(dataPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                    removeDude(user);
                }
            }
        }
    }

    /**
     * Default method for sending a message to every one in this conf
     *
     * @param dataPackage contains message data
     * @param from who sent it
     */

    public void sendMessage(AbstractDataPackage dataPackage, int from){
        for (ServerUser user : users) {
            if (user.getId() != from) {
                ServerController controller = user.getController();
                try {
                    controller.getWriter().transferMessage(dataPackage);
                } catch (IOException e) {
                    e.printStackTrace();
                    removeDude(user);
                }
            }
        }
    }

    /**
     * Add new user(s) to this conference
     *
     * @param exclusive who called this method
     * @param user      to be added
     */

    public synchronized void addDude(ServerUser exclusive, ServerUser... user) {
        for (ServerUser serverUserExist : users) {
            if (!serverUserExist.equals(exclusive)) {
                for (ServerUser serverUserToAdd : user) {
                    serverUserExist.getController().getWriter().writeAddToConv(serverUserToAdd.getId(), serverUserExist.getId());
                    serverUserToAdd.setConversation(this);
                }
            }
        }
        users.addAll(Arrays.asList(user));
    }

    /**
     * Remove user(s) from this conference
     * Also when there is last dude, it tells him that conversation is ended
     * and terminate all links
     *
     * @param user to be removed
     */

    public synchronized void removeDude(ServerUser user) {
        user.setConversation(null);
        if (!users.remove(user)) {
            return;
        }
        for (ServerUser serverUser : users) {
            serverUser.getController().getWriter().writeRemoveFromConv(user.getId(), serverUser.getId());
        }
        if (users.size() == 1) {
            ServerUser lastUser = users.get(0);
            lastUser.getController().getWriter().writeStopConv(lastUser.getId());
            lastUser.setConversation(null);
            users.clear();
        }
    }

    /**
     * Not used
     * For checking is there a dude
     *
     * @param user to be checked
     * @return if he is in
     */

    public synchronized boolean contains(ServerUser user) {
        return users.contains(user);
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

    public synchronized String getAllToString(ServerUser exclusive) {
        StringBuilder result = new StringBuilder();
        for (ServerUser user : users) {
            if (!user.equals(exclusive)) {
                result.append(user).append("\n");
            }
        }
        return result.toString();
    }
}
