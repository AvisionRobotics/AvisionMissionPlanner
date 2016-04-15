package org.droidplanner.android.core.api;

import android.support.annotation.IntDef;
import android.support.annotation.NonNull;

import org.droidplanner.android.core.observer.NetSubscriber;
import org.droidplanner.android.core.observer.Subject;
import org.droidplanner.android.net.model.ServerPoint;
import org.droidplanner.android.net.model.User;

import java.util.List;


public interface Net extends Subject<NetSubscriber> {
    @IntDef({LOG_IN, GET_POSTS})
    @interface NetEvent {
    }

    int LOG_IN = 102;
    int GET_POSTS = 202;
    int CHECK_ROUTE = 302;

    void login(@NonNull User user);

    void getPosts();

    void checkRoute(@NonNull List<ServerPoint> serverPoints);
}