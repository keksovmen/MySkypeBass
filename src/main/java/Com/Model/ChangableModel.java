package Com.Model;

import Com.Networking.Utility.BaseUser;

public interface ChangableModel extends UnEditableModel {

    void addToModel(BaseUser user);

    void addToModel(BaseUser[] users);

    void removeFromModel(BaseUser user);

    void removeFromModel(int user);

    void addToConversation(BaseUser user);

    void removeFromConversation(BaseUser user);

    void clear();

    void clearConversation();

}
