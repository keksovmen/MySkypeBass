package Bin.GUI.Interfaces;

import java.util.function.Supplier;

public interface ConferencePaneActions {

    Runnable endCall();
    Supplier<Boolean> mute();
}
