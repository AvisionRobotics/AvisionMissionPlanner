package org.droidplanner.android.net.httpurlconnection.post;

import android.support.annotation.NonNull;

import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.LoginRequest;

public class Login extends HttpRequest<Object> {

    public Login() {
        super(Url.LOGIN);
        LoginRequest request = new LoginRequest();
        request.setLogin("operator");
        request.setPassword("Avision");
        setBody(gson.toJson(request));
    }

    @NonNull
    @Override
    protected String httpMethod() {
        return POST;
    }

    @Override
    protected void response(String response, int statusCode) {
        if (callback != null) {
            notifySuccess("", statusCode);
        }
    }
}
