package Com.Model;

import Com.Networking.Utility.BaseUser;

/**
 * Interface for model that can change its values
 */

public interface ChangeableModel extends UnEditableModel {

    void addToModel(BaseUser user);

    void addToModel(BaseUser[] users);

    void removeFromModel(BaseUser user);

    void removeFromModel(int user);

    void addToConversation(BaseUser user);

    void removeFromConversation(BaseUser user);

    void clear();

    void clearConversation();

}
