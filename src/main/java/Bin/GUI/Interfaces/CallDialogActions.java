package Bin.GUI.Interfaces;

import java.util.function.Consumer;

public interface CallDialogActions {

    Consumer<String> cancelCall();
    Consumer<String> acceptCall();
    Consumer<String> denyCall();
}
