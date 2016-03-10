package com.kukarin.app.gyronav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.kukarin.app.gyronav.sensor.SensorsService;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private android.support.v7.widget.PopupMenu popupMenu;
    private View menubutton;
    private GoogleMap mMap;
    private float mStepX=1, mStepY=1;
    private int mMenuSelection = 0;
    private int[] mMenuChecks = {R.id.m_rspeed, R.id.m_btime, R.id.m_shspeed, R.id.m_proxtime };
    private int mCurValue = -1;
    private int mCurSetting = -1; //inactive initially
    private int mGyroMode = 0; //loopmenu selection
    private int mGyroModeCounter = 0;
    private String[] gyroModes = {"Scroll", "Zoom", "Map Mode","Waypoint"};

    private int mMapTypeSwitch = 0;
    private int[] mapTypes = {GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_TERRAIN, GoogleMap.MAP_TYPE_HYBRID};

    private final int MODESCROLL=0;
    private final int MODEZOOM  =1;
    private final int MODEMTYPE =2;
    private final int MODEWAYPO =3;

    private HideLooperLater loopHider = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_map);
        Busy.getInstance().setContext(this);
        Settings.init(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Menu
        menubutton = findViewById(R.id.bnMenu);
        menubutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });
        //mCurSetting = -1; //hidden

        //+/-Buttons block
        findViewById(R.id.bnMinus).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurValue-=Settings.getmGyStep();
                setSettings();
                setControlValue();
            }
        });
        findViewById(R.id.bnPlus).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                mCurValue += Settings.getmGyStep();
                setSettings();
                setControlValue();
            }
        });

        //Other
        updateHelpButton();
        ///registerBroadcastReceiver(true);
    }

    /**
     * Use the Index of the currently selected setting to tweak by +/-
     * to update the Settings object for that value from current one
     * and the corresponding menu item title at its value part
     */
    private void setSettings() {
        switch (mCurSetting) {
            case 0:
                Settings.setmGyRotSpeed(mCurValue);
                updateMenuItemText();
                break;
            case 1:
                Settings.setmGyDelayTime(mCurValue);
                updateMenuItemText();
                break;
            case 2:
                Settings.setmGyShakeSpeed(mCurValue);
                updateMenuItemText();
                break;
            case 3:
                Settings.setmGyProxyTime(mCurValue);
                updateMenuItemText();
                break;
        }
    }

    /**
     * Read cur value to work with +/- from Settings object
     * according to currently selected in the menu value to tweak
     */
    private void getSettings() {
        switch(mCurSetting){
            case 0:
                mCurValue = Settings.getmGyRotSpeed();
                break;
            case 1:
                mCurValue = Settings.getmGyDelayTime();
                break;
            case 2:
                mCurValue = Settings.getmGyShakeSpeed();
                break;
            case 3:
                mCurValue = Settings.getmGyProxyTime();
                break;
        }
    }

    /**
     * Replace TV text adding a value
     */
    private void updateMenuItemText() {
        if(popupMenu!=null) {
            MenuItem i = popupMenu.getMenu().getItem(mCurSetting);
            String t = (String) i.getTitle();
            t = t.substring(0, t.indexOf("[")) + "[" + mCurValue / 10f + "]";
            i.setTitle(t);
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        setupMap(googleMap);

        //Gestures service start
        Intent intent = new Intent(this, SensorsService.class);
        startService(intent);

        //Get screen size
        getDisplaySize();
        My.msg(TAG, "map ready");
    }

    private void registerBroadcastReceiver(boolean register){
        if(register) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                    new IntentFilter(Settings.BROADCASTFILTER));
        }
        else {
            LocalBroadcastManager.getInstance(this).unregisterReceiver(mMessageReceiver);
        }
    }

    private BroadcastReceiver mMessageReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            handleMessage(intent);
        }
    };

    private void handleMessage(Intent msg) {
        Bundle data = msg.getExtras();
        switch (data.getInt(Settings.EXID_GESTURE, 0)) {
            //GYRO ----------------------------------------
            case 1: //Up
                showLooper(false);
                switch(mGyroMode){
                    case MODESCROLL: //Scroll
                        moveMap(0, 1);
                        break;
                    case MODEZOOM: //Zoom
                        zoomMap(-1);
                        break;
                    case MODEMTYPE: //MapMode
                        switchMap(1);
                        break;
                    case MODEWAYPO: //waypoins
                        break;
                }
                break;
            case 2: //Rt //
                showLooper(false);
                switch(mGyroMode){
                    case MODESCROLL: //Scroll
                        moveMap(1, 0);
                        break;
                    case MODEZOOM: //Zoom
                        break;
                    case MODEMTYPE: //MapMode
                        break;
                    case MODEWAYPO: //waypoins
                        break;
                }
                break;
            case 3: //Dn
                showLooper(false);
                switch(mGyroMode){
                    case MODESCROLL: //Scroll
                        moveMap(0, -1);
                        break;
                    case MODEZOOM: //Zoom
                        zoomMap(1);
                        break;
                    case MODEMTYPE: //MapMode
                        switchMap(-1);
                        break;
                    case MODEWAYPO: //waypoins
                        break;
                }
                break;
            case 4: //Lt
                showLooper(false);
                switch(mGyroMode){
                    case MODESCROLL: //Scroll
                        moveMap(-1, 0);
                        break;
                    case MODEZOOM: //Zoom
                        break;
                    case MODEMTYPE: //MapMode
                        break;
                    case MODEWAYPO: //waypoins
                        break;
                }

                break;
            case 5: //rotate CW
                gyroModeSwitch(-1);
                break;
            case 6: //rotate CCW
                gyroModeSwitch(1);
                break;
            case 10: // proximity ----------------------------
                break;
            // ACCELL ----------------------------------------
            case 11: //Y Up
                moveMap(0, 1);
                break;
            case 12: //X Rt
                moveMap(1, 0);
                break;
            case 13: //Y Dn
                moveMap(0, -1);
                break;
            case 14: //X Left
                moveMap(-1, 0);
                break;
            case 15: //Z Out
                zoomMap(-1);
                break;
            case 16: //Z In
                zoomMap(1);
                break;
            default:
                return; //if 0 or other don't process
        }

    }

    private void showLooper(boolean show){
        findViewById(R.id.loopmenu).setVisibility(show ? View.VISIBLE : View.GONE);
        showMode(!show);
        if(!show) mGyroModeCounter = 0;
    }

    private void showMode(boolean b) {
        findViewById(R.id.tv_mode).setVisibility(b?View.VISIBLE:View.GONE);
    }


    /**
     * Cycle map modes
     * @param i
     */
    private void switchMap(int i) {
        mMapTypeSwitch = loopValue(mMapTypeSwitch, i, mapTypes.length);
        mMap.setMapType(mapTypes[mMapTypeSwitch]);
    }

    /**
     * Switchy gyro sensor mode menu control
     * @param i
     */
    private void gyroModeSwitch(int i) {
        if(mGyroModeCounter==0){ //first rotate brings up the menu
            mGyroModeCounter = 1;
            showLooper(true);
            loopHider = new HideLooperLater();
            loopHider.execute(""); //hide looper after a while
            Log.d(TAG, "1 mGm="+mGyroMode);
            return;
        }

        //second rotate - move the menu
        if(loopHider!=null) loopHider.prolong();
        int sz = gyroModes.length;
        int i1 = loopValue(mGyroMode-1, i, sz);
        int i2 = loopValue(mGyroMode,   i, sz);
        int i3 = loopValue(mGyroMode+1, i, sz);

        ((TextView)findViewById(R.id.tv_loop1)).setText(gyroModes[i1]);
        ((TextView)findViewById(R.id.tv_loop2)).setText(gyroModes[i2]);
        ((TextView)findViewById(R.id.tv_loop3)).setText(gyroModes[i3]);

        mGyroMode = i2;
        updateHelpButton();
        Log.d(TAG, "2 mGm=" + mGyroMode);
    }

    /**
     * Update rigt bottom available gestures indicator
     */
    private void updateHelpButton() {
        switch(mGyroMode){
            case MODESCROLL:
                findViewById(R.id.bnHelp).setBackgroundResource(R.mipmap.ic_help1);
                break;
            case MODEZOOM:
            case MODEMTYPE:
            case MODEWAYPO:
                findViewById(R.id.bnHelp).setBackgroundResource(R.mipmap.ic_help2);
                break;
        }
        ((TextView)findViewById(R.id.tv_mode)).setText(gyroModes[mGyroMode]);

    }

    /**
     * Safely loop a provided index through the loopmenu allowed values in a loop
     * @param cur
     * @param i
     * @return
     */
    private int loopValue(int cur, int i, int sz) {
        cur+=i;
        if(cur<0) cur += sz;
        else if(cur>=sz) cur -= sz;
        return cur;
    }

    private void zoomMap(int i) {
        if(mMap==null) return;
        if(i>0){
            mMap.animateCamera(CameraUpdateFactory.zoomIn(), 200, null);
        }
        else {
            mMap.animateCamera(CameraUpdateFactory.zoomOut(), 200, null);
        }
        //mMap.moveCamera(cu);
    }

    private void moveMap(int x, int y) {
        if(mMap==null) return;
        CameraUpdate cu = CameraUpdateFactory.scrollBy(mStepX * x, mStepY * y);
        //mMap.moveCamera(cu);
        mMap.animateCamera(cu, 200, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        registerBroadcastReceiver(true);
    }
    @Override
    protected void onStop() {
        registerBroadcastReceiver(false);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (code != ConnectionResult.SUCCESS) { //No google connection for map
            GooglePlayServicesUtil.getErrorDialog(code, this, 0).show();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
    }

    private void getDisplaySize() {
        /*Display display = getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        int width = size.x;
        int height = size.y;
        mStepX = width/9;
        mStepY = height/9;
        */
        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        mStepX = metrics.widthPixels/4;
        mStepY = metrics.heightPixels/4;
        if(mStepX<mStepY) mStepY = mStepX;
        else mStepX = mStepY;
    }

    /**
     * Back from external intent call
     *
     * @param requestCode stock
     * @param resultCode  stock
     * @param data        stock
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        switch (requestCode) {
            case Settings.RESRET_FILE_SELECTOR: //New file is selected
                if (resultCode == RESULT_OK) {

                }
                break;
            case Settings.RESRET_EXTRAPOL:
                if (data.hasExtra(Settings.EXID_TARGETTIME)) {
                    String targetTime = data.getStringExtra(Settings.EXID_TARGETTIME);
                    boolean recStart = data.getBooleanExtra(Settings.EXID_TARGETSTART, false);
                    int flags = 0;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    private void showButtons(boolean b) {
        findViewById(R.id.bnBlock).setVisibility(b ? View.VISIBLE : View.GONE);
    }

    public void setupMap(GoogleMap upMap) {
        mMap = upMap;

        // Add a marker in SFBA and move the camera
        LatLng devhq = new LatLng(38, -122);
        mMap.addMarker(new MarkerOptions().position(devhq).title("Dev HQ"));
        mMap.moveCamera(CameraUpdateFactory.newLatLng(devhq));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                My.msg(TAG, "Click");
            }
        });
    }

    //MENU =========================================================================================

    /**
     * app menu show and process
     */
    private void showMenu() {
        popupMenu = new PopupMenu(MapActivity.this, menubutton);
        popupMenu.setOnDismissListener(new PopupMenu.OnDismissListener() {
            @Override
            public void onDismiss(PopupMenu menu) {

            }
        });
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.m_rspeed:
                        if (menuToggle(item)) {
                            if(mCurSetting<0) showGauge();
                            mCurSetting = 0;
                            mCurValue = Settings.getmGyRotSpeed();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_btime:
                        if (menuToggle(item)) {
                            if(mCurSetting<0) showGauge();
                            mCurSetting = 1;
                            mCurValue = Settings.getmGyDelayTime();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_shspeed:
                        if (menuToggle(item)) {
                            if(mCurSetting<0) showGauge();
                            mCurSetting = 2;
                            mCurValue = Settings.getmGyShakeSpeed();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_proxtime:
                        if (menuToggle(item)) {
                            if(mCurSetting<0) showGauge();
                            mCurSetting = 3;
                            mCurValue = Settings.getmGyProxyTime();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_hide:
                        hideGauge();
                        break;
                }
                return true;
            }
        });
        popupMenu.inflate(R.menu.popup_menu);

        int temp = mCurSetting;
        for(int i=0;i<4;i++) { //set menu items text
            mCurSetting = i;
            getSettings(); //mCurVal is now having val from Settings
            updateMenuItemText();
        }
        mCurSetting = temp;
        if(mCurSetting>-1) {
            menuToggle(popupMenu.getMenu().getItem(mCurSetting));
        }

        popupMenu.show();
    }

    /**
     * Initially the +/- contrll is hidden show it on menu selection
     */
    private void showGauge() {
        findViewById(R.id.bnBlock).setVisibility(View.VISIBLE);
    }
    private void hideGauge() {
        findViewById(R.id.bnBlock).setVisibility(View.GONE);
        mCurSetting = -1;
    }

    /**
     * Gauge type note from menu item Title
     */
    private void setControlText() {
        String t = popupMenu.getMenu().getItem(mCurSetting).getTitle().toString();
        t = t.substring(0, t.indexOf("[")-1);
        ((TextView)findViewById(R.id.tv_explan)).setText(t);
    }

    /**
     * Set the text of the value on the screen
     */
    private void setControlValue() {
        ((TextView)findViewById(R.id.tv_gauge1)).setText(String.format("%.1f", mCurValue/10f));
    }

    /**
     * Toggle the radio button
     */
    private boolean menuToggle(MenuItem item) {
        if (item.isChecked()) item.setChecked(false);
        else item.setChecked(true);
        return item.isChecked();
    }

    /*
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.vibrate:
            case R.id.dont_vibrate:
                if (item.isChecked()) item.setChecked(false);
                else item.setChecked(true);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void showExtrapolationFragment() {
        Intent intent = new Intent(this, ExtrapolationActivity.class);
        intent.putExtra(Settings.EXID_STARTTIME, mStartTime);
        intent.putExtra(Settings.EXID_ENDTIME, mEndTime);
        startActivityForResult(intent, Settings.RESRET_EXTRAPOL);
    }
    */

    private class HideLooperLater extends AsyncTask<String, Void, String> {
        private final int maxloops = 6;
        int loops;

        @Override
        protected String doInBackground(String... params) {
            for(loops=maxloops; loops>0; loops--) { //~5 sec
                try {
                    Thread.sleep(1000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            return null;
        }

        @Override
        protected void onPostExecute(String result) {
            showLooper(false);
        }

        @Override
        protected void onPreExecute() {
        }

        @Override
        protected void onProgressUpdate(Void... values) {
        }

        public void prolong() {
            loops = maxloops;
        }
    }

}
