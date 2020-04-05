package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.ClientUser;
import com.Abstraction.Networking.Utility.Users.User;

/**
 * Interface for model that can change its values
 */

public interface ChangeableModel extends UnEditableModel {

    void addToModel(User user);

    void addToModel(User[] users);

    default void removeFromModel(User user) {
        removeFromModel(user.getId());
    }

    void removeFromModel(int user);

    void addToConversation(User user);

    void removeFromConversation(User user);

    /**
     * Clear both conversation list and users list
     */

    void clear();

    /**
     * Clears only conversation list
     */

    void clearConversation();

    ClientUser getMyself();

    void setMyself(ClientUser me);

    User getUser(int id);

}
