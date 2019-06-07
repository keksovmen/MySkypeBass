package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.Networking.Utility.BaseUser;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SecondSkinActions extends CallDialogActions, ConferencePaneActions, ThirdSkinActions {

    Runnable disconnect() throws NotInitialisedException;

    Runnable callForUsers() throws NotInitialisedException;

    BiConsumer<Integer, String> sendMessage() throws NotInitialisedException;

    Consumer<BaseUser> callSomeOne() throws NotInitialisedException;

    void updateDisconnect(Runnable disconnect);

    void updateCallForUsers(Runnable callForUsers);

    void updateCallSomeOne(Consumer<BaseUser> callSomeOne);

}
