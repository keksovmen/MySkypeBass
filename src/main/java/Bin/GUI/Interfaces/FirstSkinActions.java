package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.Function;

public interface FirstSkinActions extends AudioFormatStatsActions{

    /**
     * Action for connecting to a server
     * String[0...] @see clientController connect methods
     * @return not null action, true if connected false if not and null if an Exception
     * @throws NotInitialisedException if return is null
     */

    Function<String[], Boolean> connect() throws NotInitialisedException;

    void updateConnect(Function<String[], Boolean> connect);

}
