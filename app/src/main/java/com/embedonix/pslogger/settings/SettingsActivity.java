package com.embedonix.pslogger.settings;

import android.app.Activity;
import android.os.Bundle;

/**
 * Activity for dealing with settings
 */
public class SettingsActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        getFragmentManager().beginTransaction().replace(android.R.id.content,
                new SettingsFragment()).commit();
    }
}
