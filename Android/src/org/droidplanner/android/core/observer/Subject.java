package org.droidplanner.android.core.observer;

import android.support.annotation.MainThread;

@MainThread
public interface Subject<O extends Subscriber>{
    void subscribe(O observer);

    void unsubscribe(O observer);

    boolean isSubscribe(O observer);

    void notifySuccessSubscribers(int eventId, Object object);

    void notifyErrorSubscribers(int eventId, Object object);
}
