package Bin.GUI.Interfaces;

import java.util.function.BiConsumer;
import java.util.function.Consumer;

public interface SecondSkinActions extends CallDialogActions, ConferencePaneActions, ThirdSkinActions {

    Runnable disconnect();

    Runnable callForUsers();

    Consumer<String> sendMessage(BiConsumer<Integer, String> test);

}
