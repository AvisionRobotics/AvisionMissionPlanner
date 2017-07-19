package org.droidplanner.android.net.model;

import com.google.gson.annotations.SerializedName;

/**
 * Created by Vadim on 07.07.17.
 */

public class LoginRequest {
    @SerializedName("l")
    private String login;
    @SerializedName("p")
    private String password;

    public String getLogin() {
        return login;
    }

    public void setLogin(String login) {
        this.login = login;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
