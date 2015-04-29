package com.example.aniruddha.activitymonitor;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.ToggleButton;

import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created by Aniruddha on 4/28/2015.
 *
 * Features list(A list of floating points) contains features in this order:
 * Mean(X, Y, Z); Median(X,Y,Z); Min(X,Y,Z); Max(X,Y,Z); Mean Magnitude;
 *
 * (Each element is a floating point number)
 *
 */
public class MainActivity extends Activity {
    private   Boolean       toggleOn        = Boolean.FALSE;
    protected SensorManager sMgr;
    protected Sensor        accelerometer;
    protected SensorData    obj             = new SensorData();
    protected Compute       compute         = new Compute();
    protected List<float[]> sample          = new ArrayList<>();
    protected float[]       values;
    protected final int     MAX_SAMPLE_SIZE = 100;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        /*Intent intent = new Intent();
        intent.setClass(this, SensorData.class);
        startActivity(intent);
        */
        sMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        accelerometer = sMgr.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
    }
    protected class Compute {
        private List    dataSet     = new ArrayList();
        private List    features    = new ArrayList();
        private int     count       = 0;
        private float[] x           = new float[MAX_SAMPLE_SIZE];
        private float[] y           = new float[MAX_SAMPLE_SIZE];
        private float[] z           = new float[MAX_SAMPLE_SIZE];

        protected void createAxialData(){
            int i = 0;
            for(float[] each: sample) {
                x[i] = each[0];
                y[i] = each[1];
                z[i] = each[2];
                i++;
            }
        }
        private   void calMean() {
            float sumX = 0;
            float sumY = 0;
            float sumZ = 0;
            float meanX, meanY, meanZ;

            for(float [] each: sample){
                sumX += each[0];
                sumY += each[1];
                sumZ += each[2];
            }
            meanX = sumX/MAX_SAMPLE_SIZE;
            meanY = sumY/MAX_SAMPLE_SIZE;
            meanZ = sumZ/MAX_SAMPLE_SIZE;

            features.add(meanX);
            features.add(meanY);
            features.add(meanZ);
        }
        private   void calMedian() {
            float medX, medY, medZ;
            float minX, minY, minZ, maxX, maxY, maxZ;

            Arrays.sort(x);
            Arrays.sort(y);
            Arrays.sort(z);

            if(x.length % 2 == 0) medX = (x[x.length/2] + x[x.length/2 -1])/2;
            else                  medX =  x[x.length/2];

            if(y.length % 2 == 0) medY = (y[y.length/2] + y[y.length/2 -1])/2;
            else                  medY =  y[y.length/2];

            if(z.length % 2 == 0) medZ = (z[z.length/2] + z[z.length/2 -1])/2;
            else                  medZ =  z[z.length/2];

            features.add(medX);
            features.add(medY);
            features.add(medZ);

            // Min and max values for the 3 axis obtained here itself
            // to avoid sorting again
            minX = x[0];    maxX = x[MAX_SAMPLE_SIZE - 1];
            minY = y[0];    maxY = y[MAX_SAMPLE_SIZE - 1];
            minZ = z[0];    maxZ = z[MAX_SAMPLE_SIZE - 1];
            features.add(minX);
            features.add(minY);
            features.add(minZ);
            features.add(maxX);
            features.add(maxY);
            features.add(maxZ);
        }
        private   void calMeanMagnitude(){
            float sum = 0;
            for(float[] v: sample) {
                sum += Math.sqrt(Math.pow(v[0],2) + Math.pow(v[1],2) + Math.pow(v[2],2));
            }
            features.add(sum/MAX_SAMPLE_SIZE);
        }
        public    void createSample() {
            if (count < MAX_SAMPLE_SIZE) {
                sample.add(values);
                count++;
            }
            else {
                saveToDisk();
                createAxialData();
                featureSelection();
                dataSet.add(features);
                Log.d("values", features.toString());
                // RESET
                sample.clear();
                features.clear();
                count = 0;
            }
        }
        public    void featureSelection(){
            calMean();
            calMedian();
            calMeanMagnitude();
            //    calDeviation();
            //    calVariance();
        }
        public    void saveToDisk() {
           String filename = "testValues.csv";
           FileOutputStream outputStream;
           try {
               outputStream = openFileOutput(filename, Context.MODE_PRIVATE);
               for(float[] element: sample) {
                    String string = element[0] + "," + element[1] + "," + element[2] + "\n";
                    outputStream.write(string.getBytes());
               }
               outputStream.close();
           } catch (Exception e) {
               e.printStackTrace();
           }

        }

    }
    protected class SensorData implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            values = event.values;
            String data = values[0] + "\n" + values[1] + "\n" + values[2];
            TextView textView = (TextView) findViewById(R.id.textView);
            textView.setText(data);
            compute.createSample();
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
        }
    }
    public void onToggleClicked(View view) {
        // Is the toggle on?
        toggleOn = ((ToggleButton) view).isChecked();

        if (toggleOn) {
            sMgr.registerListener(obj, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
        }
        else {
            sMgr.unregisterListener(obj);
        }
    }
     /*
    protected void onResume() {
        super.onResume();
        if (toggleOn) sMgr.registerListener(obj, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);
     }
    protected void onPause() {
        super.onPause();
        sMgr.unregisterListener(obj);
    }
    */

}
