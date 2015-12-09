package com.embedonix.pslogger.data.logging;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 16/07/2014.
 */
public class LogData {
    private AccelerometerData mAcc;
    private LocationData mLoc;
    private MiscSensorsData mMsc;

    public LogData(AccelerometerData acc, LocationData loc, MiscSensorsData misc) {
        mAcc = acc;
        mLoc = loc;
        mMsc = misc;
    }

    public AccelerometerData getAcc() {
        return mAcc;
    }

    public LocationData getLoc() {
        return mLoc;
    }

    public MiscSensorsData getMsc() {
        return mMsc;
    }

    public String getAsCommaSeparated(boolean limitAccDecimals, boolean limitLocDecimals
            , boolean limitMiscDecimals){

        StringBuilder sb = new StringBuilder();

        if(mAcc != null) {
            sb.append(mAcc.getAsCommaSeparated(limitAccDecimals));
        } else {
            sb.append(AccelerometerData.PLACEHOLDER);
        }

        sb.append(",");

        if(mLoc != null) {
            sb.append(mLoc.getAsCommaSeparated(limitLocDecimals));
        } else {
            sb.append(LocationData.PLACEHOLDER);
        }

        sb.append(",");

        if(mMsc != null) {
            sb.append(mMsc.getAsCommaSeparated(limitMiscDecimals));
        } else {
            sb.append(MiscSensorsData.PLACEHOLDER);
        }

        return sb.toString().trim();
    }
}
