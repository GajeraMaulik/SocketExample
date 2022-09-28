package com.example.socketexample

import android.app.Application
import android.app.Dialog
import android.content.Context
import android.media.AudioManager
import android.media.MediaPlayer
import android.media.Ringtone
import android.media.RingtoneManager
import android.os.Build
import android.util.Log
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleObserver
import androidx.lifecycle.OnLifecycleEvent
import androidx.lifecycle.ProcessLifecycleOwner
import com.example.socketexample.Utillity.PreferenceData


class VaultLockerApp : Application(), LifecycleObserver {


    companion object {
          @JvmField
        var exitBeaconList: ArrayList<String> = ArrayList()
        lateinit var mLocationDialog: Dialog
        var mBeaconDialog: Dialog? = null
        var mRingtone: MediaPlayer? = null
         var preferenceData: PreferenceData? = null

    }
    override fun onCreate() {
        super.onCreate()
        ProcessLifecycleOwner.get().lifecycle.addObserver(this)
        Log.d("VaultLockerApp", "Application class created")
        mLocationDialog = Dialog(this)
        mBeaconDialog = Dialog(this)
        preferenceData = PreferenceData(this)

        /*  val audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
          audioManager.setStreamVolume(
              AudioManager.STREAM_MUSIC,
              audioManager.getStreamMaxVolume(AudioManager.STREAM_MUSIC),
              0
          )*/

        // status 1 FirstLogin
        // status 2 bluetooth
        // status 3 exit Beacon detect

       /* mRingtone = MediaPlayer.create(
            applicationContext,
            R.raw.alert_ring //Custom ringtone
            // RingtoneManager.getDefaultUri(RingtoneManager.TYPE_ALARM)  /default ringtone
        )
        mRingtone?.setVolume(100f, 100f)*/


    }



    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onAppBackgrounded() {
        //App in background
        Log.d("VaultLockerApp", "App in background")
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onAppForegrounded() {
        // App in foreground
        Log.d("VaultLockerApp", "App in foreground")
    }

}