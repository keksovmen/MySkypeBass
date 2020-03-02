package com.Abstraction.Util.Resources;

import com.Abstraction.Util.Collection.Track;

import java.util.Map;
import java.util.Properties;

public abstract class AbstractResources {

    protected final Properties properties;

    public AbstractResources() {
        properties = initialisation(createDefaultProperties());
    }

    private static Properties createDefaultProperties() {
        Properties properties = new Properties();

        properties.setProperty("MicCaptureSizeDivider", "20");//server -> audio

        properties.setProperty("HistorySize", "10");//gui

        properties.setProperty("ReaderWriterBuffer", "64"); //in kB client, server logic

        properties.setProperty("TimeOut", "10");//client logic -> gui

        properties.setProperty("DefaultName", "");//client logic -> gui
        properties.setProperty("DefaultIP", "127.0.0.1");//client logic -> gui
        properties.setProperty("DefaultPort", "8188");//client logic -> gui

        properties.setProperty("DefaultRate", "24000");//server logic -> gui

        return properties;
    }

    protected abstract Properties initialisation(Properties defaultProperties);

    public Properties getProperties() {
        return properties;
    }

    /**
     * Contain unique id as key from 0 to track amount
     *
     * @return unmodifiable view of underlying map
     */

    public abstract Map<Integer, Track> getNotificationTracks();

    public int getMiCaptureSizeDivider() {
        return Integer.parseInt(properties.getProperty("MicCaptureSizeDivider"));
    }

    public int getHistorySize() {
        return Integer.parseInt(properties.getProperty("HistorySize"));
    }

    public int getBufferSize() {
        return Integer.parseInt(properties.getProperty("ReaderWriterBuffer")) * 1024;
    }

    public int getTimeOut() {
        return Integer.parseInt(properties.getProperty("TimeOut"));
    }

    public String getDefaultName() {
        return properties.getProperty("DefaultName");
    }

    public String getDefaultIP() {
        return properties.getProperty("DefaultIP");
    }

    public String getDefaultPort() {
        return properties.getProperty("DefaultPort");
    }

    public String getDefaultRate() {
        return properties.getProperty("DefaultRate");

    }

}
