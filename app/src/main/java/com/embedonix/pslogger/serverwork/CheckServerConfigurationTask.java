package com.embedonix.pslogger.serverwork;

import android.os.AsyncTask;
import android.util.Log;

import com.embedonix.pslogger.settings.AppPreferences;
import com.embedonix.pslogger.MainActivity;
import com.embedonix.pslogger.SensorLoggerApplication;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 23/07/2014.
 */
public class CheckServerConfigurationTask extends AsyncTask<Void, Void, Boolean> {

    public static final String TAG = "CheckServerConfigurationTask";

    private WeakReference<MainActivity> mActivityReference = null;
    private String mUrl;

    private AppPreferences mAppPrefs;

    public CheckServerConfigurationTask(MainActivity activity, String url) {
        mActivityReference = new WeakReference<MainActivity>(activity);
        mUrl = url;
        mAppPrefs = ((SensorLoggerApplication) mActivityReference.get().getApplication())
                .getAppPreferences();
    }

    @Override
    protected Boolean doInBackground(Void... params) {

        HttpResponse response = null;
        HttpPost httppost = new HttpPost(mUrl + ServerConstants.URL_TEST_SERVER);
        List<NameValuePair> pairs = new ArrayList<NameValuePair>();

        pairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME
                , "check"));

        try {
            httppost.setEntity(new UrlEncodedFormEntity(pairs));
            HttpClient client = new DefaultHttpClient();
            response = client.execute(httppost);
        } catch (Exception e) {
            Log.e(TAG, "can not connect to " + mUrl);
            e.printStackTrace();
            return false;
        }

        if (response != null) {
            String result = "";
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    result = EntityUtils.toString(response.getEntity());
                    if (result.equals(ServerConstants.SERVER_OK)) { //check ok
                        return true;
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    return false;
                }
            }
        }
        return false;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(Boolean result) {
        super.onPostExecute(result);
        if (mActivityReference.get() != null) {
            if (result) {
                mAppPrefs.setServerBaseUrlValidity(true);
                mAppPrefs.setServerBaseUrl(mUrl);
            }
            mActivityReference.get().onCheckServerConfiguration(result);
        } else {
            this.cancel(true);
        }
    }
}
