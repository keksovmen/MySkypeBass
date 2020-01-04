package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.BaseUser;

import java.util.Map;
import java.util.Set;

/**
 * Interface for read only purposes
 */

public interface UnEditableModel {

    /**
     * @return copy or unmodifiable map of underlying data structure
     */

    Map<Integer, BaseUser> getUserMap();

    /**
     * @return copy or unmodifiable set of underlying data structure
     */

    Set<BaseUser> getConversation();

    boolean inConversationWith(BaseUser user);


}
