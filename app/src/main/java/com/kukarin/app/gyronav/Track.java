package com.kukarin.app.gyronav;

import android.location.Location;

import com.google.android.gms.maps.model.LatLng;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;

/**
 * Created by alexk on 3/14/2016.
 */
public class Track {
    int sz;
    ArrayList<LatLng>   coords;
    ArrayList<Long>     times;
    ArrayList<Waypoint> trackwp;
    ArrayList<LatLng>   manualcoords;
    ArrayList<Waypoint> manualwp;

    public ArrayList<LatLng> getManualcoords() {
        return manualcoords;
    }



    public ArrayList<LatLng> getCoords() {
        return coords;
    }

    public LatLng getLastManulaWPcoords(int i){
        int size = manualcoords.size();
        if(size==0) return null;
        return manualcoords.get(size -1);
    }

    public LatLng getManulaWPcoords(int i){
        return manualcoords.get(i);
    }

    Track() {
        sz = 0;
        coords = new ArrayList<>();
        times = new ArrayList<>();
        trackwp = new ArrayList<>();
        manualwp = new ArrayList<>();
        manualcoords = new ArrayList<>();
    }

    public void add(double lat, double lon, long t) {
        coords.add(new LatLng(lat, lon));
        times.add(t);
    }

    public void add(LatLng a) {
        coords.add(a);
    }

    public int size() {
        return sz;
    }

    public void add(Location location, long time) {
        sz++;
        coords.add(new LatLng(location.getLatitude(), location.getLongitude()));
        times.add(time);
        trackwp.add(new Waypoint(location));
    }

    public void addManualWaypoint(Location location) {
        manualcoords.add(new LatLng(location.getLatitude(), location.getLongitude()));
        manualwp.add(new Waypoint(location));
    }

    public String getLastManualWPinfo() {
        int size = manualwp.size();
        if (size == 0) return "no data";
        return getWPinfo(size - 1);
    }

    public String getWPinfo(int i){
        String LF = "\n";
        Waypoint lwp = manualwp.get(i);
        String out = ""+ i + "." + lwp.getName()+ LF
                + DateFormat.getTimeInstance().format(new Date(lwp.getTime()))
                + "ac:" + lwp.getAccuracy()
                + ", al:"+lwp.getAltitude()
                + ", sp:" + lwp.getSpeed();
        return out;
    }

    private class Waypoint {
        private String name;
        private double mAltitude=0;
        private float mSpeed=0f;
        private float mAccuracy=0f;
        private long  mTime=0;

        Waypoint(Location l) {
            name = "NONAME";
            if(l.hasAccuracy()) mAccuracy = l.getAccuracy();
            if(l.hasAltitude()) mAltitude = l.getAltitude();
            if(l.hasSpeed())    mSpeed = l.getSpeed();
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getName() {
            return name;
        }

        public float getAccuracy() {
            return mAccuracy;
        }

        public long getTime() {
            return mTime;
        }

        public double getAltitude() {
            return mAltitude;
        }

        public float getSpeed() {
            return mSpeed;
        }
    }
}
