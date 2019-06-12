package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.BiConsumer;

public interface ThirdSkinActions {

    /**
     * Action for sending message, Integer is ID to write who
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    BiConsumer<Integer, String> sendMessage() throws NotInitialisedException;

    /**
     * Action for closing a tab with who you talk
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Runnable closeTab() throws NotInitialisedException;

    void updateSendMessage(BiConsumer<Integer, String> sendMessage);

    void updateCloseTab(Runnable closeTab);
}
