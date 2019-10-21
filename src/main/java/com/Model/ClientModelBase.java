package com.Model;

import com.Networking.Utility.BaseUser;
import com.Util.Interfaces.Registration;
import com.Util.Logging.LoggerUtils;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.Util.Logging.LoggerUtils.clientLogger;

/**
 * Contain listeners for users update
 * And can register and remove them
 */

public class ClientModelBase extends BaseUnEditableModel implements Registration<Updater>, ChangeableModel {


    private final Set<Updater> listeners;

    /**
     * LinkedHashSet because want order dependency, just in case
     */

    public ClientModelBase() {
        listeners = new LinkedHashSet<>();//Because of put in order
    }

    @Override
    public boolean registerListener(Updater listener) {
        return listeners.add(listener);
    }

    @Override
    public boolean removeListener(Updater listener) {
        return listeners.remove(listener);
    }

    /**
     * Clear underlying map
     * And put new dudes in it
     * Update listeners
     *
     * @param users to put
     */

    @Override
    public synchronized void addToModel(BaseUser users[]) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "addToModel",
                "Adding to model many dudes - " + Arrays.toString(users));
        userMap.clear();
        for (BaseUser user : users) {
            userMap.put(user.getId(), user);
        }
        notifyListeners();
    }

    /**
     * When one dude is added
     * Notifies listeners
     *
     * @param user the dude
     */

    @Override
    public synchronized void addToModel(BaseUser user) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "addToModel",
                "Adding to model this dude - " + user);
        userMap.put(user.getId(), user);
        notifyListeners();
    }

    /**
     * For removing one dude
     * Notifies listeners
     *
     * @param user the dude
     */

    @Override
    public synchronized void removeFromModel(BaseUser user) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "removeFromModel",
                "Removing this dude - " + user);
        removeFromModel(user.getId());
    }

    /**
     * Removing from user map
     * and if succeed notifies listeners
     *
     * @param user id to remove
     */

    @Override
    public synchronized void removeFromModel(int user) {
        clientLogger.logp(Level.FINER, this.getClass().getName(), "removeFromModel",
                "Removing this dude by unique id - " + user);
        BaseUser remove = userMap.remove(user);
        if (remove != null) {
            clientLogger.logp(Level.FINER, this.getClass().getName(), "removeFromModel",
                    "Removing this dude from conversation - " + user);
            conversation.remove(remove);
            notifyListeners();
        }
    }

    @Override
    public synchronized void clear(){
        clientLogger.logp(Level.FINER, this.getClass().getName(), "clear",
                "Clear all dudes from both conversation and storage");
        conversation.clear();
        if (!userMap.isEmpty()) {
            userMap.clear();
            notifyListeners();
        }

    }

    @Override
    public synchronized void addToConversation(BaseUser dude){
        clientLogger.logp(Level.FINER, this.getClass().getName(), "addToConversation",
                "Adding this dude to conversation - " + dude);
        if (conversation.add(dude)) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void removeFromConversation(BaseUser dude){
        clientLogger.logp(Level.FINER, this.getClass().getName(), "removeFromConversation",
                "Removing this dude from conversation - " + dude);
        if (conversation.remove(dude)) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void clearConversation(){
        clientLogger.logp(Level.FINER, this.getClass().getName(), "clearConversation",
                "Clearing conversation");
        if (conversation.isEmpty())
            return;
        conversation.clear();
        notifyListeners();
    }

    private void notifyListeners() {
        clientLogger.entering(this.getClass().getName(),"notifyListeners");
        listeners.forEach(updater -> updater.update(this));
        clientLogger.exiting(this.getClass().getName(),"notifyListeners");
    }
}
