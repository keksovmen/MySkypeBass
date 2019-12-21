package com.Abstraction.Util;

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

        properties.setProperty("MicQueueSize", "10");//audio
        properties.setProperty("MicCaptureSizeDivider", "8");//server -> audio

        properties.setProperty("HistorySize", "10");//gui

        properties.setProperty("ReaderWriterBuffer", "32"); //in kB client, server logic

        properties.setProperty("WriterLockTime", "300");//server
        properties.setProperty("TimeOut", "10");//client logic -> gui

        properties.setProperty("DefaultName", "");//client logic -> gui
        properties.setProperty("DefaultIP", "127.0.0.1");//client logic -> gui
        properties.setProperty("DefaultPort", "8188");//client logic -> gui

        properties.setProperty("DefaultRate", "44100");//server logic -> gui

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

    public int getMicQueueSize() {
        return Integer.parseInt(properties.getProperty("MicQueueSize"));
    }

    public int getMiCaptureSizeDivider() {
        return Integer.parseInt(properties.getProperty("MicCaptureSizeDivider"));
    }

    public int getHistorySize() {
        return Integer.parseInt(properties.getProperty("HistorySize"));
    }

    public int getBufferSize() {
        return Integer.parseInt(properties.getProperty("ReaderWriterBuffer")) * 1024;
    }

    public int getLockTime() {
        return Integer.parseInt(properties.getProperty("WriterLockTime"));
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
