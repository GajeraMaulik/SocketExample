package com.example.socketexample.Service

import android.Manifest
import android.app.*
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.IntentSender.SendIntentException
import android.content.pm.PackageManager
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.util.Log.d
import android.util.Log.i
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.example.socketexample.Interface.ConnectivityReceiverListener
import com.example.socketexample.Interface.Constants
import com.example.socketexample.MainActivity
import com.example.socketexample.R
import com.example.socketexample.Utillity.Utility
import com.example.socketexample.Utillity.Utility.clearNotification
import com.example.socketexample.Utillity.Utility.isAppIsInBackground
import com.example.socketexample.VaultLockerApp
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.*
import com.google.android.gms.location.LocationServices.getFusedLocationProviderClient
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.tasks.Task
import java.util.*


class SocketBackgroundService : Service(), ConnectivityReceiverListener {

    private val CHANNEL_ID = "ForegroundService Kotlin"
    private lateinit var mFusedLocationClient: FusedLocationProviderClient
    lateinit var notificationManager: NotificationManager
    lateinit var notificationChannel: NotificationChannel
    lateinit var builder: Notification.Builder
     var dialog :Dialog?= null
    lateinit var locationManager : LocationManager
    private val permissionId = 2
    var TAG = "Socket Service"
    var context = Activity()

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


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate() {
        super.onCreate()
        Log.e(TAG, "Service is Creating")

        d(TAG, "The service has been created".toUpperCase())

        mFusedLocationClient = getFusedLocationProviderClient(this)

        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (dialog == null){
            dialog= Dialog(this@SocketBackgroundService)

        }


         locationManager = getSystemService(LOCATION_SERVICE) as LocationManager

        if(checkForInternet(this)){
            Toast.makeText(this, "Your internet connection was restored.", Toast.LENGTH_LONG).show()
            Log.e(TAG, "isConnected :: Your internet connection was restored.")

        }else{
            Toast.makeText(this, "You are currently offline", Toast.LENGTH_LONG).show()
            Log.e(TAG, "!isConnected :: You are currently offline")
        }


        if (!foregroundServiceRunning()) {

           if (!isAppIsInBackground(baseContext)){
                val serviceIntent = Intent(
                    this,
                    SocketBackgroundService::class.java
                )
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(serviceIntent)
                    //GpsLocationDialog("Please turn on location","Ok")
                    createNotification()
                }else{
                    startForegroundService(serviceIntent)
                    //GpsLocationDialog("Please turn on location","Ok")
                    createNotification()
                }
            }
        }

    }
    override fun onBind(p0: Intent?): IBinder? = null

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        super.onStartCommand(intent, flags, startId)

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
        return START_STICKY
    }


    // Gps Location Dialog
    fun GpsLocationDialog(title:String,message: String, okButtonText: String) {
     //   VaultLockerApp.preferenceData?.isDialogShowing = true
        !dialog!!.isShowing
        Log.e(TAG,"service : GpsLocation dialod show")

        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog?.setContentView(R.layout.alert_dialog);
        val tvTitle = dialog?.findViewById<View>(R.id.tvTitle1) as TextView
        val tvMsg = dialog?.findViewById<View>(R.id.tvMsg) as TextView
        val tvOk = dialog?.findViewById<Button>(R.id.tvOk) as TextView
        // tvTitle.text = "Device Locked!!"
        tvTitle.text = title
        tvMsg.text = message
        if (!TextUtils.isEmpty(okButtonText)) {
            tvOk.text = okButtonText
        }

        tvOk.setOnClickListener {
            // Utility.stopAlarm(this)
            Log.e(TAG,"--------------------->okBtn Click")
            // val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            //    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)

          //  VaultLockerApp.preferenceData?.isDialogShowing = false
            !dialog!!.isShowing
            Log.e(TAG,"----dialog--------------->${isAppIsInBackground(this@SocketBackgroundService)}")
            if (isAppIsInBackground(this@SocketBackgroundService)) {
                Log.e("Service", "--->Please turn on location ")

                val locationRequest: LocationRequest = LocationRequest.create()
                locationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                locationRequest.setInterval(10000)
                locationRequest.setFastestInterval(10000 / 2)

                val locationSettingsRequestBuilder = LocationSettingsRequest.Builder()

                locationSettingsRequestBuilder.addLocationRequest(locationRequest)
                locationSettingsRequestBuilder.setAlwaysShow(true)

                val settingsClient = LocationServices.getSettingsClient(this)
                val task: Task<LocationSettingsResponse> = settingsClient.checkLocationSettings(locationSettingsRequestBuilder.build())
                task.addOnSuccessListener {

                    dialog?.dismiss()
                    Toast.makeText(this, "Location settings (GPS) is ON.", Toast.LENGTH_LONG).show()
                    Log.e(TAG,"Location settings (GPS) is ON.")

                    Thread{
                      //  when(true) {
                            try {
                                UpdateLocation()
                                Thread.sleep(3000)
                            } catch (e: Exception) {
                                e.printStackTrace()
                            }
                       // }
                    }.start()
                }
                 dialog?.dismiss()

                task.addOnFailureListener() { e -> //  dialog.show()

                    if (e is ResolvableApiException) {
                        // Location settings are not satisfied, but this can be fixed
                        // by showing the user a dialog.
                        try {
                            // Show the dialog by calling startResolutionForResult(),
                            // and check the result in onActivityResult().
                            //e.startResolutionForResult(context,REQUEST_CHECK_SETTINGS)
                            val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                            i.addCategory(Intent.CATEGORY_DEFAULT);
                            // i.setData(Uri.parse("package:$packageName"));
                            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                            i.putExtra(Constants.PARAM_PACKAGE_NAME, packageName)
                            startActivity(i)

                            Log.e(TAG, "GPS Off")


                        } catch (e: SendIntentException) {
                            e.printStackTrace()
                        }
                    }
                }

            }else{
                dialog?.dismiss()
             //   UpdateLocation()
                Log.e(TAG, "--> turn on location true")
            }

          //  dialog.dismiss()
            //  Log.e(TAG,"else --->")
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }
        if (!isLocationEnabled()) {
            dialog?.show()
        }else{
            dialog?.dismiss()
        }
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

    fun statusCheck(context: Context) {
        val intent= Intent()
        val enabled = intent.getBooleanExtra("enabled", isLocationEnabled())

        Log.e(TAG,"before:  $enabled")
        if(enabled){
            dialog?.dismiss()

            Log.e(TAG,"if $enabled")
            Toast.makeText(context, "GPS false : $enabled", Toast.LENGTH_SHORT).show()

        }else{
            Log.e(TAG,"else $enabled")
            if (!dialog?.isShowing!!){
                GpsLocationDialog("Location Permission","Please turn on location","Ok")

                Toast.makeText(context, "GPS true : $enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }


    override fun onStart(intent: Intent?, startId: Int) {
        super.onStart(intent, startId)
    /*    Log.e(TAG,"--------->start")
        val location = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(BootReceiver(),location)

        val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val gpsLocation: IntentFilter = IntentFilter(LocationManager.GPS_PROVIDER)
        registerReceiver(BootReceiver(),location)


        Log.e(TAG,"------service---------$location")*/
    }


    // Notification
    @RequiresApi(Build.VERSION_CODES.O)
    fun createNotification() {

        Log.e(TAG,"--------->start")

            //    try {
                    //  Toast.makeText(this,"Service is Running..",Toast.LENGTH_LONG).show()
                    if (checkPermission()) {
                      //  Log.e("Service", " if Service is running try ...")
                        if (!isLocationEnabled()) {

                            Log.e("Service", " location On false")
                            if (!dialog?.isShowing!!) {

                                val handle = Handler()

                                handle.postDelayed({
                                    GpsLocationDialog("Location Permission","Please turn on location","Ok")
                                    Log.e(TAG,"handl dilog show")
                                    //        statusCheck(context)
                                                   },1500)
                                Log.e(TAG, "Service dialog open")
                            }
                        } else {
                            Log.e(TAG, "Please turn on location true")
                               UpdateLocation()

                        }
                    }
             //   } catch (e: InterruptedException) {
              //      e.printStackTrace()
              //  }


        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_ID, NotificationManager.IMPORTANCE_LOW)

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
        Log.e(TAG,"GPS : ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)} "+ "Network :${locationManager.isProviderEnabled(
                            LocationManager.NETWORK_PROVIDER)}")
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER

        )

    }


    // Location Permissions Check
    private fun checkPermission(): Boolean {
        val result = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
        val result1 = ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION)
        //   Log.e("Service","result  : $result " + "result1 : $result1")
        return result == PackageManager.PERMISSION_GRANTED && result1 == PackageManager.PERMISSION_GRANTED
    }


    private fun checkForInternet(context: Context): Boolean {

        // register activity with the connectivity manager service
        val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        // if the android version is equal to M
        // or greater we need to use the
        // NetworkCapabilities to check what type of
        // network has the internet connection
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            // Returns a Network object corresponding to
            // the currently active default data network.
            val network = connectivityManager.activeNetwork ?: return false

            // Representation of the capabilities of an active network.
            val activeNetwork = connectivityManager.getNetworkCapabilities(network) ?: return false

            return when {
                // Indicates this network uses a Wi-Fi transport,
                // or WiFi has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_WIFI) -> true

                // Indicates this network uses a Cellular transport. or
                // Cellular has network connectivity
                activeNetwork.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) -> true

                // else return false
                else -> false
            }
        } else {
            // if the android version is below M
            @Suppress("DEPRECATION") val networkInfo =
                connectivityManager.activeNetworkInfo ?: return false
            @Suppress("DEPRECATION")
            return networkInfo.isConnected
        }
    }

    // Internet On/Off
    private fun internetPermission(isConnected: Boolean) {
        if (!isConnected) {

            Toast.makeText(this, "You are currently offline", Toast.LENGTH_LONG).show()
            Log.e("Service", "!isConnected :: You are currently offline")

        } else {
            Toast.makeText(this, "Your internet connection was restored.", Toast.LENGTH_LONG).show()
            Log.e("Service", "isConnected :: Your internet connection was restored.")


        }
    }

    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        Log.e("Service", "isConnected :: $isConnected")
       internetPermission(isConnected)
    }

    fun isAppIsInBackground(context: Context): Boolean {
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

    private fun stopForegroundService() {
        stopForeground(true)
        stopSelf()
    }


}

