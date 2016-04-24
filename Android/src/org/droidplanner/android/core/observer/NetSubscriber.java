package org.droidplanner.android.core.observer;

import org.droidplanner.android.core.api.Net;
import org.droidplanner.android.net.model.NetError;

public interface NetSubscriber extends Subscriber {
    void onNetRequestSuccess(@Net.NetEvent int eventId, Object netObject);

    void onNetRequestError(@Net.NetEvent int eventId, NetError netError);
}
