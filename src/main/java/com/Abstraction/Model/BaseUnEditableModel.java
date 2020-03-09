package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.User;
import com.Abstraction.Util.Logging.TreeMapProxy;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Contain underlying layer with users on client side
 * and in a conversation
 */

public abstract class BaseUnEditableModel implements UnEditableModel {

    protected final Map<Integer, User> userMap;

    protected final Set<User> conversation;

    /**
     * TreeMap just for easy to find on GUI
     */

    BaseUnEditableModel() {
        userMap = new TreeMap<>();
        conversation = new HashSet<>();
    }

    /**
     * @return Copy of this map so you can change it
     * and won't throw Concurrent Exception
     */

    @Override
    public Map<Integer, User> getUserMap() {
        return new TreeMapProxy<>(userMap);
    }

    /**
     * @return Copy of this set, as instant snapshot
     */

    @Override
    public Set<User> getConversation() {
        return new HashSet<>(conversation);
    }

    @Override
    public synchronized boolean inConversationWith(User dude) {
        return conversation.contains(dude);
    }

    @Override
    public String toString() {
        return "BaseUnEditableModel{" +
                "userMap=" + userMap +
                ", conversation=" + conversation +
                '}';
    }
}
