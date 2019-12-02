package com;

import com.Audio.Audio;
import com.Client.AbstractClient;
import com.Client.Client;
import com.GUI.Frame;
import com.Model.ClientModelBase;
import com.Networking.Protocol.AbstractDataPackagePool;
import com.Networking.Protocol.CODE;
import com.Networking.Protocol.DataPackagePool;
import com.Networking.Utility.WHO;
import com.Pipeline.CompositeComponent;
import com.Pipeline.SimpleComponent;

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
                AbstractClient client = new Client(model);

                CompositeComponent frame = new Frame();
                client.attach(frame);
                //register audio part for package exchange actions

                model.attach(frame);

                frame.attach(client);

                SimpleComponent audio = new Audio(client);

                client.attach(audio);
                frame.attach(audio);
                model.attach(audio);
                //register audio part for gui action
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Swing fucked up in thread invocation");
        }
    }
}