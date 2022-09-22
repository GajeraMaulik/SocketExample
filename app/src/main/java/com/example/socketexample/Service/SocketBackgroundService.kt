package com.example.socketexample.Service

import android.Manifest
import android.Manifest.permission.ACCESS_BACKGROUND_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.TargetApi
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.media.AudioManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.Log.d
import android.util.TypedValue
import android.view.View
import android.view.WindowManager
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.app.ActivityCompat
import androidx.core.app.ActivityCompat.requestPermissions
import androidx.core.app.ActivityCompat.shouldShowRequestPermissionRationale
import androidx.core.content.ContextCompat
import com.example.socketexample.Interface.ConnectivityReceiverListener
import com.example.socketexample.Interface.Constants
import com.example.socketexample.MainActivity
import com.example.socketexample.R
import com.example.socketexample.VaultLockerApp
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import java.util.*
import android.view.Window as Window1


class SocketBackgroundService : Service(), ConnectivityReceiverListener {

    private val CHANNEL_ID = "ForegroundService Kotlin"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
    private val permissionId = 2
    var TAG = "Socket Service"
    val context = Activity()
    override fun onCreate() {
        super.onCreate()
        Log.e("Service", "Service is Creating")

        d(TAG, "The service has been created".toUpperCase())

        mFusedLocationClient = getFusedLocationProviderClient(this)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager


        if (!foregroundServiceRunning()) {
            val serviceIntent = Intent(
                this,
                SocketBackgroundService::class.java
            )

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                //  Comman()
                //  MapsActivity().checkPermissions()
                startForegroundService(serviceIntent)
                createNotification()
            }
        }


    }
    private fun locationDialog(message: String, okButtonText: String) {
        VaultLockerApp.preferenceData!!.isDialogShowing = true
        val dialog = Dialog(this)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alert_dialog);
        val tvTitle = dialog.findViewById<View>(R.id.tvTitle) as TextView
        val tvMsg = dialog.findViewById<View>(R.id.tvMsg) as TextView
        val tvOk = dialog.findViewById<View>(R.id.tvOk) as TextView
        // tvTitle.text = "Device Locked!!"
    //    tvTitle.text = "Woah there cowboy!"
        tvMsg.text = message
        if (!TextUtils.isEmpty(okButtonText)) {
            tvOk.text = okButtonText
        }

        tvOk.setOnClickListener {
           // Utility.stopAlarm(this)
            val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)

            VaultLockerApp.preferenceData!!.isDialogShowing = false
            if (isAppIsInBackground(this@SocketBackgroundService)) {
                val startTime = Calendar.getInstance().timeInMillis
                val intent= Intent()
                intent.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                //   i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                //  i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                intent.putExtra(Constants.PARAM_PACKAGE_NAME, packageName)
                startActivity(intent)
                // val endTime = Calendar.getInstance().timeInMillis
            }
            dialog.dismiss()
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }

    }

    companion object {
        fun startService(context: Context, message: String) {

            val startIntent = Intent(context, SocketBackgroundService::class.java)
            startIntent.putExtra("inputExtra", message)
            ContextCompat.startForegroundService(context, startIntent)
        }

        fun stopService(context: Context) {
            val stopIntent = Intent(context, SocketBackgroundService::class.java)
            context.stopService(stopIntent)
        }
    }

    override fun onBind(p0: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {

        /* Thread {
             while (true) {
                 Log.e("Service", "Service is running...")
                 try {
                     //  Toast.makeText(this,"Service is Running..",Toast.LENGTH_LONG).show()

                     Thread.sleep(2000)
                 } catch (e: InterruptedException) {
                     e.printStackTrace()
                 }
             }
         }.start()


         val channel = NotificationChannel(
             CHANNEL_ID,
             CHANNEL_ID,
             NotificationManager.IMPORTANCE_LOW
         )

         val notificationIntent_Close = Intent(this, SocketBackgroundService::class.java)

         val closeIntent =PendingIntent.getService(this,1, notificationIntent_Close,PendingIntent.FLAG_MUTABLE)


         getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
         val notification: Notification.Builder = Notification.Builder(this, CHANNEL_ID)
             .setContentText("Service is running")
             .setContentTitle("Service enabled")
             .addAction( R.drawable.ic_notification_close, "Close",closeIntent)
             .setSmallIcon(com.example.socketexample.R.drawable.ic_baseline_notifications_active_24)


         startForeground(1001, notification.build())*/
        createNotification()


        return super.onStartCommand(intent, flags, startId)
    }

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }

    fun foregroundServiceRunning(): Boolean {
        val activityManager = getSystemService(ACTIVITY_SERVICE) as ActivityManager
        for (service in activityManager.getRunningServices(Int.MAX_VALUE)) {
            if (SocketBackgroundService::class.java.getName() == service.service.className) {
                return true
            }
        }
        return false
    }


    override fun onDestroy() {
        super.onDestroy()
        d(TAG, "The service has been destroyed".toUpperCase())
        Toast.makeText(this, "Service destroyed", Toast.LENGTH_SHORT).show()
        Log.e(TAG, "Service Stopped")
        stopForegroundService()
        if (mFusedLocationClient != null) {
            val intent = Intent(context, SocketBackgroundService::class.java)
            val i = PendingIntent.getActivity(this, 0, intent, 0)
            mFusedLocationClient.removeLocationUpdates(i)
            Log.e(TAG, "Location Update Callback Removed")
        }


        // stopForegroundService()
    }


    // Notification
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification(): ConnectivityReceiverListener {
        Thread {
            while (true) {
                // Log.e("Service", "Service is running...")
                try {

                    //  Log.e("Service", "Service is running try ...")
                    // Toast.makeText(this,"Service is Running..",Toast.LENGTH_LONG).show()

                    if (checkPermission()) {
                        //  Log.e("Service", " if Service is running try ...")
                        // isLocationEnabled1()
                        if (isLocationEnabled()) {


                            UpdateLocation()


                        } else {
                            Log.e("Service", "Please turn on location")

                     //       locationDialog("Please turn on location","Ok")
//                            Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                            // locationPermission()
                            //  Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()
                            //val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            // startActivity(intent)
                        }
                    } else {
                        //  showSettingsAlert()
                    }


                    Thread.sleep(3000)
                } catch (e: AccessDeniedException) {

                    Log.e("null", "${e.message.toString()}")
                    e.printStackTrace()
                }
            }
        }.start()


        val channel =
            NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)

        val notificationIntent = Intent(this, MainActivity::class.java)

        val pendingIntent =
            PendingIntent.getActivity(this, 1, notificationIntent, PendingIntent.FLAG_MUTABLE)


        getSystemService(NotificationManager::class.java)
            .createNotificationChannel(channel)
        val notification: Notification.Builder = Notification.Builder(this, CHANNEL_ID)
            .setContentText("Service is running...")
            .setContentTitle("Background Location")
            .setContentIntent(pendingIntent)
            //    .addAction( R.drawable.ic_notification_close, "Close",closeIntent)
            .setSmallIcon(com.example.socketexample.R.drawable.ic_baseline_notifications_active_24)

        startForeground(1001, notification.build())

        return this

    }

    fun UpdateLocation() {
        val locationRequest: com.google.android.gms.location.LocationRequest =
            com.google.android.gms.location.LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 3000
        locationRequest.priority =
            com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY


        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED

        ) {

            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest, object : LocationCallback() {
                override fun onLocationResult(locationresult: LocationResult) {
                    super.onLocationResult(locationresult)

                    LocationServices.getFusedLocationProviderClient(this@SocketBackgroundService)
                        .removeLocationUpdates(this)
                    if (locationresult.locations != null && locationresult.locations.size > 0) {
                        val latestLocationIndex: Int = locationresult.locations.size - 1
                        val latitude: Double =
                            locationresult.locations.get(latestLocationIndex).latitude
                        val longitude: Double =
                            locationresult.locations.get(latestLocationIndex).longitude

                        val latLng = LatLng(latitude, longitude)
                        //    drawMarker(latLng)
                        val geocoder = Geocoder(this@SocketBackgroundService, Locale.getDefault())
                        val list: List<Address> =
                            geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)

                        /*  Log.e("Service", "Latitude ---> ${list[0].latitude}")
                          Log.e("Service", "Longitude ---> ${list[0].longitude}")
                          Log.e("Service", "contuny name ---> ${list[0].countryName}")
                          Log.e("Service", "Locality ---> ${list[0].locality}")*/
                        Log.e("Service", "Address  ----> ${list[0].getAddressLine(0)}")


                        val location: Location = Location("providerNA")
                        location.latitude = latitude
                        location.longitude = longitude
                        // fetchAddressFromLatLong(location)
                        //placeMarkerOnMap(latLng)

                    } else {


                    }
                }
            }, Looper.getMainLooper())

    }


    // Location Enabled
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )

    }


    // Location Permissions Check
    private fun checkPermission(): Boolean {
        val result =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 =
            ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        //   Log.e("Service","result  : $result " + "result1 : $result1")
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }

    // Internet On/Off
    private fun internetPermission(isConnected: Boolean) {
        if (!isConnected) {

            Toast.makeText(this, "You are currently offline", Toast.LENGTH_LONG).show()
            Log.e("Service", "!isConnected :: You are currently offline")
            /*   snackBar = Snackbar.make(findViewById(R.id.), "You are offline", Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
               snackBar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
               snackBar?.show()*/
        } else {
            Toast.makeText(this, "Your internet connection was restored.", Toast.LENGTH_LONG).show()
            Log.e("Service", "isConnected :: Your internet connection was restored.")

            // snackBar?.dismiss()
        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Log.e("Service", "isConnected :: $isConnected")
       internetPermission(isConnected)
    }

    private fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(ACTIVITY_SERVICE) as ActivityManager
        val runningProcesses = am.runningAppProcesses
        for (processInfo in runningProcesses) {
            if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                for (activeProcess in processInfo.pkgList) {
                    if (activeProcess == context.packageName) {
                        isInBackground = false
                    }
                }
            }
        }
        return isInBackground
    }
}