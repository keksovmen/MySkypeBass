package com.Abstraction.Networking.Utility;


import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;
import com.Abstraction.Util.Logging.LogManagerHelper;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/**
 * Handles all conversation actions
 * Transfer sound, soon messages, adds and removes dudes
 */

public class Conversation {

    private final BaseLogger serverLogger = LogManagerHelper.getInstance().getServerLogger();

    /**
     * Must be concurrent
     * CopyOnWriteArrayList works well
     */

    private final List<ServerUser> users;

    /**
     * Creates conversation for two dudes that know about each other
     *
     * @param first  initiator
     * @param second dude
     */

    public Conversation(ServerUser first, ServerUser second) {
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
        for (ServerUser user : users) {
            if (user.getId() == from)
                continue;
            try {
                long beforeNano = System.nanoTime();
                user.getWriter().transferAudio(dataPackage, user.getAddress(), user.getPort());
                long timeMicro = (System.nanoTime() - beforeNano) / 1000;
                serverLogger.logp(this.getClass().getName(), "sendSound", "To - " + user.getName() + " Time - " + timeMicro);
            } catch (IOException ignored) {
                //His thread will fix it
            }
        }
    }

    /**
     * Default method for sending a message to every one in this conf
     *
     * @param dataPackage contains message data
     * @param me id to exclude
     */

    public void sendMessage(AbstractDataPackage dataPackage, int me) {
        for (ServerUser user : users) {
            if (user.getId() == me)
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
     * @param dude   who to add
     * @param except who must not receive message about adding dude
     */

    public synchronized void addDude(ServerUser dude, ServerUser except) {
        users.forEach(user -> {
            if (user.getId() == except.getId())
                return;
            try {
                user.getWriter().writeAddToConv(dude.getId(), user.getId());
            } catch (IOException ignored) {
                //who you send message is offline ignore it. His thread will handleDataPackageRouting shit
            }
        });
        users.add(dude);
        serverLogger.logp(this.getClass().getName(), "addDude",
                "Added this dude to conversation - " + dude);
        dude.setConversation(this);
    }

    /**
     * Remove user from this conference
     * Also when there is last dude, it tells him that conversation is ended
     * and terminate all links
     *
     * @param user to be removed
     */

    public synchronized void removeDude(ServerUser user) {
        users.remove(user);
        serverLogger.logp(this.getClass().getName(), "removeDude",
                "Removed this dude from conversation - " + user);
        user.setConversation(null);
        users.forEach(serverController -> {
            try {
                serverController.getWriter().writeRemoveFromConv(user.getId(), serverController.getId());
            } catch (IOException ignored) {
                //Ignore this dude's thread will handleDataPackageRouting the mess
            }
        });
        if (users.size() == 1) {
            ServerUser last = users.get(0);
            serverLogger.logp(this.getClass().getName(), "removeDude",
                    "Last dude in conversation, trying to notify him about it - " + last);
            try {
                last.getWriter().writeStopConv(last.getId());
                last.setConversation(null);
            } catch (IOException ignored) {
                //Ignore this dude's thread will handleDataPackageRouting the mess
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

    public synchronized String getAllToString(ServerUser exclusive) {
        StringBuilder result = new StringBuilder();
        users.forEach(user -> {
            if (user.getId() == exclusive.getId())
                return;
            result.append(user.toString()).append("\n");
        });
        return result.toString();
    }

}
