package com.Model;

import com.Networking.Utility.Users.BaseUser;

/**
 * Interface for model that can change its values
 */

public interface ChangeableModel extends UnEditableModel {

    void addToModel(BaseUser user);

    void addToModel(BaseUser[] users);

    default void removeFromModel(BaseUser user){removeFromModel(user.getId());}

    void removeFromModel(int user);

    void addToConversation(BaseUser user);

    void removeFromConversation(BaseUser user);

    void clear();

    void clearConversation();

}
