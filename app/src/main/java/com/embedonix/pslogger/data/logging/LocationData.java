package com.embedonix.pslogger.data.logging;

import android.location.Location;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 16/07/2014.
 */
public class LocationData extends Holder {

    public static final String TAG = "LocationData";
    public static final String START_TAG = "loc";
    public static final String HEADER = START_TAG
            + ",milli,fixmilli,provider,longitude,latitude,altitude";
    public static final String PLACEHOLDER = START_TAG + "," + NOT_NEW + "," + NOT_NEW
            + "," + NOT_NEW + "," + NOT_NEW + "," + NOT_NEW + "," + NOT_NEW;

    private Location mLocation;

    public LocationData(long timestamp, Location location) {
        super(timestamp, "loc");
        mLocation = location;
    }

    public Location getLocation() {
        return mLocation;
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public String getAsCommaSeparated(boolean limitDecimalPlaces) {

        if(mLocation == null) {
            return getStartTag()
                    + "," + String.valueOf(getTimestamp())
                    + "," + DISABLED_ON_DEVICE
                    + "," + DISABLED_ON_DEVICE
                    + "," + DISABLED_ON_DEVICE
                    + "," + DISABLED_ON_DEVICE
                    + "," + DISABLED_ON_DEVICE;
        }

        if (!limitDecimalPlaces) {
            return getStartTag()
                    + "," + String.valueOf(getTimestamp())
                    + "," + mLocation.getTime()
                    + "," + mLocation.getProvider()
                    + "," + mLocation.getLongitude()
                    + "," + mLocation.getLatitude()
                    + "," + mLocation.getAltitude();
        } else {
            return getStartTag()
                    + "," + String.valueOf(getTimestamp())
                    + "," + mLocation.getTime()
                    + "," + mLocation.getProvider()
                    + "," + String.format("%.4f", mLocation.getLongitude())
                    + "," + String.format("%.4f", mLocation.getLatitude())
                    + "," + String.format("%.4f", mLocation.getAltitude());
        }
    }

    @Override
    public String getNullDataPlaceHolder() {
        return PLACEHOLDER;
    }

    @Override
    public boolean equals(Object other) {

        //standard equality check
        if (other == null) return false;
        if (other == this) return true;
        if (!(other instanceof LocationData)) return false;

        //object specific
        LocationData oc = (LocationData) other;
        if (oc.getTimestamp() != getTimestamp()) return false;
        return !oc.getLocation().equals(mLocation);
    }
}
