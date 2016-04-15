package org.droidplanner.android.net.httpurlconnection.post;

import android.support.annotation.NonNull;

//import com.google.gson.reflect.TypeToken;

import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.ServerPoint;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by macbook on 15.04.16.
 */
public class Route extends HttpRequest<List<ServerPoint>> {


    public Route(List<ServerPoint> pointList) {
        super(Url.ROUTE);
//        setBody(gson.toJson(pointList));
    }

    @NonNull
    @Override
    protected String httpMethod() {
        return POST;
    }

    @Override
    protected void response(final String response, final int statusCode) {
        if (callback != null) {
//            Type listType = new TypeToken<ArrayList<ServerPoint>>() {
//            }.getType();
//            List<ServerPoint> points = gson.fromJson(response, listType);
//            notifySuccess(points, statusCode);
        }
    }
}
