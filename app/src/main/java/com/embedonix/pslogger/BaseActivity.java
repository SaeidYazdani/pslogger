package com.embedonix.pslogger;

import android.app.Activity;
import android.os.Bundle;

import com.embedonix.pslogger.data.user.UserRegistration;
import com.embedonix.pslogger.settings.AppPreferences;

/**
 * <p>
 * This is class is derived from {@link android.app.Activity} and is used to give some
 * flexibility to access functionalists which are provided in
 * {@link com.embedonix.pslogger.SensorLoggerApplication} for other Activities
 * </p>
 *
 *
 * Pervasive Systems Research Group
 * University Twente
 *
 * Created by Saeid on 13/07/2014.
 */
public abstract class BaseActivity extends Activity {

    private SensorLoggerApplication mApplication;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mApplication = (SensorLoggerApplication)getApplication();
    }

    /**
     * Get a reference to the application main entry point
     *
     * @return SensorLoggerApplication
     */
    public SensorLoggerApplication getApplicationReference(){
        return mApplication;
    }

    /**
     * Get a reference to AppPreferences which is a wrapped for SharedPreferences
     *
     * @return AppPreferences
     */
    public AppPreferences getAppPreferences(){
        return mApplication.getAppPreferences();
    }

    /**
     * Get a reference to UserRegistration
     *
     * @param newReference If given true, a new reference will be created and returned. reading
     *                     objects from disk can be expensive so only request a new reference
     *                     if only necessary
     *
     * @return UserRegistration
     */
    public UserRegistration getUserRegistration(boolean newReference) {
        return mApplication.getUserRegistration(newReference);
    }
}
