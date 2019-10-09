package com;

import com.Audio.Audio;
import com.GUI.Frame;
import com.Model.ClientModelBase;
import com.Networking.ClientController;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.CODE;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Utility.WHO;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class Main {


    public static void main(String[] args) {
        CODE.uniqueIdCheck();
        WHO.uniqueIdCheck();
        AbstractDataPackagePool.init(new DataPackagePool());
//        String callSongName = Resources.callSongName;
        try {
            SwingUtilities.invokeAndWait(() -> {
                ClientModelBase model = new ClientModelBase();
                ClientController clientController = new ClientController(model);

                Frame frame = new Frame();
                clientController.registerListener(frame);
                //register audio part for package exchange actions

                model.registerListener(frame);

                frame.registerListener(clientController.getController());

                Audio audio = new Audio(clientController.getSendSoundFunction());

                clientController.registerListener(audio);
                frame.registerListener(audio);
                model.registerListener(audio);
                //register audio part for gui action
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Swing fucked up in thread invocation");
        }

    }
}