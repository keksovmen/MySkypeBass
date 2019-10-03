package Com.Util;

import javax.swing.*;
import java.util.Properties;

public class Resources {

    public static ImageIcon ricardo;
    public static ImageIcon onlineIcon;
    public static ImageIcon offlineIcon;
    public static ImageIcon conversationIcon;

    static {
        ricardo = new ImageIcon(Resources.class.getResource("/Images/ricardo.png"));
        onlineIcon = new ImageIcon(Resources.class.getResource("/Images/online16.png"));
        offlineIcon = new ImageIcon(Resources.class.getResource("/Images/offline16.png"));
        conversationIcon = new ImageIcon(Resources.class.getResource("/Images/conversation16.png"));

//        Properties defaultProp = new Properties();
//        defaultProp.put("Buffer size", 8192);
//        defaultProp.put()
    }
}
