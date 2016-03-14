package com.kukarin.app.gyronav;

import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by alexk on 3/14/2016.
 */
public class Track {
    ArrayList<LatLng> coords;
    ArrayList<Long> times;

    Track() {
        coords = new ArrayList<>();
    }

    public void add(double lat, double lon, long t) {
        coords.add(new LatLng(lat, lon));
        times.add(t);
    }

    public void add(LatLng a) {
        coords.add(a);
    }

    public int size() {
        return coords.size();
    }
}
