package com.embedonix.pslogger.serverwork;

import android.app.Fragment;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import com.embedonix.pslogger.data.user.UserInfo;
import com.embedonix.pslogger.fragments.CollapsibleFragment;
import com.embedonix.pslogger.fragments.UploadQueueFragment;
import com.embedonix.pslogger.helpres.AppConstants;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.AppLogType;
import com.embedonix.pslogger.helpres.FileHelpers;
import com.embedonix.pslogger.settings.AppPreferences;

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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 02/08/2014.
 */
public class UploadNotUploadedFiles extends AsyncTask<File, Integer, Boolean> {

    public static final String TAG = "UploadNotUploadedFiles";

    private Context mContext;
    private UserInfo mUserInfo;
    private ArrayList<NameValuePair> mPostPairs;
    private AppPreferences mAppPreferences;
    private UploadQueueFragment mFragment;

    public UploadNotUploadedFiles(Context context, UserInfo userInfo) {
        mContext = context;
        mUserInfo = userInfo;
        mAppPreferences = new AppPreferences(context);
    }

    public UploadNotUploadedFiles(Context context, UserInfo userInfo
            , UploadQueueFragment fragment) {
        mContext = context;
        mUserInfo = userInfo;
        mAppPreferences = new AppPreferences(context);
        mFragment = fragment;
    }

    @Override
    protected Boolean doInBackground(File... files) {

        File tempFolder = new File(Environment.getExternalStorageDirectory() + File.separator
                + AppConstants.LOG_FOLDER_NAME);
        tempFolder.mkdirs();

        boolean allUploaded = false;

        for (int i = 0; i < files.length; i++) {

            File file = files[i];

            if(file.getPath().endsWith("gz")) { //this is GZ should decompress first!

                File decompressed = new File(tempFolder, file.getName().replace(".gz", ""));
                try {
                    byte[] buffer = new byte[1024];
                    GZIPInputStream gzi = new GZIPInputStream(new FileInputStream(file));
                    FileOutputStream fos = new FileOutputStream(decompressed);
                    int len;
                    while((len = gzi.read(buffer)) > 0) {
                        fos.write(buffer, 0, len);
                    }
                    gzi.close();
                    fos.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }

                if(decompressed != null && decompressed.exists() && decompressed.length() > 0) {
                    allUploaded = uploadFile(decompressed, true);
                }
            } else {
                allUploaded = uploadFile(files[i], false);
            }

            if(mAppPreferences.shouldDeleteFilesAfterUpload()) {
                if(!files[i].delete()){
                  Log.e(TAG, "can not delete " +  files[i].getName());
                }
            }
            publishProgress(i);
        }

        return allUploaded;
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        if(mFragment != null && mFragment.getOperationState()
                == CollapsibleFragment.OperationState.ACTIVE) {
            mFragment.onUploadProgressReceived(values[0]);
        }
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if(result) {
            Log.i(TAG, "uploaded and deleted all files");
            boolean deleted = mContext.deleteFile(AppConstants.LOG_WORK_LIST_FILENAME);
            if(mFragment != null && mFragment.getOperationState()
                    == CollapsibleFragment.OperationState.ACTIVE) {
                mFragment.onUploadResultReceived(result);
            }
        } else {
            Log.i(TAG, "error while uploading/deleting remaining files");
        }
    }

    private boolean uploadFile(File file, boolean shouldDeleteFile) {

        HttpResponse response = null;

        HttpPost httppost = new HttpPost(
                AppHelpers.getServerBaseUrl(mContext) + ServerConstants.URL_RECEIVE_LOGS);

        String fileAsBase64 = null;
        try {
            fileAsBase64 = FileHelpers.compressAndBase64(
                    FileHelpers.convertTextFileToByteArray(file));
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }


        mPostPairs = new ArrayList<NameValuePair>(3);
        mPostPairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME
                , mUserInfo.getUserName()));
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
        boolean deleted = false;

        if (response != null) {
            String result = "";
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    result = EntityUtils.toString(response.getEntity());
                    if (result.equals(ServerConstants.UPLOAD_OK)) { //upload is OK
                        isUploaded = true;
                        if(shouldDeleteFile) {
                            deleted = file.delete();
                        }
                    } else if(result.equals(ServerConstants.FILE_EXIST)) {
                        deleted = file.delete();
                        FileHelpers.appendToAppLog(new DateTime().getMillis(), AppLogType.WAR
                                , TAG, "File " + file.getName()
                                + " already exist on the server database");
                        isUploaded = true;
                    }

                    Log.v(TAG, "Upload file result:" + result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if(shouldDeleteFile) {
            return isUploaded && deleted;
        } else {
            return isUploaded;
        }
    }
}
