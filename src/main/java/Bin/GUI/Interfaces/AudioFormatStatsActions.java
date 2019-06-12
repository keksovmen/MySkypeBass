package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.Function;

public interface AudioFormatStatsActions {

    /**
     * Function for creating a server
     * String[0..] values @see serer create methods
     * @return not null action
     * @throws NotInitialisedException if it is null
     */

    Function<String[], Boolean> createServer() throws NotInitialisedException;

    void updateCreateServer(Function<String[], Boolean> createServer);
}
