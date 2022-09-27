package com.example.socketexample.Service

import android.annotation.TargetApi
import android.app.ActivityManager
import android.app.Dialog
import android.app.Service
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.ContentValues.TAG
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.content.IntentSender
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.location.Location
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
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
import androidx.core.content.ContextCompat.startActivity
import com.example.socketexample.Interface.ConnectivityReceiverListener
import com.example.socketexample.R
import com.example.socketexample.Utillity.Utility
import com.example.socketexample.VaultLockerApp
import com.google.android.gms.common.api.ResolvableApiException
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.LocationSettingsRequest
import com.google.android.gms.location.LocationSettingsResponse
import com.google.android.gms.tasks.Task
import java.util.*
import android.location.LocationManager as LocationManager1


class BootReceiver: BroadcastReceiver() {
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onReceive(context: Context, intent: Intent) {
        val action = intent.action
        val state: Int
            when (action) {
                Intent.ACTION_BOOT_COMPLETED -> {

                    SocketBackgroundService.startService(context, "Start Service...")
                    Toast.makeText(context, "afert Boot Service is running...", Toast.LENGTH_LONG)
                        .show()
                    Log.e("BootReceiver", "afert Boot Service is running...")
                  /*  if (connectivityReceiverListener != null) {
                        connectivityReceiverListener!!.onNetworkConnectionChanged(
                            isConnectedOrConnecting(context)
                        )
                    }*/

                }

                Intent.ACTION_REBOOT -> {

                    SocketBackgroundService.stopService(context)
                    SocketBackgroundService().createNotification()
                    Log.e("Service", " ReBoot Service is stop...")

                }


      /*      LocationManager.MODE_CHANGED_ACTION->{
                    val locationManager =
                        context.getSystemService(LOCATION_SERVICE) as LocationManager
                    val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                       when(isGpsEnabled){
                           true ->{
                               Log.e("BroadcastActions", "Gps is on")
                           }
                           false -> {
                               // open Location dialog
                              *//* if (VaultLockerApp.mLocationDialog != null
                                   && !VaultLockerApp.mLocationDialog!!.isShowing
                               ) {
                                   Utility.locationDialog(context)
                               }
*//*
                               GpsLocationDialog("Please turn on location","Ok",context)
                               Log.e("BroadcastActions", "Gps is off")
                           }
                       }


                val mLocationManager = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
                val locationListener: android.location.LocationListener = object :
                    android.location.LocationListener {
                    override fun onLocationChanged(location: Location) {
                        // calling listener to inform that updated location is available
                    }
                    override fun onProviderEnabled(provider: String) {}
                    override fun onProviderDisabled(provider: String) {}
                    override fun onStatusChanged(provider: String, status: Int, extras: Bundle) {
                        when(status){
                            0 ->{
                                Log.e("BroadcastActions", "Gps is off")
                            }
                            1->{
                                Log.e("BroadcastActions", "Gps is on")
                            }
                        }
                    }

                }
                }*/
            }




            if (connectivityReceiverListener != null) {
                connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context))
            }

        statusCheck(context)

    }
    private fun GpsLocationDialog(message: String, okButtonText: String,context: Context) {
//        VaultLockerApp.preferenceData!!.isDialogShowing = true

        Log.e("Service","GpsLocation")

        val dialog = Dialog(context)
        dialog.setCancelable(false)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT));
        dialog.setContentView(R.layout.alert_dialog);
        val tvTitle = dialog.findViewById<View>(R.id.tvTitle1) as TextView
        val tvMsg = dialog.findViewById<View>(R.id.tvMsg) as TextView
        val tvOk = dialog.findViewById<Button>(R.id.tvOk) as TextView
        // tvTitle.text = "Device Locked!!"
        tvTitle.text = "Location Permission!"
        tvMsg.text = message
        if (!TextUtils.isEmpty(okButtonText)) {
            tvOk.text = okButtonText
        }

        tvOk.setOnClickListener {
            // Utility.stopAlarm(this)
            Log.e(TAG,"--------------------->okBtn Click")
            // val audioManager = getSystemService(AUDIO_SERVICE) as AudioManager
            //    audioManager.setStreamVolume(AudioManager.STREAM_MUSIC, 100, 0)


            if (!isAppIsInBackground(context)) {
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
                    VaultLockerApp.preferenceData?.isDialogShowing = false

                    Toast.makeText(context, "Location settings (GPS) is ON.", Toast.LENGTH_LONG).show()
                    Log.e(TAG,"Location settings (GPS) is ON.")
                    dialog.dismiss()

                }

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

            }else{
                dialog.dismiss()
                Log.e(TAG,"else --->")
            }
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            dialog.window?.setType(WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY)
        }
        dialog.show()
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
        return isInBackground
    }
    fun statusCheck(context: Context) {

        val manager = context.getSystemService(LOCATION_SERVICE) as LocationManager1?
        if (!manager!!.isProviderEnabled(LocationManager1.GPS_PROVIDER)) {
            GpsLocationDialog("Please turn on location","Ok",context)
         //   buildAlertMessageNoGps(context)
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

            Log.e("alert","--boot-->Ok")
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