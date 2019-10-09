package com.Util;

import javax.swing.*;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

/**
 * Contain resources that can be got directly
 * Or from suitable static functions - preferred!
 */

public class Resources {

    private final static Resources instance = new Resources();

    private final List<String> messagePath = new ArrayList<>();
    private final List<String> descriptions = new ArrayList<>();
    private final Properties properties;


    private Resources() {
        XMLWorker.retrieveNames("/sound/Notifications.xml").
                forEach(pair -> {
                    messagePath.add("/sound/messageNotification/" + pair.getFirst());
                    descriptions.add(pair.getSecond());
                });

        properties = new Properties(getDefault());

        try {
            properties.load(Resources.class.getResourceAsStream("/properties/CustomProperties.properties"));
        } catch (IOException e) {
            System.err.println("Cant load custom properties, default one will be used!");
        }
    }

    private Properties getDefault() {
        Properties properties = new Properties();
        properties.put("MainIcon", "/Images/ricardo.png");
        properties.put("OnlineIcon", "/Images/online16.png");
        properties.put("OfflineIcon", "/Images/offline16.png");
        properties.put("ConversationIcon", "/Images/conversation16.png");

        properties.put("AudioFragmentSize", 8192);
        properties.put("MicQueueSize", 10);

        properties.put("HistorySize", 10);

        properties.put("ReaderWriterBuffer", 32); //in kB

        properties.put("WriterLockTime", 300);
        properties.put("TimeOut", 10);

        properties.put("DefaultName", "");
        properties.put("DefaultIP", "127.0.0.1");
        properties.put("DefaultPort", 8188);

        properties.put("DefaultRate", 44100);

        return properties;

    }

    public static Properties getProperties() {
        return instance.properties;
    }

    public static ImageIcon getMainIcon() {
        return new ImageIcon(Resources.class.getResource(instance.properties.getProperty("MainIcon")));
    }

    public static ImageIcon getOnlineIcon() {
        return new ImageIcon(Resources.class.getResource(instance.properties.getProperty("OnlineIcon")));
    }

    public static ImageIcon getOfflineIcon() {
        return new ImageIcon(Resources.class.getResource(instance.properties.getProperty("OfflineIcon")));
    }

    public static ImageIcon getConversationIcon() {
        return new ImageIcon(Resources.class.getResource(instance.properties.getProperty("ConversationIcon")));
    }

    public static List<String> getMessagePaths() {
        return instance.messagePath;
    }

    public static List<String> getDescriptions() {
        return instance.descriptions;
    }


    public static int getAudioFragmentSize() {
        return Integer.parseInt(instance.properties.getProperty("AudioFragmentSize"));
    }

    public static int getHistorySize() {
        return Integer.parseInt(instance.properties.getProperty("HistorySize"));
    }

    public static int getMicQueueSize() {
        return Integer.parseInt(instance.properties.getProperty("MicQueueSize"));
    }

    public static int getBufferSize() {
        return Integer.parseInt(instance.properties.getProperty("ReaderWriterBuffer"));
    }

    public static int getLockTime() {
        return Integer.parseInt(instance.properties.getProperty("WriterLockTime"));
    }

    public static int getTimeOut() {
        return Integer.parseInt(instance.properties.getProperty("TimeOut"));
    }

    public static String getDefaultName() {
        return instance.properties.getProperty("DefaultName");
    }

    public static String getDefaultIP() {
        return instance.properties.getProperty("DefaultIP");
    }

    public static String getDefaultPort() {
        return instance.properties.getProperty("DefaultPort");
    }

    public static String getDefaultRate() {
        return instance.properties.getProperty("DefaultRate");
    }

}
