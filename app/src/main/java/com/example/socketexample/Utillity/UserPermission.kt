package com.example.socketexample.Utillity

import android.Manifest
import android.app.Activity
import android.content.Context
import androidx.core.app.ActivityCompat
import android.content.pm.PackageManager
import com.example.socketexample.Utillity.UserPermission
import android.os.Build
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.annotation.RequiresApi

class UserPermission(private val activity: Activity) {

    fun checkReadPhoneStatePermission(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) == PackageManager.PERMISSION_GRANTED
    }

    fun requestReadPhoneStatePermission() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.READ_PHONE_STATE), READ_PHONE_STATE_PERMISSION_CONSTANT)
        }
    }

    fun checkOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(activity)
        } else {
            true
        }
    }

    fun requestOverlayPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (!Settings.canDrawOverlays(activity)) {
                val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION)
                intent.data = Uri.parse("package:" + activity.packageName)
                activity.startActivityForResult(intent, ACTION_MANAGE_OVERLAY_PERMISSION_CONSTANT)
            }
        }
    }

    fun checkAccessFineLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    fun checkAccessCoarseLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun checkBackgroundLocationPermission(): Boolean {
        return ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
    }

    @RequiresApi(api = Build.VERSION_CODES.Q)
    fun requestBackgroundLocationPermission() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_BACKGROUND_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_BACKGROUND_LOCATION), ACCESS_BACKGROUND_LOCATION_PERMISSION_CONSTANT)
        }
    }

    fun requestAccessFineLocationPermission() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), ACCESS_FINE_LOCATION_PERMISSION_CONSTANT)
        }
    }

    fun requestAccessCourseLocationPermission() {
        if (ActivityCompat.checkSelfPermission(activity, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(activity, arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), ACCESS_COARSE_LOCATION_PERMISSION_CONSTANT)
        }
    }

    companion object {
        private const val READ_PHONE_STATE_PERMISSION_CONSTANT = 101
        private const val ACTION_MANAGE_OVERLAY_PERMISSION_CONSTANT = 102
        private const val DEVICE_ADMIN_PERMISSION_CONSTANT = 103
        private const val ACCESS_FINE_LOCATION_PERMISSION_CONSTANT = 104
        private const val ACCESS_COARSE_LOCATION_PERMISSION_CONSTANT = 105
        private const val ACCESS_BACKGROUND_LOCATION_PERMISSION_CONSTANT = 106
        fun hasPermissions(context: Context?, vararg permissions: String?): Boolean {
            if (context != null && permissions != null) {
                for (permission in permissions) {
                    if (ActivityCompat.checkSelfPermission(
                            context,
                            permission!!
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        return false
                    }
                }
            }
            return true
        }
    }
}