package com.Implementation;

import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Audio.BaseAudio;
import com.Implementation.Audio.Factory.AudioDesktopFactory;
import com.Implementation.Audio.Helpers.SimpleHelper;
import com.Abstraction.Client.AbstractClient;
import com.Implementation.Client.Client;
import com.Implementation.GUI.Frame;
import com.Abstraction.Model.ClientModelBase;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.CompositeComponent;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;

public class Main {


    public static void main(String[] args) {
        CODE.uniqueIdCheck();
        WHO.uniqueIdCheck();
        AbstractDataPackagePool.init(new DataPackagePool());
        AudioSupplier.setHelper(new SimpleHelper());
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

                BaseAudio audio = new BaseAudio(client, new AudioDesktopFactory());

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