package Com.Model;

import Com.Networking.Utility.BaseUser;

import java.util.Map;
import java.util.Set;

public interface UnEditableModel {

    Map<Integer, BaseUser> getUserMap();

    Set<BaseUser> getConversation();

    boolean inConversationWith(BaseUser user);


}
