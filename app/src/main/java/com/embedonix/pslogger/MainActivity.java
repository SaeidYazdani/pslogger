package com.embedonix.pslogger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.EditText;
import android.widget.Toast;

import com.embedonix.pslogger.data.logging.AccelerometerData;
import com.embedonix.pslogger.data.logging.LocationData;
import com.embedonix.pslogger.data.logging.LogData;
import com.embedonix.pslogger.data.logging.LoggerListener;
import com.embedonix.pslogger.data.logging.LoggingMode;
import com.embedonix.pslogger.data.logging.MiscSensorsData;
import com.embedonix.pslogger.data.user.UserInfo;
import com.embedonix.pslogger.data.user.UserRegistration;
import com.embedonix.pslogger.fragments.AccelerometerFragment;
import com.embedonix.pslogger.fragments.CollapsibleFragment;
import com.embedonix.pslogger.fragments.CollapsibleFragmentContainer;
import com.embedonix.pslogger.fragments.CollapsibleFragmentSupport;
import com.embedonix.pslogger.fragments.LocationFragment;
import com.embedonix.pslogger.fragments.MiscSensorsFragment;
import com.embedonix.pslogger.fragments.UploadQueueFragment;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.Storage;
import com.embedonix.pslogger.registrationwizard.RegistrationWizardStepOneActivity;
import com.embedonix.pslogger.serverwork.CheckServerConfigurationTask;
import com.embedonix.pslogger.serverwork.LoginTask;
import com.embedonix.pslogger.serverwork.UploadAppLogTask;
import com.embedonix.pslogger.settings.AppPreferences;
import com.embedonix.pslogger.settings.SettingsActivity;

import org.joda.time.DateTime;


/**
 * Pervasive Systems Research Group
 * University Twente
 * <p/>
 * Created by Saeid on 13/07/2014.
 */
public class MainActivity extends BaseActivity implements LoggerListener
        , CollapsibleFragmentSupport {

    public static final String TAG = "MainActivity";

    private Context mContext;
    private Menu mMenu;

    private SharedPreferences mPrefs;
    private PreferenceChangeListener mPreferenceListener = null;

    private MiscSensorsFragment mMiscSensors;
    private AccelerometerFragment mAccSensor;
    private LocationFragment mLocMonitor;
    private UploadQueueFragment mUploadFragment;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mContext = this;

        if (getAppPreferences().isServerConfigurationSkipped()) {
            UserRegistration ur = Storage.readUserRegistration(this);
            if (ur == null || !ur.isRegistrationDone()) {
                if (AppHelpers.isInternetAvailable(this)) {
                    //showServerConfigurationDialog(null);
                    showRegistrationDialog();
                    new CheckServerConfigurationTask((MainActivity) mContext
                            , "http://www.embedonix.com/apps/pslogger/").execute();
                } else {
                    Toast.makeText(this, "you are not connected to internet. internet connection"
                            + " is necessary to do registration", Toast.LENGTH_SHORT).show();
                }
            }
        }

        FragmentManager fm = getFragmentManager();
        mAccSensor = (AccelerometerFragment)
                fm.findFragmentById(R.id.fragment_accelerometer);
        mLocMonitor = (LocationFragment) fm.findFragmentById(R.id.fragment_location);
        mMiscSensors = (MiscSensorsFragment) fm.findFragmentById(R.id.fragment_miscsensors);
        mUploadFragment = (UploadQueueFragment) fm.findFragmentById(R.id.fragment_upload_queue);


        uploadAppLogFile();
        setupPreferencesChangeListener();
    }

    private void showRegistrationDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("registration");
        b.setMessage("for uploading logged data to server, you need to be registered."
                + " registration is easy and only takes a minute! If you already "
                + " have registered you can log in using your existing account");
        b.setPositiveButton("register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext()
                        , RegistrationWizardStepOneActivity.class);
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
                return;
            }
        });

        b.setNeutralButton("login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showLoginDialog();
                return;
            }
        });

        b.setNegativeButton("not now", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getAppPreferences().setUserHasSkippedRegistration(true);
            }
        });

        b.setCancelable(false);

        AlertDialog dialog = b.create();
        dialog.setCanceledOnTouchOutside(false);

        dialog.show();
    }

    private void uploadAppLogFile() {
        if (getAppPreferences().isLoggedIn()) {
            if (AppHelpers.isInternetAvailable(getApplicationContext())) {
                long lastTime = getAppPreferences().getLastAppLogUploadTime();
                long now = new DateTime().getMillis();
                if ((now - lastTime) > 43200000) {
                    new UploadAppLogTask(getApplicationContext()).execute();
                }
            }
        }
    }

    private void setupPreferencesChangeListener() {
        mPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        mPreferenceListener = new PreferenceChangeListener();
        mPrefs.registerOnSharedPreferenceChangeListener(mPreferenceListener);
    }

    private void showLoginDialog() {

        if (!AppHelpers.isInternetAvailable(this)) {
            AppHelpers.showNoInternetConnectionDialog(this);
            return;
        }

        if (getAppPreferences().getServerBaseUrl().equals("NULL")) {
            new CheckServerConfigurationTask((MainActivity) mContext
                    , "http://www.embedonix.com/apps/pslogger/").execute();
        }

        AlertDialog.Builder builder;
        View view;
        final EditText username;
        final EditText password;

        LayoutInflater inflater = getLayoutInflater();
        view = inflater.inflate(R.layout.dialog_login, null);

        username = (EditText) view.findViewById(R.id.loginDialogUsername);
        password = (EditText) view.findViewById(R.id.loginDialogPassword);

        builder = new AlertDialog.Builder(this);
        builder.setPositiveButton("login", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new LoginTask((MainActivity) mContext
                        , username.getText().toString()
                        , password.getText().toString()).execute();
            }
        });
        builder.setNeutralButton("register", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(getApplicationContext()
                        , RegistrationWizardStepOneActivity.class);
                startActivity(intent);
            }
        });
        builder.setNegativeButton("cancel", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        builder.setView(view);
        builder.setTitle("login");
        builder.create().show();
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.i(TAG, "onStart() was called");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.i(TAG, "onRestart() was called");
    }

    @Override
    protected void onResume() {
        super.onResume();
        Log.i(TAG, "onResume() was called");
        getApplicationReference().stopBackgroundLogger();
        toggleFragments(getAppPreferences().wasLogButtonChecked());
        if(mUploadFragment.getOperationState() != CollapsibleFragment.OperationState.ACTIVE) {
            mUploadFragment.activate();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        Log.i(TAG, "onPause() was called");
        Log.i(TAG, "onPause() says that isFinishing() returned " + isFinishing());

    }

    @Override
    protected void onStop() {
        if(mUploadFragment.getOperationState() == CollapsibleFragment.OperationState.ACTIVE) {
            mUploadFragment.deactivate();
        }
        super.onStop();
    }

    @Override
    protected void onDestroy() {
        Log.i(TAG, "onDestroy() was called");
        if (getAppPreferences().wasLogButtonChecked()) {

            if(mAccSensor != null) {
                mAccSensor.deactivate();
            }

            if(mLocMonitor != null) {
                mLocMonitor.deactivate();
            }

            if(mMiscSensors != null) {
                mMiscSensors.deactivate();
            }

            getApplicationReference()
                    .removeLoggingStatusNotification(LoggingMode.FOREGROUND);

            if (getAppPreferences().canLogInBackground()) {
                getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                        .changeMode(LoggingMode.BACKGROUND);
                getApplicationReference().startBackgroundLogger();
            } else {
                getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                        .finalizeCurrentQueue();
            }
        }
        super.onDestroy();
    }

    @Override
    public void onBackPressed() {
        Log.i(TAG, "onBackPressed() is called");

        if (getAppPreferences().wasLogButtonChecked() && !getAppPreferences().canLogInBackground()) {
            AlertDialog.Builder b = new AlertDialog.Builder(this).setTitle("warning").setMessage(
                    getString(R.string.app_name) + " is logging data and you have disabled the " +
                            " background logger. if the app goes off-screen, it will not " +
                            "be able to continue logging data!"
                            + "\nto log data on background please enable this feature from settings menu"
            ).setPositiveButton("go out", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    redirectToOnBackPressed();
                }
            }).setNegativeButton("stay", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    Log.i(TAG, "onBackPressed() was surpassed by alert dialog");
                }
            }).setNeutralButton("enable and go", new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    getAppPreferences().setCanLogInBackground(true);
                    redirectToOnBackPressed();
                }
            });
            b.setCancelable(false);
            Dialog d = b.create();
            d.setCanceledOnTouchOutside(false);
            d.show();
        } else {
            super.onBackPressed();
        }
    }

    private void redirectToOnBackPressed() {
        super.onBackPressed();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        mMenu = menu;
        getMenuInflater().inflate(R.menu.main, menu);
        MenuItem startOrStop = menu.findItem(R.id.action_toggle_logging);
        if (getAppPreferences().wasLogButtonChecked()) {
            startOrStop.setIcon(R.drawable.ic_action_stop);
        } else {
            startOrStop.setIcon(R.drawable.ic_action_play);
        }

        MenuItem login = menu.findItem(R.id.action_login_or_logout);

        if (getAppPreferences().isLoggedIn()) {
            login.setTitle("logout");
            login.setIcon(R.drawable.ic_action_cancel);
        } else {
            login.setTitle("login");
            login.setIcon(R.drawable.ic_action_person);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        boolean result = true;
        Intent intent = null;
        switch (item.getItemId()) {
            case R.id.action_settings:
                intent = new Intent(this, SettingsActivity.class);
                break;
            case R.id.action_toggle_logging:
                if (getAppPreferences().wasLogButtonChecked()) {

                    getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                            .finalizeCurrentQueue();
                    item.setIcon(R.drawable.ic_action_play);
                    if(mUploadFragment != null && !mUploadFragment.isUploading()) {
                        mUploadFragment.deactivate();
                        mUploadFragment.activate();
                    }
                } else {
                    if (!getAppPreferences().canLogAnything()) {
                        showNothingSelectedToLogDialog();
                        break;
                    } else {
                        item.setIcon(R.drawable.ic_action_stop);
                    }
                }
                getAppPreferences().setLogButtonCheckedState(
                        !getAppPreferences().wasLogButtonChecked());
                toggleFragments(getAppPreferences().wasLogButtonChecked()); //inform fragments
                break;
            case R.id.action_show_help_activity:
                Toast.makeText(getApplicationContext(), "help is not implemented yet\n" +
                        "log files are being saved in External Storage under PS_LOGGER folder"
                        , Toast.LENGTH_SHORT).show();
                break;
            case R.id.action_login_or_logout:
                if (getAppPreferences().isLoggedIn()) {
                    showLogoutDialog(item);
                } else {
                    showLoginDialog();
                }
                break;
            default:
                break;
        }

        if (intent != null) {
            startActivity(intent);
            return true;
        } else {
            return result;
        }
    }

    private void showNothingSelectedToLogDialog() {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("nothing to log");
        b.setMessage("you have currently not selected any sensor or location monitoring."
                + " please select at least a sensor (group) or location from settings menu.");
        b.setPositiveButton("show settings", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(intent);
            }
        });
        b.setNegativeButton("go back", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                //do nothing
            }
        });
        b.create().show();

    }

    private void showLogoutDialog(final MenuItem item) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setMessage("do you want to log out?");
        b.setPositiveButton("yes", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getAppPreferences().setLoggedIn(false);
                Toast.makeText(mContext, "you are now logged out!"
                        , Toast.LENGTH_LONG).show();
                item.setTitle("login");
                item.setIcon(R.drawable.ic_action_user);
            }
        });
        b.setNegativeButton("no", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {

            }
        });
        b.create().show();
    }

    private void toggleFragments(boolean checked) {
        if (checked) {
            getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                    .changeMode(LoggingMode.FOREGROUND);

            if (getAppPreferences().canLogLocation()) {
                if (mLocMonitor.getOperationState() == CollapsibleFragment.OperationState.ACTIVE) {
                    mLocMonitor.expand();
                } else {
                    mLocMonitor.activate();
                }
            } else {
                mLocMonitor.collapse();
            }
            if (getAppPreferences().canLogAccelerometer()) {
                if (mAccSensor.getOperationState() == CollapsibleFragment.OperationState.ACTIVE) {
                    mAccSensor.expand();
                } else {
                    mAccSensor.activate();
                }
            } else {
                mAccSensor.collapse();
            }
            if (getAppPreferences().canLogMiscSensors()) {
                if (mMiscSensors.getOperationState() == CollapsibleFragment.OperationState.ACTIVE) {
                    mMiscSensors.expand();
                } else {
                    mMiscSensors.activate();
                }
            } else {
                mMiscSensors.collapse();
            }
            if (getAppPreferences().canLogAnything()) {
                getApplicationReference().showLoggingInForegroundNotification();
            }
        } else {
            mLocMonitor.deactivate();
            mAccSensor.deactivate();
            mMiscSensors.deactivate();
            getApplicationReference()
                    .removeLoggingStatusNotification(LoggingMode.FOREGROUND);
        }
    }

    private void showButtonRegistrationReminder() {
        //TODO implement this, bring up a button on top to remind about registration
    }

    @Override
    public void onAccelerometerDataReceived(AccelerometerData accData) {
        getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                .addToQueue(new LogData(accData, null, null));
    }

    @Override
    public void onLocationDataReceived(LocationData locData) {
        getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                .updateLocationData(locData);

        //this should happen when the user does not want accelerometer to be logged
        if (!getAppPreferences().canLogAccelerometer()) {
            getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                    .addToQueue(new LogData(null, null, null));
        }

    }

    @Override
    public void onMiscSensorsReceived(MiscSensorsData miscData) {
        getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                .updateMiscSensorsData(miscData);
        //this should happen when the user does not want accelerometer to be logged
        if (!getAppPreferences().canLogAccelerometer()) {
            getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                    .addToQueue(new LogData(null, null, null));
        }
    }

    @Override
    public CollapsibleFragmentContainer getContainer() {
        return null;
    }

    public void onCheckServerConfiguration(boolean result) {

        if (result) {
            getAppPreferences().setUserHasSkippedServerConfiguration(false);
            //showRegistrationDialog();
        } else {
            getAppPreferences().setUserHasSkippedServerConfiguration(true);
        }

        Toast.makeText(this, "server configuration " + (result ? "succeeded" : "failed")
                , Toast.LENGTH_SHORT).show();

        if (result == false) {
            showServerConfigurationDialog("http://www.embedonix.com/apps/pslogger/");
        }
    }

    private void showServerConfigurationDialog(String givenUrl) {
        AlertDialog.Builder b = new AlertDialog.Builder(this);
        b.setTitle("server configuration");

        View view = getLayoutInflater().inflate(R.layout.dialog_server_configuration, null);
        final EditText url = (EditText) view.findViewById(R.id.dialogServerConfigUrl);

        b.setView(view);

        b.setPositiveButton("apply", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                new CheckServerConfigurationTask((MainActivity) mContext
                        , url.getText().toString()).execute();
            }
        });

        b.setNegativeButton("skip", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                getAppPreferences().setUserHasSkippedServerConfiguration(true);
            }
        });

        b.create().show();
    }

    public void onProcessedLoginDialog(UserInfo userInfo) {

        if (userInfo != null) {
            UserRegistration ur = new UserRegistration();
            userInfo.setPassword("");
            ur.setUserInfo(userInfo);
            Storage.updateUserRegistration(getApplicationContext(), ur, true);
            getAppPreferences().setLoggedIn(true);
            MenuItem item = mMenu.findItem(R.id.action_login_or_logout);
            item.setTitle("logout");
            item.setIcon(R.drawable.ic_action_cancel);
        } else {
            getAppPreferences().setLoggedIn(false);
            MenuItem item = mMenu.findItem(R.id.action_login_or_logout);
            item.setTitle("login");
            item.setIcon(R.drawable.ic_action_person);
        }

        Toast.makeText(this, (userInfo != null ? "login successful" : "login failed")
                , Toast.LENGTH_SHORT).show();
    }

    private class PreferenceChangeListener implements
            SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onSharedPreferenceChanged(SharedPreferences prefs,
                                              String key) {
            Log.i(TAG, key + " has changed");

            if (key.equals(AppPreferences.CAN_LOG_ACCELEROMETER)) {
                if (getAppPreferences().canLogAccelerometer()) {
                    if (getAppPreferences().wasLogButtonChecked()) {
                        mAccSensor.activate();
                    }
                } else {
                    mAccSensor.deactivate();
                }
            }

            if (key.equals(AppPreferences.CAN_LOG_LOCATION)) {
                if (getAppPreferences().canLogLocation()) {
                    if (getAppPreferences().wasLogButtonChecked()) {
                        mLocMonitor.activate();
                    }
                } else {
                    mLocMonitor.deactivate();
                }
            }

            if (key.equals(AppPreferences.CAN_LOG_MISC_SENSORS)) {
                if (getAppPreferences().canLogMiscSensors()) {
                    if (getAppPreferences().wasLogButtonChecked()) {
                        mMiscSensors.activate();
                    }
                } else {
                    mMiscSensors.deactivate();
                }
            }

            if (key.equals(AppPreferences.CAN_LOG_ACCELEROMETER) ||
                    key.equals(AppPreferences.CAN_LOG_LOCATION) ||
                    key.equals(AppPreferences.CAN_LOG_MISC_SENSORS)) {


                getApplicationReference().getLogger(TAG, LoggingMode.FOREGROUND)
                        .setQueueThreshold(getAppPreferences().canLogAccelerometer()
                                , getAppPreferences().canLogLocation()
                                , getAppPreferences().canLogMiscSensors());
            }

            if (key.equals(AppPreferences.ACCELEROMETER_SPEED)) {
                if (mAccSensor != null /*&& mAccSensor.getOperationState()
                        == CollapsibleFragment.OperationState.ACTIVE*/) {
                    mAccSensor.notifyDefaultSpeedIsChanged();
                }
            }

            if (key.equals(AppPreferences.ACC_GRAPH_VIEWPORT)) {
                if (mAccSensor != null) {
                    mAccSensor.notifyDefaultGraphViewPortIsChanged();
                }
            }

            if (key.equals(AppPreferences.DEFAULT_LOCATION_PROVIDER)) {
                if (mLocMonitor != null /*&& mLocMonitor.getOperationState()
                        == CollapsibleFragment.OperationState.ACTIVE*/) {
                    mLocMonitor.notifyDefaultProviderIsChanged();
                }
            }
        }
    }
}
