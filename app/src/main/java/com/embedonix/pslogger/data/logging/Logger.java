package com.embedonix.pslogger.data.logging;

import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.util.Log;

import com.embedonix.pslogger.R;
import com.embedonix.pslogger.data.user.UserInfo;
import com.embedonix.pslogger.helpres.AppConstants;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.AppLogType;
import com.embedonix.pslogger.helpres.FileHelpers;
import com.embedonix.pslogger.helpres.Storage;
import com.embedonix.pslogger.serverwork.ServerConstants;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.joda.time.DateTime;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * <p>Logger will take care of adding sensor and location data to an queue. It automatically
 * calculates the necessary size for the queue depending on what things it should log. The
 * Logger also takes care of writing logged data to storage and also uploading them to server</p>
 * <p/>
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 16/07/2014.
 */
public class Logger {

    public static final String TAG = "Logger";
    int accCount = 0;
    int locCount = 0;
    int mscCount = 0;
    private Context mContext;
    private Queue<LogData> mQueue;
    private Thread mProcessThread;
    private boolean mRunning;
    private LoggingMode mLoggingMode;
    private int mThreshold = 5000;
    private MiscSensorsData mLastMiscData;
    private LocationData mLastLocData;
    private ArrayList<LogData> mTempData;
    private List<Sensor> mSensorNames;
    private String mAppName;
    private String mAppVersionName;

    public Logger(LoggingMode loggingMode, Context context) {
        mLoggingMode = loggingMode;
        mContext = context;
        mQueue = new ConcurrentLinkedQueue<LogData>();
        mAppName = mContext.getString(R.string.app_name);

        //find app version
        try {
            mAppVersionName = mContext.getPackageManager()
                    .getPackageInfo(mContext.getPackageName(), 0).versionName;
        } catch (PackageManager.NameNotFoundException e) {
            mAppVersionName = "unknown version";
            e.printStackTrace();
        }

        //get sensor model names
        SensorManager sm = (SensorManager) context.getSystemService(context.SENSOR_SERVICE);
        mSensorNames = sm.getSensorList(Sensor.TYPE_ALL);


        Log.i(TAG, mLoggingMode + " Starting on Thread #" + Thread.currentThread().getId());
    }

    private String getFileHeader(int size) {

        StringBuilder sb = new StringBuilder();

        if (AppHelpers.getAppPreferences(mContext).canInsertDetailedDescription()) {
            sb.append("# " + mAppName + " " + mAppVersionName + "\r\n");
            sb.append("# Copyright 2014 - University Twente - Pervasive Systems Research Group\r\n");
            sb.append("# \tDesign & development: Saeid Yazdani @saeidkalhor\r\n");
            sb.append("# local time of creating time: "
                    + new SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
                    .format(new DateTime().getMillis()) + "\r\n");
            sb.append("# Logging mode for this file: " + String.valueOf(mLoggingMode).toLowerCase()
                    + "\r\n");
            sb.append("# NOTE: timestamps in the log data are all UTC.\r\n"
                    + "# You may need to convert it according your local time if necessary\r\n");
            sb.append("#\r\n#\r\n");
            sb.append("# Quick description about log file is given here. please refer to app manual"
                    + " for full details\r\n#\r\n");
            sb.append("#\t" + Holder.NOT_NEW + " = data is not available or is not updated since last time it"
                    + " was reported\r\n");
            sb.append("#\t" + Holder.DISABLED_ON_DEVICE
                    + " = location provider on the device was/is disabled\r\n");
            sb.append("#\t" + Holder.NOT_TRIGGERED
                    + " = sensor is not triggered, this is the case for interrupt" +
                    " based sensors such as proximity sensor\r\n");
            sb.append("#\t" + Holder.NOT_SUPPORTED
                    + " = sensor is not supported on this device\r\n#\r\n"); //last line of desc
            sb.append("# List of sensors on current device:\r\n");
            sb.append("#\t" + getDeviceName() + "\r\n");
            for (Sensor s : mSensorNames) {
                sb.append("#\t" + s.getVendor() + " " + s.getName()
                        + ", MAX_RANGE = " + s.getMaximumRange()
                        + ", POWER = " + s.getPower()
                        + ", RESOLUTION = " + s.getResolution()
                        + "\r\n");
            }
            sb.append("#\r\n");
            sb.append("# TOTAL_DATA_ROWS = " + size + "\r\n");
        }
        if (AppHelpers.getAppPreferences(mContext).canInsertColumnGuide()) {
            sb.append(AccelerometerData.HEADER
                    + "," + LocationData.HEADER
                    + "," + MiscSensorsData.HEADER + "\r\n");
        }

        sb.append("");

        return sb.toString();
    }

    private void onHandleDataResult(Boolean result) {

    }

    public void start(boolean isAccelerometerActive, boolean isLocationActive
            , boolean isMiscSensorsActive) {
        setQueueThreshold(isAccelerometerActive, isLocationActive, isMiscSensorsActive);
        startThread();
    }

    /**
     * <p>
     * This is for when accelerometer is not being logged (disabled by user). because the logger
     * assumes the accelerometer is the fastest reporting sensor, it bases its work on the
     * reports coming from accelerometer. In case the accelerometer is not being logged, the
     * threshold should be lowered to minimize loss of data.
     * </p>
     * <p/>
     *
     * @param isAccelerometerActive
     * @param isLocationActive
     * @param isMiscSensorsActive
     */
    public void setQueueThreshold(boolean isAccelerometerActive, boolean isLocationActive
            , boolean isMiscSensorsActive) {

        int oldThreshold = mThreshold;
        int recommendedValue = 0;

        if (isAccelerometerActive) {

            String defAccSpeed = AppHelpers.getAppPreferences(mContext)
                    .getDefaultAccelerometerSpeed();

            if (defAccSpeed.equals("fastest")) {
                recommendedValue += 2000;
            }

            recommendedValue += 3000;
        }

        if (isLocationActive) {
            recommendedValue += 500;
        }

        if (isMiscSensorsActive) {
            recommendedValue += 1000;
        }

        if (recommendedValue != mThreshold) {

            //threshold is changed, so first finalizeCurrentQueue the current queue
            if (mProcessThread != null) {
                if (mQueue != null && mQueue.size() > 0) {
                    showDebugCounters();
                    mRunning = false; //pause the runnable of the mProcessThread
                    ArrayList<LogData> data = new ArrayList<LogData>();
                    while (!mQueue.isEmpty()) {
                        data.add(mQueue.poll());
                    }
                    mRunning = true; //resuming the runnable
                    new HandleDataTask(data).execute();
                }
            }

            mThreshold = recommendedValue;
            Log.i(TAG, mLoggingMode + " Queue threshold changed from " + oldThreshold
                    + " to " + mThreshold);
        }
    }

    private void startThread() {
        if (mProcessThread != null) {
            return;
        }
        if (mQueue == null) {
            mQueue = new ConcurrentLinkedQueue<LogData>();
        }

        mProcessThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (mRunning) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    if (mQueue.size() > mThreshold) {
                        showDebugCounters();
                        mTempData = new ArrayList<LogData>();
                        while (!mQueue.isEmpty()) {
                            mTempData.add(mQueue.poll());
                        }
                        new HandleDataTask(mTempData).execute();
                    }
                }
            }

        });
        mProcessThread.start();

        mRunning = true;
    }

    private void showDebugCounters() {
        StringBuilder sb = new StringBuilder();

        sb.append("\n");
        sb.append("\nGoing to write queue to disk.");
        sb.append("\nQueue size: " + mQueue.size());
        sb.append("\nNumber of AccelerometerData objects received :" + accCount);
        sb.append("\nNumber of LocationData objects received:" + locCount);
        sb.append("\nNumber of MiscSensorsData objects received: " + mscCount);

        Log.i(TAG, mLoggingMode + sb.toString());

        accCount = locCount = mscCount = 0;
    }

    /**
     * Adds an instance of LogData to the queue
     *
     * @param data
     */
    public void addToQueue(LogData data) {
        accCount++;
        mQueue.offer(new LogData(data.getAcc(), mLastLocData, mLastMiscData));
        mLastLocData = null;
        mLastMiscData = null;
    }

    /**
     * Updates the current valid instance of location to be used in the log
     *
     * @param locData
     */
    public void updateLocationData(LocationData locData) {
        //TODO maybe its good idea to check if coming locData is different than stored mLastLocData
        locCount++;
        mLastLocData = locData;
    }

    /**
     * Updates the current valid instance of miscellaneous sensors to be used in the log
     *
     * @param miscData
     */
    public void updateMiscSensorsData(MiscSensorsData miscData) {
        //TODO maybe check if miscData is equal or not to mLastMiscData
        mscCount++;
        mLastMiscData = miscData;
    }

    /**
     * This method will write all the remaining data of the queue to storage
     */
    public void finalizeCurrentQueue() {
        Log.i(TAG, mLoggingMode + " finalizing the queue");

        if (mProcessThread != null) {
            if (mQueue.size() > 0) {
                showDebugCounters();
                mRunning = false; //to stop runnable
                ArrayList<LogData> data = new ArrayList<LogData>();
                while (!mQueue.isEmpty()) {
                    data.add(mQueue.poll());
                }
                mRunning = true;
                new HandleDataTask(data).execute();
            }
        }
    }

    public void changeMode(LoggingMode mode) {
        mLoggingMode = mode;
    }

    public String getDeviceName() {

        String deviceInfo = "";

        try {
            deviceInfo = Build.MANUFACTURER;
            deviceInfo += " " + Build.MODEL;

        } catch (Exception e) {
            deviceInfo = "UNKNOWN";
        }

        return deviceInfo;
    }


    private class HandleDataTask extends AsyncTask<Void, Void, Boolean> {

        private HandleDataTask instance = null;
        private ArrayList<LogData> logData;

        private HandleDataTask(ArrayList<LogData> logData) {
            this.logData = logData;
            instance = this;
        }

        @Override
        protected Boolean doInBackground(Void... params) {

            long now = new DateTime().getMillis();
            File dir = null;
            File file = null;

            if (AppHelpers.getAppPreferences(mContext).shouldUseSubFoldersForLogs()) {

                String monthFolder = FileHelpers.getFolderForMonth(now);
                String todayFolder = FileHelpers.getFolderForDay(now);

                dir = new File(Environment.getExternalStorageDirectory() + File.separator
                        + AppConstants.LOG_FOLDER_NAME + File.separator + monthFolder
                        + File.separator + todayFolder);
            } else {
                dir = new File(Environment.getExternalStorageDirectory() + File.separator
                        + AppConstants.LOG_FOLDER_NAME);
            }

            dir.mkdirs(); //make directory structure if not exist

            String fileName = FileHelpers.getNewFileName(now
                    , mContext.getString(R.string.app_name).toUpperCase());

            file = new File(dir, fileName);

            try { //write content to file
                FileWriter writer = new FileWriter(file);
                String fileHeader = getFileHeader(logData.size());
                if (fileHeader != null && !fileHeader.isEmpty()) {
                    writer.write(getFileHeader(logData.size()));
                }
                for (int i = 0; i < logData.size(); i++) {
                    writer.write(logData.get(i).getAsCommaSeparated(true, true, true)
                            + "\r\n");
                }
                writer.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            if (!(file.length() > 0)) {
                Log.e(TAG, "failed to write list of LogData to disk");
                boolean deleted = file.delete();
                return false;
            }


            boolean loggedIn = AppHelpers.getAppPreferences(mContext).isLoggedIn();//user logged in?
            boolean serverOk = AppHelpers.getAppPreferences(mContext)
                    .isServerBaseUrlValid(); //server url is valid
            boolean canUpload = AppHelpers.getAppPreferences(mContext).canUploadLogFiles();
            boolean internet = AppHelpers.isInternetAvailable(mContext); //internet available?
            boolean isWiFi = AppHelpers.isOnWifiInternet(mContext);
            boolean isMobile = AppHelpers.isOnMobileInternet(mContext); //is on mobile internet?
            boolean useMobile = AppHelpers.getAppPreferences(mContext)
                    .canUseMobileNetwork(); // can ise mobile internet?
            boolean mustDelete = AppHelpers.getAppPreferences(mContext)
                    .shouldDeleteFilesAfterUpload();
            boolean shouldCompress
                    = AppHelpers.getAppPreferences(mContext).shouldCompressForStorage();

            //Under the conditions below, it is not possible to upload file to server
            if (!loggedIn || !serverOk || !internet || !canUpload) {

                if (shouldCompress) {
                    FileHelpers.compressAndStoreFile(mContext, file, true);
                }

                FileHelpers.addFileToNotUploadedFiles(mContext, file);

                return true;
            }

            //These conditions mean it is possible to upload file to server
            if (loggedIn && serverOk && canUpload && internet) {

                UserInfo userInfo = Storage.readUserRegistration(mContext).getUserInfo();

                if (isWiFi || (isMobile && useMobile)) {
                    new UploadFileTask(instance, file, userInfo).execute();
                }
            }

            return null;
        }
    }

    private class UploadFileTask extends AsyncTask<Void, Void, Void> {

        private HandleDataTask caller = null;
        private File file;
        private UserInfo userInfo;

        private UploadFileTask(HandleDataTask caller, File file, UserInfo userInfo) {
            this.caller = caller;
            this.file = file;
            this.userInfo = userInfo;
        }

        @Override
        protected Void doInBackground(Void... params) {


            boolean mustDelete = AppHelpers.getAppPreferences(mContext)
                    .shouldDeleteFilesAfterUpload();
            boolean shouldCompress
                    = AppHelpers.getAppPreferences(mContext).shouldCompressForStorage();


            HttpResponse response = null;

            HttpPost httppost = new HttpPost(
                    AppHelpers.getServerBaseUrl(mContext) + ServerConstants.URL_RECEIVE_LOGS);


            String fileAsBase64 = null;
            try {
                fileAsBase64 = FileHelpers.compressAndBase64(
                        FileHelpers.convertTextFileToByteArray(file));
            } catch (IOException e) {
                e.printStackTrace();
            }


            List<NameValuePair> mPostPairs = new ArrayList<NameValuePair>(3);
            mPostPairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME
                    , userInfo.getUserName()));
            mPostPairs.add(new BasicNameValuePair(ServerConstants.PF_FILENAME
                    , FileHelpers.removeExtensionFromFileName(file.getName())));
            mPostPairs.add(new BasicNameValuePair(ServerConstants.PF_FILE_CONTENT
                    , fileAsBase64));

            try {
                httppost.setEntity(new UrlEncodedFormEntity(mPostPairs));
                HttpClient client = new DefaultHttpClient();
                response = client.execute(httppost);
            } catch (Exception e) {
                Log.e(TAG, "Can not upload file: " + file.getName());
                e.printStackTrace();
            }

            boolean isUploaded = false;
            boolean existOnServer = false;
            boolean isDeleted = false;

            if (response != null) {
                String result = "";
                if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                    try {
                        result = EntityUtils.toString(response.getEntity());
                        if (result.equals(ServerConstants.UPLOAD_OK)) { //upload is OK
                            isUploaded = true;
                        } else if (result.equals(ServerConstants.FILE_EXIST)) {
                            FileHelpers.appendToAppLog(new DateTime().getMillis(), AppLogType.WAR
                                    , TAG, "File " + file.getName()
                                    + " already exist on the server database");
                            existOnServer = true;
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }

            if (isUploaded) { //file uploaded to server successfully
                if (mustDelete) {
                    isDeleted = file.delete();
                } else {
                    if (shouldCompress) {
                        FileHelpers.compressAndStoreFile(mContext, file, false);
                    }
                }
            } else {
                FileHelpers.addFileToNotUploadedFiles(mContext, file);
            }

            return null;
        }
    }
}
