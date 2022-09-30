package com.example.socketexample.Service;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.example.socketexample.Utillity.Utils;

public class MyBeaconWorker extends Worker {
    private final Context context;
    private String TAG = "MyWorker";

    public MyBeaconWorker(
            @NonNull Context context,
            @NonNull WorkerParameters params) {
        super(context, params);
        this.context = context;
    }

    @NonNull
    @Override
    public Result doWork() {
        Log.d(TAG, "doWork called for: " + this.getId());
        Log.d(TAG, "Service Running: " + new SocketBackgroundService());
       // if (!MyServiceBeacon.isServiceRunning) {
        if (!Utils.isServiceRunning(context, SocketBackgroundService.class)) {
            Log.d(TAG, "starting service from doWork");
            Intent intent = new Intent(this.context, SocketBackgroundService.class);
            ContextCompat.startForegroundService(context, intent);
        }
        return Result.success();
    }

    @Override
    public void onStopped() {
        Log.d(TAG, "onStopped called for: " + this.getId());
        super.onStopped();
    }
}