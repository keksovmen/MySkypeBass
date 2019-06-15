package Bin.GUI.Interfaces;

import java.util.function.Supplier;

public interface MainFrameActions extends FirstSkinActions, SecondSkinActions {

    /**
     * Action for getting your name and id, mean baseUser.toString()
     * @return not null action
     * @throws IllegalStateException if return is null
     */

    Supplier<String> nameAndId() throws IllegalStateException;

    void updateNameAndId(Supplier<String> nameAndId);
}
