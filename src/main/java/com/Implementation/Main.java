package com.Implementation;

import com.Abstraction.Application;

import javax.swing.*;
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