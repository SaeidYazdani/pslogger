package com.embedonix.pslogger.fragments;

import android.app.Activity;
import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.embedonix.pslogger.R;
import com.embedonix.pslogger.data.logging.LocationData;
import com.embedonix.pslogger.data.logging.LoggerListener;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.embedonix.pslogger.helpres.AppLogType;
import com.embedonix.pslogger.helpres.FileHelpers;

import org.joda.time.DateTime;

import java.text.SimpleDateFormat;
import java.util.List;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 29/07/2014.
 */
public class LocationFragment extends CollapsibleFragment implements LocationListener {

    public static final String TAG = "LocFragment";

    private Context mContext;

    private Looper mLooper;
    private LoggerListener mLoggerListener;
    private LocationManager mLocationManager;
    private String mProvider;

    private TextView mTvLongitude;
    private TextView mTvLatitude;
    private TextView mTvStatus;
    private TextView mTvProvider;
    private TextView mTvAltitude;
    private TextView mTvLastFixTime;
    private ImageView mImageStatus;
    private Spinner mSpinnerProviders;
    private boolean mIsSpinnerSetupCompleted;

    private ArrayAdapter<String> mProviderAdapter;

    public LocationFragment() {
        setName(TAG);
        super.TAG = TAG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_location, container, false);
        setupView();
        setupWidgets();
        if (mShouldActivateOnCreate) {
            activate();
        }
        return mView;
    }

    @Override
    protected void setupView() {
        mLoggerListener = (LoggerListener) getActivity();
        mSupport = (CollapsibleFragmentSupport) getActivity();
        mHeaderLayout = mView.findViewById(R.id.locationHeader);
        //mHeaderLayout.setOnTouchListener(mSupport.getContainer());
        mContainer = mView.findViewById(R.id.locationContainer);
        mCollapsedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
        mExpandedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_add);
        mToggleVisibilityImageView =
                (ImageView) mView.findViewById(R.id.locationToggleVisibility);
        mToggleVisibilityImageView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (getVisibilityState()) {
                    case COLLAPSED:
                        Animation expandAnimation = AnimationUtils.loadAnimation(getActivity()
                                , android.R.anim.fade_in);
                        expand(expandAnimation, false);
                        return;
                    case EXPANDED:
                        Animation collapseAnimation = AnimationUtils.loadAnimation(getActivity()
                                , android.R.anim.fade_out);
                        collapse(collapseAnimation, false);
                        return;
                }
            }
        });
    }

    private void setupWidgets() {
        mTvLongitude = (TextView) mView.findViewById(R.id.fragLocLongitude);
        mTvLatitude = (TextView) mView.findViewById(R.id.fragLocLatitude);
        mTvStatus = (TextView) mView.findViewById(R.id.fragLocStatus);
        mTvProvider = (TextView) mView.findViewById(R.id.fragLocProvider);
        mTvAltitude = (TextView) mView.findViewById(R.id.fragLocAltitude);
        mTvLastFixTime = (TextView) mView.findViewById(R.id.fragLocLastFixTime);
        mImageStatus = (ImageView) mView.findViewById(R.id.fragLocImageViewLocationMonitoring);
        mImageStatus.setImageResource(R.drawable.ic_signal_strength_one);

        mTvLatitude.setText("----");
        mTvLongitude.setText("----");
        mTvAltitude.setText("----");
        mTvLastFixTime.setText("----");

        mSpinnerProviders = (Spinner) mView.findViewById(R.id.locationProvidersSpinner);

        setupSpinner();
    }

    private void setupSpinner() {

        final String defaultProvider = AppHelpers.getAppPreferences(mContext)
                .getDefaultLocationProvider();

        final List<String> providers = mLocationManager.getProviders(false);

        mProviderAdapter = new ArrayAdapter<String>(mContext
                , R.layout.spinner_location_providers
                , providers.toArray(new String[providers.size()]));
        mSpinnerProviders.setAdapter(mProviderAdapter);

        for (int i = 0; i < mSpinnerProviders.getAdapter().getCount(); i++) {
            if (mSpinnerProviders.getItemAtPosition(i).equals(defaultProvider)) {
                mProvider = defaultProvider;
                mSpinnerProviders.setSelection(i);
                break;
            }
        }

        mSpinnerProviders.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mProvider = (String) mSpinnerProviders
                        .getItemAtPosition(position);

                AppHelpers.getAppPreferences(getActivity())
                        .setDefaultLocationProvider((mProvider));

                if(getOperationState() == OperationState.ACTIVE) {
                    deactivate();
                    activate();
                }


/*                if (!mIsSpinnerSetupCompleted) {
                    mProvider = (String) parent.getAdapter().getItem(position);
                    if (getOperationState() == OperationState.ACTIVE) {
                        mTvStatus.setText("changing...");
                        mTvProvider.setText(mProvider);
                        mTvLatitude.setText("----");
                        mTvLongitude.setText("----");
                        mTvAltitude.setText("----");
                        mTvLastFixTime.setText("----");
                        mLocationManager.removeUpdates(LocationFragment.this);
                        mLocationManager.requestLocationUpdates(mProvider, 0, 0
                                , LocationFragment.this);

                        AppHelpers.getAppPreferences(getActivity())
                                .setDefaultLocationProvider((String) mSpinnerProviders
                                        .getItemAtPosition(position));

                        mIsSpinnerSetupCompleted = true;
                    }
                } else {
                    mProvider = (String) mSpinnerProviders.getItemAtPosition(position);
                    AppHelpers.getAppPreferences(getActivity())
                            .setDefaultLocationProvider(mProvider);
                    }

                if(getOperationState() == OperationState.ACTIVE) {
                    deactivate();
                    activate();
                }*/
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void handleAccuracy(float accuracy) {
        if (accuracy >= 0 && accuracy < 20) {
            mImageStatus.setImageResource(R.drawable.ic_signal_strength_four);
            return;
        }

        if (accuracy >= 20 && accuracy < 40) {
            mImageStatus.setImageResource(R.drawable.ic_signal_strength_three);
            return;
        }

        if (accuracy >= 40) {
            mImageStatus.setImageResource(R.drawable.ic_signal_strength_two);
            return;
        }
    }

    public void notifyDefaultProviderIsChanged() {
        if (getOperationState() == OperationState.ACTIVE) {
            deactivate();
            mIsSpinnerSetupCompleted = false;
            setupSpinner();
            activate();
        } else {
            deactivate();
            mIsSpinnerSetupCompleted = false;
            setupSpinner();
        }
    }

    @Override
    public void activate() {
        super.activate();
        mIsSpinnerSetupCompleted = false;
        HandlerThread handlerThread = new HandlerThread("LocFragHandler");
        handlerThread.start();
        mLooper = handlerThread.getLooper();
        mLocationManager.requestLocationUpdates(mProvider, 0, 0, this, mLooper);

        if (!mLocationManager.isProviderEnabled(mProvider)) {
            mTvStatus.setText("disabled");
            Toast.makeText(mContext, "Location provider is disabled on your device, " +
                    "please enable it.", Toast.LENGTH_SHORT).show();
        } else {
            mTvStatus.setText("enabling...");
        }
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (mLocationManager != null) {
            mLocationManager.removeUpdates(this);
            if(mLooper != null) {
                mLooper.quit();
                mLooper = null;
            }
        } else {
            FileHelpers.appendToAppLog(new DateTime().getMillis(), AppLogType.ERR
                    , TAG, "mLocationManager" +
                    " reported to be null while trying to deactivate");
        }



        mTvStatus.setText("stopped");
        mTvProvider.setText("---");
        mTvLatitude.setText("---");
        mTvLongitude.setText("---");
        mTvAltitude.setText("---");
        mTvLastFixTime.setText("---");
        mImageStatus.setImageResource(R.drawable.ic_signal_strength_one);

    }

    @Override
    protected void expand(Animation animation, boolean shouldActivate) {
        super.expand(animation, shouldActivate);
    }

    @Override
    protected void collapse(Animation animation, boolean shouldDeactivate) {
        super.collapse(animation, shouldDeactivate);
    }

    public void expand() {
        if(super.getVisibilityState() == VisibilityState.COLLAPSED) {
            super.mToggleVisibilityImageView.performClick();
        }
    }

    public void collapse() {
        if(super.getVisibilityState() == VisibilityState.EXPANDED) {
            super.mToggleVisibilityImageView.performClick();
        }
    }

    @Override
    public void setShouldActivateOnCreate(boolean shouldActivateOnCreate) {
        mShouldActivateOnCreate = shouldActivateOnCreate;
    }

    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        mContext = getActivity();
        mLocationManager = (LocationManager)
                mContext.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onLocationChanged(final Location location) {
        mLoggerListener.onLocationDataReceived(new LocationData(System.currentTimeMillis(),
                location));
        getActivity().runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mTvStatus.setText("active");
                mTvProvider.setText(mProvider);
                mTvLongitude.setText(String.format("%.4f", location.getLongitude()));
                mTvLatitude.setText(String.format("%.4f", location.getLatitude()));
                mTvAltitude.setText(String.format("%.4f", location.getAltitude()));
                mTvLastFixTime.setText(new SimpleDateFormat("HH:mm:ss").format(new DateTime()
                        .withMillis(location.getTime()).getMillis()));

                handleAccuracy(location.getAccuracy());
            }
        });
    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {

        if(!provider.equals(mProvider)) {
            return;
        }

        String text = "";
        switch (status) {
            case LocationProvider.AVAILABLE:
                if (getOperationState() == OperationState.ACTIVE) {
                    getActivity().runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            deactivate();
                            activate();
                        }
                    });

                }
                Log.i(TAG, "Location status changed to AVAILABLE. registering listener");
                break;

            case LocationProvider.OUT_OF_SERVICE:
                mLoggerListener.onLocationDataReceived(new LocationData(System.currentTimeMillis()
                        , null));
                text = "out of service";
                Log.i(TAG, "Location status changed to OUT_OF_SERVICE.");
                break;

            case LocationProvider.TEMPORARILY_UNAVAILABLE:
                text = "temp. unavailable";
                mLoggerListener.onLocationDataReceived(new LocationData(System.currentTimeMillis()
                        , null));
                Log.i(TAG, "Location status changed to TEMPORARILY_UNAVAILABLE.");
                break;
        }

        final String toReport = text;
        if(toReport != null && !toReport.isEmpty()) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvStatus.setText(toReport);
                }
            });
        }
    }

    @Override
    public void onProviderEnabled(String provider) {
        if(provider.equals(mProvider)) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mTvStatus.setText("enabling...");
                }
            });
        }
    }

    @Override
    public void onProviderDisabled(String provider) {

        if(!provider.equals(mProvider)) {
            return;
        }

        if(getOperationState() == OperationState.ACTIVE) {

            mLoggerListener.onLocationDataReceived(new LocationData(System.currentTimeMillis(),
                    null));
            final String prov = provider;
           getActivity().runOnUiThread(new Runnable() {
               @Override
               public void run() {
                   Toast.makeText(mContext, prov + " has been disabled by user or system"
                           , Toast.LENGTH_SHORT).show();
                   mTvStatus.setText("disabled");
               }
           });
        }
    }
}
