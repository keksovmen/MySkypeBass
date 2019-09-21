package Com.GUI.Forms.ActionHolder;

import Com.Networking.Utility.BaseUser;

import java.util.function.BiConsumer;

public class MessangerActions {

    private final BiConsumer<String, BaseUser> sendMessage;

    public MessangerActions(BiConsumer<String, BaseUser> sendMessage) {
        this.sendMessage = sendMessage;
    }

    public BiConsumer<String, BaseUser> getSendMessage() {
        return sendMessage;
    }
}
