package com.embedonix.pslogger.data.logging;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 29/07/2014.
 */
public class MiscSensorsData extends Holder {

    public static final String TAG = "MiscSensorsData";
    public static final String START_TAG = "msc";
    public static final String HEADER = START_TAG + ",milli,proximity,magnetic,light";
    public static final String PLACEHOLDER = START_TAG + "," + NOT_NEW + "," + NOT_NEW
            + "," + NOT_NEW + "," + NOT_NEW;

    private String mProximityState;
    private float mMagneticField;
    private float mLight;

    public MiscSensorsData(long timestamp, String proximityState, float magneticField, float light) {
        super(timestamp, START_TAG);
        mProximityState = proximityState;
        mMagneticField = magneticField;
        mLight = light;
    }

    public String getProximityState() {
        return mProximityState;
    }

    public double getMagneticField() {
        return mMagneticField;
    }

    public double getLight() {
        return mLight;
    }

    public String getHeader() {
        return HEADER;
    }

    @Override
    public String getAsCommaSeparated(boolean limitDecimalPlaces) {

        if (!limitDecimalPlaces) {
            return getStartTag()
                    + "," + getTimestamp()
                    + "," + mProximityState
                    + "," + (mMagneticField != INVALID_VALUE ?
                    String.valueOf(mMagneticField) : NOT_SUPPORTED)
                    + "," + (mLight != INVALID_VALUE ? String.valueOf(mLight) : NOT_SUPPORTED);
        } else {
            return getStartTag()
                    + "," + getTimestamp()
                    + "," + mProximityState
                    + "," + (mMagneticField != INVALID_VALUE ?
                    String.format("%.4f", mMagneticField) : NOT_SUPPORTED)
                    + "," + (mLight != INVALID_VALUE ? String.format("%.4f", mLight) : NOT_SUPPORTED);
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
        MiscSensorsData oc = (MiscSensorsData) other;
        if (oc.getTimestamp() != getTimestamp()) return false;
        if (!oc.getProximityState().equals(mProximityState)) return false;
        if (oc.getMagneticField() != mMagneticField) return false;
        return oc.getLight() == mLight;
    }
}
