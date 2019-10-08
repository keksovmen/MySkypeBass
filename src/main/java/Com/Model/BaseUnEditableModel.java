package Com.Model;

import Com.Networking.Utility.BaseUser;
import Com.Networking.Utility.ClientUser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Contain underlying layer with users on client side
 */

public abstract class BaseUnEditableModel implements UnEditableModel{

    final Map<Integer, BaseUser> userMap;

    final Set<BaseUser> conversation;

//    ClientUser me;
    /**
     * TreeMap just for easy to find on GUI
     */

    public BaseUnEditableModel() {
        userMap = new TreeMap<>();
        conversation = new HashSet<>();
    }

    /**
     * @return Copy of this map so you can change it
     * and won't throw Concurrent Exception
     */

    @Override
    public Map<Integer, BaseUser> getUserMap() {
        return new TreeMap<>(userMap);
    }

    @Override
    public Set<BaseUser> getConversation(){
        return new HashSet<>(conversation);
    }

    @Override
    public synchronized boolean inConversationWith(BaseUser dude){
        return conversation.contains(dude);
    }

}
