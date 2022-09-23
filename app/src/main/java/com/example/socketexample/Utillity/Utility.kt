package com.example.socketexample.Utillity

import android.annotation.SuppressLint
import android.app.*
import android.app.job.JobService
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.drawable.ColorDrawable
import android.net.ConnectivityManager
import android.net.wifi.WifiManager
import android.os.Build
import android.provider.Settings
import android.provider.Settings.SettingNotFoundException
import android.text.SpannableStringBuilder
import android.text.style.RelativeSizeSpan
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.view.inputmethod.InputMethodManager
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.AppCompatEditText
import androidx.core.app.NotificationCompat
import com.example.socketexample.Interface.Constants
import com.example.socketexample.MainActivity
import com.example.socketexample.R
import com.example.socketexample.VaultLockerApp
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.gms.tasks.Task
import java.net.InetAddress
import java.net.NetworkInterface
import java.util.*


object Utility : Constants {
    private var mToast: Toast? = null

    @SuppressLint("ShowToast")
    fun toast(context: Context?, message: String) {
        if (context == null) return
        if (mToast != null) mToast!!.cancel()
        if (context.resources.configuration.screenLayout and
            Configuration.SCREENLAYOUT_SIZE_MASK ==
            Configuration.SCREENLAYOUT_SIZE_LARGE
        ) {
            // on a large screen device ...
            val biggerText = SpannableStringBuilder(message)
            biggerText.setSpan(RelativeSizeSpan(1.35f), 0, message.length, 0)
            mToast = Toast.makeText(context, biggerText, Toast.LENGTH_LONG)
        } else {
            mToast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        }
        mToast?.show()

        // if(!toast.getView().isShown())
        // Toast.makeText(context, message, Toast.LENGTH_LONG).show();
    }

    @SuppressLint("ShowToast")
    fun toast(context: Context?, message: Int) {
        if (context == null) return
        if (mToast != null) mToast!!.cancel()
        if (context.resources.configuration.screenLayout and
            Configuration.SCREENLAYOUT_SIZE_MASK ==
            Configuration.SCREENLAYOUT_SIZE_LARGE
        ) {
            // on a large screen device ...
            val msg = context.getString(message)
            val biggerText = SpannableStringBuilder(msg)
            biggerText.setSpan(RelativeSizeSpan(1.35f), 0, msg.length, 0)
            mToast = Toast.makeText(context, biggerText, Toast.LENGTH_LONG)
        } else {
            mToast = Toast.makeText(context, message, Toast.LENGTH_LONG)
        }
        mToast?.show()
    }

    fun setTintColor(imageView: ImageView?, color: Int) {
        if (imageView == null) return
        imageView.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    fun hideKeyboard(context: Context, view: View?) {
        // Check if no view has focus:
        try {
            if (view != null) {
                val inputManager =
                    context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                inputManager.hideSoftInputFromWindow(
                    view.windowToken,
                    InputMethodManager.HIDE_NOT_ALWAYS
                )
            }
        } catch (e: NullPointerException) {
        }
    }

    fun setProgressStyle(mContext: Context?, progressBar: ProgressBar, color: Int) {
        progressBar.isIndeterminate = true
        progressBar.indeterminateDrawable.setColorFilter(color, PorterDuff.Mode.MULTIPLY)
    }

    fun checkNullValue(value: String?): Boolean {
        return value != null && !value.equals(
            "null",
            ignoreCase = true
        ) && value != ""
    }

    fun isValueNull(appCompatEditText: AppCompatEditText?): Boolean {
        return if (appCompatEditText == null) true else isValueNull(appCompatEditText.text.toString())
    }

    fun getText(appCompatEditText: AppCompatEditText): String {
        return appCompatEditText.text.toString()
    }

    private fun isValueNull(value: String?): Boolean {
        return value != null && !value.trim { it <= ' ' }
            .equals("null", ignoreCase = true) && value.trim { it <= ' ' } != ""
    }

    fun getScreenWidth(context: Context): Int {
        val metrics = context.resources.displayMetrics
        return metrics.widthPixels
        // int height = metrics.heightPixels;
    }

    fun getScreenHeight(context: Context): Int {
        val metrics = context.resources.displayMetrics
        return metrics.heightPixels
        // int height = metrics.heightPixels;
    }

    /**
     * Check weather the Internet connection is available
     *
     * @param context
     * @return
     */
    fun isInternetConnectionAvailable(context: Context): Boolean {
        return try {
            val connec =
                context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
            val wifi = connec.getNetworkInfo(ConnectivityManager.TYPE_WIFI)
            val mobile = connec.getNetworkInfo(ConnectivityManager.TYPE_MOBILE)
            wifi != null && wifi.isConnected || mobile != null && mobile.isConnected
        } catch (e: NullPointerException) {
            //Logger.e(TAG, Log.getStackTraceString(e));
            false
        }
    }

    fun giveTintEffect(imageView: ImageView?, color: Int) {
        imageView?.drawable?.setColorFilter(color, PorterDuff.Mode.SRC_ATOP)
    }

    /**
     * Returns MAC address of the given interface name.
     *
     * @param interfaceName eth0, wlan0 or NULL=use first interface
     * @return mac address or empty string
     */
    fun getMACAddress(context: Context, interfaceName: String?): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                if (interfaceName != null) {
                    if (!intf.name.equals(interfaceName, ignoreCase = true)) continue
                }
                val mac = intf.hardwareAddress ?: return ""
                val buf = StringBuilder()
                for (aMac in mac) buf.append(String.format("%02X:", aMac))
                if (buf.isNotEmpty()) buf.deleteCharAt(buf.length - 1)
                return buf.toString()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        } // for now eat exceptions
        return "02:00:00:00:00:00"

    }

   /* fun getMACAddress(context: Context): String {
        var macAddress = "02:00:00:00:00:00"
        // val admin = DeviceAdminReceiver()
       // val admin = AppDeviceAdminReceiver()
        val devicePolicyManager: DevicePolicyManager = admin.getManager(context)
        val name1: ComponentName = admin.getWho(context)
        *//* if (devicePolicyManager.isDeviceOwnerApp(context.packageName)) {
             Log.d("DataLogin Name : ", "This app is set up as the device owner. Show the main features.")
         }else{
             Log.d("DataLogin Name : ", "This app is not set up as the device owner. Show instructions.")
            *//**//* The app is not set up as the device owner of this device. Use NfcProvisioning sample to set
                    up this app as the device owner of this device (This requires factory resetting the device).*//**//*
        }*//*

        Log.d("DataLogin Name : ", "admin : " + Gson().toJson(devicePolicyManager.activeAdmins))
        Log.d("DataLogin Name : ", "" + name1)

        if (devicePolicyManager.isAdminActive(name1)) {
            Log.d("DataLogin", "Is device owner app: " + devicePolicyManager.isDeviceOwnerApp(context.packageName))
//            //devicePolicyManager.setProfileEnabled(name1)
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                try {
                    macAddress = devicePolicyManager.getWifiMacAddress(name1)!!
                } catch (e: Exception) {
                    macAddress = "" + e.message
                }
            } else {
                macAddress = getMACAddress(context, "wlan0")
            }
            Log.d("DataLogin ", "isAdmin macAddress :$macAddress")
        } else {
            macAddress = getMACAddress(context, "wlan0")
            Log.d("DataLogin  ", "macAddress:$macAddress")
        }
        return macAddress
    }*/

    /**
     * Get IP address from first non-localhost interface
     *
     * @param useIPv4 true=return ipv4, false=return ipv6
     * @return address or empty string
     */
    fun getIPAddress(useIPv4: Boolean): String {
        try {
            val interfaces: List<NetworkInterface> =
                Collections.list(NetworkInterface.getNetworkInterfaces())
            for (intf in interfaces) {
                val addrs: List<InetAddress> = Collections.list(intf.inetAddresses)
                for (addr in addrs) {
                    if (!addr.isLoopbackAddress) {
                        val sAddr = addr.hostAddress
                        //boolean isIPv4 = InetAddressUtils.isIPv4Address(sAddr);
                        val isIPv4 = sAddr.indexOf(':') < 0
                        if (useIPv4) {
                            if (isIPv4) return sAddr
                        } else {
                            if (!isIPv4) {
                                val delim = sAddr.indexOf('%') // drop ip6 zone suffix
                                return if (delim < 0) sAddr.uppercase(Locale.getDefault()) else sAddr.substring(
                                    0,
                                    delim
                                ).uppercase(Locale.getDefault())
                            }
                        }
                    }
                }
            }
        } catch (ignored: Exception) {
        } // for now eat exceptions
        return ""
    }

    fun isLocationEnabled(context: Context): Boolean {
        var locationMode = 0
        var locationProviders: String
        locationMode = try {
            Settings.Secure.getInt(context.contentResolver, Settings.Secure.LOCATION_MODE)
        } catch (e: SettingNotFoundException) {
            e.printStackTrace()
            return false
        }
        return locationMode != Settings.Secure.LOCATION_MODE_OFF
    }

    fun getWifiSSID(activity: Activity): String {
        val wifiManager =
            activity.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        val info = wifiManager.connectionInfo
        return info.ssid
    }

    private var progressDialog: ProgressDialog? = null
    fun showProgress(message: String?, activity: Activity?) {
        progressDialog = ProgressDialog(activity)
        progressDialog!!.setMessage(message ?: "Loading...")
        progressDialog!!.setCancelable(false)
        progressDialog!!.setProgressStyle(ProgressDialog.STYLE_SPINNER)
        progressDialog!!.show()
    }

    fun checkProgressOpen(): Boolean {
        return progressDialog != null && progressDialog!!.isShowing
    }

    fun cancelProgress() {
        if (progressDialog != null && progressDialog!!.isShowing) {
            progressDialog!!.cancel()
            progressDialog = null
        }
    }




    fun locationDialog(context: Context) {
        //  dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
        if (VaultLockerApp.mLocationDialog == null) {
            VaultLockerApp.mLocationDialog = Dialog(context)
        }

        /* if (Utils.isServiceRunning(context, MyJobService::class.java)) {
             //stop MyJobService
             Log.d("classTag", "stop service")
             context.stopService(Intent(context, MyJobService::class.java))
         }*/

    //    startAlarm(context, 2)
        //send notification
        setNotification(context, 101, "Bluetooth", "Please turn on bluetooth")

        VaultLockerApp.mLocationDialog?.setCancelable(false)
        VaultLockerApp.mLocationDialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))
        VaultLockerApp.mLocationDialog?.setContentView(R.layout.alert_dialog)
        val tvON = VaultLockerApp.mLocationDialog?.findViewById<View>(R.id.tvOk) as TextView
        tvON.setOnClickListener {
            //start service
            // Utils.scheduleJob(context)
            //Turn on bluetooth
          //  LocationManager.EXTRA_LOCATION_ENABLED


            val locationRequest: LocationRequest = LocationRequest.create()
            locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
            locationRequest.setInterval(10000)
            locationRequest.setFastestInterval(10000 / 2)

            val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()

            locationSettingsRequestBuilder.addLocationRequest(locationRequest)
            locationSettingsRequestBuilder.setAlwaysShow(true)

            val settingsClient = LocationServices.getSettingsClient(context)
            val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())
            task.addOnSuccessListener {
                Toast.makeText(context, "Location settings (GPS) is ON.", Toast.LENGTH_LONG).show()
                Log.e(TAG,"Location settings (GPS) is ON.")
            }


            /*     val intent= Intent()
                 intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                 //   i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                 //  i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                 intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                 intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                 intent.putExtra(Constants.PARAM_PACKAGE_NAME, context.packageName)
                 context.startActivity(intent)
             */

            //stop  alarm
          //  stopAlarm(context)

            //clear notification
            clearNotification(context, 101)

            VaultLockerApp.mLocationDialog?.dismiss()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            VaultLockerApp.mLocationDialog?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }
        VaultLockerApp.mLocationDialog?.show()
    }

 /*   fun startAlarm(context: Context, playRing: Int) {
        // status 1 FirstLogin
        // status 2 bluetooth
        // status 3 exit Beacon detect

        //  if (VaultLockerApp.mRingtone == null) {
        when (playRing) {
            1 -> {
                VaultLockerApp.mRingtone = MediaPlayer.create(
                    context,
                    R.raw.alert_login
                )
            }
            2 -> {
                VaultLockerApp.mRingtone = MediaPlayer.create(
                    context,
                    R.raw.alert_bluetooth
                )
            }
            3 -> {
                VaultLockerApp.mRingtone = MediaPlayer.create(
                    context,
                    R.raw.alert_ring
                )
            }
        }
        VaultLockerApp.mRingtone?.setVolume(100f, 100f)
        //  }

        if (VaultLockerApp.mRingtone != null && VaultLockerApp.mRingtone!!.isPlaying) {
            VaultLockerApp.mRingtone!!.stop()
            // VaultLockerApp.mRingtone!!.release()
        }
        try {
            VaultLockerApp.mRingtone?.setOnPreparedListener { mp ->
                VaultLockerApp.mRingtone?.setVolume(100f, 100f)
                mp.isLooping = true
                mp.start()
            }
            VaultLockerApp.mRingtone?.prepareAsync()
        } catch (e: java.lang.Exception) {
            Log.d("MyJobService", "restart Alarm11 Exception :  " + e.message)
            VaultLockerApp.mRingtone?.setVolume(100f, 100f)
            VaultLockerApp.mRingtone?.isLooping = true
            VaultLockerApp.mRingtone?.start()
            e.printStackTrace()
        }
    }

    fun stopAlarm(context: Context) {
        Log.d("MyJobService", "ALARM OFF")
        if (VaultLockerApp.mRingtone == null) {
            VaultLockerApp.mRingtone = MediaPlayer.create(
                context,
                R.raw.alert_ring
                // RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)
            )
            VaultLockerApp.mRingtone?.setVolume(100f, 100f)
        }
        try {
            if (VaultLockerApp.mRingtone != null && VaultLockerApp.mRingtone!!.isPlaying) {
                VaultLockerApp.mRingtone!!.stop()
                // VaultLockerApp.mRingtone!!.release()
            }
        } catch (e: Exception) {
            Log.d("MyJobService", "stopAlarm Exception :  " + e.message)
        }
    }
*/
    fun setNotification(context: Context, id: Int, title: String, msg: String) {
        //wakeUpScreen()
        val notificationBuilder: NotificationCompat.Builder = NotificationCompat.Builder(context)
            .setSmallIcon(R.mipmap.ic_launcher)
            .setContentTitle(title)
            .setContentText(msg)
            .setOngoing(true)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        val notificationIntent = Intent(context, MainActivity::class.java)
        val contentIntent = PendingIntent.getActivity(
            context, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        notificationBuilder.setContentIntent(contentIntent)
        // Add as notification
        val notificationManager =
            context.getSystemService(JobService.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(id, notificationBuilder.build())

    }

    fun clearNotification(context: Context, id: Int) {
        val notificationManager =
            context.getSystemService(JobService.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.cancel(id)
    }


}