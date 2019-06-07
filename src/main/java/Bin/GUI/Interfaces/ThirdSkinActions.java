package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.BiConsumer;

public interface ThirdSkinActions {

    BiConsumer<Integer, String> sendMessage() throws NotInitialisedException;

    Runnable closeTab() throws NotInitialisedException;

    void updateSendMessage(BiConsumer<Integer, String> sendMessage);

    void updateCloseTab(Runnable closeTab);
}
