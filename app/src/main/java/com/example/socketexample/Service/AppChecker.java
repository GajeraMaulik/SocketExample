package com.example.socketexample.Service;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;

import com.example.socketexample.Interface.Detector;
import com.example.socketexample.Utillity.Utils;

import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class AppChecker {
    static final int DEFAULT_TIMEOUT = 1000;
    int timeout = 1000;
    ScheduledExecutorService service;
    Runnable runnable;
    AppChecker.Listener unregisteredPackageListener;
    AppChecker.Listener anyPackageListener;
    Map<String, Listener> listeners = new HashMap();
    Detector detector;
    Handler handler = new Handler(Looper.getMainLooper());

    public AppChecker() {
        if (Utils.postLollipop()) {
            this.detector = new LollipopDetector();
        } else {
            this.detector = new PreLollipopDetector();
        }

    }

    public AppChecker timeout(int timeout) {
        this.timeout = timeout;
        return this;
    }

    public AppChecker when(String packageName, AppChecker.Listener listener) {
        this.listeners.put(packageName, listener);
        return this;
    }

    /** @deprecated */
    @Deprecated
    public AppChecker other(AppChecker.Listener listener) {
        return this.whenOther(listener);
    }

    public AppChecker whenOther(AppChecker.Listener listener) {
        this.unregisteredPackageListener = listener;
        return this;
    }

    public AppChecker whenAny(AppChecker.Listener listener) {
        this.anyPackageListener = listener;
        return this;
    }

    public void start(Context context) {
        this.runnable = this.createRunnable(context.getApplicationContext());
        this.service = new ScheduledThreadPoolExecutor(1);
        this.service.schedule(this.runnable, (long)this.timeout, TimeUnit.MILLISECONDS);
    }

    public void stop() {
        if (this.service != null) {
            this.service.shutdownNow();
            this.service = null;
        }

        this.runnable = null;
    }

    private Runnable createRunnable(final Context context) {
        return new Runnable() {
            public void run() {
                AppChecker.this.getForegroundAppAndNotify(context);
                AppChecker.this.service.schedule(AppChecker.this.createRunnable(context), (long)AppChecker.this.timeout, TimeUnit.MILLISECONDS);
            }
        };
    }

    private void getForegroundAppAndNotify(Context context) {
        String foregroundApp = this.getForegroundApp(context);
        boolean foundRegisteredPackageListener = false;
        if (foregroundApp != null) {
            Iterator var4 = this.listeners.keySet().iterator();

            while(var4.hasNext()) {
                String packageName = (String)var4.next();
                if (packageName.equalsIgnoreCase(foregroundApp)) {
                    foundRegisteredPackageListener = true;
                    this.callListener((AppChecker.Listener)this.listeners.get(foregroundApp), foregroundApp);
                }
            }

            if (!foundRegisteredPackageListener && this.unregisteredPackageListener != null) {
                this.callListener(this.unregisteredPackageListener, foregroundApp);
            }
        }

        if (this.anyPackageListener != null) {
            this.callListener(this.anyPackageListener, foregroundApp);
        }

    }

    void callListener(final AppChecker.Listener listener, final String packageName) {
        this.handler.post(new Runnable() {
            public void run() {
                listener.onForeground(packageName);
            }
        });
    }

    public String getForegroundApp(Context context) {
        return this.detector.getForegroundApp(context);
    }

    public interface Listener {
        void onForeground(String var1);
    }
}
