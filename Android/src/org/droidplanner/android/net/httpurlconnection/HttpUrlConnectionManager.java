package org.droidplanner.android.net.httpurlconnection;

import android.content.Context;
import android.support.annotation.NonNull;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.core.api.App;
import org.droidplanner.android.core.api.Net;
import org.droidplanner.android.core.observer.NetSubscriber;
import org.droidplanner.android.net.httpurlconnection.get.GetPosts;
import org.droidplanner.android.net.httpurlconnection.post.Login;
import org.droidplanner.android.net.httpurlconnection.post.Route;
import org.droidplanner.android.net.model.NetError;
import org.droidplanner.android.net.model.Post;
import org.droidplanner.android.net.model.ServerPoint;
import org.droidplanner.android.net.model.User;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;

public class HttpUrlConnectionManager implements Net {
    private final List<NetSubscriber> observers = new ArrayList<>();
    private final Executor executor;

    public HttpUrlConnectionManager(Context context) {
        App app = DroidPlannerApp.getApp(context);
        executor = app.getExecutor();
    }

    @Override
    public void subscribe(NetSubscriber observer) {
        if (!observers.contains(observer)) {
            observers.add(observer);
        }
    }

    @Override
    public void unsubscribe(NetSubscriber observer) {
        if (observers.contains(observer)) {
            observers.remove(observer);
        }
    }

    @Override
    public boolean isSubscribe(NetSubscriber observer) {
        return observers.contains(observer);
    }

    @Override
    public void notifySuccessSubscribers(@NetEvent int eventId, Object object) {
        for (NetSubscriber observer : observers) {
            observer.onNetRequestSuccess(eventId, object);
        }
    }

    @Override
    public void notifyErrorSubscribers(int eventId, Object object) {
        for (NetSubscriber observer : observers) {
            observer.onNetRequestError(eventId, (NetError) object);
        }
    }

    @Override
    public void login(@NonNull User user) {
        Login login = new Login(user);
        login.setCallback(new HttpRequest.Callback<User>() {
            @Override
            public void onLoadFinish(User response, int statusCode) {
                notifySuccessSubscribers(LOG_IN, response);
            }

            @Override
            public void onLoadError(NetError netError) {
                notifyErrorSubscribers(LOG_IN, netError);
            }
        });
        executor.execute(login);
    }

    @Override
    public void getPosts() {
        GetPosts getPosts = new GetPosts();
        getPosts.setCallback(new HttpRequest.Callback<List<Post>>() {
            @Override
            public void onLoadFinish(List<Post> response, int statusCode) {
                notifySuccessSubscribers(GET_POSTS, response);
            }

            @Override
            public void onLoadError(NetError netError) {
                notifyErrorSubscribers(GET_POSTS, netError);
            }
        });
        executor.execute(getPosts);
    }

    @Override
    public void checkRoute(@NonNull List<ServerPoint> serverPoints) {
        Route route = new Route(serverPoints);
        route.setCallback(new HttpRequest.Callback<List<ServerPoint>>() {

            @Override
            public void onLoadFinish(List<ServerPoint> response, int statusCode) {
                notifyErrorSubscribers(CHECK_ROUTE, response);
            }

            @Override
            public void onLoadError(NetError netError) {
                notifyErrorSubscribers(CHECK_ROUTE, netError);
            }
        });
        executor.execute(route);
    }
}
