package com.embedonix.pslogger.serverwork;

import android.os.AsyncTask;
import android.util.Log;

import com.embedonix.pslogger.settings.AppPreferences;
import com.embedonix.pslogger.MainActivity;
import com.embedonix.pslogger.data.user.UserInfo;

import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.NameValuePair;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.util.EntityUtils;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 22/07/2014.
 */
public class LoginTask extends AsyncTask<Void, Void, UserInfo> {

    public static final String TAG = "LoginTask";

    private WeakReference<MainActivity> mActivity = null;
    private String username;
    private String password;
    private AppPreferences mAppPrefs;

    public LoginTask(MainActivity activity, String username, String password) {
        mActivity = new WeakReference<MainActivity>(activity);
        this.username = username;
        this.password = password;
        mAppPrefs = mActivity.get().getAppPreferences();
    }

    @Override
    protected UserInfo doInBackground(Void... params) {
        HttpResponse response = null;


        HttpPost httppost = new HttpPost(mAppPrefs.getServerBaseUrl()
                + ServerConstants.URL_LOGIN);

        List<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME
                , username));
        pairs.add(new BasicNameValuePair(ServerConstants.PF_PASSWORD
                , password));
        try {
            httppost.setEntity(new UrlEncodedFormEntity(pairs));
            HttpClient client = new DefaultHttpClient();
            response = client.execute(httppost);
        } catch (Exception e) {
            Log.e(TAG, "can not connect to " + httppost.getURI());
            e.printStackTrace();
        }

        if (response != null) {
            String result = "";
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    result = EntityUtils.toString(response.getEntity());
                    return parseUserInfoFromJson(result);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }

    @Override
    protected void onPreExecute() {
        super.onPreExecute();
    }

    @Override
    protected void onPostExecute(UserInfo userInfo) {
        super.onPostExecute(userInfo);
        if (mActivity.get() != null) {
            mActivity.get().onProcessedLoginDialog(userInfo);
        }
    }

    private UserInfo parseUserInfoFromJson(String result) {
        JSONObject json = null;
        UserInfo info = null;
        try {
            json = new JSONObject(result);

            if (json.getString("response").equals(ServerConstants.LOGIN_OK)) {
                String username = json.getString("username");
                String fullname = json.getString("fullname");
                String email = json.getString("email");

                info = new UserInfo();
                info.setEmailAddress(email);
                info.setUserName(username);
                info.setFullName(fullname);
                return info;
            }
        } catch (JSONException e) {
            Log.e(TAG, "error while parsing login json response");
            e.printStackTrace();
        }

        return null;
    }
}
