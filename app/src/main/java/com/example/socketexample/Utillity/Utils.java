package com.example.socketexample.Utillity;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.ActivityManager;
import android.app.AlertDialog;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Process;
import android.util.Log;
import android.widget.Toast;


import com.example.socketexample.Service.SocketBackgroundService;

import java.io.UnsupportedEncodingException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.TimeZone;

public class Utils {
    private final static char[] hexArray = "0123456789ABCDEF".toCharArray();

    public final static String bytesToHex(byte[] bytes) {
        if (bytes == null) return null;

        char[] hexChars = new char[bytes.length * 2];
        for (int j = 0; j < bytes.length; j++) {
            int v = bytes[j] & 0xFF;
            hexChars[j * 2] = hexArray[v >>> 4];
            hexChars[j * 2 + 1] = hexArray[v & 0x0F];
        }
        return "0x" + new String(hexChars);
    }
    public static boolean postLollipop() {
        return Build.VERSION.SDK_INT >= 21;
    }

    @TargetApi(19)
    public static boolean hasUsageStatsPermission(Context context) {
        AppOpsManager appOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow("android:get_usage_stats", Process.myUid(), context.getPackageName());
        boolean granted = mode == 0;
        return granted;
    }
    public final static String bytesToHexAndString(byte[] bytes) {
        if (bytes == null) return null;

        return bytesToHex(bytes) + " (" + new String(bytes) + ")";
    }

    public final static String now() {
        TimeZone tz = TimeZone.getTimeZone("UTC");
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.ENGLISH);
        df.setTimeZone(tz);
        return df.format(new Date());
    }

    public static Double getSmallValue(List<Double> list) {
        try {
            System.out.println("Minimum element : " + Collections.min(list));
            return Collections.min(list);
        } catch (ClassCastException | NoSuchElementException e) {
            System.out.println("Exception caught : " + e);
        }
        return 0.0;
    }


    public static void showNfcSettingsDialog(final Activity app) {
        new AlertDialog.Builder(app)
                .setTitle("NFC is disabled")
                .setMessage("You must enable NFC to use this app.")
                .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        app.startActivity(new Intent(android.provider.Settings.ACTION_NFC_SETTINGS));
                    }
                })
                .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
                    public void onClick(DialogInterface dialog, int which) {
                        app.finish();
                    }
                })
                .setIcon(android.R.drawable.ic_dialog_alert)
                .show();
    }

    public static String readRecord(byte[] payload) throws UnsupportedEncodingException {
        String textEncoding = ((payload[0] & 128) == 0) ? "UTF-8" : "UTF-16";
        int languageCodeLength = payload[0] & 63;
        return new String(payload, languageCodeLength + 1, payload.length - languageCodeLength - 1, textEncoding);
    }

    public static void showMessage(Context context, String messageToShow) {
        Toast.makeText(context, messageToShow, Toast.LENGTH_SHORT).show();
    }

    public static boolean isServiceRunning(Context context, Class<?> serviceClass) {
        final ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningServiceInfo> services = activityManager.getRunningServices(Integer.MAX_VALUE);
        for (ActivityManager.RunningServiceInfo runningServiceInfo : services) {
            Log.d("Utils :;    ", String.format("Service:%s", runningServiceInfo.service.getClassName()));
            Log.d("Utils getClassName : ", runningServiceInfo.service.getClassName());
            if (runningServiceInfo.service.getClassName().equals(serviceClass.getName())) {
                return true;
            }
        }
        return false;
    }

    /*public static void scheduleJob(Context context) {
        if (!isServiceRunning(context, MyJobService.class)) {
            Log.d("MyJobService", "MyJobService scheduleJob------");
            ComponentName serviceComponent = new ComponentName(context, MyJobService.class);
            JobInfo.Builder builder = new JobInfo.Builder(0, serviceComponent);
           // builder.setMinimumLatency(2 * 1000); // Wait at least 10s
            builder.setMinimumLatency(0L); // Wait at least 0s
            builder.setOverrideDeadline(10 * 1000); // Maximum delay 30s
            // builder.setMinimumLatency(30 * 1000); // Wait at least 30s
            //    builder.setOverrideDeadline(60 * 1000); // Maximum delay 60s
            JobScheduler jobScheduler = (JobScheduler) context.getSystemService(Context.JOB_SCHEDULER_SERVICE);
            jobScheduler.schedule(builder.build());
        } else {
            Log.d("MyJobService", "MyJobService already running------");
        }
    }*/

    public static void startService(Context context) {
        if (!isServiceRunning(context, SocketBackgroundService.class)) {
            Log.d("MyJobService", "MyJobService scheduleJob------");
            Intent serviceIntent = new Intent(context, SocketBackgroundService.class);
            //  ContextCompat.startForegroundService(this, serviceIntent)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(serviceIntent);
            } else {
                context.startService(serviceIntent);
            }

        } else {
            Log.d("MyJobService", "MyJobService already running------");
        }
    }

    public static boolean appInstalledOrNot(Context context, String uri) {
        PackageManager pm = context.getPackageManager();
        boolean app_installed = false;
        try {
            pm.getPackageInfo(uri, PackageManager.GET_ACTIVITIES);
            app_installed = true;
        } catch (PackageManager.NameNotFoundException e) {
            app_installed = false;
        }
        return app_installed;
    }


}
