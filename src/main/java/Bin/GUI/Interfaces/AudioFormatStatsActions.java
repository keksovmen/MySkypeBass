package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.Function;

public interface AudioFormatStatsActions {

    Function<String[], Boolean> createServer() throws NotInitialisedException;

    void updateCreateServer(Function<String[], Boolean> createServer);
}
