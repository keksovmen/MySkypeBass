package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.Supplier;

public interface ConferencePaneActions {

    Runnable endCall() throws NotInitialisedException;

    Supplier<Boolean> mute() throws NotInitialisedException;

    void updateEndCall(Runnable endCall);

    void updateMute(Supplier<Boolean> mute);
}
