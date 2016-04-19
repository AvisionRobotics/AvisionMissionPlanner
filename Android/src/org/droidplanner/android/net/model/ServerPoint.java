package org.droidplanner.android.net.model;

import com.google.gson.annotations.SerializedName;
import com.o3dr.services.android.lib.drone.mission.item.MissionItem;

/**
 * Created by macbook on 13.04.16.
 */
public class ServerPoint {

    @SerializedName("lat")
    private double latitude;

    @SerializedName("lng")
    private double longitude;

    private String cn;
    private boolean tf;
    private boolean cr = false;

    @SerializedName("ch")
    private double altitude;

    public static ServerPoint toServerModel(MissionItem missionItem){
     ServerPoint serverPoint = new ServerPoint();
        serverPoint.setCn(missionItem.getType().getLabel());
        serverPoint.setLatitude(((MissionItem.SpatialItem)missionItem).getCoordinate().getLatitude());
        serverPoint.setLongitude(((MissionItem.SpatialItem)missionItem).getCoordinate().getLongitude());
        serverPoint.setAltitude(((MissionItem.SpatialItem)missionItem).getCoordinate().getAltitude());
        return serverPoint;
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

    public String getCn() {
        return cn;
    }

    public void setCn(String cn) {
        this.cn = cn;
    }

    public boolean isTf() {
        return tf;
    }

    public void setTf(boolean tf) {
        this.tf = tf;
    }

    public boolean isCr() {
        return cr;
    }

    public void setCr(boolean cr) {
        this.cr = cr;
    }

    public double getAltitude() {
        return altitude;
    }

    public void setAltitude(double altitude) {
        this.altitude = altitude;
    }
}
