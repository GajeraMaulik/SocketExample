package com.example.socketexample

import android.Manifest
import android.Manifest.permission.ACCESS_COARSE_LOCATION
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.annotation.TargetApi
import android.app.Activity
import android.app.Dialog
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.location.*
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.Settings
import android.util.Log
import android.util.Log.d
import android.view.View
import android.widget.Toast
import androidx.annotation.NonNull
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequest
import androidx.work.WorkManager
import com.example.socketexample.Interface.ConnectivityReceiverListener
import com.example.socketexample.Service.BootReceiver
import com.example.socketexample.Service.SocketBackgroundService
import com.example.socketexample.SocketHandler.SocketCreate
import com.example.socketexample.Utillity.UserPermission
import com.example.socketexample.Utillity.Utility
import com.example.socketexample.Utillity.Utils
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationCallback
import com.google.android.gms.location.LocationResult
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.model.LatLng
import io.socket.client.Socket
import io.socket.client.Socket.*
import io.socket.emitter.Emitter
import kotlinx.android.synthetic.main.activity_main.*
import org.json.JSONObject
import java.util.*
import java.util.concurrent.TimeUnit


class MainActivity : AppCompatActivity(), ConnectivityReceiverListener {



    lateinit var mFusedLocationClient: FusedLocationProviderClient
    var alertDialog: AlertDialog.Builder? = null
    var userPermission: UserPermission? = null
    lateinit var mSocket: Socket
    var context  = Activity()
    var TAG = "MainActvity"
    private val UNIQUE_WORK_NAME = "StartMyBeaconServiceViaWorker"
    lateinit var locationManager : LocationManager
    lateinit var mLocationDialog: Dialog
    companion object{
        private const val REQUEST_LOCATION_CODE = 1
        private const val UPDATE_INTERVAL_IN_MILLISECONDS = 10000L
        private const val FASTEST_UPDATE_INTERVAL_IN_MILLISECONDS =
            UPDATE_INTERVAL_IN_MILLISECONDS / 2
    }
    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
/*
       val actionBar = supportActionBar
       actionBar!!.hide()
        actionBar?.setDisplayHomeAsUpEnabled(true)*/


        window.statusBarColor = ContextCompat.getColor(this, R.color.purple_500)


        mFusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        locationManager = getSystemService(LOCATION_SERVICE) as LocationManager
        mLocationDialog = Dialog(this)

        userPermission = UserPermission(this@MainActivity)

        if (intent.getBooleanExtra("close_app", false)) {
            Toast.makeText(this,"Please enable device admin permission",Toast.LENGTH_LONG).show()
        }

   //     startServiceViaWorker()

        startService()


        btSend.text = "Disconnect"
        tvOutput.text = "Connected"


        SocketCreate.setSocket()
        mSocket = SocketCreate.getSocket()

        mSocket.connect()
        try {

            mSocket.on(EVENT_CONNECT) {
                Log.e("Connected", "connected");
                var activeId =
                    """ {"0":{"id":"1","name":"Service Users"},"1":{"id":"2","name":"First Floor"},"2":{"id":"3","name":"New"},"3":{"id":"456","name":"Maintenance Team"}}"""
                val json = JSONObject(activeId)
                d("json", "$json")
                //   json.fromJson(,activeId)
                mSocket.emit("connectZones", json)
                mSocket.on("alert", alerts)

            }.on(EVENT_DISCONNECT) {
                Log.e("Disconnected", "Disconnected");
            }.on(EVENT_CONNECT_ERROR, onError)
            Log.e("error", "error");


        } catch (e: Error) {
            d("connect", "$e")
        }

      /*  val serviceIntent = Intent(this, SocketBackgroundService::class.java)
        startService(serviceIntent)*/

        //  mSocket.to(alerts)

        //d("print", "${mSocket.connected()}")

        //    mSocket.on("message${sender.id}", onConnect)


        btSend.setOnClickListener {
            if (mSocket.connected()) {
                mSocket.disconnect()
                tvOutput.text = "Disconnect"
                btSend.text = "Connect"

            } else {
                mSocket.connect()
                tvOutput.text = "Connect"
                btSend.text = "Disconnect"
            }
        }

      /*  btSendMessage.setOnClickListener {
            if (etMessage.text.isEmpty()) {
                Toast.makeText(this, "empty Filed", Toast.LENGTH_LONG).show()
            } else {
                //sendMessage()
            }
        }*/



/*        if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
                Log.e("alert","if")
                showSettingsAlert()
              //  ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE);
            } else {
                Log.e("alert","else")
                showSettingsAlert()
            //    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionId)
                //checkPermissions()
            }
        }else{
            getLocation()
        }*/

/*       if (!userPermission!!.checkAccessCoarseLocationPermission()) {
           showSettingsAlert()
              //userPermission!!.requestAccessCourseLocationPermission()
        } else if (!userPermission!!.checkAccessFineLocationPermission()) {
            showSettingsAlert()
             // userPermission!!.requestAccessFineLocationPermission()
            // requestLocationPermission()

        } else if (!userPermission!!.checkOverlayPermission()) {
            userPermission!!.requestOverlayPermission()
        } else {

                Log.e(TAG, "AppCheckerForegroundServices already running")

           getLocation()
        }*/




        if (!userPermission!!.checkAccessCoarseLocationPermission()) {
            showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                    " 'All the Time'.We need this when the device \n" +
                    "is lost and can be located for \n" +
                    "audit purposes")
            //  userPermission!!.requestAccessCourseLocationPermission()
        } else if (!userPermission!!.checkAccessFineLocationPermission()) {
            showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                    " 'All the Time'.We need this when the device \n" +
                    "is lost and can be located for \n" +
                    "audit purposes")
            //  userPermission!!.requestAccessFineLocationPermission()
            // requestLocationPermission()

        } else if (!userPermission!!.checkOverlayPermission()) {
            showOvarlayDialog(this,"Appear on Top","Please Allow Permission")
        } else if (!userPermission!!.checkBackgroundLocationPermission()){
            userPermission!!.requestBackgroundLocationPermission()
        }else {
            if (!Utils.isServiceRunning(this, SocketBackgroundService::class.java)) {
                val intent = Intent(this, SocketBackgroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e(TAG, "Starting the service in >=26 Mode")
                    startForegroundService(intent)

                        UpdateLocationData()

                } else {
                    Log.e(TAG, "Starting the service in < 26 Mode")
                    startForegroundService(intent)
                }
            } else {
              //  val intent = Intent(this, SocketBackgroundService::class.java)
                getLocation()
               // startService(intent)
                Log.e(TAG, "AppCheckerForegroundServices already running")
            }

        }

        // Btn
        btn_location.setOnClickListener {

 /*              if (ContextCompat.checkSelfPermission(this, ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, ACCESS_COARSE_LOCATION)) {
                    Log.e("alert","btn ->if")
                  //  getLocation()
                 //   showSettingsAlert()
                    showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                            " 'All the Time'.We need this when the device \n" +
                            "is lost and can be located for \n" +
                            "audit purposes")
              //      ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.ACCESS_BACKGROUND_LOCATION), permissionId);
                } else {
                    Log.e("alert","btn -> else")
                    //checkPermissions()
                  //  showSettingsAlert()
                    showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                            " 'All the Time'.We need this when the device \n" +
                            "is lost and can be located for \n" +
                            "audit purposes")
                //    ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), permissionId);

                }
            }else{
                Log.e(TAG,"btn -> else else")

                   Log.e(TAG, "btn -> AppCheckerForegroundServices already running")
                  getLocation()
            }*/

            if (!userPermission!!.checkAccessCoarseLocationPermission()) {
                showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                        " 'All the Time'.We need this when the device \n" +
                        "is lost and can be located for \n" +
                        "audit purposes")
                //  userPermission!!.requestAccessCourseLocationPermission()
            } else if (!userPermission!!.checkAccessFineLocationPermission()) {
                showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                        " 'All the Time'.We need this when the device \n" +
                        "is lost and can be located for \n" +
                        "audit purposes")
                //  userPermission!!.requestAccessFineLocationPermission()
                // requestLocationPermission()

            } else if (!userPermission!!.checkOverlayPermission()) {
                showOvarlayDialog(this,"Appear on Top","Please Allow Permission")
            } else {
                if (!Utils.isServiceRunning(this, SocketBackgroundService::class.java)) {
                    val intent = Intent(this, SocketBackgroundService::class.java)
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                        Log.e(TAG, "Starting the service in >=26 Mode")
                        startForegroundService(intent)

                        var hadler = Handler()


                           UpdateLocationData()

                    } else {
                        Log.e(TAG, "Starting the service in < 26 Mode")
                        startService(intent)
                    }
                } else {
                   getLocation()
                    Log.e(TAG, "AppCheckerForegroundServices already running")
                }

            }
        }
    }



    // method for starting the service
    fun startService() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            // check if the user has already granted
            // the Draw over other apps permission
            if (Settings.canDrawOverlays(this)) {
                // start the service based on the android version
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    startForegroundService(Intent(this, SocketBackgroundService::class.java))
                } else {
                    startService(Intent(this, SocketBackgroundService::class.java))
                }
            }
        } else {
            startService(Intent(this, SocketBackgroundService::class.java))
        }
    }


    // Socket connetion error
    val onError = Emitter.Listener { args ->

        val error = args[0] as io.socket.engineio.client.EngineIOException;
        Log.e("Error", "" + error.cause?.message);

    }


    // alerts
    val alerts = Emitter.Listener { args ->


        val data = args[0].toString()

        d("data", "$data")
        SharePref.save(this, "alertdata", data)
        //  val jsonObject = JSONObject(args[0].toString())
        //   val data = SharePref.getJsonObject(this,"alertdata")

        //  d("alert", "${data.toString()}")


    }

    override fun onRequestPermissionsResult(requestCode: Int, @NonNull permissions: Array<out String>,@NonNull grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        /*   when(requestCode){
         REQUEST_LOCATION_CODE -> if (grantResults.size > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
             // isLocationPermissionGranted()
             if(!isLocationEnabled()){
                 startService()
             }else{
                 UpdateLocationData()
             }
             Log.e("alert","onRequest---->Permission Granted")
             Toast.makeText(this, "Permission Granted", Toast.LENGTH_SHORT).show()
         }

         else -> {

             Log.e("alert","onRequest---->Permission Denied")
             if(!isLocationEnabled()){
                 startService()
             }
             Toast.makeText(this, "Permission Denied", Toast.LENGTH_SHORT).show()
         }
     }*/

        if (requestCode == REQUEST_LOCATION_CODE) {
            var foreground = false
            var background = false
            for (i in permissions.indices) {
                if (permissions[i].equals(Manifest.permission.ACCESS_COARSE_LOCATION, ignoreCase = true)
                ) {
                    //foreground permission allowed
                    if (grantResults[i] >= 0) {
                        foreground = true
                        Toast.makeText(applicationContext, "Foreground location permission allowed", Toast.LENGTH_SHORT).show()
                        Log.e(TAG,"Foreground location permission allowed")
                        startService()
                        getLocation()
                        continue
                    } else {
                        Toast.makeText(applicationContext, "Location Permission denied", Toast.LENGTH_SHORT).show()
                        Log.e(TAG,"Location Permission denied")
                        break
                    }
                }
                if (permissions[i].equals(
                        Manifest.permission.ACCESS_BACKGROUND_LOCATION,
                        ignoreCase = true
                    )
                ) {
                    if (grantResults[i] >= 0) {
                        foreground = true
                        background = true

                        Toast.makeText(applicationContext, "Background location location permission allowed", Toast.LENGTH_SHORT).show()
                        Log.e(TAG,"Background location location permission allowed")
                        startService()

                    } else {
                      //  SocketBackgroundService().GpsLocationDialog("Location Permission!","Please turn on location","Ok")
                       startService()

                        val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                        val location: IntentFilter = IntentFilter(LocationManager.GPS_PROVIDER)
                        registerReceiver(BootReceiver(),location)

                        Toast.makeText(applicationContext, "Background location location permission denied", Toast.LENGTH_SHORT).show()
                        Log.e(TAG,"Background location location permission denied")
                    }
                }
            }
            if (foreground) {
                if (background) {
                    handleLocationUpdates()
                } else {
                    handleForegroundLocationUpdates()
                }
            }
        }

        }

    private fun handleLocationUpdates() {
        //foreground and background
        Toast.makeText(applicationContext, "Start Foreground and Background Location Updates", Toast.LENGTH_SHORT).show()
        Log.e(TAG,"Start Foreground and Background Location Updates")
    }

    private fun handleForegroundLocationUpdates() {
        //handleForeground Location Updates
        Toast.makeText(applicationContext, "Start foreground location updates", Toast.LENGTH_SHORT).show()
        Log.e(TAG,"Start Foreground  Location Updates")
    }




    // getLocation
    private fun getLocation() {
     //   if (checkPermissions()) {
        Log.e(TAG,"--------->start")

        val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
        val location: IntentFilter = IntentFilter(LocationManager.GPS_PROVIDER)
        registerReceiver(BootReceiver(),location)
 /*       val location = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(BootReceiver(),location)
        Log.e(TAG,"------service--------->${location.toString()}")
*/
        if (isLocationEnabled()) {

                    UpdateLocationData()

            } else {

                Log.e("alert","get location---->Please turn on location ")
                      //locationPermission(this)
                startService()

              //  showLocationDialog(this,"Location Permission","Please turn on location")
                Toast.makeText(this, "Please turn on location", Toast.LENGTH_LONG).show()

               /* val i = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                i.addCategory(Intent.CATEGORY_DEFAULT);
               // i.setData(Uri.parse("package:$packageName"));
                i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
                startActivity(i)*/
          //  }
        }

/*        if (!userPermission!!.checkAccessCoarseLocationPermission()) {
            showSettingsAlert()
            //  userPermission!!.requestAccessCourseLocationPermission()
        }
        else if (!userPermission!!.checkAccessFineLocationPermission()) {
            showSettingsAlert()
            //  userPermission!!.requestAccessFineLocationPermission()
            // requestLocationPermission()

        } else if (!userPermission!!.checkOverlayPermission()) {
            userPermission!!.requestOverlayPermission()
        } else {
         *//*   if (!Utils.isServiceRunning(this, SocketBackgroundService::class.java)) {
                val intent = Intent(this, SocketBackgroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e(TAG, "Starting the service in >=26 Mode")
                    startForegroundService(intent)
                    UpdateLocationData()
                } else {
                    Log.e(TAG, "Starting the service in < 26 Mode")
                    startService(intent)
                }
            } else {*//*
                UpdateLocationData()
                Log.e(TAG, "AppCheckerForegroundServices already running")
       //     }*/


        }


    // UpdateLocation
    fun UpdateLocationData(){
        progressBarMaps.visibility = View.VISIBLE
        val locationRequest: com.google.android.gms.location.LocationRequest =
            com.google.android.gms.location.LocationRequest()
        locationRequest.interval = 1000
        locationRequest.fastestInterval = 3000
        locationRequest.priority = com.google.android.gms.location.LocationRequest.PRIORITY_HIGH_ACCURACY

        if (ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        LocationServices.getFusedLocationProviderClient(this)
            .requestLocationUpdates(locationRequest,object : LocationCallback(){
                override fun onLocationResult(locationresult: LocationResult) {
                    super.onLocationResult(locationresult)

                    LocationServices.getFusedLocationProviderClient(this@MainActivity)
                        .removeLocationUpdates(this)
                    if (locationresult.locations != null && locationresult.locations.size > 0){
                        val latestLocationIndex:Int = locationresult.locations.size - 1
                        val latitude:Double = locationresult.locations.get(latestLocationIndex).latitude
                        val longitude:Double = locationresult.locations.get(latestLocationIndex).longitude

                        val latLng = LatLng(latitude,longitude)
                    //    drawMarker(latLng)
                        val geocoder = Geocoder(this@MainActivity, Locale.getDefault())
                        val list: List<Address> = geocoder.getFromLocation(latLng.latitude, latLng.longitude, 1)
                        tv_latitude.text = "Latitude\n${list[0].latitude}"
                        tv_longitude.text = "Longitude\n${list[0].longitude}"
                        tv_countryName.text = "Country Name\n${list[0].countryName}"
                        tv_Locality.text = "Locality\n${list[0].locality}"
                        tv_address.text = "Address\n${list[0].getAddressLine(0)}"
                        progressBarMaps.visibility = View.GONE

                        val location:Location = Location("providerNA")
                        location.latitude = latitude
                        location.longitude = longitude
                        // fetchAddressFromLatLong(location)
                        //placeMarkerOnMap(latLng)

                    }else{
                        progressBarMaps.visibility = View.GONE

                    }
                }
            }, Looper.getMainLooper())

    }

    // Location
    private fun isLocationEnabled(): Boolean {
        val locationManager: LocationManager =
            getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }


    // Location Permissions Check
    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED ||
            ActivityCompat.checkSelfPermission(
                this,
                ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            Log.e("alert","Permission ----> true")
            return true
        }
        Log.e("alert","Permission ----> false")
        return false
    }


    private fun requestPermissions() {

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P){
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission. ACCESS_COARSE_LOCATION), REQUEST_LOCATION_CODE)

        }else{
            ActivityCompat.requestPermissions(this,
                arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION,ACCESS_FINE_LOCATION), REQUEST_LOCATION_CODE)
        }


    }

    // GPS Location Permission Dialog
    @TargetApi(30)
    private fun showLocationDialog(context: Context,title: String,msg:String) {
        val alertDialog: AlertDialog.Builder = AlertDialog.Builder(context)

        // Setting Dialog Title
        alertDialog.setTitle(title)


        // Setting Dialog Message
        alertDialog.setMessage(msg)

        // On pressing Settings button
        alertDialog.setPositiveButton("Ok"){dialogInterface,which ->
            dialogInterface.dismiss()

            Log.e("alert","--boot-->Ok")
            openGPSSettings()
        /*    val i= Intent()
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

    // Location Permission Settings open
    @TargetApi(30)
    private fun showSettingsAlert() {

         val  alertDialog:AlertDialog.Builder  = AlertDialog.Builder(this@MainActivity)


        // Setting Dialog Title
        alertDialog.setTitle("Location Permission!")

        // Setting Dialog Message
       alertDialog.setMessage("Please allow access to the device location \n 'All the Time'.We need this when the device \nis lost and can be located for \naudit purposes")

        // On pressing Settings button

            alertDialog.setPositiveButton("Ok"){ dialogInterface,which ->

            Log.e("alert","---->Ok")

                dialogInterface.dismiss()


            val i = Intent()
            i.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
            i.addCategory(Intent.CATEGORY_DEFAULT);
            i.setData(Uri.parse("package:$packageName"));
//            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
//            i.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY);
//            i.addFlags(Intent.FLAG_ACTIVITY_EXCLUDE_FROM_RECENTS);
            startActivity(i)
        }

            alertDialog.setNegativeButton("Cancel"){dialogInterface,which ->

                dialogInterface.dismiss()
            }

       alertDialog.setCancelable(false)
        alertDialog.create()
        alertDialog.show()


    }

    fun showSettingsDialog(title: String, msg: String) {
        val builder = android.app.AlertDialog.Builder(this)
        builder.setTitle(title)
        builder.setMessage(msg)
        builder.setPositiveButton(android.R.string.ok, null)
        builder.setOnDismissListener {dialog ->
            dialog.dismiss()

            openSettings()
        }
        builder.show()
        /* mAlertDialog = builder.create()
         if (mAlertDialog != null) {
             if (!mAlertDialog!!.isShowing) {
                 mAlertDialog!!.show()
             }
         }*/
    }
    // navigating user to app settings
    private fun openSettings() {
        val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
        val uri = Uri.fromParts("package", packageName, null)
        intent.data = uri
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        //  startActivityForResult(intent, 101)
        startActivity(intent)
    }

    // navigating user to app settings
    private fun openGPSSettings() {
        val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
      //  val uri = Uri.fromParts("package", packageName, null)
     //   intent.data = uri
        intent.addCategory(Intent.CATEGORY_DEFAULT);
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK;
        //  startActivityForResult(intent, 101)
        startActivity(intent)
    }

    // sendMessage
/*    fun sendMessage() {
        //     val senderId = UUID.randomUUID().toString()
        val sender = Sender("7", "Simon Basilone")
        val receiver = Receiver("4160", "Mae Jacobs")
        val group = Group("")
        val message = Message(
            etMessage.text.toString(),
            "4160",
            "7",
            sender,
            receiver,
            "",
            group,
            "2022-09-08 14:59:50.969853",
            "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",
            false
        )
        d("message", "---$message")

        mSocket.emit("message${Sender().id}", message)
        Toast.makeText(this, "message sent", Toast.LENGTH_LONG).show()

    }*/

   // Internet On/Off
    private fun showNetworkMessage(isConnected: Boolean) {
        if (!isConnected) {

            Toast.makeText(this,"You are currently offline",Toast.LENGTH_LONG).show()
         /*   snackBar = Snackbar.make(findViewById(R.id.), "You are offline", Snackbar.LENGTH_LONG) //Assume "rootLayout" as the root layout of every activity.
            snackBar?.duration = BaseTransientBottomBar.LENGTH_INDEFINITE
            snackBar?.show()*/
        } else {
            Toast.makeText(this,"Your internet connection was restored.",Toast.LENGTH_LONG).show()
           // snackBar?.dismiss()
        }
    }
    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(ConnectivityManager.CONNECTIVITY_ACTION)
        registerReceiver(BootReceiver(), filter)
        val location = IntentFilter(LocationManager.PROVIDERS_CHANGED_ACTION)
        registerReceiver(BootReceiver(),location)
    }
    override fun onPause() {

        super.onPause()
        Log.e(TAG, "App paused")
    //    VaultLockerApp.preferenceData!!.isDialogShowing = false

    }


    override fun onBackPressed() {

        super.onBackPressed()
    }

    @RequiresApi(Build.VERSION_CODES.Q)
    override fun onResume() {
        super.onResume()
      //  startService()

//        VaultLockerApp.preferenceData!!.isDialogShowing = false
       if (!userPermission!!.checkAccessCoarseLocationPermission()) {
         //  showSettingsAlert()
           showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                   " 'All the Time'.We need this when the device \n" +
                   "is lost and can be located for \n" +
                   "audit purposes")
        //   userPermission!!.requestAccessCourseLocationPermission()
        }
        else if (!userPermission!!.checkAccessFineLocationPermission()) {
               //  showSettingsAlert()
           showSettingsDialog("Location Permission!","Please allow access to the device location \n" +
                   " 'All the Time'.We need this when the device \n" +
                   "is lost and can be located for \n" +
                   "audit purposes")
        //  userPermission!!.requestAccessFineLocationPermission()
            // requestLocationPermission()

        } else if (!userPermission!!.checkOverlayPermission()) {
           showOvarlayDialog(this,"Appear on Top","Please Allow Permission")
          //  userPermission!!.requestOverlayPermission()
        }else if (!userPermission!!.checkBackgroundLocationPermission()){
           userPermission!!.requestBackgroundLocationPermission()
       } else {
            if (!Utils.isServiceRunning(this, SocketBackgroundService::class.java)) {
                val intent = Intent(this, SocketBackgroundService::class.java)
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Log.e(TAG, "Starting the service in >=26 Mode")
                    startForegroundService(intent)
                } else {
                    Log.e(TAG, "Starting the service in < 26 Mode")
                    startService(intent)
                }
            } else {

                Log.e(TAG, "AppCheckerForegroundServices already running")
            }

            val locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
           if (!locationManager.isLocationEnabled) {
               if (mLocationDialog.isShowing) {
                   val isGpsEnabled: Boolean = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)
                   val location: IntentFilter = IntentFilter(LocationManager.GPS_PROVIDER)
                   registerReceiver(BootReceiver(),location)
                  // Utility.locationDialog(this)
               }
           }

        }
        //BootReceiver.connectivityReceiverListener = this

    }


    override fun onNetworkConnectionChanged(isConnected: Boolean) {
        showNetworkMessage(isConnected)
    }

}





