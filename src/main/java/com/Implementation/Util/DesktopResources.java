package com.Implementation.Util;

import com.Abstraction.Util.Resources.AbstractResources;
import com.Abstraction.Util.Collection.Pair;
import com.Abstraction.Util.Collection.Track;
import com.Abstraction.Util.Resources.Resources;

import java.io.IOException;
import java.net.URL;
import java.nio.file.NoSuchFileException;
import java.util.*;

public class DesktopResources extends AbstractResources {


    private final Map<Integer, Track> notificationTracks;

    public DesktopResources() {
        notificationTracks = new HashMap<>();
        int id = 0;
        List<Pair<String, String>> pairs;
        try {
            pairs = XMLWorker.retrieveNames(Checker.getCheckedInput("/sound/Notifications.xml"));
        } catch (NoSuchFileException e) {
            e.printStackTrace();
            pairs = new ArrayList<>();
        }
        for (Pair<String, String> pair : pairs) {
            notificationTracks.put(id, new Track(pair.getFirst(), pair.getSecond()));
            id++;
        }
    }

    @Override
    protected Properties initialisation(Properties defaultProperties) {
        defaultProperties.setProperty("AudioFragmentSize", "8192");
        defaultProperties.setProperty("MainIcon", "/Images/ricardo.png");
        defaultProperties.setProperty("OnlineIcon", "/Images/online16.png");
        defaultProperties.setProperty("OfflineIcon", "/Images/offline16.png");
        defaultProperties.setProperty("ConversationIcon", "/Images/conversation16.png");

        Properties desktopProp = new Properties(defaultProperties);

        try {
            desktopProp.load(Resources.class.getResourceAsStream("/properties/CustomProperties.properties"));
            return desktopProp;
        } catch (IOException e) {
            e.printStackTrace();
            System.err.println("Can't read desired resources, default one will be used");
            return defaultProperties;
        }
    }

    public int getAudioFragmentSize() {
        return Integer.parseInt(properties.getProperty("AudioFragmentSize"));
    }

    public URL getMainIcon() {
        return DesktopResources.class.getResource(properties.getProperty("MainIcon"));
    }

    public URL getOnlineIcon() {
        return DesktopResources.class.getResource(properties.getProperty("OnlineIcon"));
    }

    public URL getOfflineIcon() {
        return DesktopResources.class.getResource(properties.getProperty("OfflineIcon"));
    }

    public URL getConversationIcon() {
        return DesktopResources.class.getResource(properties.getProperty("ConversationIcon"));
    }

    @Override
    public Map<Integer, Track> getNotificationTracks() {
        return Collections.unmodifiableMap(notificationTracks);
    }
}
