package Bin.GUI.Interfaces;

import java.util.function.Consumer;

public interface ThirdSkinActions {

    Consumer<String> sendMessage();

    Runnable closeTab();
}
