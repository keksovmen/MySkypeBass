package com.Abstraction.Networking.Utility.Users;

import com.Abstraction.Networking.Readers.BaseReader;
import com.Abstraction.Networking.Utility.Conversation;
import com.Abstraction.Networking.Writers.ServerWriter;

public interface AbstractServerUser {

    Conversation getConversation();

    void setConversation(Conversation conversation);

    boolean inConversation();

    ServerWriter getWriter();

    BaseReader getReader();
}
