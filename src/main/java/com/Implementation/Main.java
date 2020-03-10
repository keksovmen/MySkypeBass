package com.Implementation;

import com.Abstraction.Application;

import javax.swing.*;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;

public class Main {

    public static final String NOTIFICATION_PATH = "/sound/messageNotification/";

    public static void main(String[] args) {
        try {
            SwingUtilities.invokeAndWait(() -> {
                boolean logE = true;
                if (args.length > 0){
                    for (String arg : args) {
                        if (arg.equals("-logD")) {
                            logE = false;
                            break;
                        }
                    }
                }
                Application application = new Application(logE ?
                        new DesktopApplicationFactoryLog() : new DesktopApplicationFactory());
                application.start();
            });
        } catch (InterruptedException | InvocationTargetException e) {
            e.printStackTrace();
            System.err.println("Swing fucked up in thread invocation");
        }
    }
}