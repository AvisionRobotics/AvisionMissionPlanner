package org.droidplanner.android.net.httpurlconnection;

import android.os.Handler;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.StringDef;
import android.support.annotation.UiThread;
import android.support.v4.util.ArrayMap;
import android.text.TextUtils;
import android.util.Log;

import com.google.gson.Gson;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.net.model.NetError;
import org.droidplanner.android.utils.IOUtils;

import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public abstract class HttpRequest<C> implements Runnable {
    private static final String TAG = HttpRequest.class.getName();
    private static final int DEFAULT_TIMEOUT = 1000 * 60;

    protected final static String POST = "POST";
    protected final static String GET = "GET";
    protected final static String PUT = "PUT";
    protected Gson gson;

    @StringDef({POST, GET, PUT})
    @interface HttpMethod {
    }

    @UiThread
    public interface Callback<C> {
        void onLoadFinish(C response, int statusCode);

        void onLoadError(NetError netError);
    }

    protected final Handler mainThreadHandler;
    private final Map<String, String> headers = new ArrayMap<>();
    private final String endpoint;

    private String body;

    protected Callback<C> callback;

    @NonNull
    @HttpMethod
    protected abstract String httpMethod();

    protected abstract void response(String response, int statusCode);

    public HttpRequest(@NonNull String endpoint) {
        this.endpoint = endpoint;
        mainThreadHandler = new Handler(Looper.getMainLooper());
        gson = new Gson();
    }

    public void setCallback(Callback<C> callback) {
        this.callback = callback;
    }

    protected void executeRequest() {
        addHeader("Content-Type", "application/json");

//        try {
//
//            KeyStore keyStore =...;
//            String algorithm = TrustManagerFactory.getDefaultAlgorithm();
//            TrustManagerFactory tmf = TrustManagerFactory.getInstance(algorithm);
//
//            tmf.init(keyStore);
//
//            SSLContext context = null;
//            context = SSLContext.getInstance("TLS");
//
//            context.init(null, tmf.getTrustManagers(), null);
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyManagementException e) {
//            e.printStackTrace();
//        } catch (NoSuchAlgorithmException e) {
//            e.printStackTrace();
//        } catch (KeyStoreException e) {
//            e.printStackTrace();
//        }

        HttpURLConnection urlConnection = null;
        boolean posting = false;
        int statusCode = 0;
        String httpMethod = httpMethod();
        if (httpMethod.equals(POST) || httpMethod.equals(PUT)) {
            posting = true;
        }
        try {
            URL url = new URL(endpoint);
            urlConnection = (HttpURLConnection) url.openConnection();
            urlConnection.setConnectTimeout(DEFAULT_TIMEOUT);
            urlConnection.setReadTimeout(DEFAULT_TIMEOUT);
            urlConnection.setDoOutput(posting);
            urlConnection.setDoInput(true);
            urlConnection.setRequestMethod(httpMethod);
            if (posting) {
                setHeaders(urlConnection);

                OutputStream outputStream = urlConnection.getOutputStream();
                IOUtils.writeToStream(outputStream, body);
            }

            Log.d("---> " + httpMethod, endpoint + " \n " + body);

            urlConnection.connect();
            statusCode = urlConnection.getResponseCode();
            Log.d(TAG, endpoint + "\t" + String.valueOf(statusCode));

            extractCookie(urlConnection);

            String response = IOUtils.convertStreamToString(urlConnection.getInputStream());
            Log.d(TAG, response);
            response(response, urlConnection.getResponseCode());
        } catch (final Exception exception) {
            String errorMessage = null;
            if (urlConnection != null && urlConnection.getErrorStream() != null) {
                errorMessage = IOUtils.convertStreamToString(urlConnection.getErrorStream());
                Log.d(TAG, errorMessage);
            }
            Log.d(TAG, exception.getMessage());
            if (callback != null) {
                final NetError netError = new NetError();
                netError.setException(exception);
                netError.setMessage(errorMessage);
                netError.setUrl(endpoint);
                netError.setStatusCode(statusCode);
                notifyError(netError);
            }
        } finally {
            if (urlConnection != null) {
                urlConnection.disconnect();
            }
        }
    }

    @Override
    public void run() {
        executeRequest();
    }

    private void setHeaders(HttpURLConnection urlConnection) {
        Log.d("Headers:", "***");
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            urlConnection.setRequestProperty(entry.getKey(), entry.getValue());
            Log.d("--->", entry.getKey() + " : " + entry.getValue());
        }
    }

    protected void addHeader(String key, String value) {
        headers.put(key, value);
    }

    protected void setBody(String body) {
        this.body = body;
    }

    protected void extractCookie(HttpURLConnection urlConnection) {
        if (!TextUtils.isEmpty(urlConnection.getHeaderField("Set-Cookie"))) {
//            String s = urlConnection.getHeaderField("Set-Cookie").split("=")[1].split(";")[0];
            String s = urlConnection.getHeaderField("Set-Cookie").split(";")[0];
//            String s = urlConnection.getHeaderField("Set-Cookie");
            DroidPlannerApp.getInstance().setToken(s);
        }
    }

    protected final void notifyError(final NetError error) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onLoadError(error);
            }
        });
    }

    protected final void notifySuccess(final C object, final int statusCode) {
        mainThreadHandler.post(new Runnable() {
            @Override
            public void run() {
                callback.onLoadFinish(object, statusCode);
            }
        });
    }
}
