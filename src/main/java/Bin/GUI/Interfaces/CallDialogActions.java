package Bin.GUI.Interfaces;

import Bin.Networking.Utility.BaseUser;

import java.util.function.Consumer;

public interface CallDialogActions {

    /**
     * Action for cancel a call
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Consumer<BaseUser> cancelCall() throws IllegalStateException;

    /**
     * Action for accept a call
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Consumer<BaseUser[]> acceptCall() throws IllegalStateException;

    /**
     * Action for deny a call
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Consumer<BaseUser> denyCall() throws IllegalStateException;

    void updateCancelCall(Consumer<BaseUser> cancelCall);

    void updateAcceptCall(Consumer<BaseUser[]> acceptCall);

    void updateDenyCall(Consumer<BaseUser> denyCall);
}
