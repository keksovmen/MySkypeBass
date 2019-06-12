package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.Networking.Utility.BaseUser;

import java.util.function.Consumer;

public interface CallDialogActions {

    /**
     * Action for cancel a call
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Consumer<BaseUser> cancelCall() throws NotInitialisedException;

    /**
     * Action for accept a call
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Consumer<BaseUser[]> acceptCall() throws NotInitialisedException;

    /**
     * Action for deny a call
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Consumer<BaseUser> denyCall() throws NotInitialisedException;

    void updateCancelCall(Consumer<BaseUser> cancelCall);

    void updateAcceptCall(Consumer<BaseUser[]> acceptCall);

    void updateDenyCall(Consumer<BaseUser> denyCall);
}
