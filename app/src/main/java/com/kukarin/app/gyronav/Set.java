package com.kukarin.app.gyronav;

import android.os.Environment;
import android.provider.Settings;
import android.util.Log;

import java.io.File;

/**
 * Created by alexk on 2/9/2016.
 */
public class Set {

    private static final String TAG = "Settings";
    public static final String BROADCASTFILTER = "com.kukarin.gpxedit.sensor.communication.GESTURE_EVENT";;

    public static int LICOFFSET = 5432;
    public static int LICENSEGOOD = 645;
    public static final int LICENSEBAD = 32731;
    private static String tempFilePath;
    private static MapActivity mApp = null;
    private static String mAppDir;
    public static int mIsLicensed = LICENSEBAD;
    private static String BASE64_PUBLIC_KEY;

    //RETURN RESULTS
    public static final int RESRET_CANCEL = 88;
    public static final int RESRET_WPNAME = 88;
    public static final int RESRET_FILE_SELECTOR = 2;
    public static final int RESRET_EXTRAPOL = 3;

    //EXTRAS
    public static final String EXID_STARTTIME   = "starttime";
    public static final String EXID_ENDTIME     = "endtime";
    public static final String EXID_TARGETTIME  = "targettime";
    public static final String EXID_TARGETSTART = "targetstart";
    public static final String EXID_TARGETFLAGS = "targetflags";
    public static final String EXID_GESTURE     = "alxgesturenum";

    //Strings work
    public static final String DATETIMEFORMAT = "yyyy/MM/dd HH:mm:ss";
    private static int mGyRotSpeed = 65 ;
    private static int mGyDelayTime = 6;
    private static int mAccMin = 101;
    private static int mAccTime = 30;
    private static int mGyStep = 1;
    private static int mAccTop = 4; //(mine right side up)
    private static int mOriginalTimeout = 0;

    public static void init(MapActivity app) {
        mApp = app;
        setAppDir();
        setTempFilePath();
        BASE64_PUBLIC_KEY = "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAhH/P63pYr9pISZ4WpSOzVPcVl4fJmtkVdkBcvZ8ucRJwydyliEHxxv97y3qf3R55+A6lW8SeVIdERyKKuSWUz0JrKWZYkSQBzHaZCN4Cqnjy8iLaRTgWBvMACn9gNgSybfYqLdYNVdsU+EKBNA5/dfC98+QS1PUaE5S8KwZ1wSWAAg32NqSYnDHIt1zZLClMmDGCh/sLYETgjmIztoUj5crZIAs+VsLNecSuk1CJzjFPVR5Kv2xcpRx3PVFfYv4ixD6FjwSVT5hjERg1tfxlsob9rK3fsW6VJTqUSb62Ch2qgXCefd1wCDN+BvfkURfUpE3U3kY8KrPXDj+Pk7yiNQIDAQAB";
        LICENSEGOOD = LICENSEBAD + LICOFFSET;

        mOriginalTimeout = Settings.System.getInt(
                Busy.getInstance().getContext().getContentResolver(),
                Settings.System.SCREEN_OFF_TIMEOUT, 120000);
        Log.d(TAG, "T="+mOriginalTimeout);

        ////String i = Installation.id(app.getApplicationContext()); //initialize for the first time
    }

    public static int getMaxPointsToSelect() {
        return 10;
    }

    public static double getMaxDistanceToSelect() {
        return 0.01; //square of 10%
    }

    public static double getNewPointShift() {
        return 0.0005; //50m
    }

    public static String getTempFilePath() {
        return tempFilePath;
    }

    private static void setAppDir() {
        File fDir;
        mAppDir = Environment.getExternalStorageDirectory() +"/"+ ((String) mApp.getResources().getText(R.string.appdir));
        try {
            fDir = new File(mAppDir);
            fDir.mkdirs();
        }
        catch(Exception e) {
            My.msg(TAG, e.getMessage());
        }
    }

    private static void setTempFilePath() {
        tempFilePath = mAppDir + "/" + mApp.getResources().getText(R.string.tempfname);
    }

    public static String getAppFolder() {
        return mAppDir;
    }

    /**
     * Check: if(Settings.mIsLicensed-Settings.LICOFFSET == Settings.LICENSEBAD)
     * @param b
     */
    public static void isLicensed(boolean b) {
        mIsLicensed = b?LICENSEGOOD:LICENSEBAD;
    }

    public static String getBase64PublicKey() {
        return BASE64_PUBLIC_KEY;
    }

    public static int getmGyDelayTime() {
        return mGyDelayTime;
    }
    public static void setmGyDelayTime(int mGyDelayTime) {
        Set.mGyDelayTime = mGyDelayTime;
    }

    public static int getmAccMin() {
        return mAccMin;
    }
    public static void setmAccMin(int mAccMin) {
        Set.mAccMin = mAccMin;
    }

    public static int getmAccTime() {
        return mAccTime;
    }
    public static void setmAccTime(int mAccTime) {
        Set.mAccTime = mAccTime;
    }

    public static int getmGyRotSpeed() {
        return mGyRotSpeed;
    }
    public static void setmGyRotSpeed(int mGyRotSpeed) {
        Set.mGyRotSpeed = mGyRotSpeed;
    }

    public static int getmGyStep() {
        return mGyStep;
    }
    public static void setmGyStep(int mGyStep) {
        Set.mGyStep = mGyStep;
    }

    public static int getmAccTop() {
        return mAccTop;
    }
    public static void setmAccTop(int v) {
        Set.mAccTop = v;
    }

    public static void setOriginalTimeout(int originalTimeout) {
        Set.mOriginalTimeout = originalTimeout;
    }

    public static int getOriginalTimeout() {
        return mOriginalTimeout;
    }
}
