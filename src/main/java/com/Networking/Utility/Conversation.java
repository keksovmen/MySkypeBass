package com.Networking.Utility;


import com.Networking.Protocol.AbstractDataPackage;
import com.Networking.ServerController;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

import static com.Util.Logging.LoggerUtils.serverLogger;

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
        for (ServerController user : users) {
            if (user.getId() == from)
                continue;
            try {
                user.getWriter().transferAudio(dataPackage);
            } catch (IOException ignored) {
                //His thread will fix it
            }
        }
    }

    /**
     * Default method for sending a message to every one in this conf
     *
     * @param dataPackage contains message data
     */

    public void sendMessage(AbstractDataPackage dataPackage, ServerController me) {
        for (ServerController user : users) {
            if (user.equals(me))
                continue;
            try {
                user.getWriter().transferPacket(dataPackage);
            } catch (IOException ignored) { // His thread will fix it
            }
        }
    }

    /**
     * Add new user to this conference
     * and link this conversation to his field
     *
     * @param dude who to add
     * @param except who must not receive message about adding dude
     */

    public synchronized void addDude(ServerController dude, ServerController except) {
        users.forEach(serverController -> {
            if (serverController.getId() == except.getId())
                return;
            try {
                serverController.getWriter().writeAddToConv(dude.getId(), serverController.getId());
            } catch (IOException ignored) {
                //who you send message is offline ignore it. His thread will handle shit
            }
        });
        users.add(dude);
        serverLogger.logp(Level.FINER, this.getClass().getName(), "addDude",
                "Added this dude to conversation - " + dude);

        dude.getMe().setConversation(this);
    }

    /**
     * Remove user from this conference
     * Also when there is last dude, it tells him that conversation is ended
     * and terminate all links
     *
     * @param user to be removed
     */

    public synchronized void removeDude(ServerController user) {
        users.remove(user);
        serverLogger.logp(Level.FINER, this.getClass().getName(), "removeDude",
                "Removed this dude from conversation - " + user.getMe());
        user.getMe().setConversation(null);
        users.forEach(serverController -> {
            try {
                serverController.getWriter().writeRemoveFromConv(user.getId(), serverController.getId());
            } catch (IOException ignored) {
                //Ignore this dude's thread will handle the mess
            }
        });
        if (users.size() == 1){
            ServerController last = users.get(0);
            serverLogger.logp(Level.FINER, this.getClass().getName(), "removeDude",
                    "Last dude in conversation, trying to notify him about it - " + last);
            try {
                last.getWriter().writeStopConv(last.getId());
                last.getMe().setConversation(null);
            } catch (IOException ignored) {
                //Ignore this dude's thread will handle the mess
            }
        }
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

    @Override
    public String toString() {
        return "Conversation{" +
                "users=" + users +
                '}';
    }
}
