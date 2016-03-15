package com.kukarin.app.gyronav;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.PendingResult;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.LocationListener;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.kukarin.app.gyronav.MapActivity;

/**
 * Created by alexk on 3/14/2016.
 */
public class GPS {
    private final String TAG = "GPS";
    private final long INTERVAL = 1000 * 30;
    private final long FASTEST_INTERVAL = 1000 * 10;

    private LocationListener mApp;
    private boolean mIsGPSpermissionGranted = true;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;

    GPS(MapActivity a) {
        mApp = a;
        createLocationRequest();
        mGoogleApiClient = new GoogleApiClient.Builder((Context) mApp)
                .addApi(LocationServices.API)
                .addConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) mApp)
                .addOnConnectionFailedListener((GoogleApiClient.OnConnectionFailedListener) mApp)
                .build();

    }

    protected void createLocationRequest() {
        mLocationRequest = new LocationRequest();
        mLocationRequest.setInterval(INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
    }

    protected void startLocationUpdates() {
        if (!isConnected() || !mIsGPSpermissionGranted) return;
        PendingResult<Status> pendingResult = LocationServices.FusedLocationApi.requestLocationUpdates(
                mGoogleApiClient, mLocationRequest, mApp);
        Log.d(TAG, "Location update started ..............: ");
    }

    protected void stopLocationUpdates() {
        if(isConnected())
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, mApp);
        Log.d(TAG, "Location update stopped .......................");
    }

    public void connect() {
        mGoogleApiClient.connect();
    }

    public void disconnect() {
        mGoogleApiClient.disconnect();
        mGoogleApiClient.unregisterConnectionCallbacks((GoogleApiClient.ConnectionCallbacks) mApp);
    }

    public boolean isConnected() {
        return mGoogleApiClient.isConnected();
    }

    public boolean permitionIsGranted() {
        if (ActivityCompat.checkSelfPermission((Context) mApp,
                Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                ActivityCompat.checkSelfPermission((Context) mApp, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            mIsGPSpermissionGranted = false;
        }
        mIsGPSpermissionGranted = true;
        return mIsGPSpermissionGranted;
    }
}
