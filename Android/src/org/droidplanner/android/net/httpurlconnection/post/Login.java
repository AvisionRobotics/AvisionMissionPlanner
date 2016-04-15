package org.droidplanner.android.net.httpurlconnection.post;


import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import org.droidplanner.android.net.httpurlconnection.HttpRequest;
import org.droidplanner.android.net.httpurlconnection.meta.Header;
import org.droidplanner.android.net.httpurlconnection.meta.Url;
import org.droidplanner.android.net.model.User;
import org.json.JSONException;
import org.json.JSONObject;

public class Login extends HttpRequest<User> {
    private final User user;

    public Login(User user) {
        super(Url.LOGIN);
        this.user = user;
        addHeader(Header.Key.APPLICATION_ID, Header.Value.APPLICATION_ID);
        addHeader(Header.Key.SECRET_KEY, Header.Value.SECRET_KEY);
        addHeader(Header.Key.CONTENT_TYPE, Header.Value.CONTENT_TYPE);
        addHeader(Header.Key.APPLICATION_TYPE, Header.Value.APPLICATION_TYPE);
        setBody(body(user));
    }

    @NonNull
    @Override
    protected String httpMethod() {
        return POST;
    }

    @Override
    protected void response(final String response, final int statusCode) {
        if (callback != null) {
            notifySuccess(parseJSON(response), statusCode);
        }
    }

    private String body(User user) {
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("login", user.getLogin());
            jsonObject.put("password", user.getPassword());
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return jsonObject.toString();
    }

    @Nullable
    private User parseJSON(String response) {
        try {
            JSONObject jsonObject = new JSONObject(response);
            user.setObjectId(jsonObject.getString("objectId"));
            user.setEmail(jsonObject.getString("email"));
            user.setName(jsonObject.getString("name"));
        } catch (JSONException e) {
            e.printStackTrace();
            return null;
        }
        return user;
    }
}
