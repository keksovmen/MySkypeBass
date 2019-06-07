package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.Supplier;

public interface MainFrameActions extends FirstSkinActions, SecondSkinActions {

    Supplier<String> nameAndId() throws NotInitialisedException;

    void updateNameAndId(Supplier<String> nameAndId);
}
