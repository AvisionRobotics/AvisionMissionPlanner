package org.droidplanner.android.net.httpurlconnection.get;

import android.support.annotation.NonNull;

import com.google.android.gms.maps.model.LatLng;

import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.WeatherInfo;

public class GetWeather extends HttpRequest<WeatherInfo> {

    public GetWeather(LatLng latLng) {
        super(String.format(Url.GET_WEATGER_INFO, latLng.latitude, latLng.longitude));
    }

    @NonNull
    @Override
    protected String httpMethod() {
        return GET;
    }

    @Override
    protected void response(String response, int statusCode) {
        if (callback != null) {
            notifySuccess(gson.fromJson(response, WeatherInfo.class), statusCode);
        }
    }
}
