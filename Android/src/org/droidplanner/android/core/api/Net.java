package org.droidplanner.android.core.api;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.core.observer.NetSubscriber;
import org.droidplanner.android.core.observer.Subject;
import org.droidplanner.android.net.model.ServerPoint;

import java.util.List;


public interface Net extends Subject<NetSubscriber> {

    @IntDef({CALCULATE_ROUTE, RESTRICTED_AREA,
            GET_WEATHER_INFO, LOGIN})
    @interface NetEvent {
    }

    int CALCULATE_ROUTE = 1;
    int RESTRICTED_AREA = 2;
    int GET_WEATHER_INFO = 3;
    int LOGIN = 4;

    void calculateRoute(@NonNull List<ServerPoint> serverPoints, @NonNull String droneId);

    void calculateRoute(@NonNull List<ServerPoint> serverPoints, @NonNull LatLng latLng);

    void getRestrictedArea(LatLng latLng);

    void getWeatherInfo(@NonNull LatLng latLng);

    void login();

}