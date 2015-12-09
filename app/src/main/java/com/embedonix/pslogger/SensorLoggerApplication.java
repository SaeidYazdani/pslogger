package com.embedonix.pslogger;

import android.app.Application;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.graphics.BitmapFactory;
import android.preference.PreferenceManager;
import android.util.Log;

import com.embedonix.pslogger.helpres.AppLogType;
import com.embedonix.pslogger.helpres.FileHelpers;
import com.embedonix.pslogger.helpres.Storage;
import com.embedonix.pslogger.data.user.UserRegistration;
import com.embedonix.pslogger.data.logging.LoggingMode;
import com.embedonix.pslogger.services.LoggerService;
import com.embedonix.pslogger.data.logging.Logger;
import com.embedonix.pslogger.settings.AppPreferences;

import org.joda.time.DateTime;

/**
 * This is the application's main entry point
 * <p />
 * <p>Methods which are useful through the lifecycle of the application are declared here
 * </p>
 * <p/>
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 13/07/2014.
 */
public class SensorLoggerApplication extends Application {

    public static final String TAG = "SensorLoggerApplication";
    private AppPreferences mAppPreferences;
    private UserRegistration mUserRegistration;
    private Logger mLogger;

    private static final int NOTIFICATION_ID_BACKGROUND_LOGGER = 0x75;
    private static final String NOTIFICATION_TAG_BACKGROUND_LOGGER = "sensorlogger_background";

    private static final int NOTIFICATION_ID_FOREGROUND_LOGGER = 0x76;
    private static final String NOTIFICATION_TAG_FOREGROUND_LOGGER = "sensorlogger_foreground";

    private NotificationManager mNotificationManager;
    private Notification mBackgroundNotification;
    private Notification mForegroundNotification;

    @Override
    public void onCreate() {
        super.onCreate();

        mAppPreferences = new AppPreferences(getApplicationContext());

        mUserRegistration = Storage.readUserRegistration(getApplicationContext());
        if (mUserRegistration == null) { //its first time install, or after delete cache by user
            PreferenceManager.setDefaultValues(this, R.xml.preferences, false);
            mAppPreferences.setIsRegistrationDone(false);
        }

        mLogger = new Logger(LoggingMode.FOREGROUND, this);
        mLogger.start(getAppPreferences().canLogAccelerometer()
                , getAppPreferences().canLogLocation()
                , getAppPreferences().canLogMiscSensors());

        mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

        FileHelpers.appendToAppLog(new DateTime().getMillis(), AppLogType.INF,
                TAG, "SensorLoggerApplication onCreate()");

    }

    /**
     * Get a reference to current instance of
     * {@link com.embedonix.pslogger.data.logging.Logger} object which takes care
     * of adding sensors and location reading to a queue, saving to storage and uploading.
     *
     * @return An already existing reference to Logger instance. if Logger instance is null, a new
     * one will be created and returned. this shouldn't happen in reality.
     */
    public Logger getLogger(String callerTag, LoggingMode mode){
        if(mLogger == null) {
            Log.e(TAG, "Logger was null, returning a new instance. Requested by "
                    + callerTag);
            mLogger = new Logger(mode, this);
            mLogger.start(getAppPreferences().canLogAccelerometer()
                    , getAppPreferences().canLogLocation()
                    , getAppPreferences().canLogMiscSensors());
            FileHelpers.appendToAppLog(new DateTime().getMillis(), AppLogType.ERR, TAG
                    , "Logger instance was null while requested by '" + callerTag + "' on " + mode);
        }

        return mLogger;
    }

    /**
     * Get a reference to AppPreferences which is a wrapped for SharedPreferences
     *
     * @return AppPreferences
     */
    public AppPreferences getAppPreferences() {
        if (mAppPreferences == null) {
            Log.w(TAG, "AppPreferences instance was null, creating an instance for it");
            mAppPreferences = new AppPreferences(getApplicationContext());
        }
        return mAppPreferences;


    }

    /**
     * Get a reference to UserRegistration
     *
     * @param newReference If given true, a new reference will be created and returned. reading
     *                     objects from disk can be expensive so only request a new reference
     *                     if only necessary
     *
     * @return UserRegistration
     */
    public UserRegistration getUserRegistration(boolean newReference){

        boolean justCreatedFromNull = false;

        if(mUserRegistration == null){
            mUserRegistration = Storage.readUserRegistration(getApplicationContext());
            justCreatedFromNull = true;
        }

        if(newReference && !justCreatedFromNull) {
            Log.w(TAG, "AppPreferences instance was null, creating an instance for it");
            mUserRegistration = Storage.readUserRegistration(getApplicationContext());
        }

        return mUserRegistration;
    }

    /**
     * Shows a notification on status bar about the app is active in background
     */
    public void showLoggingInBackgroundNotification() {

        String title = "Logging in background";

        StringBuilder sb = new StringBuilder();

        if(mAppPreferences.wasLogButtonChecked()){
            sb.append(mAppPreferences.canLogAccelerometer() ? "Accelerometer " : "");
            sb.append(mAppPreferences.canLogLocation() ? "Location " : "" );
            sb.append(mAppPreferences.canLogMiscSensors()? "Misc. Sensors " : "");
        }

        int icon = (mAppPreferences.wasLogButtonChecked() ? android.R.drawable.ic_media_play :
                    R.drawable.ic_action_stop);

        Intent notificationIntent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        mBackgroundNotification =
                getNotificationBuilder(title, sb.toString()
                        , icon).setContentIntent(contentIntent).build();
        mBackgroundNotification.flags |= Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(NOTIFICATION_TAG_BACKGROUND_LOGGER
                , NOTIFICATION_ID_BACKGROUND_LOGGER, mBackgroundNotification);
    }

    /**
     * Shows a notification on status bar about the app is active in background
     */
    public void showLoggingInForegroundNotification() {

        String title = "Logging in foreground";

        StringBuilder sb = new StringBuilder();

        if(mAppPreferences.wasLogButtonChecked()){
            sb.append(mAppPreferences.canLogAccelerometer() ? "Accelerometer " : "");
            sb.append(mAppPreferences.canLogLocation() ? "Location " : "" );
            sb.append(mAppPreferences.canLogMiscSensors()? "Misc. Sensors " : "");
        }

        int icon = (mAppPreferences.wasLogButtonChecked() ? android.R.drawable.ic_media_play :
                R.drawable.ic_action_stop);

        Intent notificationIntent = new Intent(this, MainActivity.class)
                .setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);

        PendingIntent contentIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        mForegroundNotification =
                getNotificationBuilder(title, sb.toString()
                        , icon).setContentIntent(contentIntent).build();
        mForegroundNotification.flags |= Notification.FLAG_ONGOING_EVENT;

        mNotificationManager.notify(NOTIFICATION_TAG_FOREGROUND_LOGGER
                , NOTIFICATION_ID_FOREGROUND_LOGGER, mForegroundNotification);
    }

    /**
     * Removes status bar notification about logging, if it exist
     * @param mode which mode to remove its notification
     * {@link com.embedonix.pslogger.data.logging.LoggingMode}
     */
    public void removeLoggingStatusNotification(LoggingMode mode){
        switch (mode){
            case BACKGROUND:
                if(mNotificationManager != null) {
                    mNotificationManager.cancel(NOTIFICATION_TAG_BACKGROUND_LOGGER
                            , NOTIFICATION_ID_BACKGROUND_LOGGER);
                }
                break;
            case FOREGROUND:
                if(mNotificationManager != null) {
                    mNotificationManager.cancel(NOTIFICATION_TAG_FOREGROUND_LOGGER
                            , NOTIFICATION_ID_FOREGROUND_LOGGER);
                }
                break;
        }
    }

    private Notification.Builder getNotificationBuilder(String title, String msg, int smallIcon) {
        Notification.Builder  builder = new Notification.Builder(this);
        builder.setContentTitle(title)
        .setContentText(msg)
        .setSmallIcon(smallIcon)
        .setLargeIcon(BitmapFactory.decodeResource(getResources(), R.drawable.ic_launcher));

        return builder;
    }

    /**
     * Start logging in background using
     * {@link com.embedonix.pslogger.services.LoggerService}
     */
    public void startBackgroundLogger() {
        if(mAppPreferences.wasLogButtonChecked()) {
            Log.i(TAG, "Trying to start to background logger service");
            mLogger.changeMode(LoggingMode.BACKGROUND);
            Intent backgroundLogger = new Intent(this, LoggerService.class);
            backgroundLogger.setAction(LoggerService.ACTION_START);
            startService(backgroundLogger);
        }
        else {
            Log.i(TAG, "Logging is in STOPPED state, not starting the background logger");
        }
    }

    /**
     * Stop logging in background
     */
    public void stopBackgroundLogger(){
        Log.i(TAG, "Trying to stop background logger service");
        Intent backgroundLogger = new Intent(this, LoggerService.class);
        backgroundLogger.setAction(LoggerService.ACTION_STOP);
        backgroundLogger.putExtra("CALLER", TAG);
        startService(backgroundLogger);
    }

    public String getAboutAppString(){

        return  "PS Logger is designed and developed by Pervasive Systems Research Group of"
                + " University Twente. "
                + "PS Logger is an application to log various sensors of mobile phones for"
                + " scientific purposes.\n\n"
                + "Copyright 2014 - University Twente\n\n"
                + "http://www.ps.ewi.utwente.nl/";
    }
}
