package com.Implementation;

import com.Abstraction.Application;
import com.Abstraction.Audio.AudioSupplier;
import com.Abstraction.Audio.BaseAudio;
import com.Abstraction.Client.AbstractClient;
import com.Abstraction.Model.ClientModelBase;
import com.Abstraction.Networking.Protocol.AbstractDataPackagePool;
import com.Abstraction.Networking.Protocol.CODE;
import com.Abstraction.Networking.Protocol.DataPackagePool;
import com.Abstraction.Networking.Utility.WHO;
import com.Abstraction.Pipeline.CompositeComponent;
import com.Abstraction.Pipeline.SimpleComponent;
import com.Abstraction.Util.Resources;
import com.Implementation.Audio.Factory.AudioDesktopFactory;
import com.Implementation.Audio.Helpers.SimpleHelper;
import com.Implementation.Client.Client;
import com.Implementation.GUI.Frame;
import com.Implementation.Util.DesktopResources;

import javax.swing.*;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class Main {

    public static final String NOTIFICATION_PATH = "/sound/messageNotification/";

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                Application application = new Application(new DesktopApplicationFactory());
                application.start();
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Swing fucked up in thread invocation");
        }
    }
}