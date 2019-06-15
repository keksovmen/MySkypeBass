package Bin.GUI.Interfaces;

import java.util.function.BiConsumer;

public interface ThirdSkinActions {

    /**
     * Action for sending message, Integer is ID to write who
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    BiConsumer<Integer, String> sendMessage() throws IllegalStateException;

    /**
     * Action for closing a tab with who you talk
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Runnable closeTab() throws IllegalStateException;

    void updateSendMessage(BiConsumer<Integer, String> sendMessage);

    void updateCloseTab(Runnable closeTab);
}
