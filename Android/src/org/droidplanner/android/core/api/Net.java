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
    @IntDef({LOG_IN, GET_POSTS, CALCULATE_ROUTE, RESTRICTED_AREA})
    @interface NetEvent {
    }

    int LOG_IN = 102;
    int GET_POSTS = 202;
    int CALCULATE_ROUTE = 302;
    int RESTRICTED_AREA = 402;

    void login(@NonNull User user);

    void getPosts();

    void calculateRoute(@NonNull List<ServerPoint> serverPoints, @NonNull String droneId);

    void getRestrictedArea(LatLng latLng);
}