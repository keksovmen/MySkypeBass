package Com.Util;

import javax.swing.*;

public class Resources {

    public static ImageIcon ricardo;
    public static ImageIcon onlineIcon;
    public static ImageIcon offlineIcon;

    static {
        ricardo = new ImageIcon(Resources.class.getResource("/Images/ricardo.png"));
        onlineIcon = new ImageIcon(Resources.class.getResource("/Images/online.png"));
        offlineIcon = new ImageIcon(Resources.class.getResource("/Images/offline.png"));
    }
}
