package Bin.GUI.Interfaces;

import Bin.Networking.Utility.BaseUser;

import java.util.function.Consumer;

public interface SecondSkinActions extends CallDialogActions, ConferencePaneActions, ThirdSkinActions {

    /**
     * Action for disconnecting
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Runnable disconnect() throws IllegalStateException;

    /**
     * Action for refresh call of users
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Runnable callForUsers() throws IllegalStateException;



//    BiConsumer<Integer, String> sendMessage() throws NotInitialisedException;

    /**
     * Action for call some one
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Consumer<BaseUser> callSomeOne() throws IllegalStateException;

    void updateDisconnect(Runnable disconnect);

    void updateCallForUsers(Runnable callForUsers);

    void updateCallSomeOne(Consumer<BaseUser> callSomeOne);

}
