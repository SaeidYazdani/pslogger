package com.embedonix.pslogger.registrationwizard;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.util.Log;

import com.embedonix.pslogger.R;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.data.user.UserInfo;
import com.embedonix.pslogger.data.user.UserRegistration;
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

import java.io.IOException;
import java.util.ArrayList;

/**
 * Created by Saeid on 01/07/2014.
 */
public class FinalizeRegistration {

    public final static String TAG = "FinalizeRegistration";

    private Activity mActivity;
    private UserRegistration mRegistration;

    public FinalizeRegistration(Activity activity, UserRegistration registration) {
        mActivity = activity;
        mRegistration = registration;
        new ProcessRegistrationAsyncTask().execute();
    }

    private ArrayList<NameValuePair> getPostPairs(){

        UserInfo user = mRegistration.getUserInfo();
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair(ServerConstants.PF_USER_FULLNAME //full name
                , user.getFullName()));
        pairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME //username
                , user.getUserName()));
        pairs.add(new BasicNameValuePair(ServerConstants.PF_EMAIL //email
                , user.getEmailAddress()));
        pairs.add(new BasicNameValuePair(ServerConstants.PF_PASSWORD //password
                , user.getPassword()));
        pairs.add(new BasicNameValuePair(ServerConstants.PF_PHOTO //photo
                , user.getProfilePhotoBase64())); //this is base64 of gzip byte array!

        return pairs;
    }

    private class ProcessRegistrationAsyncTask extends AsyncTask<Void, Void, String> {

        //TODO complete this shit!

        ProgressDialog dialog;
        ArrayList<NameValuePair> pairs;

        private ProcessRegistrationAsyncTask() {

        }

        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            dialog = new ProgressDialog(mActivity);
            dialog.setMessage("contacting server...");
            dialog.show();
        }

        @Override
        protected String doInBackground(Void... voids) {
            pairs = getPostPairs();
            return sendRegistrationRequest(pairs);
        }

        @Override
        protected void onPostExecute(String s) {
            super.onPostExecute(s);

            RegistrationWizardStepThreeActivity activity =
                    (RegistrationWizardStepThreeActivity) mActivity;

            if(s != null && s.equals(ServerConstants.REGISTRATION_OK)) {
                dialog.setMessage("registration is complete!");


                if (dialog.isShowing()) {
                    dialog.dismiss();
                    dialog = null;
                }
                activity.onCompleteSendingDataToServer("Thanks for registering with "
                        + mActivity.getString(R.string.app_name) + ". You can now start using "
                        + "the application!");
            } else {
                activity.onErrorSendingRegistrationRequest(s);
            }
        }
    }

    private String sendRegistrationRequest(ArrayList<NameValuePair> post) {
        HttpResponse response = null;
        HttpPost httppost = new HttpPost(
                AppHelpers.getAppPreferences(mActivity.getApplicationContext()).getServerBaseUrl()
                         + ServerConstants.URL_REGISTRATION_PROCESS);

        try {
            httppost.setEntity(new UrlEncodedFormEntity(post));
            HttpClient client = new DefaultHttpClient();
            response = client.execute(httppost);
        } catch (Exception e) {
            Log.e(TAG, "Error while trying to send registration to server");
            e.printStackTrace();
        }

        if (response != null) {
            String result = "";
            if (response.getStatusLine().getStatusCode() == HttpStatus.SC_OK) {
                try {
                    result = EntityUtils.toString(response.getEntity());
                    if (result.equals(ServerConstants.REGISTRATION_OK)) {
                        return ServerConstants.REGISTRATION_OK;
                    } else if (result.equals(ServerConstants.INVALID_FORM_DATA)) {
                        return ServerConstants.INVALID_FORM_DATA;
                    } else if (result.equals(ServerConstants.USERNAME_EXIST)) {
                        return ServerConstants.USERNAME_EXIST;
                    }

                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        return null;
    }
}
