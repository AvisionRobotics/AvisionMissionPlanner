package org.droidplanner.android.net.model;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by macbook on 16.04.16.
 */
public class RestrictedArea {

    private List<Point> restricted;
    private List<Point> geofencing;

    public List<Point> getRestricted() {
        return restricted;
    }

    public void setRestricted(List<Point> restricted) {
        this.restricted = restricted;
    }

    public List<Point> getGeofencing() {
        return geofencing;
    }

    public void setGeofencing(List<Point> geofencing) {
        this.geofencing = geofencing;
    }

    public class Point {
        private String name;
        private int radius;
        @SerializedName("lat")
        private double latitude;
        @SerializedName("lng")
        private double longitude;
        private long id;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public int getRadius() {
            return radius;
        }

        public void setRadius(int radius) {
            this.radius = radius;
        }

        public double getLatitude() {
            return latitude;
        }

        public void setLatitude(double latitude) {
            this.latitude = latitude;
        }

        public double getLongitude() {
            return longitude;
        }

        public void setLongitude(double longitude) {
            this.longitude = longitude;
        }

        public long getId() {
            return id;
        }

        public void setId(long id) {
            this.id = id;
        }
    }
}


//{"restricted":[
//        {
//        "name":"test1",
//        "radius":100.0,
//        "lat":50.45231,
//        "lng":30.46672,
//        "id":"1531304378436157440"
//        },
//        {
//        "name":"test2",
//        "radius":100.0,
//        "lat":50.69994,
//        "lng":31.39069,
//        "id":"1531304378447691776"
//        }
//        ],"geofencing":[
//        {
//        "name":"test3",
//        "simplePointList":[],
//        "lat":50.45231,
//        "lng":30.46672,
//        "id":"1531304378450837504"
//        }
//        ]}