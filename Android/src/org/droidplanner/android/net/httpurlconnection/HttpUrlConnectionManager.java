package org.droidplanner.android.net.httpurlconnection;

import android.content.Context;
import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.core.api.App;
import org.droidplanner.android.core.api.Net;
import org.droidplanner.android.core.observer.NetSubscriber;
import org.droidplanner.android.net.httpurlconnection.get.GetWeather;
import org.droidplanner.android.net.httpurlconnection.put.Restricted;
import org.droidplanner.android.net.httpurlconnection.put.RouteByCoord;
import org.droidplanner.android.net.httpurlconnection.put.RouteByID;
import org.droidplanner.android.net.model.NetError;
import org.droidplanner.android.net.model.Path;
import org.droidplanner.android.net.model.RestrictedArea;
import org.droidplanner.android.net.model.ServerPoint;
import org.droidplanner.android.net.model.WeatherInfo;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class HttpUrlConnectionManager implements Net {
    private final List<NetSubscriber> observers = new ArrayList<>();
    private final Executor executor;

    public HttpUrlConnectionManager(Context context) {
        App app = DroidPlannerApp.getApp(context);
        executor = app.getExecutor();
    }

    @Override
    public void subscribe(NetSubscriber observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unsubscribe(NetSubscriber observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    @Override
    public boolean isSubscribe(NetSubscriber observer) {
        return observers.contains(observer);
    }

    @Override
    public void notifySuccessSubscribers(@NetEvent int eventId, Object object) {
        for (NetSubscriber observer : observers) {
            observer.onNetRequestSuccess(eventId, object);
        }
    }

    @Override
    public void notifyErrorSubscribers(int eventId, Object object) {
        for (NetSubscriber observer : observers) {
            observer.onNetRequestError(eventId, (NetError) object);
        }
    }

    @Override
    public void calculateRoute(@NonNull List<ServerPoint> serverPoints, String droneId) {
        RouteByID routeByID = new RouteByID(serverPoints, droneId);
        routeByID.setCallback(new HttpRequest.Callback<Path>() {

            @Override
            public void onLoadFinish(Path response, int statusCode) {
                notifySuccessSubscribers(CALCULATE_ROUTE, response);
            }

            @Override
            public void onLoadError(NetError netError) {
                notifyErrorSubscribers(CALCULATE_ROUTE, netError);
            }
        });
        executor.execute(routeByID);
    }

    @Override
    public void calculateRoute(@NonNull List<ServerPoint> serverPoints, @NonNull LatLng latLng) {
        RouteByCoord routeByCoord = new RouteByCoord(serverPoints, latLng);
        routeByCoord.setCallback(new HttpRequest.Callback<Path>() {

            @Override
            public void onLoadFinish(Path response, int statusCode) {
                notifySuccessSubscribers(CALCULATE_ROUTE, response);
            }

            @Override
            public void onLoadError(NetError netError) {
                notifyErrorSubscribers(CALCULATE_ROUTE, netError);
            }
        });
        executor.execute(routeByCoord);
    }

    @Override
    public void getRestrictedArea(LatLng latLng) {
        Restricted restricted = new Restricted(latLng);
        restricted.setCallback(new HttpRequest.Callback<RestrictedArea>() {
            @Override
            public void onLoadFinish(RestrictedArea response, int statusCode) {
                notifySuccessSubscribers(RESTRICTED_AREA, response);
            }

            @Override
            public void onLoadError(NetError netError) {
                notifyErrorSubscribers(RESTRICTED_AREA, netError);
            }
        });
        executor.execute(restricted);
    }

    @Override
    public void getWeatherInfo(@NonNull LatLng latLng) {
        GetWeather getWeather = new GetWeather(latLng);
        getWeather.setCallback(new HttpRequest.Callback<WeatherInfo>() {
            @Override
            public void onLoadFinish(WeatherInfo response, int statusCode) {
                notifySuccessSubscribers(GET_WEATHER_INFO, response);
            }

            @Override
            public void onLoadError(NetError netError) {
                notifyErrorSubscribers(GET_WEATHER_INFO, netError);
            }
        });
        executor.execute(getWeather);
    }
}
