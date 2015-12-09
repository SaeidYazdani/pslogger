package com.embedonix.pslogger.settings;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import org.joda.time.DateTime;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * This class deals with SharedPreferences
 * <p/>
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 13/07/2014.
 */
public class AppPreferences {

    public static final String TAG = "AppPreferences";

    public static final String WAS_LOG_BUTTON_CHECKED = "WAS_LOG_BUTTON_CHECKED";
    public static final String CAN_UPLOAD = "CAN_UPLOAD";
    public static final String CAN_USE_MOBILE_NETWORK = "CAN_USE_MOBILE_NETWORK";
    public static final String CAN_LOG_ACCELEROMETER = "CAN_LOG_ACCELEROMETER";
    public static final String CAN_LOG_LOCATION = "CAN_LOG_LOCATION";
    public static final String CAN_LOG_MISC_SENSORS = "CAN_LOG_MISC_SENSORS";
    public static final String LOGGING_START_TIME = "LOGGING_START_TIME";
    public static final String LOGGING_STOP_TIME = "LOGGING_STOP_TIME";
    public static final String SHOULD_DELETE_FILES_AFTER_UPLOAD = "DELETE_FILES_AFTER_UPLOAD";
    public static final String SERVER_BASE_URL = "SERVER_BASE_URL";
    public static final String IS_SERVER_BASE_URL_VALID = "IS_SERVER_BASE_URL_VALID";
    public static final String IS_REGISTRATION_INCOMPLETE = "IS_REGISTRATION_INCOMPLETE";
    public static final String USER_SKIPPED_REGISTRATION = "USER_SKIPPED_REGISTRATION";
    public static final String USER_SKIPPED_SERVER_CONFIGURATION = "SKIPPED_SERVER_CONFIGURATION";
    public static final String LOGGED_IN = "LOGGED_IN";
    public static final String ACCELEROMETER_SPEED = "ACCELEROMETER_SPEED";
    public static final String ACC_GRAPH_VIEWPORT = "ACC_GRAPH_VIEWPORT";
    public static final String DEFAULT_LOCATION_PROVIDER = "DEFAULT_LOCATION_PROVIDER";
    public static final String INSERT_DETAILED_DESCRIPTION = "INSERT_DETAILED_DESCRIPTION";
    public static final String INSERT_GUIDE_COLUMN = "INSERT_GUIDE_COLUMN";
    public static final String LOCAL_COMPRESS_LOG_FILES = "LOCAL_COMPRESS_LOG_FILES";
    public static final String BACKGROUND_LOGGING_SERVICE = "BACKGROUND_LOGGING_SERVICE";
    public static final String USE_SUB_FOLDERS = "USE_SUB_FOLDERS";
    public static final String LAST_APP_LOG_UPLOAD = "LAST_APP_LOG_UPLOAD";
    public static final String CAN_UPLOAD_AFTER_RECONNECT = "CAN_UPLOAD_AFTER_RECONNECT";

    private Context mContext;
    private SharedPreferences mPreferences;


    /**
     * Initialize Application Preferences helper class
     *
     * @return AppPreferences
     */
    public AppPreferences(Context context) {
        if (mContext == null) {
            mContext = context;
            mPreferences = PreferenceManager.getDefaultSharedPreferences(mContext);
        } else {
            Log.d(TAG, "Already was initialized");
            return;
        }
    }

    public boolean wasLogButtonChecked(){
        return mPreferences.getBoolean(WAS_LOG_BUTTON_CHECKED, false);
    }

    public void setLogButtonCheckedState(boolean checked) {
        mPreferences.edit().putBoolean(WAS_LOG_BUTTON_CHECKED, checked).apply();
    }

    public boolean canUploadLogFiles() {
        return mPreferences.getBoolean(CAN_UPLOAD, false);
    }

    public boolean canUseMobileNetwork() {
        return mPreferences.getBoolean(CAN_USE_MOBILE_NETWORK, false);
    }

    public boolean canLogAccelerometer() {
        return mPreferences.getBoolean(CAN_LOG_ACCELEROMETER, false);
    }

    public boolean canLogLocation() {
        return mPreferences.getBoolean(CAN_LOG_LOCATION, false);
    }

    public boolean canLogMiscSensors() {
        return mPreferences.getBoolean(CAN_LOG_MISC_SENSORS, false);
    }

    public Date getLoggingStartTime() {
        String timeString = mPreferences.getString(LOGGING_START_TIME, "00:00");
        DateFormat df = new SimpleDateFormat("hh:mm");
        Date d = null;

        try {
            d = df.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new NullPointerException("Can not parse " + timeString + " to Date object");
        }

        return d;
    }

    public Date getLoggingStopTime() {
        String timeString = mPreferences.getString(LOGGING_STOP_TIME, "00:00");
        DateFormat df = new SimpleDateFormat("hh:mm");
        Date d = null;

        try {
            d = df.parse(timeString);
        } catch (ParseException e) {
            e.printStackTrace();
            throw new NullPointerException("Can not parse " + timeString + " to Date object");
        }

        return d;
    }

    public boolean shouldDeleteFilesAfterUpload() {
        return mPreferences.getBoolean(SHOULD_DELETE_FILES_AFTER_UPLOAD, false);
    }

    public String getServerBaseUrl() {
        return mPreferences.getString(SERVER_BASE_URL, "NULL");
    }

    public void setServerBaseUrlValidity(boolean validity) {
        mPreferences.edit().putBoolean(IS_SERVER_BASE_URL_VALID, validity).apply();
    }

    public boolean isServerBaseUrlValid(){
        return mPreferences.getBoolean(IS_SERVER_BASE_URL_VALID, false);
    }

    public void setServerBaseUrl(String text) {
        mPreferences.edit().putString(SERVER_BASE_URL, text).apply();
    }

    public void setIsRegistrationDone(boolean b) {
        mPreferences.edit().putBoolean(IS_REGISTRATION_INCOMPLETE, b).apply();
    }

    public boolean isRegistrationDone(){
        return mPreferences.getBoolean(IS_REGISTRATION_INCOMPLETE, false);
    }

    public void setUserHasSkippedRegistration(boolean b) {
        mPreferences.edit().putBoolean(USER_SKIPPED_REGISTRATION, b).apply();
    }

    public boolean isRegistrationSkipped(){
        return mPreferences.getBoolean(USER_SKIPPED_REGISTRATION, true);
    }

    public void setUserHasSkippedServerConfiguration(boolean b) {
        mPreferences.edit().putBoolean(USER_SKIPPED_SERVER_CONFIGURATION, b).apply();
    }

    public boolean isServerConfigurationSkipped(){
        return mPreferences.getBoolean(USER_SKIPPED_SERVER_CONFIGURATION, true);
    }

    public void setLoggedIn(boolean b) {
        mPreferences.edit().putBoolean(LOGGED_IN, b).apply();
    }

    public boolean isLoggedIn(){
        return mPreferences.getBoolean(LOGGED_IN, false);
    }

    public String getDefaultAccelerometerSpeed() {
        return mPreferences.getString(ACCELEROMETER_SPEED, "3");
    }

    public int getDefaultAccelerometerGraphViewPortSize() {
        return mPreferences.getInt(ACC_GRAPH_VIEWPORT, 20);
    }

    public void setAccelerometerDefaultSpeed(String speed) {
        Log.i(TAG, "Accelerometer default speed is now: " + speed);
        mPreferences.edit().putString(ACCELEROMETER_SPEED, speed).apply();

    }

    public String getDefaultLocationProvider() {
        return mPreferences.getString(DEFAULT_LOCATION_PROVIDER, "gps");
    }

    public void setDefaultLocationProvider(String itemAtPosition) {
        mPreferences.edit().putString(DEFAULT_LOCATION_PROVIDER, itemAtPosition).apply();
    }

    public boolean canLogAnything() {
        return canLogAccelerometer() || canLogLocation() || canLogMiscSensors();
    }

    public boolean canInsertDetailedDescription() {
        return mPreferences.getBoolean(INSERT_DETAILED_DESCRIPTION, true);
    }

    public boolean canInsertColumnGuide(){
        return mPreferences.getBoolean(INSERT_GUIDE_COLUMN, true);
    }

    public boolean shouldCompressForStorage(){
        return mPreferences.getBoolean(LOCAL_COMPRESS_LOG_FILES, false);
    }

    public boolean canLogInBackground() {
        return mPreferences.getBoolean(BACKGROUND_LOGGING_SERVICE, false);
    }

    public void setCanLogInBackground(boolean b) {
        mPreferences.edit().putBoolean(BACKGROUND_LOGGING_SERVICE, b).apply();
    }

    public boolean shouldUseSubFoldersForLogs() {
        return mPreferences.getBoolean(USE_SUB_FOLDERS, true);
    }

    public long getLastAppLogUploadTime() {
        return mPreferences.getLong(LAST_APP_LOG_UPLOAD, (new DateTime().getMillis() - 86400000));
    }

    public void setLastAppLogUpload(long millis) {
        mPreferences.edit().putLong(LAST_APP_LOG_UPLOAD, millis).apply();
    }

    public boolean canAutomaticallyUploadQueuedLogFilesAfterReconnect() {
       return mPreferences.getBoolean(CAN_UPLOAD_AFTER_RECONNECT, false);
    }
}
