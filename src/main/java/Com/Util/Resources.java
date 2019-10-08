package Com.Util;

import javax.swing.*;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class Resources {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");

    private final static Resources instance = new Resources();

    private final List<String> messagePath = new ArrayList<>();
    private final Properties properties;


    private Resources() {
//        ricardo = new ImageIcon(Resources.class.getResource("/Images/ricardo.png"));
//        onlineIcon = new ImageIcon(Resources.class.getResource("/Images/online16.png"));
//        offlineIcon = new ImageIcon(Resources.class.getResource("/Images/offline16.png"));
//        conversationIcon = new ImageIcon(Resources.class.getResource("/Images/conversation16.png"));

        XMLWorker.retrieveNames("/sound/Notifications.xml").
                forEach(s -> messagePath.add("/sound/messageNotification/" + s));

        properties = new Properties(getDefault());

        try {
            properties.load(Resources.class.getResourceAsStream("/properties/CustomProperties.properties"));
        } catch (IOException e) {
//            e.printStackTrace();
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

    public static int getLockTime(){
        return Integer.parseInt(instance.properties.getProperty("WriterLockTime"));
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
