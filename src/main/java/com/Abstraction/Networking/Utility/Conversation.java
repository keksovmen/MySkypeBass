package com.Abstraction.Networking.Utility;


import com.Abstraction.Networking.Protocol.AbstractDataPackage;
import com.Abstraction.Networking.Utility.Users.ConversationServerUser;
import com.Abstraction.Networking.Utility.Users.ServerUser;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.function.Consumer;

/**
 * Handles all conversation actions
 * Transfer sound, soon messages, adds and removes dudes
 */

public class Conversation {

    /**
     * Must be concurrent
     * CopyOnWriteArrayList works well
     */

    private final List<ConversationServerUser> users;

    /**
     * Displays duration of 1 audio fragment
     * Measured in microseconds
     */

    private final int AUDIO_FRAME_DURATION = 1_000_000 / Resources.getInstance().getMiCaptureSizeDivider();

    private final int AUDIO_FRAME_DURATION_PART = AUDIO_FRAME_DURATION / Resources.getInstance().getServerAudioDividerForLags();

    private final Consumer<Runnable> serverExecutorService;

    /**
     * Creates conversation for two dudes that know about each other
     *
     * @param first  initiator
     * @param second dude
     */

    public Conversation(ServerUser first, ServerUser second, Consumer<Runnable> serverExecutorService) {
        users = new CopyOnWriteArrayList<>();
        users.add(new ConversationServerUser(first));
        users.add(new ConversationServerUser(second));
        this.serverExecutorService = serverExecutorService;
    }

    /**
     * Default method for sending sound data for multiple targets
     *
     * @param dataPackage contains sound
     * @param from        who called this method
     */

    public void sendSound(AbstractDataPackage dataPackage, int from) {
        for (ConversationServerUser user : users) {
            if (user.IsSuspended() || user.getId() == from)
                continue;
            try {
                long before = System.nanoTime();
                user.getWriter().transferAudio(dataPackage);
                long after = (System.nanoTime() - before) / 1_000;
//                System.out.println(after + "\t" + user.toString());
                if (after > AUDIO_FRAME_DURATION_PART){
                    //This dude has shitty internet connection fuck him, let him chill some time
                    if (user.suspend()) {
                        serverExecutorService.accept(() -> {
                            try {
                                Thread.sleep(3000);
                                user.unSuspend();
                            } catch (InterruptedException ignored) {
                                //if happens server is dead by this time
                            }
                        });
                    }
                }
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

    public void sendMessage(AbstractDataPackage dataPackage, int me) {
        for (ConversationServerUser user : users) {
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
     * @param dude who to add
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
        users.add(new ConversationServerUser(dude));
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
        users.remove(new ConversationServerUser(user)); //Supa retarded code but will work, due to our object is proxy and on remove it compares through equals()
        user.setConversation(null);
        users.forEach(serverController -> {
            try {
                serverController.getWriter().writeRemoveFromConv(user.getId(), serverController.getId());
            } catch (IOException ignored) {
                //Ignore this dude's thread will handleDataPackageRouting the mess
            }
        });
        if (users.size() == 1){
            ConversationServerUser last = users.get(0);
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
