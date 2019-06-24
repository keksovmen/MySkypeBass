package Bin.GUI.Interfaces;

import java.util.function.Function;

public interface FirstSkinActions extends AudioFormatStatsActions {

    /**
     * Action for connecting to a server
     * String[0...] @see clientController connect methods
     *
     * @return not null action, true if connected false if not and null if an Exception
     * @throws IllegalStateException if return is null
     */

    Function<String[], Boolean> connect() throws IllegalStateException;

    void updateConnect(Function<String[], Boolean> connect);

}
