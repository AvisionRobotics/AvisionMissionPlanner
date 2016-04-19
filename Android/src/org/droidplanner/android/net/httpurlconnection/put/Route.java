package org.droidplanner.android.net.httpurlconnection.put;

import android.support.annotation.NonNull;

import com.google.gson.reflect.TypeToken;

import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.Path;
import org.droidplanner.android.net.model.ServerPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by macbook on 15.04.16.
 */
public class Route extends HttpRequest<Path> {

    public Route(List<ServerPoint> pointList, String droneId) {
        super(String.format(Url.CALCULATE_ROUTE, droneId));
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
