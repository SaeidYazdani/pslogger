package com.embedonix.pslogger.data.logging;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 16/07/2014.
 */
public interface LoggerListener {
    public void onAccelerometerDataReceived(AccelerometerData accData);
    public void onLocationDataReceived(LocationData locData);
    public void onMiscSensorsReceived(MiscSensorsData miscData);
}
