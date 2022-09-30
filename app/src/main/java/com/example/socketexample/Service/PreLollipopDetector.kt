package com.example.socketexample.Service

import android.annotation.SuppressLint
import android.app.ActivityManager
import android.app.ActivityManager.RunningTaskInfo
import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import com.example.socketexample.Interface.Detector

class PreLollipopDetector : Detector {
    @SuppressLint("WrongConstant")
    override fun getForegroundApp(context: Context?): String? {
        val am = context!!.getSystemService("activity") as ActivityManager
        val foregroundTaskInfo = am.getRunningTasks(1)[0] as RunningTaskInfo
        val foregroundTaskPackageName = foregroundTaskInfo.topActivity!!.packageName
        val pm: PackageManager? = context.getPackageManager()
        var foregroundAppPackageInfo: PackageInfo? = null

        foregroundAppPackageInfo = try {
            pm?.getPackageInfo(foregroundTaskPackageName, 0)
        } catch (var8: PackageManager.NameNotFoundException) {
            var8.printStackTrace()
            return null
        }

        var foregroundApp: String? = null
        if (foregroundAppPackageInfo != null) {
            foregroundApp = foregroundAppPackageInfo.applicationInfo.packageName
        }

        return foregroundApp
    }
}


