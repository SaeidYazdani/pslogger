package com.embedonix.pslogger.data.user;

import java.io.Serializable;

/**
 * Created by Saeid on 28/06/2014.
 */
public class UserRegistration implements Serializable {

    private UserInfo mUserInfo;
    private boolean mIsRegistrationDone;

    /**
     * Default constructor, when called it will save the timestamp of initialization as the
     * timestamp for which the application started for the first time!
     */
    public UserRegistration() {
        mUserInfo = new UserInfo();
    }

    public void setUserInfo(UserInfo userInfo) {
        if (userInfo != null) {
            mUserInfo = userInfo;
        } else {
            throw new NullPointerException("User can not be null!");
        }
    }

    public UserInfo getUserInfo() {
        return mUserInfo;
    }

    public boolean isRegistrationDone() {
        return mIsRegistrationDone;
    }

    public void setIsRegistrationDone(boolean mIsRegistrationDone) {
        this.mIsRegistrationDone = mIsRegistrationDone;
    }
}
