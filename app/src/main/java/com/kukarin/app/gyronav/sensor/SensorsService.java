package com.kukarin.app.gyronav.sensor;

import android.app.Service;
import android.content.Intent;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.IBinder;
import android.os.Vibrator;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.widget.Toast;

import com.kukarin.app.gyronav.Settings;

/**
 * Created by Alex on 3/4/2016.
 */
public class SensorsService extends Service {
    private static final String TAG = "SensorsService";
    private SensorManager sensorManager;
    private long lastUpdate;
    SensorEventListener listen;

    //Gyro
    private static final float EPSILON = 1.0f;
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float giroTimeStamp;
    private boolean hasInitialOrientation;
    private float[] initialRotationMatrix;
    private int mIsRotXstarted = 0;
    private int mIsRotYstarted = 0;
    private int mIsRotZstarted = 0;
    private int mCount = 0;
    private int mIsXMoveStarted = 0;
    private int mIsYMoveStarted = 0;
    private int mIsZMoveStarted = 0;
    private long accTimeStamp = 0;


    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        registerListeners();
        return START_STICKY;
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    private void registerListeners() {
        lastUpdate = System.currentTimeMillis();
        listen = new SensorListen();
        sensorManager = (SensorManager) getApplicationContext()
                .getSystemService(SENSOR_SERVICE);
        Sensor accel = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        Sensor gyro = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        sensorManager.registerListener(listen, accel, SensorManager.SENSOR_DELAY_UI);
        sensorManager.registerListener(listen, gyro, SensorManager.SENSOR_DELAY_UI);
    }

    private void getAccelerometer(SensorEvent event) {
        /*
        float[] values = event.values;
        // Movement
        float axisX = values[0];
        float axisY = values[1];
        float axisZ = values[2];

        //Log.i(TAG, "("+axisX+"|"+axisY+"|"+axisZ+")");

        float fastMove = 5f;
        float timeDelta = 0.6f;

        float movXabs = 0, movYabs = 0, movZabs = 0;
        int movXsig = 0, movYsig = 0, movZsig = 0;
        if (axisX > fastMove) {
            movXabs = axisX;
            movXsig = 1;
        }
        if (axisX < -fastMove) {
            movXabs = -axisX;
            movXsig = -1;
        }
        if (axisY > fastMove) {
            movYabs = axisY;
            movYsig = 1;
        }
        if (axisY < -fastMove) {
            movYabs = -axisY;
            movYsig = -1;
        }
        if (axisZ > fastMove) {
            movZabs = axisZ;
            movZsig = 1;
        }
        if (axisZ < -fastMove) {
            movZabs = -axisZ;
            movZsig = -1;
        }

        //Log.d(TAG, "X="+axisX);
        int cmd = mIsXMoveStarted + mIsYMoveStarted + mIsZMoveStarted;

        //rotation is too slow => either none, or stopped
        if (movXsig == 0 && movYsig == 0 && movZsig == 0) {
            if (cmd > 0) { //something already started rotating, so this is end of that move
                if((event.timestamp-accTimeStamp)* NS2S > timeDelta) //don't register too fast events
                    sendEvent(cmd);
                mIsXMoveStarted = mIsYMoveStarted = mIsZMoveStarted = 0; //reset command
                accTimeStamp = event.timestamp;
            }
            return;
        }
        mCount++;
        // else fast enough rotation detected
        if (cmd == 0) //Nothing rotated yet, check which axis is rotating fastest
            Log.i(TAG, "<"+movXsig+"|"+movYsig+"|"+movZsig+">");
        if (movXabs > movYabs && movXabs > movZabs) {
            if (mIsXMoveStarted == 0) mIsXMoveStarted = movXsig > 0 ? 12 : 14;  //   1
        } else if (movYabs > movXabs && movYabs > movZabs) {                //4     2
            if (mIsYMoveStarted == 0) mIsYMoveStarted = movYsig > 0 ? 11 : 13;  //   3
        } else if (movZabs > movXabs && movZabs > movYabs) {                // CCW  6
            if (mIsZMoveStarted == 0) mIsZMoveStarted = movZsig > 0 ? 16 : 15;  // CW   5
        }
        */

        /* ORIG INTERPOL
        float accelationSquareRoot = (x * x + y * y + z * z)
                / (SensorManager.GRAVITY_EARTH * SensorManager.GRAVITY_EARTH);
        long actualTime = System.currentTimeMillis();
        if (accelationSquareRoot >= 7) //
        {
            if (actualTime - lastUpdate < 2000) {
                return;
            }
            lastUpdate = actualTime;
            Toast.makeText(this,
                    "Device was shuffed _ " + accelationSquareRoot,
                    Toast.LENGTH_SHORT).show();
            Vibrator v = (Vibrator) getApplicationContext().getSystemService(VIBRATOR_SERVICE);
            v.vibrate(1000);
            Intent startMain = new Intent(Intent.ACTION_MAIN);
            startMain.addCategory(Intent.CATEGORY_HOME);
            startMain.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(startMain);
        }
        */
    }

    private void getGyroscope(SensorEvent event) {
        // This timestep's delta rotation to be multiplied by the current rotation
        // after computing it from the gyro sample data.
        //if (giroTimeStamp != 0) {
            //final float dT = (event.timestamp - giroTimeStamp) * NS2S;
            // rotation speed
            float axisX = event.values[0];
            float axisY = event.values[1];
            float axisZ = event.values[2];
            float timeDelta = 0.6f; //sec
            float fastRotation = 3.5f; //rad/sec
            float rotXabs = 0, rotYabs = 0, rotZabs = 0;
            int rotXsig = 0, rotYsig = 0, rotZsig = 0;
            if (axisX > fastRotation) {
                rotXabs = axisX;
                rotXsig = 1;
            }
            if (axisX < -fastRotation) {
                rotXabs = -axisX;
                rotXsig = -1;
            }
            if (axisY > fastRotation) {
                rotYabs = axisY;
                rotYsig = 1;
            }
            if (axisY < -fastRotation) {
                rotYabs = -axisY;
                rotYsig = -1;
            }
            if (axisZ > fastRotation) {
                rotZabs = axisZ;
                rotZsig = 1;
            }
            if (axisZ < -fastRotation) {
                rotZabs = -axisZ;
                rotZsig = -1;
            }

            //Log.d(TAG, "X="+axisX);
            int cmd = mIsRotXstarted + mIsRotYstarted + mIsRotZstarted;

            //rotation is too slow => either none, or stopped
            if (rotXsig == 0 && rotYsig == 0 && rotZsig == 0) {
                if (cmd > 0) { //something already started rotating, so this is end of that move
                    if((event.timestamp-giroTimeStamp)* NS2S > timeDelta) //don't register too fast events
                        sendEvent(cmd);
                    mIsRotXstarted = mIsRotYstarted = mIsRotZstarted = 0; //reset command
                    giroTimeStamp = event.timestamp;
                }
                return;
            }
            mCount++;
            // else fast enough rotation detected
            if (cmd == 0) //Nothing rotated yet, check which axis is rotating fastest
                Log.i(TAG, "["+rotXsig+"|"+rotYsig+"|"+rotZsig+"]");
                if (rotXabs > rotYabs && rotXabs > rotZabs) {
                    if (mIsRotXstarted == 0) mIsRotXstarted = rotXsig > 0 ? 3 : 1;  //   1
                } else if (rotYabs > rotXabs && rotYabs > rotZabs) {                //4     2
                    if (mIsRotYstarted == 0) mIsRotYstarted = rotYsig > 0 ? 4 : 2;  //   3
                } else if (rotZabs > rotXabs && rotZabs > rotYabs) {                // CCW  6
                    if (mIsRotZstarted == 0) mIsRotZstarted = rotZsig > 0 ? 6 : 5;  // CW   5
                }
            //else it's the same event



            /* ORIGINAL ANGLE INTEGRATOR
            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON) {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the timestep
            // We will convert this axis-angle representation of the delta rotation
            // into a quaternion before turning it into the rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;
            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);
            deltaRotationVector[0] = sinThetaOverTwo * axisX;
            deltaRotationVector[1] = sinThetaOverTwo * axisY;
            deltaRotationVector[2] = sinThetaOverTwo * axisZ;
            deltaRotationVector[3] = cosThetaOverTwo;
            */
        //}
        //giroTimeStamp = event.timestamp;
        /*
        float[] deltaRotationMatrix = new float[9];
        SensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
        */
        // User code should concatenate the delta rotation we computed with the current rotation
        // in order to get the updated rotation.
        // rotationCurrent = rotationCurrent * deltaRotationMatrix;
    }

    /**
     * Send broadcast to the app
     * @param cmd
     */
    private void sendEvent(int cmd) {
        //Log.d("sender", "Broadcasting result message: " + data);
        Intent intent = new Intent(Settings.BROADCASTFILTER);
        intent.putExtra(Settings.EXID_GESTURE, cmd);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private float[] matrixMultiplication(float[] a, float[] b) {
        float[] result = new float[9];

        result[0] = a[0] * b[0] + a[1] * b[3] + a[2] * b[6];
        result[1] = a[0] * b[1] + a[1] * b[4] + a[2] * b[7];
        result[2] = a[0] * b[2] + a[1] * b[5] + a[2] * b[8];

        result[3] = a[3] * b[0] + a[4] * b[3] + a[5] * b[6];
        result[4] = a[3] * b[1] + a[4] * b[4] + a[5] * b[7];
        result[5] = a[3] * b[2] + a[4] * b[5] + a[5] * b[8];

        result[6] = a[6] * b[0] + a[7] * b[3] + a[8] * b[6];
        result[7] = a[6] * b[1] + a[7] * b[4] + a[8] * b[7];
        result[8] = a[6] * b[2] + a[7] * b[5] + a[8] * b[8];

        return result;
    }

    /*
    private void calculateInitialOrientation()
    {
        hasInitialOrientation = SensorManager.getRotationMatrix( initialRotationMatrix, null, acceleration, magnetic);

    }

    public void onGyroscopeSensorChanged(float[] gyroscope, long timestamp)
    {
        // don't start until first accelerometer/magnetometer orientation has
        // been acquired
        if (!hasInitialOrientation)
        {
            return;
        }

        // Initialization of the gyroscope based rotation matrix
        if (!stateInitializedCalibrated)
        {
            currentRotationMatrixCalibrated = matrixMultiplication(
                    currentRotationMatrixCalibrated, initialRotationMatrix);

            stateInitializedCalibrated = true;
        }

        // This timestep's delta rotation to be multiplied by the current
        // rotation after computing it from the gyro sample data.
        if (timestampOldCalibrated != 0 && stateInitializedCalibrated)
        {
            final float dT = (timestamp - timestampOldCalibrated) * NS2S;

            // Axis of the rotation sample, not normalized yet.
            float axisX = gyroscope[0];
            float axisY = gyroscope[1];
            float axisZ = gyroscope[2];

            // Calculate the angular speed of the sample
            float omegaMagnitude = (float) Math.sqrt(axisX * axisX + axisY
                    * axisY + axisZ * axisZ);

            // Normalize the rotation vector if it's big enough to get the axis
            if (omegaMagnitude > EPSILON)
            {
                axisX /= omegaMagnitude;
                axisY /= omegaMagnitude;
                axisZ /= omegaMagnitude;
            }

            // Integrate around this axis with the angular speed by the timestep
            // in order to get a delta rotation from this sample over the
            // timestep. We will convert this axis-angle representation of the
            // delta rotation into a quaternion before turning it into the
            // rotation matrix.
            float thetaOverTwo = omegaMagnitude * dT / 2.0f;

            float sinThetaOverTwo = (float) Math.sin(thetaOverTwo);
            float cosThetaOverTwo = (float) Math.cos(thetaOverTwo);

            deltaRotationVectorCalibrated[0] = sinThetaOverTwo * axisX;
            deltaRotationVectorCalibrated[1] = sinThetaOverTwo * axisY;
            deltaRotationVectorCalibrated[2] = sinThetaOverTwo * axisZ;
            deltaRotationVectorCalibrated[3] = cosThetaOverTwo;

            SensorManager.getRotationMatrixFromVector(
                    deltaRotationMatrixCalibrated,
                    deltaRotationVectorCalibrated);

            currentRotationMatrixCalibrated = matrixMultiplication(
                    currentRotationMatrixCalibrated,
                    deltaRotationMatrixCalibrated);

            SensorManager.getOrientation(currentRotationMatrixCalibrated,
                    gyroscopeOrientationCalibrated);
        }

        timestampOldCalibrated = timestamp;
    }
    */
    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub
        sensorManager.unregisterListener(listen);
        Toast.makeText(this, "Destroy", Toast.LENGTH_SHORT).show();
        super.onDestroy();
    }

    public class SensorListen implements SensorEventListener {

        @Override
        public void onSensorChanged(SensorEvent event) {
            switch (event.sensor.getType()) {
                case Sensor.TYPE_ACCELEROMETER:
                    getAccelerometer(event);
                    break;
                case Sensor.TYPE_GYROSCOPE:
                    getGyroscope(event);
                    break;
            }
        }

        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {
            // TODO Auto-generated method stub

        }

    }

}
