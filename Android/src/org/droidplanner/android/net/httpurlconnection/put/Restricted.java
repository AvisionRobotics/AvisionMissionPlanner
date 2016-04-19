package org.droidplanner.android.net.httpurlconnection.put;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.RestrictedArea;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * Created by macbook on 15.04.16.
 */
public class Restricted extends HttpRequest<RestrictedArea> {

    public Restricted(LatLng latLng) {
        super(Url.AREA);
        try {
            JSONObject jsonObject = new JSONObject();
            jsonObject.put("lat", latLng.latitude);
            jsonObject.put("lng", latLng.longitude);
            setBody(jsonObject.toString());
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @NonNull
    @Override
    protected String httpMethod() {
        return PUT;
    }

    @Override
    protected void response(final String response, final int statusCode) {
        if (callback != null) {
            RestrictedArea area = gson.fromJson(response, RestrictedArea.class);
            notifySuccess(area, statusCode);
        }
    }
}
