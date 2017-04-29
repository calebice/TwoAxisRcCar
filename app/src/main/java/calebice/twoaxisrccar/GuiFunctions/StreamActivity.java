/*
 * Copyright 2014 Google Inc. All Rights Reserved.

 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package calebice.twoaxisrccar.GuiFunctions;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

import com.google.vrtoolkit.cardboard.CardboardView;
import com.google.vrtoolkit.cardboard.EyeTransform;
import com.google.vrtoolkit.cardboard.HeadTransform;
import com.google.vrtoolkit.cardboard.Viewport;

import java.io.IOException;
import java.util.HashMap;

import javax.microedition.khronos.egl.EGLConfig;

import calebice.twoaxisrccar.R;
import calebice.twoaxisrccar.Servo.messageProcessor;
import calebice.twoaxisrccar.Client.UDP_Client;
import calebice.twoaxisrccar.mjpeg.MjpegInputStream;
import calebice.twoaxisrccar.mjpeg.MjpegPlayer;

/**
 * A Cardboard application that streams video from an online address and port
 * Builds on top of a CardboardView
 */
public class StreamActivity extends Activity implements CardboardView.StereoRenderer {

    private static final String TAG = "StreamActivity";

    final Context me = this;
    private OverlayView mOverlayView;
    private MjpegPlayer mp;
    private String baseUrl = "http://";
    private ClientThread CT;
    private String ip="";
    private String port="";

    public StreamActivity() {
    }

    /**
     * Sets the view to our CardboardView and initializes the transformation matrices we will use
     * to render our scene.
     * @param savedInstanceState
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.common_ui);
        CardboardView cardboardView = (CardboardView) findViewById(R.id.cardboard_view);
        cardboardView.setRenderer(this);

        Intent i = getIntent();
        ip += i.getExtras().get("ip");
        port += i.getExtras().get("port");
        baseUrl += ip;

        CT = new ClientThread();

        mOverlayView = (OverlayView) findViewById(R.id.overlay);
        CT.run();

        startPlayer();
    }


    /**
     * Sets up the MjpegPlayer using the OverlayView
     * Attaches the :5000/stream/video.mjpeg in order to get valid stream
     * Creates a DoRead oject which creates a thread to render the Mjpeg stream
     */
    private void startPlayer(){
        String URL = baseUrl + ":5000/stream/video.mjpeg";
        mp = new MjpegPlayer(mOverlayView);
        (new DoRead()).execute(URL);
    }

    /*Required methods in order to use the Google API renderer */
    /*None of these are required for video streaming, required implementations of abstracts*/
    /*Used for implementing CardboardView.StereoRenderer*/
    @Override
    public void onRendererShutdown(){Log.i(TAG, "onRendererShutdown");}
    @Override
    public void onSurfaceChanged(int width, int height) {Log.i(TAG, "onSurfaceChanged");}
    @Override
    public void onSurfaceCreated(EGLConfig config) {Log.i(TAG, "onSurfaceCreated");}
    @Override
    public void onNewFrame(HeadTransform headTransform) {}
    @Override
    public void onDrawEye(EyeTransform transform) {}
    @Override
    public void onFinishFrame(Viewport viewport) {}
    /*End non-used method implementations*/

    class DoRead extends AsyncTask<String, Void, MjpegInputStream> {

        @Override
        protected MjpegInputStream doInBackground(String... params) {
            return MjpegInputStream.read(params[0]);
        }

        protected void onPostExecute(MjpegInputStream result) {
            if (result == null){
                Toast.makeText(me,ip+" has no valid stream",Toast.LENGTH_LONG).show();

                startActivity(new Intent(me,ConfigActivity.class));
            }
            mp.setSource(result);
            Log.i(TAG, "running mjpeg input stream");
        }
    }

    /**
     * Helper class that accesses Android accelerometer values and then prepares and sends
     * a message to the Raspberry Pi server listener program
     */
    private class ClientThread implements SensorEventListener {
        boolean closeProgram = false;
        messageProcessor servoMsg = new messageProcessor();
        Sensor accelerometer;
        SensorManager sm;
        TextView text_x;
        TextView text_y;
        TextView text_z;
        TextView text_drive;
        int x, y, z, o_x, o_y, o_z;
        int d;

        /**
         * Sets up the textfields, the sensor listener, quit button and the modeSwitch
         */
        public void run() {

            //Toast.makeText(MainActivity.this, "Starting Program...", Toast.LENGTH_SHORT).show();

            final Intent i = getIntent();
            //TextView Setup for X, Y, Z
            text_x = (TextView) findViewById(R.id.textView_x);
            text_y = (TextView) findViewById(R.id.textView_y);
            text_z = (TextView) findViewById(R.id.textView_z);
            text_drive = (TextView) findViewById(R.id.textView_d);

            //Accelerometer Setup
            sm = (SensorManager) getSystemService(SENSOR_SERVICE);
            accelerometer = sm.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
            sm.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_NORMAL);

            final Switch modeSwitch = (Switch)findViewById(R.id.modeSwitch);
            modeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener(){
                public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                    if (isChecked) {
                        servoMsg.setMode(2);
                        modeSwitch.setText(R.string.textSurv);
                    } else {
                        servoMsg.setMode(1);
                        modeSwitch.setText(R.string.textSport);
                    }
                }
            });

            final Button fwdButton = (Button)findViewById(R.id.fwdbuttonID);
            fwdButton.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    /*
                      Reads the current mode and if it is in sport (mode = 1) then it changes on press
                      otherwise it ignores user input
                     */
                    switch(servoMsg.getMode()){
                        case 1:
                            switch(event.getAction()){
                                case MotionEvent.ACTION_DOWN:
                                    fwdButton.performClick();
                                    d = 1;
                                    text_drive.setText("D: FWD");
                                    break;
                                case MotionEvent.ACTION_UP:
                                    d = 0;
                                    text_drive.setText("D: STOP");
                                    break;
                            }
                    }
                    return false;
                }
            });


            final Button revButton = (Button)findViewById(R.id.revbuttonID);
            revButton.setOnTouchListener(new View.OnTouchListener(){
                @Override
                public boolean onTouch(View v, MotionEvent event) {

                    /*
                       Reads the current mode and only responds if it is in sport mode (mode = 1)
                     */
                    switch(servoMsg.getMode()){
                        case 1:
                            switch(event.getAction()) {
                                case MotionEvent.ACTION_DOWN:
                                    d = 2;
                                    text_drive.setText("D: REV");
                                    break;
                                case MotionEvent.ACTION_UP:
                                    d = 0;
                                    text_drive.setText("D: STOP");
                                    break;
                            }

                    }
                    return false;

                }
            });

            final Button quitButton = (Button)findViewById(R.id.quitButton);
            quitButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    closeProgram = true;

                    finishActivity(0);
                    Intent ReturnToSignin = new Intent(StreamActivity.this, ConfigActivity.class);
                    System.exit(0);
                    startActivity(ReturnToSignin);
                }
            });


        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {}

        /**
         * Listens for changes in the different axis accelerometer ranges and upon a change
         * puts together a message and writes it out to the server using a UDP_Client object
         * @param event Instance of a accelerometer value change
         */
        @Override
        public void onSensorChanged(SensorEvent event) {
            x = -1*((int) event.values[1]) + 9;
            y = (int) event.values[1] + 9;
            z = (int) event.values[2] + 9;
            servoMsg.setFormatMessage(x,y,z,d);

            //if old_val != new_val send message to RPI3
            if (x != o_x ||y != o_y || z != o_z) {
                HashMap<String, Object> params = new HashMap<>();
                params.put("ip", ip);
                params.put("msg", servoMsg.getFormatMessage());
                params.put("port", port);
                if(!closeProgram) {
                    try {
                        new UDP_Client().execute(params);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }

                /**/
                o_x = x;
                o_y = y;
                o_z = z;
                text_x.setText("X: " + x);
                text_y.setText("Y: " + y);
                text_z.setText("Z: " + z);
            }
        }
    }


}