package org.droidplanner.android.net.httpurlconnection.meta;

public class Url {
    private static final String ROOT = "http://dev.avisionrobotics.com/api/";

    private static final String USERS_PART = "users/";
    private static final String DATA_PART = "data/";

    public static final String LOGIN = ROOT + USERS_PART + "login";
    public static final String POST = ROOT + DATA_PART + "Post";

    public static final String AREA = ROOT + "area/8";
    public static final String CALCULATE_ROUTE_BY_ID = ROOT + "flight/drone/%s/calculate";
    public static final String CALCULATE_ROUTE_BY_COORDINATES = ROOT + "flight/drone/%s/%s/calculate";
    public static final String GET_WEATGER_INFO = ROOT + "map/weather/%s/%s";
}
