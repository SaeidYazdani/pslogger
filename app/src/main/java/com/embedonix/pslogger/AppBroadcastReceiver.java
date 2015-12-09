package com.embedonix.pslogger;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.Log;
import android.widget.Toast;

import com.embedonix.pslogger.data.user.UserInfo;
import com.embedonix.pslogger.helpres.FileHelpers;
import com.embedonix.pslogger.helpres.Storage;
import com.embedonix.pslogger.serverwork.UploadNotUploadedFiles;
import com.embedonix.pslogger.settings.AppPreferences;

import java.io.File;
import java.util.ArrayList;

public class AppBroadcastReceiver extends BroadcastReceiver {

    public static final String TAG = "AppBroadcastReceiver";

    private AppPreferences mAppPreferences;

    public AppBroadcastReceiver() {

    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent == null) {
            return;
        }

        mAppPreferences = new AppPreferences(context);

        String action = intent.getAction();

/*     don't need to check for boot :P

        if (action.equals("android.intent.action.BOOT_COMPLETED")) {
            handleBootCompleted(context, intent);
        }

*/

        if (action.equals("android.intent.action.DEVICE_STORAGE_LOW")) { //disable logging
            handleDeviceStorageIsLow(context, intent);
        }

        if (action.equals("android.intent.action.DEVICE_STORAGE_OK")) {
            handleDeviceStorageIsOk(context, intent);
        }

        if (action.equals("android.intent.action.AIRPLANE_MODE")) {
            handleAirplaneMode(context, intent);
        }

        if (action.equals("android.net.wifi.STATE_CHANGE")) { //check WiFi connected
            if (mAppPreferences.isLoggedIn()) {
                if(mAppPreferences.canAutomaticallyUploadQueuedLogFilesAfterReconnect()) {
                    handleWiFiStateChange(context, intent);
                }
            }
        }

        if (action.equals("android.net.conn.CONNECTIVITY_CHANGE")) {
            if (mAppPreferences.isLoggedIn()) {
                if(mAppPreferences.canAutomaticallyUploadQueuedLogFilesAfterReconnect()) {
                    handleConnectivityChange(context, intent);
                }
            }
        }
    }

    private void handleBootCompleted(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.app_name)
                + " BOOT_COMPLETED", Toast.LENGTH_SHORT).show();
    }

    private void handleDeviceStorageIsLow(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.app_name)
                + " DEVICE_STORAGE_LOW", Toast.LENGTH_SHORT).show();
    }

    private void handleDeviceStorageIsOk(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.app_name)
                + " DEVICE_STORAGE_OK", Toast.LENGTH_SHORT).show();
    }


    private void handleAirplaneMode(Context context, Intent intent) {
        Toast.makeText(context, context.getString(R.string.app_name)
                + " AIRPLANE_MODE", Toast.LENGTH_SHORT).show();
    }

    private void handleWiFiStateChange(Context context, Intent intent) {
        NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
        if (info != null) {
            if (info.isConnected()) {
                Log.i(TAG, "WiFi connection is now established...uploading remaining log files");
                uploadRemainingLogFiles(context);
            }
        }
    }

    private void handleConnectivityChange(Context context, Intent intent) {
        int type = intent.getIntExtra(ConnectivityManager.EXTRA_NETWORK_TYPE, -100);
        ConnectivityManager manager =
                (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = manager.getNetworkInfo(type);

        if (info != null) {

            AppPreferences prefs = new AppPreferences(context);

            if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (info.isAvailable() && info.isConnected()) {
                    Log.i(TAG, "Mobile internet is connected, uploading pending log files");
                    if (prefs.canUploadLogFiles() && prefs.canUseMobileNetwork()) {
                        uploadRemainingLogFiles(context);
                    }
                }
            }
        }
    }

    private void uploadRemainingLogFiles(Context context) {
        ArrayList<File> files = null;

        try {
            files = (ArrayList<File>) FileHelpers.getListOfNotUploadedFiles(context);
        } catch (NullPointerException e) {
            e.printStackTrace();
            return;
        }

        if (files != null && files.size() > 0) {
            Log.i(TAG, "going to upload " + files.size());
            UserInfo userInfo = Storage.readUserRegistration(context).getUserInfo();
            UploadNotUploadedFiles task = new UploadNotUploadedFiles(context
                    , userInfo);
            task.execute(files.toArray(new File[files.size()]));
        }
    }
}
