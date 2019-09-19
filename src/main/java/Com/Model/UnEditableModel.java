package Com.Model;

import Com.Networking.Utility.BaseUser;

import java.util.Map;
import java.util.TreeMap;

/**
 * Contain underlying layer with users on client side
 */

public abstract class UnEditableModel {

    final Map<Integer, BaseUser> userMap;

    BaseUser me;
    /**
     * TreeMap just for easy to find on GUI
     */

    public UnEditableModel() {
        userMap = new TreeMap<>();
    }

    /**
     * @return Copy of this map so you can change it
     * and won't throw Concurrent Exception
     */

    public Map<Integer, BaseUser> getUserMap() {
        return new TreeMap<>(userMap);
    }

    public BaseUser getMe() {
        return me;
    }
}
