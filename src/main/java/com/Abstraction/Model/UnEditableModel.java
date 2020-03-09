package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.User;

import java.util.Map;
import java.util.Set;

/**
 * Interface for read only purposes
 */

public interface UnEditableModel {

    /**
     * @return copy or unmodifiable map of underlying data structure
     */

    Map<Integer, User> getUserMap();

    /**
     * @return copy or unmodifiable set of underlying data structure
     */

    Set<User> getConversation();

    boolean inConversationWith(User user);


}
