package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.BaseUser;

import java.util.Map;
import java.util.Set;

/**
 * Interface for read only purposes
 */

public interface UnEditableModel {

    Map<Integer, BaseUser> getUserMap();

    Set<BaseUser> getConversation();

    boolean inConversationWith(BaseUser user);


}
