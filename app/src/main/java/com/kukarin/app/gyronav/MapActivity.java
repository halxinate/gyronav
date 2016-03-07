package com.kukarin.app.gyronav;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.Uri;
import android.os.AsyncTask;
import android.support.v4.app.FragmentActivity;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.PopupMenu;
import android.text.InputType;
import android.util.DisplayMetrics;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GooglePlayServicesUtil;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.kukarin.app.gyronav.sensor.SensorsService;

public class MapActivity extends FragmentActivity implements OnMapReadyCallback {
    private static final String TAG = "MapActivity";

    private android.support.v7.widget.PopupMenu popupMenu;
    private View menubutton;
    private GoogleMap mMap;
    private float mStepX=1, mStepY=1;

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

        menubutton = findViewById(R.id.bnMenu);
        menubutton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showMenu();
            }
        });
        findViewById(R.id.bnMinus).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //moveCursor(-1);
            }
        });
        findViewById(R.id.bnPlus).setOnClickListener(new Button.OnClickListener() {
            @Override
            public void onClick(View v) {
                //moveCursor(1);
            }
        });
        registerBroadcastReceiver(true);
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
                moveMap(0, 1);
                break;
            case 2: //Rt //
                moveMap(1, 0);
                break;
            case 3: //Dn
                moveMap(0, -1);
                break;
            case 4: //Lt
                moveMap(-1, 0);
                break;
            case 5: //rotate CW
                zoomMap(1);
                break;
            case 6: //rotate CCW
                zoomMap(-1);
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
                /*
                switch (item.getItemId()) {

                    case R.id.m_loadgpx:
                        menuLoadGPX();
                        break;
                    case R.id.m_edittrack:
                        mEditMode = !mEditMode; //toggle edit mode
                        mapEditMode();
                        break;
                    case R.id.m_savegpx:
                        if (Settings.mIsLicensed - Settings.LICOFFSET != Settings.LICENSEBAD)
                            save(Settings.LICENSEBAD - Settings.LICOFFSET);
                        else
                            save();
                        isDirty = false;
                        break;
                    case R.id.m_commit:
                        if (!isDirty || (Settings.mIsLicensed - Settings.LICENSEBAD == 0))
                            return false;
                        commit();
                        isDirty = false;

                        break;
                    case R.id.m_cancel: //Undo edits to commit point
                        resetSelection();
                        mIsTrackSelected = false;
                        isDirty = false;

                        //Redraw the mGPX as it is after the last commit
                        break;
                    case R.id.m_inspoint:
                        isDirty = true;
                        mTrack.insertAfter(mCurIndex);
                        mTo += 1;
                        if (mTo >= mTrack.size()) mTo = mTrack.size() - 1;
                        redrawAll();
                        break;

                    case R.id.m_delpoint:
                        isDirty = true;
                        mTrack.deleteAt(mCurIndex);
                        mTo -= 1;
                        resetSelection();
                        redrawAll();
                        break;

                    case R.id.m_extrapol:
                        showExtrapolationFragment();
                        break;

                    case R.id.m_settings:
                        Intent i2 = new Intent(Busy.getInstance().getContext(),
                                SettingsActivity.class);
                        startActivityForResult(i2, 2);
                        break;

                    case R.id.m_test:
                        showExtrapolationFragment();
                        break;

                    default:
                }
                updateMenuIcon();
                */
                return false;
            }
        });
        popupMenu.inflate(R.menu.popup_menu);

        /*
        popupMenu.getMenu().findItem(R.id.m_edittrack).setVisible(mIsLoaded);
        popupMenu.getMenu().findItem(R.id.m_savegpx).setVisible(mIsLoaded);
        popupMenu.getMenu().findItem(R.id.m_editfile).setVisible(mIsLoaded);

        //Edit mode handling
        popupMenu.getMenu().findItem(R.id.m_inspoint).setVisible(!(mCurIndex < 0));
        popupMenu.getMenu().findItem(R.id.m_delpoint).setVisible(!(mCurIndex < 0));
        popupMenu.getMenu().findItem(R.id.m_edittrack).setTitle(mEditMode ? "Map Mode" : "Edit Mode");
        popupMenu.getMenu().findItem(R.id.m_loadgpx).setVisible(!mEditMode);

        popupMenu.getMenu().findItem(R.id.m_commit).setVisible(mIsTrackSelected && isDirty);
        popupMenu.getMenu().findItem(R.id.m_cancel).setVisible(mEditMode);

        popupMenu.getMenu().findItem(R.id.m_extrapol).setVisible(mEditMode);
        */
        popupMenu.show();
    }

    /*
    private void showExtrapolationFragment() {
        Intent intent = new Intent(this, ExtrapolationActivity.class);
        intent.putExtra(Settings.EXID_STARTTIME, mStartTime);
        intent.putExtra(Settings.EXID_ENDTIME, mEndTime);
        startActivityForResult(intent, Settings.RESRET_EXTRAPOL);
    }
    */

}
