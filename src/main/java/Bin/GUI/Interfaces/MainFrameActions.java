package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.Supplier;

public interface MainFrameActions extends FirstSkinActions, SecondSkinActions {

    /**
     * Action for getting your name and id, mean baseUser.toString()
     * @return not null action
     * @throws NotInitialisedException if return is null
     */

    Supplier<String> nameAndId() throws NotInitialisedException;

    void updateNameAndId(Supplier<String> nameAndId);
}
