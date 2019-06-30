package Bin.Networking.Utility;


import Bin.Networking.Protocol.AbstractDataPackage;
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
     * Private constructor
     * For sync purposes
     */

    private Conversation() {
        users = new CopyOnWriteArrayList<>();
    }

    /**
     * Create a conversation and register it on users
     * that don't have a conversation already
     *
     * @param users should be 2+ users
     */

    public static void registerSimpleConversation(ServerUser... users) {
        Conversation conversation = new Conversation();
        conversation.users.addAll(Arrays.asList(users));
        conversation.users.forEach(serverUser -> serverUser.setConversation(conversation));
    }

    /**
     * For those who already in conversation
     *
     * @param rightSide dude that in conversation and his boys, caller or receiver
     * @param leftSide  dude that in conversation and his boys, caller or receiver
     */

    public static void registerComplexConversation(ServerUser[] rightSide, ServerUser[] leftSide) {
        Conversation conversation = new Conversation();
        for (ServerUser right : rightSide) {
            right.setConversation(conversation);
            for (ServerUser left : leftSide) {
                try {
                    right.getController().getWriter().writeAddToConv(left.getId(), right.getId());
                } catch (IOException e) {
                    /*Simply ignore it's thread will handle exception*/
                }
            }
        }
        for (ServerUser left : leftSide) {
            left.setConversation(conversation);
            for (ServerUser right : rightSide) {
                try {
                    left.getController().getWriter().writeAddToConv(right.getId(), left.getId());
                } catch (IOException e) {
                    /*Simply ignore it's thread will handle exception*/
                }
            }
        }

        conversation.users.addAll(Arrays.asList(rightSide));
        conversation.users.addAll(Arrays.asList(leftSide));
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
                    /*Simply ignore it's thread will handle the exception*/
//                    e.printStackTrace();
//                    removeDude(user);
                }
            }
        }
    }

    /**
     * Default method for sending a message to every one in this conf
     *
     * @param dataPackage contains message data
     * @param from        who sent it
     */

    public void sendMessage(AbstractDataPackage dataPackage, int from) {
        for (ServerUser user : users) {
            if (user.getId() != from) {
                ServerController controller = user.getController();
                try {
                    controller.getWriter().transferMessage(dataPackage);
                } catch (IOException e) {
                    /*Simply ignore it's thread will handle the exception*/
//                    e.printStackTrace();
//                    removeDude(user);
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
                    try {
                        serverUserExist.getController().getWriter().writeAddToConv(serverUserToAdd.getId(), serverUserExist.getId());
                    } catch (IOException e) {
                        /*Simply ignore it's thread will handle the exception*/
                    }
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
            try {
                serverUser.getController().getWriter().writeRemoveFromConv(user.getId(), serverUser.getId());
            } catch (IOException e) {
                /*Simply ignore it's thread will handle the exception*/
            }
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
