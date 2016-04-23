package org.droidplanner.android.net.httpurlconnection.put;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.Path;
import org.droidplanner.android.net.model.ServerPoint;

import java.util.List;

/**
 * Created by macbook on 15.04.16.
 */
public class RouteByCoord extends HttpRequest<Path> {

    public RouteByCoord(List<ServerPoint> pointList, LatLng latLng) {
        super(String.format(Url.CALCULATE_ROUTE_BY_COORDINATES, latLng.latitude, latLng.longitude));
        setBody(gson.toJson(pointList));
    }

    @NonNull
    @Override
    protected String httpMethod() {
        return PUT;
    }

    @Override
    protected void response(final String response, final int statusCode) {
        if (callback != null) {
            notifySuccess(gson.fromJson(response, Path.class), statusCode);
        }
    }
}
