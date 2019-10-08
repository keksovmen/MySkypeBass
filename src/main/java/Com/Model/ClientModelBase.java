package Com.Model;

import Com.Networking.Utility.BaseUser;
import Com.Util.Interfaces.Registration;

import java.util.LinkedHashSet;
import java.util.Set;

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
     * For removing one dude
     * Notifies listeners
     *
     * @param user the dude
     */

    @Override
    public synchronized void removeFromModel(BaseUser user) {
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

    private void notifyListeners() {
        listeners.forEach(updater -> updater.update(this));
    }
}
