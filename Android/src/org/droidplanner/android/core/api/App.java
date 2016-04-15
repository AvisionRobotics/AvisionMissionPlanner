package org.droidplanner.android.core.api;

import java.util.concurrent.Executor;

public interface App {
    Net getNet();

    Executor getExecutor();
}
