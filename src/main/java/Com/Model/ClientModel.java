package Com.Model;

import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.ClientUser;

import java.util.LinkedHashSet;
import java.util.Set;

/**
 * Contain listeners for users update
 * And can register and remove them
 */

public class ClientModel extends UnEditableModel implements Registration<Updater> {

    private final Set<Updater> listeners;

    /**
     * LinkedHashSet because want order dependency, just in case
     */

    public ClientModel() {
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

    public synchronized void updateModel(BaseUser users[]) {
        userMap.clear();
        for (BaseUser user : users) {
            userMap.put(user.getId(), user);
        }
        notifyListeners();
    }

    /**
     * When one dude is added
     *
     * @param user the dude
     */

    public synchronized void addToModel(BaseUser user) {
        userMap.put(user.getId(), user);
        notifyListeners();
    }

    /**
     * For removing one dude
     *
     * @param user the dude
     */

    public synchronized void removeFromModel(BaseUser user) {
        removeFromModel(user.getId());
    }

    public synchronized void removeFromModel(int user) {
        BaseUser remove = userMap.remove(user);
        if (remove != null)
            notifyListeners();
    }

    public synchronized void clear(){
        if (!userMap.isEmpty()) {
            userMap.clear();
            notifyListeners();
        }
    }

//    public void setMe(ClientUser me){
//        this.me = me;
//    }

    private void notifyListeners() {
        listeners.forEach(updater -> updater.update(this));
    }
}
