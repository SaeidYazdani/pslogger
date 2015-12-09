package com.embedonix.pslogger.fragments;

import android.content.Context;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;

import com.embedonix.pslogger.R;
import com.embedonix.pslogger.data.logging.AccelerometerData;
import com.embedonix.pslogger.data.logging.LoggerListener;
import com.embedonix.pslogger.helpres.AppHelpers;
import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle;
import com.jjoe64.graphview.LineGraphView;

import java.util.HashMap;

/**
 * Pervasive Systems Research Group
 * University Twente
 * Created by Saeid on 28/07/2014.
 */
public class AccelerometerFragment extends CollapsibleFragment implements SensorEventListener {

    public static final String TAG = "AccFragment";

    private LoggerListener mLoggerListener;

    private GraphView mGraphView;
    private boolean mShouldAddDataToGraph;
    private GraphViewSeries mSeriesX;
    private GraphViewSeries mSeriesY;
    private GraphViewSeries mSeriesZ;

    private LinearLayout mGraphHolder;
    private CheckBox mCheckBoxX;
    private CheckBox mCheckBoxY;
    private CheckBox mCheckBoxZ;
    private Spinner mSpinner;
    private ArrayAdapter<String> mAdapter;

    private HashMap<String, Integer> mMapStringToInteger;
    private HashMap<Integer, String> mMapIntegerToString;

    private SensorManager mSensorManager;
    private Sensor mAccelerometer;
    private int mSpeed;
    private Handler mHandler;

    private int mSamples = 0;

    public AccelerometerFragment() {
        setName(TAG);
        super.TAG = TAG;
    }

    private void setupWidgets() {
        mCheckBoxX = (CheckBox) mView.findViewById(R.id.fragAccCheckBoxShowX);
        mCheckBoxY = (CheckBox) mView.findViewById(R.id.fragAccCheckBoxShowY);
        mCheckBoxZ = (CheckBox) mView.findViewById(R.id.fragAccCheckBoxShowZ);

        mCheckBoxX.setChecked(true);
        mCheckBoxY.setChecked(true);
        mCheckBoxZ.setChecked(true);

        mSpinner = (Spinner) mView.findViewById(R.id.accelerometerSpeedSpinner);

        mMapStringToInteger = new HashMap<String, Integer>();
        mMapIntegerToString = new HashMap<Integer, String>();
        String[] items = getActivity().getResources().
                getStringArray(R.array.accelerometer_speeds_items);
        String[] val = getActivity().getResources().
                getStringArray(R.array.accelerometer_speeds_values);

        for (int i = 0; i < items.length; i++) {
            mMapStringToInteger.put(items[i], Integer.valueOf(val[i]));
            mMapIntegerToString.put(Integer.valueOf(val[i]), items[i]);
        }

        mAdapter = new ArrayAdapter<String>(getActivity()
                , R.layout.spinner_location_providers, items);
        mSpinner.setAdapter(mAdapter);

        String defSpeed = AppHelpers.getAppPreferences(getActivity())
                .getDefaultAccelerometerSpeed();
        mSpeed = mMapStringToInteger.get(defSpeed);

        for (int i = 0; i < mSpinner.getAdapter().getCount(); i++) {
            if (mSpinner.getItemAtPosition(i).equals(defSpeed)) {
                mSpinner.setSelection(i);
            }
        }

        mSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {

                mSpeed = mMapStringToInteger.get(mSpinner.getAdapter()
                        .getItem(position));

                AppHelpers.getAppPreferences(getActivity())
                        .setAccelerometerDefaultSpeed(mMapIntegerToString.get(mSpeed));

                if (getOperationState() == OperationState.ACTIVE) {
                    deactivate();
                    activate();
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {

            }
        });
    }

    private void initializeGraphView() {
        mGraphView = getGraphView();
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT);

        mGraphView.setMinimumHeight((AppHelpers
                .getDeviceScreenHeightAsPixel(getActivity()) / 2) - 100);
        mGraphHolder.setLayoutParams(lp);
        mGraphHolder.addView(mGraphView);
    }

    private GraphView getGraphView() {

        if (mGraphView != null) {
            mGraphView.removeAllSeries();
            mGraphView.redrawAll();
        }

        GraphView gv;
        gv = new LineGraphView(getActivity(), "Acceleration");
        gv.getGraphViewStyle().setTextSize(14);
        gv.getGraphViewStyle().setNumHorizontalLabels(0);
        gv.getGraphViewStyle().setNumHorizontalLabels(6);
        gv.getGraphViewStyle().setGridStyle(GraphViewStyle.GridStyle.VERTICAL);
        gv.setManualYAxisBounds(25, -25);
        gv.getGraphViewStyle().setNumVerticalLabels(10);
        int defaultViewPort = AppHelpers.getAppPreferences(getActivity())
                .getDefaultAccelerometerGraphViewPortSize();
        gv.setViewPort(0, defaultViewPort);
        gv.setScalable(true);
        gv.setLegendAlign(GraphView.LegendAlign.BOTTOM);
        gv.setShowLegend(true);
        gv.getGraphViewStyle().setHorizontalLabelsColor(Color.TRANSPARENT);
        gv.setLayoutParams(new LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.MATCH_PARENT));
        gv.setPadding(2,0,0,0);

        mSeriesX = new GraphViewSeries("X Axis"
                , new GraphViewSeries.GraphViewSeriesStyle(Color.RED, 2)
                , new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(0, 0)
        });

        mSeriesY = new GraphViewSeries("Y Axis"
                , new GraphViewSeries.GraphViewSeriesStyle(Color.GREEN, 2)
                , new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(0, 0)
        });

        mSeriesZ = new GraphViewSeries("Z Axis"
                , new GraphViewSeries.GraphViewSeriesStyle(Color.BLUE, 2)
                , new GraphView.GraphViewData[]{
                new GraphView.GraphViewData(0, 0)
        });

        gv.addSeries(mSeriesX);
        gv.addSeries(mSeriesY);
        gv.addSeries(mSeriesZ);

        return gv;
    }

    private void offerAccelerometerDataToGraph(float x, float y, float z) {
        mSamples++;

        if (mCheckBoxX.isChecked()) {
            mSeriesX.appendData(new GraphView.GraphViewData(mSamples, x), true, 1000);
        }
        if (mCheckBoxY.isChecked()) {
            mSeriesY.appendData(new GraphView.GraphViewData(mSamples, y), true, 1000);
        }
        if (mCheckBoxZ.isChecked()) {
            mSeriesZ.appendData(new GraphView.GraphViewData(mSamples, z), true, 1000);
        }

        if (mSamples == Integer.MAX_VALUE) {
            mSamples = 0;
            mGraphView = getGraphView();
        }
    }

    public void expand() {
        if (super.getVisibilityState() == VisibilityState.COLLAPSED) {
            super.mToggleVisibilityImageView.performClick();
        }
    }

    public void collapse() {
        if (super.getVisibilityState() == VisibilityState.EXPANDED) {
            super.mToggleVisibilityImageView.performClick();
        }
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container
            , Bundle savedInstanceState) {

        mView = inflater.inflate(R.layout.fragment_accelerometer, container, false);
        setupView();
        setupWidgets();
        initializeGraphView();
        if (mShouldActivateOnCreate) {
            activate();
        }
        return mView;
    }

    @Override
    protected void setupView() {
        mLoggerListener = (LoggerListener) getActivity();
        mSupport = (CollapsibleFragmentSupport) getActivity();
        mHeaderLayout = mView.findViewById(R.id.accelerometerHeader);
        //mHeaderLayout.setOnTouchListener(mSupport.getContainer());
        mContainer = mView.findViewById(R.id.accelerometerContainer);
        mCollapsedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_close_clear_cancel);
        mExpandedStateDrawable =
                getActivity().getResources()
                        .getDrawable(android.R.drawable.ic_menu_add);

        mGraphHolder = (LinearLayout) mView.findViewById(R.id.accelerometerGraphHolder);

        mToggleVisibilityImageView =
                (ImageView) mView.findViewById(R.id.accelerometerToggleVisibility);
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

    @Override
    public void setShouldActivateOnCreate(boolean shouldActivateOnCreate) {
        mShouldActivateOnCreate = shouldActivateOnCreate;
    }

    @Override
    public void activate() {
        super.activate();
        HandlerThread handlerThread = new HandlerThread("AccFragHandler");
        handlerThread.start();
        mHandler = new Handler(handlerThread.getLooper());

        mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
        mAccelerometer = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        mSensorManager.registerListener(AccelerometerFragment.this, mAccelerometer
                , mSpeed, mHandler);
    }

    @Override
    public void deactivate() {
        super.deactivate();
        if (mSensorManager != null) {
            mSensorManager.unregisterListener(this);
            mGraphView.redrawAll();
        }

        if (mHandler != null) {
            mHandler.getLooper().quit();
            mHandler = null;
        }
    }

    @Override
    protected void expand(Animation animation, boolean shouldActivate) {
        super.expand(animation, shouldActivate);
    }

    @Override
    protected void collapse(Animation animation, boolean shouldDeactivate) {
        super.collapse(animation, shouldDeactivate);
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

        final long now = System.currentTimeMillis();
        final long eventTime = event.timestamp;
        final float x = event.values[0];
        final float y = event.values[1];
        final float z = event.values[2];

        mLoggerListener.onAccelerometerDataReceived(new AccelerometerData(now, eventTime, x, y, z));

        if (mShouldAddDataToGraph) {
            getActivity().runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    offerAccelerometerDataToGraph(x, y, z);
                }
            });
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onResume() {
        super.onResume();
        mShouldAddDataToGraph = true;
        //Log.i(TAG, "OnResume() got called. Enabling drawing of graph");
    }

    @Override
    public void onPause() {
        super.onPause();
        //mShouldAddDataToGraph = false;
        //Log.i(TAG, "OnPause() got called. Disabling drawing of graph");
    }

    @Override
    public void onStop() {
        super.onStop();
        //mShouldAddDataToGraph = false;
        Log.i(TAG, "OnResume() got called. Disabling drawing of graph");
    }

    @Override
    public void onDetach() {
        mShouldAddDataToGraph = false;
        super.onDetach();
    }

    public void notifyDefaultSpeedIsChanged() {


        if (mMapIntegerToString != null && mMapStringToInteger != null) {


            String newSpeed = AppHelpers.getAppPreferences(getActivity())
                    .getDefaultAccelerometerSpeed();

            if (mSpinner != null) {
                for (int i = 0; i < mSpinner.getAdapter().getCount(); i++) {
                    if (newSpeed.equals(mSpinner.getAdapter().getItem(i))) {
                        mSpinner.setSelection(i);
                        break;
                    }
                }
            }
        }
    }

    public void notifyDefaultGraphViewPortIsChanged() {
        if (mGraphView != null) {
            int newViewPort = AppHelpers.getAppPreferences(getActivity())
                    .getDefaultAccelerometerGraphViewPortSize();
            mGraphView.setViewPort(0, newViewPort);
        }
    }
}
