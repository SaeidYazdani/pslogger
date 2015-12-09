package com.embedonix.pslogger.serverwork;

import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import com.embedonix.pslogger.data.user.UserInfo;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.FileHelpers;
import com.embedonix.pslogger.helpres.Storage;
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
import java.io.IOException;
import java.util.ArrayList;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 02/08/2014.
 */
public class UploadAppLogTask extends AsyncTask<Void, Void, Void> {

    public static final String TAG = "UploadAppLogTask";

    private Context mContext;
    private UserInfo mUserInfo;

    public UploadAppLogTask(Context context) {
        mContext = context;
        mUserInfo = Storage.readUserRegistration(context).getUserInfo();
    }

    @Override
    protected Void doInBackground(Void... params) {
        File file = FileHelpers.getAppLog();

        if(file == null || !file.exists() || !(file.length() > 0)) {
            return null;
        }

        HttpPost httppost = new HttpPost(
                AppHelpers.getServerBaseUrl(mContext) + ServerConstants.URL_RECEIVE_LOGS);
        HttpResponse response = null;

        ArrayList<NameValuePair> postPairs;
        postPairs = new ArrayList<NameValuePair>(4);
        postPairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME
                , mUserInfo.getUserName()));
        postPairs.add(new BasicNameValuePair(ServerConstants.PF_FILE_TYPE
                , ServerConstants.TYPE_APP_LOG_FILE));
        postPairs.add(new BasicNameValuePair(ServerConstants.PF_FILENAME
                , FileHelpers.removeExtensionFromFileName(file.getName())));
        try {
            postPairs.add(new BasicNameValuePair(ServerConstants.PF_FILE_CONTENT
                    , FileHelpers.compressAndBase64(FileHelpers.convertTextFileToByteArray(file))));
        } catch (IOException e) {
            e.printStackTrace();
            return  null;
        }

        try {
            httppost.setEntity(new UrlEncodedFormEntity(postPairs));
            HttpClient client = new DefaultHttpClient();
            response = client.execute(httppost);
        } catch (Exception e) {
            Log.e(TAG, "Can not upload file: " + file.getName());
            e.printStackTrace();
            return  null;
        }

        String result;
        if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
            try {
                result = EntityUtils.toString(response.getEntity());
                if (result.equals(ServerConstants.UPLOAD_OK)) {
                    Log.i(TAG, "uploaded app log file");
                    AppPreferences pref = new AppPreferences(mContext);
                    pref.setLastAppLogUpload(new DateTime().getMillis());
                    if(!file.delete()){
                        Log.e(TAG, "could not delete app log file after upload.");
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();

            }
        }
        return null;
    }
}
