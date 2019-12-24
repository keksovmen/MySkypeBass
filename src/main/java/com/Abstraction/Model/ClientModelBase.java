package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Util.Interfaces.Registration;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contain listeners for users modelObservation
 * And can register and remove them
 */

public class ClientModelBase extends BaseUnEditableModel implements Registration<ModelObserver>, ChangeableModel {

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
    public synchronized void addToModel(BaseUser users[]) {
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
        BaseUser remove = userMap.remove(user);
        if (remove != null) {
            conversation.remove(remove);
            notifyListeners();
        }
    }

    @Override
    public synchronized void clear(){
        conversation.clear();
        if (!userMap.isEmpty()) {
            userMap.clear();
            notifyListeners();
        }

    }

    @Override
    public synchronized void addToConversation(BaseUser dude){
        if (conversation.add(dude)) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void removeFromConversation(BaseUser dude){
        if (conversation.remove(dude)) {
            notifyListeners();
        }
    }

    @Override
    public synchronized void clearConversation(){
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

    private void notifyListeners() {
        listeners.forEach(updater -> updater.modelObservation(this));
    }
}
