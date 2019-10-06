package Com.Util;

import javax.swing.*;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

public class Resources {

    public static final SimpleDateFormat dateFormat = new SimpleDateFormat("HH:mm");
    public static int historySize = 10;

    public static ImageIcon ricardo;
    public static ImageIcon onlineIcon;
    public static ImageIcon offlineIcon;
    public static ImageIcon conversationIcon;

    public static List<String> messagePath = new ArrayList<>();

    public static int CAPTURE_SIZE = 8192;
    public static int QUEUE_SIZE = 10;

    public static String callSongName = "Call.WAV";



    static {
        ricardo = new ImageIcon(Resources.class.getResource("/Images/ricardo.png"));
        onlineIcon = new ImageIcon(Resources.class.getResource("/Images/online16.png"));
        offlineIcon = new ImageIcon(Resources.class.getResource("/Images/offline16.png"));
        conversationIcon = new ImageIcon(Resources.class.getResource("/Images/conversation16.png"));

        XMLWorker.retrieveNames("/sound/Notifications.xml").
                forEach(s -> messagePath.add("/sound/messageNotification/" + s));

//        Properties defaultProp = new Properties();
//        defaultProp.put("Buffer size", 8192);
//        defaultProp.put()
    }
}
