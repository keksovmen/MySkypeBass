package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;
import Bin.Networking.Utility.BaseUser;

import java.util.function.Consumer;

public interface CallDialogActions {

    Consumer<BaseUser> cancelCall() throws NotInitialisedException;

    Consumer<BaseUser[]> acceptCall() throws NotInitialisedException;

    Consumer<BaseUser> denyCall() throws NotInitialisedException;

    void updateCancelCall(Consumer<BaseUser> cancelCall);

    void updateAcceptCall(Consumer<BaseUser[]> acceptCall);

    void updateDenyCall(Consumer<BaseUser> denyCall);
}
