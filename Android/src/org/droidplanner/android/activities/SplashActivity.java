package org.droidplanner.android.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.widget.Toast;

import org.droidplanner.android.DroidPlannerApp;
import org.droidplanner.android.R;
import org.droidplanner.android.core.api.Net;
import org.droidplanner.android.core.observer.NetSubscriber;
import org.droidplanner.android.net.model.NetError;

/**
 * Created by Vadim on 07.07.17.
 */

public class SplashActivity extends AppCompatActivity implements NetSubscriber {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
    }

    @Override
    protected void onStart() {
        super.onStart();
        DroidPlannerApp.getApp(this).getNet().subscribe(this);
        DroidPlannerApp.getApp(this).getNet().login();
    }

    @Override
    public void onStop() {
        super.onStop();
        DroidPlannerApp.getApp(this).getNet().unsubscribe(this);
    }

    @Override
    public void onNetRequestSuccess(@Net.NetEvent int eventId, Object netObject) {
        Intent intent = new Intent(this, EditorActivity.class);
        startActivity(intent);
        finish();
    }

    @Override
    public void onNetRequestError(@Net.NetEvent int eventId, NetError netError) {
        Toast.makeText(this, "Authorization Failed", Toast.LENGTH_LONG).show();
    }
}
