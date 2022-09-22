package com.example.socketexample.Service

import android.Manifest
import android.content.DialogInterface
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.PersistableBundle
import android.provider.Settings
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat.startActivity
import com.example.socketexample.R

class Comman :AppCompatActivity(){


    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        super.onCreate(savedInstanceState, persistentState)

        if (checkPermissions()){

        }else{
            showSettingsAlert()
        }
    }

companion object{
    const val PACKAGE_NAME = "com.example.socketexample"
    const val RESULT_DATA_KEY = "$PACKAGE_NAME.RESULT_DATA_KEY"
}

    fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_BACKGROUND_LOCATION) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }


    fun showSettingsAlert() {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(this)

        // Setting Dialog Title
        alertDialog.setTitle("Error!")

        // Setting Dialog Message
        alertDialog.setMessage("Please ")

        // On pressing Settings button
        alertDialog.setPositiveButton(
            resources.getString(R.string.button),
            DialogInterface.OnClickListener { dialog, which ->
                val intent = Intent(
                    Settings.ACTION_LOCATION_SOURCE_SETTINGS
                )
                startActivity(intent)
            })
        alertDialog.show()
    }
}