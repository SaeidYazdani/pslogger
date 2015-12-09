package com.embedonix.pslogger.data.logging;

/**
 * Created by Saeid on 24-4-2014.
 * <p/>
 * This class will hold a single entry of accelerometer.
 */
public class AccelerometerData extends Holder {

    public static final String TAG = "AccelerometerData";
    public static final String START_TAG = "acc";
    public static final String HEADER = START_TAG + ",milli,nano,X,Y,Z";
    public static final String PLACEHOLDER = START_TAG + "," + NOT_NEW + "," + NOT_NEW
    + "," + NOT_NEW + "," + NOT_NEW + "," + NOT_NEW;


    private long mTimestampNano;
    private float mX, mY, mZ;

    public AccelerometerData(long timestampMilli, long timestampNano, float x, float y
            , float z) {
        super(timestampMilli, START_TAG);
        mTimestampNano = timestampNano;
        mX = x;
        mY = y;
        mZ = z;
    }

    public long getTimestampNano() {
        return mTimestampNano;
    }

    public float getX() {
        return mX;
    }

    public float getY() {
        return mY;
    }

    public float getZ() {
        return mZ;
    }

    @Override
    public String getHeader() {
        return HEADER;
    }

    @Override
    public String getAsCommaSeparated(boolean limitDecimalPlaces) {
        if (!limitDecimalPlaces) {
            return getStartTag() + "," + getTimestamp() + "," + mTimestampNano
                    + "," + mX + "," + mY + "," + mZ;
        } else {
            return getStartTag() + "," + getTimestamp() + "," + mTimestampNano
                    + "," + String.format("%.4f", mX)
                    + "," + String.format("%.4f", mY)
                    + "," + String.format("%.4f", mZ);
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
        if (!(other instanceof AccelerometerData)) return false;

        //object members check
        AccelerometerData oc = (AccelerometerData) other;
        if (oc.getTimestamp() != getTimestamp()) return false;
        if (oc.getTimestampNano() != mTimestampNano) return false;

        return !(oc.getX() != mX || oc.getY() != mY || oc.getZ() != mZ);
    }
}
