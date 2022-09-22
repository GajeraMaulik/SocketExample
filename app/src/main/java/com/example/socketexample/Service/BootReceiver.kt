package com.example.socketexample.Service

import android.annotation.TargetApi
import android.bluetooth.BluetoothAdapter
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Context.LOCATION_SERVICE
import android.content.Intent
import android.location.LocationManager
import android.net.ConnectivityManager
import android.os.Build
import android.provider.Settings
import android.util.Log
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.core.content.ContextCompat.startActivity
import com.example.socketexample.Interface.ConnectivityReceiverListener
import com.example.socketexample.Utillity.Utility
import com.example.socketexample.VaultLockerApp
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
                    if (connectivityReceiverListener != null) {
                        connectivityReceiverListener!!.onNetworkConnectionChanged(
                            isConnectedOrConnecting(context)
                        )
                    }

                }

                Intent.ACTION_REBOOT -> {

                    SocketBackgroundService.stopService(context)
                    SocketBackgroundService().createNotification()
                    Log.e("Service", " ReBoot Service is stop...")

                }

            LocationManager.MODE_CHANGED_ACTION->{
                    val locationManager =
                        context.getSystemService(LOCATION_SERVICE) as LocationManager
                    val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                       when(isGpsEnabled){
                           true ->{
                               Log.d("BroadcastActions", "Gps is on")
                           }
                           false -> {
                               // open Location dialog
                               if (VaultLockerApp.mLocationDialog != null
                                   && !VaultLockerApp.mLocationDialog!!.isShowing
                               ) {
                                   Utility.locationDialog(context)
                               }

                               Log.d("BroadcastActions", "Gps is off")
                           }
                       }

                }


            }
           // statusCheck(context)


            if (connectivityReceiverListener != null) {
                connectivityReceiverListener!!.onNetworkConnectionChanged(isConnectedOrConnecting(context))
            }


       // statusCheck(context)

    }
    fun statusCheck(context: Context) {

        val manager = context.getSystemService(LOCATION_SERVICE) as LocationManager1?
        if (!manager!!.isProviderEnabled(LocationManager1.GPS_PROVIDER)) {
            buildAlertMessageNoGps(context)
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