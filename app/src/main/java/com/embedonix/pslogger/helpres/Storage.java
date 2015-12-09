package com.embedonix.pslogger.helpres;

import android.content.Context;
import android.util.Log;

import com.embedonix.pslogger.data.user.UserRegistration;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

/**
 * This class takes care of I/O to device disk
 *
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 13/07/2014.
 */
public class Storage {

    public static final String TAG = "Storage";

    /**
     * Writes or updates (if already exist) user registration file
     * @param context
     * @param ur
     * @param deleteFile
     */
    public static void updateUserRegistration(Context context
            , UserRegistration ur, boolean deleteFile) {

        if (deleteFile) {
            boolean deleted = context.deleteFile(AppConstants.INTERNAL_FILE_USER_REGISTRATION);
            if (deleted) {
                Log.d(TAG, "Deleted " + AppConstants.INTERNAL_FILE_USER_REGISTRATION);
            }
        }

        FileOutputStream fos = null;
        ObjectOutputStream oos = null;
        try {
            fos = context.openFileOutput(AppConstants.INTERNAL_FILE_USER_REGISTRATION
                    , Context.MODE_PRIVATE);
            oos = new ObjectOutputStream(fos);
            oos.writeObject(ur);
            Log.d(TAG, "Writing USER REGISTRATION to internal storage.");
        } catch (IOException e) {
            Log.e(TAG, "Failed to write USER REGISTRATION to internal storage");
            e.printStackTrace();
        } finally {
            try {
                fos.close();
                oos.close();
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Failed to write USER REGISTRATION to internal storage");
            }
        }


    }

    /**
     * Reads user registration object from disk
     * @param context
     * @return
     */
    public static UserRegistration readUserRegistration(Context context) {
        UserRegistration ur = null;
        FileInputStream fis = null;

        try {
            fis = context.openFileInput(AppConstants.INTERNAL_FILE_USER_REGISTRATION);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            return null;
        }

        if (fis != null) {
            try {
                ObjectInputStream ois = new ObjectInputStream(fis);
                try {
                    ur = (UserRegistration) ois.readObject();

                    ois.close();
                    fis.close();

                } catch (ClassNotFoundException e) {
                    e.printStackTrace();
                    Log.e(TAG, "Can not read UserRegistration class (ClassNotFoundException)");
                    return null;
                }
            } catch (IOException e) {
                e.printStackTrace();
                Log.e(TAG, "Can not read UserRegistration class (IOException)");
                return null;
            }
        }
        return ur;
    }
}
