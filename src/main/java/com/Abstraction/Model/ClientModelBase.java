package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Util.Interfaces.Registration;
import com.Abstraction.Util.Logging.Loggers.BaseLogger;
import com.Abstraction.Util.Logging.LogManagerHelper;

import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contain listeners for users modelObservation
 * And can register and remove them
 */

public class ClientModelBase extends BaseUnEditableModel implements Registration<ModelObserver>, ChangeableModel {

    private final BaseLogger clientLogger = LogManagerHelper.getInstance().getClientLogger();

    private final Set<ModelObserver> listeners;

    /**
     * Represents you on server
     * Will be set after connection to a server is established, and removed on disconnect
     */

    private ClientUser myself;

    /**
     * LinkedHashSet because want order dependency, just in case
     */

    public ClientModelBase() {
        listeners = new LinkedHashSet<>();//Because of put in order
    }

    @Override
    public void attach(ModelObserver listener) {
        listeners.add(listener);
    }

    @Override
    public void detach(ModelObserver listener) {
        listeners.remove(listener);
    }

    /**
     * Clear underlying map
     * And put new dudes in it
     * Update listeners
     *
     * @param users to put
     */

    @Override
    public synchronized void addToModel(User users[]) {
        clientLogger.logp(this.getClass().getName(), "addToModel",
                "Adding to model many dudes - " + Arrays.toString(users));
        userMap.clear();
        for (User user : users) {
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
    public synchronized void addToModel(User user) {
        clientLogger.logp(this.getClass().getName(), "addToModel",
                "Adding to model this dude - " + user);
        userMap.put(user.getId(), user);
        notifyListeners();
    }

    /**
     * Removing from user map
     * and if succeed notifies listeners
     *
     * @param user id to remove
     */

    @Override
    public synchronized void removeFromModel(int user) {
        clientLogger.logp(this.getClass().getName(), "removeFromModel",
                "Removing this dude by unique id - " + user);
        User remove = userMap.remove(user);
        if (remove != null) {
            clientLogger.logp(this.getClass().getName(), "removeFromModel",
                    "Removing this dude from conversation - " + user);
            conversation.remove(remove);
            notifyListeners();
        }
    }

    @Override
    public synchronized void clear() {
        clientLogger.logp(this.getClass().getName(), "clear",
                "Clear all dudes from both conversation and storage");
        conversation.clear();
        if (!userMap.isEmpty()) {
            userMap.clear();
            notifyListeners();
        }

    }

    @Override
    public synchronized void addToConversation(User dude) {
        clientLogger.logp(this.getClass().getName(), "addToConversation",
                "Adding this dude to conversation - " + dude);
        if (conversation.add(dude)) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void removeFromConversation(User dude) {
        clientLogger.logp(this.getClass().getName(), "removeFromConversation",
                "Removing this dude from conversation - " + dude);
        if (conversation.remove(dude)) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void clearConversation() {
        clientLogger.logp(this.getClass().getName(), "clearConversation",
                "Clearing conversation");
        if (conversation.isEmpty())
            return;
        conversation.clear();
        notifyListeners();
    }

    @Override
    public ClientUser getMyself() {
        return myself;
    }

    @Override
    public void setMyself(ClientUser me) {
        myself = me;
    }

    /**
     * Instead of {@link UnEditableModel#getUserMap()}
     * For cache purposes
     *
     * @param id of a user to get
     * @return dude or null
     */

    @Override
    public synchronized User getUser(int id) {
        User user = userMap.get(id);
        if (user == null)
            clientLogger.logp(getClass().getName(), "getUser", "Trying to get a dude with id - " + id + " but he is null");
        return user;
    }

    private void notifyListeners() {
        clientLogger.entering(this.getClass().getName(), "notifyListeners");
        listeners.forEach(updater -> updater.modelObservation(this));
        clientLogger.exiting(this.getClass().getName(), "notifyListeners");
    }
}
