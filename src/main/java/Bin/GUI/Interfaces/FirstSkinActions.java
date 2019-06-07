package Bin.GUI.Interfaces;

import Bin.GUI.Forms.Exceptions.NotInitialisedException;

import java.util.function.Function;

public interface FirstSkinActions extends AudioFormatStatsActions{

    Function<String[], Boolean> connect() throws NotInitialisedException;

    void updateConnect(Function<String[], Boolean> connect);

}
