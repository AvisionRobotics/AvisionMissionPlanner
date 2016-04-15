package org.droidplanner.android.net.httpurlconnection.meta;

public class Url {
    private static final String ROOT = "https://api.backendless.com/v1/";

    private static final String USERS_PART = "users/";
    private static final String DATA_PART = "data/";

    public static final String LOGIN = ROOT + USERS_PART + "login";
    public static final String POST = ROOT + DATA_PART + "Post";

    public static final String ROUTE = ROOT + "/route";
}
