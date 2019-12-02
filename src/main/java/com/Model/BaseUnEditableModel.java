package com.Model;

import com.Networking.Utility.Users.BaseUser;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * Contain underlying layer with users on client side
 * and in a conversation
 */

public abstract class BaseUnEditableModel implements UnEditableModel{

    protected final Map<Integer, BaseUser> userMap;

    protected final Set<BaseUser> conversation;

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
