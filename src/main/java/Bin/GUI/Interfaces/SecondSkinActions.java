package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.Networking.Utility.BaseUser;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SecondSkinActions extends CallDialogActions, ConferencePaneActions, ThirdSkinActions {

    /**
     * Action for disconnecting
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Runnable disconnect() throws NotInitialisedException;

    /**
     * Action for refresh call of users
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Runnable callForUsers() throws NotInitialisedException;



//    BiConsumer<Integer, String> sendMessage() throws NotInitialisedException;

    /**
     * Action for call some one
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Consumer<BaseUser> callSomeOne() throws NotInitialisedException;

    void updateDisconnect(Runnable disconnect);

    void updateCallForUsers(Runnable callForUsers);

    void updateCallSomeOne(Consumer<BaseUser> callSomeOne);

}
