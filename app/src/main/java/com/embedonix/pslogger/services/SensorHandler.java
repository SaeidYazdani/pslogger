package com.embedonix.pslogger.services;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.os.Bundle;
import android.os.Handler;
import android.os.HandlerThread;
import android.os.Looper;
import android.util.Log;

import com.embedonix.pslogger.R;
import com.embedonix.pslogger.SensorLoggerApplication;
import com.embedonix.pslogger.data.logging.AccelerometerData;
import com.embedonix.pslogger.data.logging.Holder;
import com.embedonix.pslogger.data.logging.LocationData;
import com.embedonix.pslogger.data.logging.LogData;
import com.embedonix.pslogger.data.logging.Logger;
import com.embedonix.pslogger.data.logging.MiscSensorsData;
import com.embedonix.pslogger.helpres.AppHelpers;

import java.util.HashMap;

/**
 * This class manages 3 listeners for accelerometer, location and misc sensors. All of these
 * listeners run on their own thread and looper. This helps removing burden from main thread.
 *
 * Created by Saeid on 23-4-2014.
 */
public class SensorHandler {

    public static final String TAG = "SensorHandler";

    private SensorLoggerApplication mApplication;
    private Logger mLogger;

    private AccelerometerListener mAccelerometerListener;
    private MiscSensorListener mMiscSensorListener;
    private LocationMonitor mLocationMonitor;

    public SensorHandler(SensorLoggerApplication application, Logger logger) {
        mApplication = application;
        mLogger = logger;

        mAccelerometerListener = new AccelerometerListener(mApplication.getApplicationContext()
                , mLogger);
        mMiscSensorListener = new MiscSensorListener(mApplication.getApplicationContext(), logger);
        mLocationMonitor = new LocationMonitor(mApplication.getApplicationContext(), logger);
    }

    public void start() {
        if(mApplication.getAppPreferences().canLogAccelerometer()) {
            mAccelerometerListener.start();
        }
        if(mApplication.getAppPreferences().canLogMiscSensors()) {
            mMiscSensorListener.start();
        }
        if(mApplication.getAppPreferences().canLogLocation()){
            mLocationMonitor.start();
        }
    }

    public void stop(){
        if(mAccelerometerListener != null) {
            mAccelerometerListener.stop();
        }
        if(mMiscSensorListener != null) {
            mMiscSensorListener.stop();
        }
        if(mLocationMonitor != null) {
            mLocationMonitor.stop();
        }
    }

    private class LocationMonitor implements LocationListener {
        
        public static final String TAG = "LocationMonitor";
        
        private Context context;
        private Logger logger;        
        private Looper looper;
        private String provider;

        public LocationMonitor(Context context, Logger logger) {
            this.context = context;
            this.logger = logger;
        }
        
        public void start(){
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            looper = handlerThread.getLooper();

            provider = AppHelpers.getAppPreferences(context).getDefaultLocationProvider();
            LocationManager locationManager
                    = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.requestLocationUpdates(provider, 0, 0, this, looper);
        }
        
        public void stop(){
            LocationManager locationManager
                    = (LocationManager)context.getSystemService(Context.LOCATION_SERVICE);
            locationManager.removeUpdates(this);
            
            if(looper != null) {
                looper.quit();
                looper = null;
            }           
        }

        @Override
        public void onLocationChanged(Location location) {
            mLogger.updateLocationData(new LocationData(System.currentTimeMillis(),
                    location));
            
            //this should happen when the user does not want accelerometer to be logged
            if (AppHelpers.getAppPreferences(context).canLogAccelerometer()) {
                mLogger.addToQueue(new LogData(null, null, null));
            }
          
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            if(this.provider.equals(provider)) {
                switch (status) {
                    case LocationProvider.AVAILABLE:

                        break;

                    case LocationProvider.OUT_OF_SERVICE:
                    case LocationProvider.TEMPORARILY_UNAVAILABLE:
                        logger.updateLocationData(new LocationData(System.currentTimeMillis(),
                                null));
                        break;
                }
            }
        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    }
    
    private class AccelerometerListener implements SensorEventListener {
        
        public static final String TAG = "AccelerometerListener";
        
        private Context context;
        private Logger logger;
        
        private Handler handler;

        public AccelerometerListener(Context context, Logger logger) {
            this.context = context;
            this.logger = logger;
        }
        
        public void start(){
            HandlerThread handlerThread = new HandlerThread(TAG);
            handlerThread.start();
            handler = new Handler(handlerThread.getLooper());

            HashMap<String, Integer> mapStrToInt = new HashMap<String, Integer>();
            HashMap<Integer, String> mapIntToStr = new HashMap<Integer, String>();
            String[] items = context.getResources()
                    .getStringArray(R.array.accelerometer_speeds_items);
            String[] values = context.getResources()
                    .getStringArray(R.array.accelerometer_speeds_values);

            for (int i = 0; i < items.length; i++) {
                mapStrToInt.put(items[i], Integer.valueOf(values[i]));
                mapIntToStr.put(Integer.valueOf(values[i]), items[i]);
            }

            int speed = mapStrToInt.get(mApplication.getAppPreferences()
                    .getDefaultAccelerometerSpeed());

            SensorManager sensorManger = 
                    (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
            sensorManger.registerListener(this
                    , sensorManger.getDefaultSensor(Sensor.TYPE_ACCELEROMETER), speed, handler);
        }
        
        public void stop() {
            SensorManager sensorManger =
                    (SensorManager)context.getSystemService(Context.SENSOR_SERVICE);
            sensorManger.unregisterListener(this);
            
            if(handler != null) {
                handler.getLooper().quit();
                handler = null;
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {
            logger.addToQueue(new LogData(new AccelerometerData(System.currentTimeMillis()
            , event.timestamp, event.values[0], event.values[1], event.values[2])
            , null, null));
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }

    private class MiscSensorListener implements SensorEventListener {

        public static final String TAG = "MiscSensorListener";

        private Context context;
        private Logger logger;

        private float proximityMaxRange;
        private String proximityValue = Holder.NOT_TRIGGERED;
        private float magneticValue;
        private float lightValue;
        private Handler handler;
        
        public MiscSensorListener(Context context, Logger logger) {
            this.context = context;
            this.logger = logger;
        }

        public void start(){

            boolean anyMiscSupported = false;

            HandlerThread thread = new HandlerThread("MiscSensorListener");
            thread.start();
            handler = new Handler(thread.getLooper());
            
            SensorManager sensorManager 
                    = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);

            //Proximity Sensor
            if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
                proximityMaxRange = sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                        .getMaximumRange(); //need the range to tell if its FAR or NEAR

                sensorManager.registerListener(this
                        , sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY)
                        , SensorManager.SENSOR_DELAY_NORMAL, handler);
                proximityValue = Holder.NOT_TRIGGERED;
                anyMiscSupported = true;
            } else {
                proximityValue = Holder.NOT_SUPPORTED;
            }

            //Magnetic Field Sensor
            if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
                sensorManager.registerListener(this
                        , sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
                        , SensorManager.SENSOR_DELAY_NORMAL, handler);
                anyMiscSupported = true;
            } else {
                magneticValue = Holder.INVALID_VALUE;
            }

            //LIGHT SENSOR
            if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
                sensorManager.registerListener(this
                        , sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT)
                        , SensorManager.SENSOR_DELAY_NORMAL, handler);
                anyMiscSupported = true;
            } else {
                lightValue = Holder.INVALID_VALUE;
            }

            if (!anyMiscSupported) {
                mLogger.updateMiscSensorsData(new MiscSensorsData(System.currentTimeMillis(),
                        Holder.NOT_SUPPORTED, Holder.INVALID_VALUE, Holder.INVALID_VALUE));
            }
        }

        public void stop(){
            SensorManager sensorManager 
                    = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
            sensorManager.unregisterListener(this);

            if(handler != null) {
                handler.getLooper().quit();
                handler = null;
            }
        }

        @Override
        public void onSensorChanged(SensorEvent event) {

            String oldProximity = proximityValue;
            float oldLight = lightValue;
            float oldMagnetic = magneticValue;

            if (event.sensor.getType() == Sensor.TYPE_PROXIMITY) {
                if (event.values[0] < proximityMaxRange) {
                    proximityValue = Holder.PROXIMITY_NEAR;
                } else {
                    proximityValue = Holder.PROXIMITY_FAR;
                }
            }

            if (event.sensor.getType() == Sensor.TYPE_LIGHT) {
                lightValue = event.values[0];
            }

            if (event.sensor.getType() == Sensor.TYPE_MAGNETIC_FIELD) {
                magneticValue = event.values[0];
            }

            //check if any of the values have been changed since last occurrence of event
            if ((!oldProximity.equals(proximityValue)) || (oldLight != lightValue)
                    || (oldMagnetic != magneticValue)) {

                logger.updateMiscSensorsData(new MiscSensorsData(System.currentTimeMillis()
                        , proximityValue, magneticValue, lightValue));

                //this should happen when the user does not want accelerometer to be logged
                if (!AppHelpers.getAppPreferences(context).canLogAccelerometer()
                        || !AppHelpers.getAppPreferences(context).canLogLocation()) {
                    logger.addToQueue(new LogData(null, null, null));
                }
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    }
}
