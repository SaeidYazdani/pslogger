package com.embedonix.pslogger.data.user;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import com.embedonix.pslogger.serverwork.ServerConstants;

import org.apache.http.NameValuePair;
import org.apache.http.message.BasicNameValuePair;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.zip.GZIPInputStream;

/**
 * Created by Saeid on 28/06/2014.
 */
public class UserInfo implements Serializable {

    private String mFullName;
    private String mUserName;
    private String mEmailAddress;
    private String mPassword;
    private String mProfilePhotoBase64;

    /**
     * IMPORTANT NOTE!!!!!!!!!!!!!!
     * String is first converted to byte array, then compressed using GZIP and then
     * the resulting byte array is encoded to Base64.DEFAULT
     * @return
     */
    public String getProfilePhotoBase64() {
        return mProfilePhotoBase64;
    }

    public Bitmap getProfilePicture(){
        if(getProfilePhotoBase64() != null) {
            byte[] decoded = Base64.decode(mProfilePhotoBase64.getBytes(), Base64.DEFAULT);

            ByteArrayOutputStream bos = new ByteArrayOutputStream();
            ByteArrayInputStream bis = new ByteArrayInputStream(decoded);

            GZIPInputStream zis = null;
            try {
                zis = new GZIPInputStream(bis);
                byte[] tmpBuffer = new byte[256];
                int n;
                while ((n = zis.read(tmpBuffer)) >= 0) {
                    bos.write(tmpBuffer, 0, n);
                }
                zis.close();
            } catch (IOException e) {
                e.printStackTrace();
            }

            Bitmap bitmap = BitmapFactory.decodeByteArray(bos.toByteArray(), 0
                    , bos.toByteArray().length);

            if(bitmap != null) {
                return bitmap;
            } else {
                return null;
            }
        }
        return null;
    }

    public void setProfilePhotoBase64(String profilePhotoBase64) {
        mProfilePhotoBase64 = profilePhotoBase64;
    }

    public String getFullName() {
        return mFullName;
    }

    public void setFullName(String fullName) {
        mFullName = fullName;
    }

    public String getUserName() {
        return mUserName;
    }

    public void setUserName(String userName) {
        mUserName = userName;
    }

    public String getEmailAddress() {
        return mEmailAddress;
    }

    public void setEmailAddress(String emailAddress) {
        mEmailAddress = emailAddress;
    }

    public String getPassword() {
        return mPassword;
    }

    public void setPassword(String password) {
        mPassword = password;
    }

    public ArrayList<NameValuePair> getUploadPostPairs(){
        ArrayList<NameValuePair> pairs = new ArrayList<NameValuePair>();
        pairs.add(new BasicNameValuePair(ServerConstants.PF_USERNAME, getUserName()));
        return pairs;
    }
}
