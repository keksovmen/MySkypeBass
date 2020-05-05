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

        properties.setProperty("PingPeriod", "10");//server logic, in minutes

        properties.setProperty("SpeedMultiplier", "0.35");//server, client network optimiser
        properties.setProperty("UnitFrameDividerServer", "2");//server, network optimiser
        properties.setProperty("UnitFrameDividerClient", "4");//client network optimiser
        properties.setProperty("ThreadSleepDuration", "1.5");//server, client network optimiser in seconds

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
        return checkIfZeroReturnDefaultInt("MicCaptureSizeDivider", 20);
    }

    public int getHistorySize() {
        return checkIfZeroReturnDefaultInt("HistorySize", 10);
    }

    /**
     *
     * @return in bytes
     */

    public int getBufferSize() {
        return checkIfZeroReturnDefaultInt("ReaderWriterBuffer", 64) * 1024;
    }

    /**
     *
     * @return in seconds
     */

    public int getTimeOut() {
        return checkIfZeroReturnDefaultInt("TimeOut", 10);
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

    /**
     *
     * @return in minutes
     */

    public int getPingPeriod(){
        return checkIfZeroReturnDefaultInt("PingPeriod", 10);
    }

    public double getSpeedMultiplier(){
        return checkIfZeroReturnDefaultDouble("SpeedMultiplier", 0.35d);
    }

    public double getUnitFrameDividerClient(){
        return checkIfZeroReturnDefaultDouble("UnitFrameDividerClient", 4d);
    }

    public double getUnitFrameDividerServer(){
        return checkIfZeroReturnDefaultDouble("UnitFrameDividerServer", 2d);
    }

    /**
     *
     * @return in seconds
     */

    public double getThreadSleepDuration(){
        return checkIfZeroReturnDefaultDouble("ThreadSleepDuration", 1.5d);
    }

    protected int checkIfZeroReturnDefaultInt(String property, int standard){
        int retrieved = Integer.parseInt(properties.getProperty(property));
        if (retrieved == 0)
            return standard;
        return retrieved;
    }

    protected double checkIfZeroReturnDefaultDouble(String property, double standard){
        double retrieved = Double.parseDouble(properties.getProperty(property));
        if (retrieved == 0)
            return standard;
        return retrieved;
    }

}
