package com.Model;

import com.Networking.Utility.BaseUser;

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
