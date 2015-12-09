package com.embedonix.pslogger.services;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

import com.embedonix.pslogger.SensorLoggerApplication;
import com.embedonix.pslogger.data.logging.LoggingMode;
import com.embedonix.pslogger.helpres.AppLogType;
import com.embedonix.pslogger.helpres.FileHelpers;

import org.joda.time.DateTime;

public class LoggerService extends Service {

    public final static String TAG = "LoggerService";

    public static String ACTION_START = "com.embedonix.pslogger.action.START_LOGGING_SERVICE";
    public static String ACTION_STOP = "com.embedonix.pslogger.action.STOP_LOGGING_SERVICE";

    private SensorLoggerApplication mApplication;
    private SensorHandler mListeners;
    private int mValidStartRequestCount;


    @Override
    public void onCreate() {
        Log.d(TAG, "Started on Thread #" + Thread.currentThread().getId());
        mApplication = (SensorLoggerApplication) getApplication();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {

        Log.d(TAG, "onStartCommand() start id " + startId
                + " on Thread #" + Thread.currentThread().getId());

        if (intent != null) {
            String action = intent.getAction();
            if (ACTION_START.equalsIgnoreCase(action)) {
                ++mValidStartRequestCount;
                if(mApplication == null) {
                    mApplication = (SensorLoggerApplication) getApplication();
                }
                startLogging();
                return START_REDELIVER_INTENT;
            } else if (ACTION_STOP.equalsIgnoreCase(action)) {
                String caller = intent.getExtras().getString("CALLER", "NONE");
                if (caller.equals(SensorLoggerApplication.TAG)) {
                    stopLogging();
                } else {
                    stopLogging();
                    stopSelf();
                }
                return START_NOT_STICKY;
            } else {
                Log.e(TAG, "intent's action is not recognizable");
            }
        } else {
            Log.e(TAG, "Received a null intent. can not get action");
        }

        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy() {
        mApplication.getLogger(TAG, LoggingMode.BACKGROUND).finalizeCurrentQueue();
        if(mListeners != null) {
            mListeners.stop();
        }
        super.onDestroy();
    }

    private void startLogging() {

        if(!mApplication.getAppPreferences().wasLogButtonChecked()) {
            Log.e(TAG, "Log button was not checked, not starting background logger");
            FileHelpers.appendToAppLog(new DateTime().getMillis(), AppLogType.ERR, TAG,
                    "startLogging() is reached out of control of the application.");
            stopSelf();
            return;
        }

        Log.i(TAG, "starting background logger");

        if (mListeners != null) {
            mListeners.stop();
        }
        mListeners = new SensorHandler(mApplication,
                mApplication.getLogger(TAG, LoggingMode.FOREGROUND));
        mListeners.start();
        mApplication.showLoggingInBackgroundNotification();
    }

    private void stopLogging() {
        if (mListeners != null) {
            mListeners.stop();
            mListeners = null;
            mApplication.removeLoggingStatusNotification(LoggingMode.BACKGROUND);
        } else {
            Log.i(TAG, "service was not started before. valid start request count: "
                    + mValidStartRequestCount);
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
