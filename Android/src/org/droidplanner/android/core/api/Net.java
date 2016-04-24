package org.droidplanner.android.core.api;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.core.observer.NetSubscriber;
import org.droidplanner.android.core.observer.Subject;
import org.droidplanner.android.net.model.ServerPoint;
import org.droidplanner.android.net.model.User;

import java.util.List;


public interface Net extends Subject<NetSubscriber> {

    @IntDef({CALCULATE_ROUTE, RESTRICTED_AREA, GET_WEATHER_INFO})
    @interface NetEvent {
    }

    int LOG_IN = 1;
    int GET_POSTS = 2;
    int CALCULATE_ROUTE = 3;
    int RESTRICTED_AREA = 4;
    int GET_WEATHER_INFO = 5;

    void login(@NonNull User user);

    void getPosts();

    void calculateRoute(@NonNull List<ServerPoint> serverPoints, @NonNull String droneId);

    void calculateRoute(@NonNull List<ServerPoint> serverPoints, @NonNull LatLng latLng);

    void getRestrictedArea(LatLng latLng);

    void getWeatherInfo(@NonNull LatLng latLng);

}