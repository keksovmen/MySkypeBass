package com.Abstraction.Model;

import com.Abstraction.Networking.Utility.Users.BaseUser;
import com.Abstraction.Networking.Utility.Users.ClientUser;

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

    ClientUser getMyself();

    void setMyself(ClientUser me);

}
