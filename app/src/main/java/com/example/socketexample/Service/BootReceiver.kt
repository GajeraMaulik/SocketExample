package com.example.socketexample.Service

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Dialog
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.text.TextUtils
import android.util.Log
import android.view.View
import android.view.WindowManager
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.*
import com.example.socketexample.Interface.ConnectivityReceiverListener
import com.example.socketexample.MainActivity
import com.example.socketexample.R
import com.example.socketexample.Utillity.UserPermission
import com.example.socketexample.Utillity.Utility
import com.example.socketexample.Utillity.Utility.showSettingsDialog
import com.example.socketexample.Utillity.Utils
import com.example.socketexample.Utillity.Utils.startService
import com.example.socketexample.VaultLockerApp.Companion.mLocationDialog
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import java.security.AllPermission
import java.util.*
import android.location.LocationManager as LocationManager1


class BootReceiver: BroadcastReceiver() {

    var TAG = "BroadcastReceiver"
     var dialog :Dialog? = null
    var userPermission: UserPermission? = null

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onReceive(context: Context, intent: Intent) {

        if(dialog == null){
            dialog = Dialog(context)
        }

        userPermission = UserPermission(MainActivity())

        val action = intent.action
        val state: Int
            when (action) {
                Intent.ACTION_BOOT_COMPLETED -> {

                    SocketBackgroundService.startService(context, "Start Service...")
                    Toast.makeText(context, "afert Boot Service is running...", Toast.LENGTH_LONG)
                        .show()
                    Log.e(TAG, "afert Boot Service is running...")
                    if (connectivityReceiverListener != null) {
                        connectivityReceiverListener!!.onNetworkConnectionChanged(
                            isConnectedOrConnecting(context)
                        )
                    }

                }

                Intent.ACTION_REBOOT -> {

                    SocketBackgroundService.stopService(context)
                    SocketBackgroundService().createNotification()
                    Log.e(TAG, " ReBoot Service is stop...")

                }

            }

         //   AllPermission(context)
          statusCheck(intent,context)

            if (connectivityReceiverListener != null) {
                connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context))
            }


    }

    @RequiresApi(Build.VERSION_CODES.Q)
    fun AllPermission(context: Context){
        if (!userPermission!!.checkAccessCoarseLocationPermission()) {
            //  showSettingsAlert()
            showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                    " 'All the Time'.We need this when the device \n" +
                    "is lost and can be located for \n" +
                    "audit purposes",context)
            //   userPermission!!.requestAccessCourseLocationPermission()
        }
        else if (!userPermission!!.checkAccessFineLocationPermission()) {
            //  showSettingsAlert()
            showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                    " 'All the Time'.We need this when the device \n" +
                    "is lost and can be located for \n" +
                    "audit purposes",context)
            //  userPermission!!.requestAccessFineLocationPermission()
            // requestLocationPermission()

        } else if (!userPermission!!.checkOverlayPermission()) {
            showOvarlayDialog(context,"Appear on Top","Please Allow Permission")
            //  userPermission!!.requestOverlayPermission()
        } else {
            if (!Utils.isServiceRunning(context, SocketBackgroundService::class.java)) {
                val intent = Intent(context, SocketBackgroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e(TAG, "Starting the service in >=26 Mode")
                    context.startForegroundService(intent)
                } else {
                    Log.e(TAG, "Starting the service in < 26 Mode")
                   context.startService(intent)
                }
            } else {

                Log.e(TAG, "AppCheckerForegroundServices already running")
                SocketBackgroundService.startService(context, "Start Service...")
           //     UpdateLocationData()
            }

            val locationManager: LocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
            if (!locationManager.isLocationEnabled) {
                if (mLocationDialog != null && !mLocationDialog!!.isShowing) {
                    /*   val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                       val location: IntentFilter = IntentFilter(LocationManager.GPS_PROVIDER)
                       registerReceiver(BootReceiver(),location)*/
                    Utility.locationDialog(context)

                }
            }else{
             Log.e(TAG," Boot : Location alredy On")
            }

        }
    }


    private fun GpsLocationDialog(message: String, okButtonText: String,context: Context) {
//        VaultLockerApp.preferenceData!!.isDialogShowing = true

        Log.e("BootRecevier","GpsLocation")

     // val   dialog = Dialog(context)
        dialog?.setCancelable(false)
        dialog?.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog?.setContentView(R.layout.alert_dialog);
        val tvTitle = dialog?.findViewById<View>(R.id.tvTitle1) as TextView
        val tvMsg = dialog?.findViewById<View>(R.id.tvMsg) as TextView
        val tvOk = dialog?.findViewById<Button>(R.id.tvOk) as TextView
        // tvTitle.text = "Device Locked!!"
        tvTitle.text = "Location Permission!"
        tvMsg.text = message
        if (!TextUtils.isEmpty(okButtonText)) {
            tvOk.text = okButtonText
        }

        tvOk.setOnClickListener {
            // Utility.stopAlarm(this)
            Log.e(TAG,"-----boot---------------->okBtn Click")
            // val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            //    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)


            if (isAppIsInBackground(context)) {
                val startTime = Calendar.getInstance().timeInMillis

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
                 //   VaultLockerApp.preferenceData?.isDialogShowing = false

                    Log.e(TAG,"------------------->${dialog?.context}")
                  //  dialog.hide()
                    dialog?.dismiss()
                    Toast.makeText(context, "Location settings (GPS) is ON.", Toast.LENGTH_LONG).show()
                    Log.e(TAG,"Location settings (GPS) is ON.")


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
                            context.startActivity(i)

                        } catch (e: IntentSender.SendIntentException) {
                            e.printStackTrace()
                        }
                    }
                    Log.e(TAG, "Failure")
                }
          //      dialog.dismiss()

            }else{
                Log.e(TAG,"dialog dissmiss")
                dialog?.dismiss()
            }
          //  dialog.dismiss()
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog?.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
            Log.e(TAG,"dialog.window overlay")
        }

        if(!Utility.isLocationEnabled(context)){
            Log.e(TAG,"dialog.show")

            dialog?.show()

       }else{
            Log.e(TAG,"dialog.hide")

            dialog?.dismiss()
       }
    }

    // Ovarlay Permission Dialog
    private fun showOvarlayDialog(context: Context,title: String,msg:String) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)

        // Setting Dialog Title
        alertDialog.setTitle(title)


        // Setting Dialog Message
        alertDialog.setMessage(msg)

        // On pressing Settings button
        alertDialog.setPositiveButton("Ok"){dialogInterface,which ->
            dialogInterface.dismiss()

            Log.e("alert","--boot-->Ok")
            userPermission!!.requestOverlayPermission()

            /* val i= Intent()
             i.setAction(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
             //   i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
             //  i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
             context.startActivity(i)*/
        }

        alertDialog.setNegativeButton("Cancel"){dialogInterface,which ->

            dialogInterface.dismiss()
        }
        //alertDialog.create()
        if (!alertDialog.show().isShowing) {
            //  Utility.startAlarm(this,1)
            alertDialog.show()
        }
    }

    // Location Enabled
    private fun isLocationEnabled(context: Context): Boolean {
        val locationManager: LocationManager = context.getSystemService(LOCATION_SERVICE) as LocationManager
        Log.e(TAG,"GPS : ${locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)} "+ "Network :${locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER)}")
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER

        )

    }

    fun isAppIsInBackground(context: Context): Boolean {
        var isInBackground = true
        val am = context.getSystemService(Service.ACTIVITY_SERVICE) as ActivityManager
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
        Log.e(TAG,"isINB $isInBackground")
        return isInBackground
    }

    fun statusCheck(intent:Intent,context: Context) {
        val enabled = intent.getBooleanExtra("enabled",isLocationEnabled(context))

        Log.e(TAG,"before:  $enabled")
        if(enabled){
            dialog?.dismiss()

            Log.e(TAG,"if $enabled")
            Toast.makeText(context, "GPS false : $enabled", Toast.LENGTH_SHORT).show()

        }else{
            Log.e(TAG,"else $enabled")
            if (!dialog?.isShowing!!){
                GpsLocationDialog("Please turn on location","Ok",context)

                Toast.makeText(context, "GPS true : $enabled", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun buildAlertMessageNoGps(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setMessage("Your GPS seems to be disabled, do you want to enable it?")
            .setCancelable(false)
            .setPositiveButton(
                "Yes"
            ) { dialog, id -> startActivity(context,Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS),null) }
            .setNegativeButton(
                "No"
            ) { dialog, id -> dialog.cancel() }
        val alert = builder.create()
        alert.show()
    }

    // Location Permission Settings open
    @TargetApi(30)
    private fun locationPermission(context: Context) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)

        // Setting Dialog Title
        alertDialog.setTitle("Location Permission")


        // Setting Dialog Message
        alertDialog.setMessage("Please turn on location")

        // On pressing Settings button
        alertDialog.setPositiveButton("Ok"){dialogInterface,which ->

            Log.e(TAG,"--boot-->Ok")
            alertDialog.setCancelable(true)
            val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
           context.startActivity(intent)
        }

        alertDialog.setNegativeButton("Cancel"){dialogInterface,which ->

            alertDialog.setCancelable(true)
        }
        alertDialog.setCancelable(true)
        //alertDialog.create()
        alertDialog.show()
    }

    private fun isConnectedOrConnecting(context: Context): Boolean {
        val connMgr = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connMgr.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnectedOrConnecting
    }


    companion object {
        var connectivityReceiverListener: ConnectivityReceiverListener? = null
    }





}