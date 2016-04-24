package org.droidplanner.android.net.model;

import java.util.List;

/**
 * Created by macbook on 18.04.16.
 */
public class Path {

    private List<ServerPoint> passedPoints;
    private double distance;

    public List<ServerPoint> getPassedPoints() {
        return passedPoints;
    }

    public void setPassedPoints(List<ServerPoint> passedPoints) {
        this.passedPoints = passedPoints;
    }

    public double getDistance() {
        return distance;
    }

    public void setDistance(double distance) {
        this.distance = distance;
    }
}
