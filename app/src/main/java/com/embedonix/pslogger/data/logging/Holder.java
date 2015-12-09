package com.embedonix.pslogger.data.logging;

import java.text.SimpleDateFormat;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 29/07/2014.
 */
public abstract class Holder {

    /**
     * This value should be assigned to sensors which they are not supported on current device
     * to indicate in the {@see #getAsCommaSeparated} that their respective field should be replaced
     * with {@see #NOT_SUPPORTED} to indicate in the log file that this sensor is not supported
     */
    public static final float INVALID_VALUE = -100000;

    /**
     * If a sensor is not present on current device, this value should be put in its respective
     * field in the log file
     */
    public static final String NOT_SUPPORTED = "NS";

    /**
     * If a sensor is interrupt based (such as proximity sensor) this value should go in its
     * respective field in the log file, indicating that this sensor has not been triggered yet.
     */
    public static final String NOT_TRIGGERED = "NT";

    /**
     * This should be used in the log file if a sensor is not updated since last time a log entry
     * was received by the {@link com.embedonix.pslogger.data.logging.Logger}
     */
    public static final String NOT_NEW = "N";

    /**
     * This value should be used when the location provider is disabled on device. meaning Disabled.
     */
    public static final String DISABLED_ON_DEVICE = "D";

    /**
     * This indicates that proximity sensor reported it is in FAR state
     */
    public static final String PROXIMITY_FAR = "FAR";

    /**
     * This indicates that proximity sensor reported it is in NEAR state
     */
    public static final String PROXIMITY_NEAR = "NEAR";

    private long mTimestamp;
    private String mStartTag;

    protected Holder(long timestamp, String startTag) {
        mTimestamp = timestamp;
        mStartTag = startTag;
    }

    public long getTimestamp() {
        return mTimestamp;
    }

    public String getStartTag() {
        return mStartTag;
    }

    public String getDateTime() {
        return new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(mTimestamp);
    }

    protected abstract String getHeader();

    protected abstract String getAsCommaSeparated(boolean limitDecimalPlaces);

    protected abstract String getNullDataPlaceHolder();
}
