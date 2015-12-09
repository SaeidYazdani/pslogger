package com.embedonix.pslogger.fragments;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import com.embedonix.pslogger.R;
import com.embedonix.pslogger.data.logging.Holder;
import com.embedonix.pslogger.data.logging.MiscSensorsData;
import com.embedonix.pslogger.data.logging.LoggerListener;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 29/07/2014.
 */
public class MiscSensorsFragment extends CollapsibleFragment
        implements SensorEventListener {

    public static final String TAG = "MiscSensorsFragment";
    private LoggerListener mLoggerListener;


    private TextView mTvProximity;
    private TextView mTvMagneticField;
    private TextView mTvLight;

    private String mProximityValue = Holder.NOT_TRIGGERED;
    private float mMagneticValue;
    private float mLightValue;

    private SensorManager mSensorManager;
    private float mProximityMaxRange;

    public MiscSensorsFragment() {
        setName(TAG);
        super.TAG = TAG;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.fragment_miscsensors, container, false);
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

        mHeaderLayout = mView.findViewById(R.id.miscsensorsHeader);
        //mHeaderLayout.setOnTouchListener(mSupport.getContainer());
        mContainer = mView.findViewById(R.id.miscsensorsContainer);
        mCollapsedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
        mExpandedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_add);

        mToggleVisibilityImageView =
                (ImageView) mView.findViewById(R.id.miscsensorsToggleVisibility);
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
        mTvProximity = (TextView) mView.findViewById(R.id.fragMiscProximity);
        mTvMagneticField = (TextView) mView.findViewById(R.id.fragMiscCompass);
        mTvLight = (TextView) mView.findViewById(R.id.fragMiscLight);

        mTvLight.setText("stopped");
        mTvLight.setText("stopped");
        mTvMagneticField.setText("stopped");
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
    protected void collapse(Animation animation, boolean shouldDeactivate) {
        super.collapse(animation, shouldDeactivate);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
        }

        mTvProximity.setText("stopped");
        mTvLight.setText("stopped");
        mTvMagneticField.setText("stopped");
    }

    @Override
    protected void expand(Animation animation, boolean shouldActivate) {
        super.expand(animation, shouldActivate);
    }

    @Override
    public void activate() {
        super.activate();

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);

        boolean isAnythingSupported = false;

        //Proximity Sensor
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
            mProximityMaxRange = mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                    .getMaximumRange(); //need the range to tell if its FAR or NEAR
            mSensorManager.registerListener(this
                    , mSensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                    , SensorManager.SENSOR_DELAY_NORMAL);
            isAnythingSupported = true;
        } else {
            mProximityValue = Holder.NOT_SUPPORTED;
            mTvProximity.setText("not supported");
        }

        //Magnetic Field Sensor
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
            mSensorManager.registerListener(this
                    , mSensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                    , SensorManager.SENSOR_DELAY_NORMAL);
            isAnythingSupported = true;
        } else {
            mMagneticValue = Holder.INVALID_VALUE;
            mTvMagneticField.setText("not supported");
        }

        //LIGHT SENSOR
        if (mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
            mSensorManager.registerListener(this
                    , mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                    , SensorManager.SENSOR_DELAY_NORMAL);
            isAnythingSupported = true;
        } else {
            mLightValue = Holder.INVALID_VALUE;
            mTvLight.setText("not supported");
        }

        //none of the sensors are supported, reporting a NS to listener
        if(!isAnythingSupported) {
            mLoggerListener.onMiscSensorsReceived(new MiscSensorsData(System.currentTimeMillis()
            , Holder.NOT_SUPPORTED, Holder.INVALID_VALUE, Holder.INVALID_VALUE));
        }
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        String oldProximity = mProximityValue;
        float oldLight = mLightValue;
        float oldMagnetic = mMagneticValue;


        if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
            if (event.values[0] < mProximityMaxRange) {
                mProximityValue = Holder.PROXIMITY_NEAR;
            } else {
                mProximityValue = Holder.PROXIMITY_FAR;
            }
            mTvProximity.setText(mProximityValue);
        }

        if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
            mLightValue = event.values[0];
            mTvLight.setText(String.valueOf(mLightValue));
        }

        if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
            mMagneticValue = event.values[0];
            mTvMagneticField.setText(String.valueOf(mMagneticValue));
        }

        //check if any of the values have been changed since last occurrence of event
        if ((!oldProximity.equals(mProximityValue)) || (oldLight != mLightValue)
                || (oldMagnetic != mMagneticValue)) {

            mLoggerListener.onMiscSensorsReceived(new MiscSensorsData(System.currentTimeMillis()
                    , mProximityValue , mMagneticValue, mLightValue));
        }

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }
}
