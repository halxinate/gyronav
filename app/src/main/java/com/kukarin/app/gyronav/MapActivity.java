package com.kukarin.app.gyronav;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.AsyncTask;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.widget.PopupMenu;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kukarin.app.gyronav.sensor.SensorsService;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

public class MapActivity extends FragmentActivity implements
        OnMapReadyCallback,
        LocationListener,
        GoogleApiClient.OnConnectionFailedListener,
        GoogleApiClient.ConnectionCallbacks {
    private static final String TAG = "MapActivity";

    private android.support.v7.widget.PopupMenu popupMenu;
    private View menubutton;
    private GoogleMap mMap;
    private float mStepX = 1, mStepY = 1;
    private int mMenuSelection = 0;
    private int[] mMenuChecks = {R.id.m_rspeed, R.id.m_btime, R.id.m_accmax, R.id.m_acctime, R.id.m_acctop};
    private int mCurValue = -1;
    private int mCurSetting = -1; //inactive initially
    private int mGyroMode = 0; //loopmenu selection
    private int mGyroModeCounter = 0;
    private String[] gyroModes = {"Scroll", "Zoom", "Map Mode", "Waypoint"};

    private int mMapTypeSwitch = 0;
    private int[] mapTypes = {GoogleMap.MAP_TYPE_NORMAL, GoogleMap.MAP_TYPE_SATELLITE, GoogleMap.MAP_TYPE_TERRAIN, GoogleMap.MAP_TYPE_HYBRID};

    private final int MODESCROLL = 0;
    private final int MODEZOOM = 1;
    private final int MODEMTYPE = 2;
    private final int MODEWAYPO = 3;

    private HideLooperLater loopHider = null;

    //Screen OFF members
    private ScreenOffWaiter scrOffwaiter = null;
    private boolean mGyroDelayedOff = false;
    ///private PowerManager mPowerManager;
    ///private PowerManager.WakeLock mWakeLock;
    ///private int field = 0x00000020;

    //GPS
    private GPS mGPS;
    private static final int MY_PERMISSIONS_REQUEST_FINE_LOCATION = 89;
    private Location mCurrentLocation;
    private String mLastUpdateTime;
    private Track mTrack;
    private Polyline mTrackLine;
    private ArrayList<Marker> mMapMarkers; //marker objects on the map

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //GPS - start as early as possible to acquire location faster
        if (!isGooglePlayServicesAvailable()) {
            finish();
        }

        setContentView(R.layout.activity_map);
        Busy.getInstance().setContext(this);
        Set.init(this);

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        //Continue initializations
        mGPS = new GPS(this);
        mTrack = new Track();
        mMapMarkers = new ArrayList<>();

        /*/Screen on/off control
        try {
            // Yeah, this is hidden field.
            field = PowerManager.class.getField("PROXIMITY_SCREEN_OFF_WAKE_LOCK").getInt(null);
        } catch (Throwable ignored) {
        }
        mPowerManager = (PowerManager) getSystemService(Context.POWER_SERVICE);
        mWakeLock = mPowerManager.newWakeLock(field, getLocalClassName());
        */

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
                My.vib(1);
                if (mCurSetting == 4) //Side, looping
                    if (mCurValue == 1) mCurValue = 5;
                mCurValue -= Set.getmGyStep();
                setSettings();
                setControlValue();
            }
        });
        findViewById(R.id.bnPlus).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                My.vib(1);
                if (mCurSetting == 4) //Side, looping
                    if (mCurValue == 4) mCurValue = 0;

                mCurValue += Set.getmGyStep();
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
                Set.setmGyRotSpeed(mCurValue);
                updateMenuItemText();
                break;
            case 1:
                Set.setmGyDelayTime(mCurValue);
                updateMenuItemText();
                break;
            case 2:
                Set.setmAccMin(mCurValue);
                updateMenuItemText();
                break;
            case 3:
                Set.setmAccTime(mCurValue);
                updateMenuItemText();
                break;
            case 4:
                Set.setmAccTop(mCurValue);
                updateMenuItemText();
                break;
        }
    }

    /**
     * Read cur value to work with +/- from Settings object
     * according to currently selected in the menu value to tweak
     */
    private void getSettings() {
        switch (mCurSetting) {
            case 0:
                mCurValue = Set.getmGyRotSpeed();
                break;
            case 1:
                mCurValue = Set.getmGyDelayTime();
                break;
            case 2:
                mCurValue = Set.getmAccMin();
                break;
            case 3:
                mCurValue = Set.getmAccTime();
                break;
        }
    }

    /**
     * Replace TV text adding a value
     */
    private void updateMenuItemText() {
        if (popupMenu != null) {
            MenuItem i = popupMenu.getMenu().getItem(mCurSetting);
            String t = (String) i.getTitle();
            if (mCurSetting == 4) //side text
                t = t.substring(0, t.indexOf("[")) + "[" + getSideName(mCurValue) + "]";
            else
                t = t.substring(0, t.indexOf("[")) + "[" + mCurValue / 10f + "]";
            i.setTitle(t);
        }
    }

    private String getSideName(int v) {
        final String[] sides = {"top", "right", "bottom", "left"};
        if (v < 1) v = 1;
        else if (v > 4) v = 4;
        return sides[v - 1];
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

    private void registerBroadcastReceiver(boolean register) {
        if (register) {
            LocalBroadcastManager.getInstance(this).registerReceiver(mMessageReceiver,
                    new IntentFilter(Set.BROADCASTFILTER));
        } else {
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
        switch (data.getInt(Set.EXID_GESTURE, 0)) {
            //ROTATE ----------------------------------------
            case 1: //Up (top down)
                showGyroMenu(false);
                switch (mGyroMode) {
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
                        addLocationMarker();
                        break;
                }
                break;
            case 2: //Rt
                showGyroMenu(false);
                switch (mGyroMode) {
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
            case 3: //Dn (top up)
                showGyroMenu(false);
                switch (mGyroMode) {
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
                        addLocationMarker(); //no name asked
                        askForWPname();
                        break;
                }
                break;
            case 4: //Lt
                showGyroMenu(false);
                switch (mGyroMode) {
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
                screenOff(false);
                break;
            case 6: //rotate CCW
                gyroModeSwitch(1);
                screenOff(false);
                break;
            case 10: // proximity ----------------------------
                break;
            // ACCELL ----------------------------------------
            case 11: //Y Top side up
                screenOff(Set.getmAccTop() == 1);
                break;
            case 12: //X Rt side up
                screenOff(Set.getmAccTop() == 2);
                break;
            case 13: //Y bottom side up
                screenOff(Set.getmAccTop() == 3);
                break;
            case 14: //X Left Side up
                screenOff(Set.getmAccTop() == 4);
                break;
            case 15: //Z Face down
                screenOff(false);
                break;
            case 16: //Z Face up
                screenOff(false);
                break;
            case 100: //Wake UP!
                screenOff(false);
                Log.d(TAG, "got 100");
                break;
            default:
                return; //if 0 or other don't process
        }
    }

    /**
     * Trigger or cancell Asynctask with delayed screen off command
     * @param turnoff
     */
    private void screenOff(boolean turnoff) {
        if(turnoff && mGyroModeCounter>0) {
            mGyroDelayedOff = true;
            return; //Dont sleep if gyro menu on
        }
        showCenterMessage(turnoff);
        if (turnoff) { //initiate screen off timer
            scrOffwaiter = new ScreenOffWaiter();
            scrOffwaiter.execute("");
        } //stop screen off timer
        else if (scrOffwaiter != null) scrOffwaiter.cancel(true);
    }

    private void showCenterMessage(boolean show) {
        findViewById(R.id.tv_centermessage).setVisibility(show ? View.VISIBLE : View.GONE);
    }

    private void showGyroMenu(boolean show) {
        My.vib(1);
        findViewById(R.id.loopmenu).setVisibility(show ? View.VISIBLE : View.GONE);
        showModeText(!show);
        if (!show) {
            mGyroModeCounter = 0;
            if(mGyroDelayedOff) screenOff(true);
            mGyroDelayedOff = false;
        }
    }

    private void showModeText(boolean b) {
        findViewById(R.id.tv_mode).setVisibility(b ? View.VISIBLE : View.GONE);
    }


    /**
     * Cycle map modes
     * @param i
     */
    private void switchMap(int i) {
        My.vib(1);
        mMapTypeSwitch = loopValue(mMapTypeSwitch, i, mapTypes.length);
        mMap.setMapType(mapTypes[mMapTypeSwitch]);
    }

    /**
     * Switchy gyro sensor mode menu control
     * @param i
     */
    private void gyroModeSwitch(int i) {
        if (mGyroModeCounter == 0) { //first rotate brings up the menu
            mGyroModeCounter = 1;
            showGyroMenu(true);
            loopHider = new HideLooperLater();
            loopHider.execute(""); //hide looper after a while
            Log.d(TAG, "1 mGm=" + mGyroMode);
            return;
        }

        My.vib(1);
        //second rotate - move the menu
        if (loopHider != null) loopHider.prolong();
        int sz = gyroModes.length;
        int i1 = loopValue(mGyroMode - 1, i, sz);
        int i2 = loopValue(mGyroMode, i, sz);
        int i3 = loopValue(mGyroMode + 1, i, sz);

        ((TextView) findViewById(R.id.tv_loop1)).setText(gyroModes[i1]);
        ((TextView) findViewById(R.id.tv_loop2)).setText(gyroModes[i2]);
        ((TextView) findViewById(R.id.tv_loop3)).setText(gyroModes[i3]);

        mGyroMode = i2;
        updateHelpButton();
        Log.d(TAG, "2 mGm=" + mGyroMode);
    }

    /**
     * Update rigt bottom available gestures indicator
     */
    private void updateHelpButton() {
        switch (mGyroMode) {
            case MODESCROLL:
                findViewById(R.id.bnHelp).setBackgroundResource(R.mipmap.ic_help1);
                break;
            case MODEZOOM:
            case MODEMTYPE:
            case MODEWAYPO:
                findViewById(R.id.bnHelp).setBackgroundResource(R.mipmap.ic_help2);
                break;
        }
        ((TextView) findViewById(R.id.tv_mode)).setText(gyroModes[mGyroMode]);

    }

    /**
     * Safely loop a provided index through the loopmenu allowed values in a loop
     * @param cur
     * @param i
     * @return
     */
    private int loopValue(int cur, int i, int sz) {
        cur += i;
        if (cur < 0) cur += sz;
        else if (cur >= sz) cur -= sz;
        return cur;
    }

    private void zoomMap(int i) {
        if (mMap == null) return;
        My.vib(1);
        if (i > 0) {
            mMap.animateCamera(CameraUpdateFactory.zoomIn(), 200, null);
        } else {
            mMap.animateCamera(CameraUpdateFactory.zoomOut(), 200, null);
        }
        //mMap.moveCamera(cu);
    }

    private void moveMap(int x, int y) {
        if (mMap == null) return;
        My.vib(1);
        CameraUpdate cu = CameraUpdateFactory.scrollBy(mStepX * x, mStepY * y);
        //mMap.moveCamera(cu);
        mMap.animateCamera(cu, 200, null);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mGPS.connect();
        registerBroadcastReceiver(true);
    }

    @Override
    protected void onStop() {
        mGPS.disconnect();
        registerBroadcastReceiver(false);
        super.onStop();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mGPS.startLocationUpdates();

        int code = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (code != ConnectionResult.SUCCESS) { //No google connection for map
            GooglePlayServicesUtil.getErrorDialog(code, this, 0).show();
        }
        restoreScreenParameters();
        showCenterMessage(false); //reset on autosleep
    }

    @Override
    protected void onPause() {
        super.onPause();
        //turn off proximity sensing
        ///if(mWakeLock.isHeld()) {
        ///    mWakeLock.release();
        ///}

        mGPS.stopLocationUpdates();

        //TODO put service to sleep as well! or not?
    }

    /**
     * After autosleep need to restore some parameters on gyro wake up
     */
    private void restoreScreenParameters() {
        Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, Set.getOriginalTimeout());

        //make proximity sensor turn off the screen
        ///if(!mWakeLock.isHeld()) {
        ///    mWakeLock.acquire();
        ///}

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_SHOW_WHEN_LOCKED
                | WindowManager.LayoutParams.FLAG_DISMISS_KEYGUARD
                ///| WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_TURN_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_ALLOW_LOCK_WHILE_SCREEN_ON);
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
        mStepX = metrics.widthPixels / 4;
        mStepY = metrics.heightPixels / 4;
        if (mStepX < mStepY) mStepY = mStepX;
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
            case Set.RESRET_FILE_SELECTOR: //New file is selected
                if (resultCode == RESULT_OK) {

                }
                break;
            case Set.RESRET_EXTRAPOL:
                if (data.hasExtra(Set.EXID_TARGETTIME)) {
                    String targetTime = data.getStringExtra(Set.EXID_TARGETTIME);
                    boolean recStart = data.getBooleanExtra(Set.EXID_TARGETSTART, false);
                    int flags = 0;
                }
                break;
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    /**
     * After the map is loaded by API, setup its properties to display
     * @param upMap
     */
    public void setupMap(GoogleMap upMap) {
        mMap = upMap;

        // Add a marker in SFBA and move the camera
        //LatLng devhq = new LatLng(38, -122);
        //mMap.addMarker(new MarkerOptions().position(devhq).title("Dev HQ"));
        //mMap.moveCamera(CameraUpdateFactory.newLatLng(devhq));

        mMap.setOnInfoWindowClickListener(new GoogleMap.OnInfoWindowClickListener() {
            @Override
            public void onInfoWindowClick(Marker marker) {
                My.msg(TAG, "Click");
            }
        });

        if(!mGPS.permitionIsGranted()) //Ask user to grant it and catch the answer
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                    MY_PERMISSIONS_REQUEST_FINE_LOCATION);
        else
            mMap.setMyLocationEnabled(true);
    }

    // GPS =================================================================================
    @Override
    public void onConnected(Bundle bundle) {
        Log.d(TAG, "onConnected - isConnected ...............: " + mGPS.isConnected());
        mGPS.startLocationUpdates();
    }

    @Override
    public void onConnectionSuspended(int i) {

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        Log.d(TAG, "Connection failed: " + connectionResult.toString());
    }

    @Override
    public void onLocationChanged(Location location) {
        Log.d(TAG, "Firing onLocationChanged..............................................");
        mCurrentLocation = location;
        Date t = new Date();
        mLastUpdateTime = DateFormat.getTimeInstance().format(t);
        mTrack.add(mCurrentLocation, t.getTime());
        drawTrack();
    }

    private void drawTrack() {
        // TODO: 3/15/2016 add processing when it's hidden
        PolylineOptions opt = new PolylineOptions();
        opt.addAll(mTrack.getCoords());
        if(mTrackLine!=null) mTrackLine.remove();
        mTrackLine = mMap.addPolyline(opt);
    }

    private void addLocationMarker() {
        if(mCurrentLocation==null) {
            My.msg(TAG, "No Location Fix yet. Is the GPS ON?");
            return;
        }

        MarkerOptions options = new MarkerOptions();

        // following four lines requires 'Google Maps Android API Utility Library'
        // https://developers.google.com/maps/documentation/android/utility/
        // I have used this to display the time as title for location markers
        // you can safely comment the following four lines but for this info
        /*
        IconGenerator iconFactory = new IconGenerator(this);
        iconFactory.setStyle(IconGenerator.STYLE_PURPLE);
        options.icon(BitmapDescriptorFactory.fromBitmap(iconFactory.makeIcon(mLastUpdateTime)));
        options.anchor(iconFactory.getAnchorU(), iconFactory.getAnchorV());
        */

        LatLng currentLatLng = new LatLng(mCurrentLocation.getLatitude(), mCurrentLocation.getLongitude());
        options.position(currentLatLng);
        Marker mapMarker = mMap.addMarker(options);
        mMapMarkers.add(mapMarker);

        mTrack.addManualWaypoint(mCurrentLocation);
        mapMarker.setTitle(mTrack.getLastManualWPinfo());

        mMap.moveCamera(CameraUpdateFactory.newLatLng(currentLatLng));
    }

    /**
     * GPS check
     * @return
     */
    private boolean isGooglePlayServicesAvailable() {
        int status = GooglePlayServicesUtil.isGooglePlayServicesAvailable(this);
        if (ConnectionResult.SUCCESS == status) {
            return true;
        } else {
            GooglePlayServicesUtil.getErrorDialog(status, this, 0).show();
            return false;
        }
    }

    /**
     * Ask user for the waypoint name
     */
    private void askForWPname() {
        // TODO: 3/15/2016 start new activity or frag woth gyro kbd and save the name

        //mock
        My.msg(TAG, "Enter the Waypoint name placeholder");
    }

    /**
     * Handle the permission request resulting from user input
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode,
                                           String permissions[], int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_FINE_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    try {
                        mMap.setMyLocationEnabled(true);
                    }
                    catch(Exception e) {} //permission exception, no map yet
                } else {
                    My.msg(TAG, "Location tracking disabled");
                    // permission denied! Disable the functionality that depends on this permission.
                }
                return;
            }

            // other 'case' lines to check for other
            // permissions this app might request
        }
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
                            if (mCurSetting < 0) showGauge();
                            mCurSetting = 0;
                            mCurValue = Set.getmGyRotSpeed();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_btime:
                        if (menuToggle(item)) {
                            if (mCurSetting < 0) showGauge();
                            mCurSetting = 1;
                            mCurValue = Set.getmGyDelayTime();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_accmax:
                        if (menuToggle(item)) {
                            if (mCurSetting < 0) showGauge();
                            mCurSetting = 2;
                            mCurValue = Set.getmAccMin();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_acctime:
                        if (menuToggle(item)) {
                            if (mCurSetting < 0) showGauge();
                            mCurSetting = 3;
                            mCurValue = Set.getmAccTime();
                            setControlValue();
                            setControlText();
                        }
                        break;
                    case R.id.m_acctop:
                        if (menuToggle(item)) {
                            if (mCurSetting < 0) showGauge();
                            mCurSetting = 4;
                            mCurValue = Set.getmAccTop();
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
        for (int i = 0; i < mMenuChecks.length; i++) { //set menu items text
            mCurSetting = i;
            getSettings(); //mCurVal is now having val from Settings
            updateMenuItemText();
        }
        mCurSetting = temp;
        if (mCurSetting > -1) {
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
        t = t.substring(0, t.indexOf("[") - 1);
        ((TextView) findViewById(R.id.tv_explan)).setText(t);
    }

    /**
     * Set the text of the value on the screen
     */
    private void setControlValue() {
        if (mCurSetting == 4) //side name
            ((TextView) findViewById(R.id.tv_gauge1)).setText(getSideName(mCurValue));
        else
            ((TextView) findViewById(R.id.tv_gauge1)).setText(String.format("%.1f", mCurValue / 10f));
        Log.d(TAG, "Cv=" + mCurValue);
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
        @Override protected void onPreExecute() {  }
        @Override protected void onProgressUpdate(Void... values) { }
        @Override
        protected String doInBackground(String... params) {
            for(loops=maxloops; loops>0; loops--) //~5 sec
                try { Thread.sleep(1000); }
                catch (InterruptedException e) { e.printStackTrace(); }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            showGyroMenu(false);
        }
        public void prolong() {
            loops = maxloops;
        }
    }

    private class ScreenOffWaiter extends AsyncTask<String, Void, String> {
        private final int maxloops = Set.getmAccTime(); //in 0.1 secs
        @Override protected void onPreExecute() {  }
        @Override protected void onProgressUpdate(Void... values) { }
        @Override protected void onCancelled() { }
        @Override
        protected String doInBackground(String... params) {
            for(int loops=maxloops; loops>0; loops--) //~5 sec
                try {
                    Thread.sleep(100);
                    if(isCancelled()) finalize();
                }
                catch (Throwable e) { e.printStackTrace(); }
            return null;
        }
        @Override
        protected void onPostExecute(String result) {
            My.vib(2);
            int t = Settings.System.getInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 120000);
            Set.setOriginalTimeout(t);
            Settings.System.putInt(getContentResolver(), Settings.System.SCREEN_OFF_TIMEOUT, 10);
        }
    }

}
