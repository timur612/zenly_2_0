package com.phtl.itlab.zenly_2_0


import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationManager
import android.os.Bundle
import android.os.Looper
import android.provider.Settings
import android.view.View
import android.widget.Button
import android.widget.Switch
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.*
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser


class MapsActivity : AppCompatActivity(), OnMapReadyCallback {


    private lateinit var mMap: GoogleMap
    val places = arrayListOf<Int>()
    val sydney = LatLng(-34.0, 151.0)
    val a = LatLng(55.754724, 37.621380)
    val b = LatLng(55.728466, 37.604155)
    var sum = 0
    val location: Location? = null
    var latitude = 0.0
    var longitude = 0.0
    var mGoogleApiClient: GoogleApiClient? = null
    private var locationManager: LocationManager? = null
    lateinit var fusedLocationClient: FusedLocationProviderClient
    var sbGPS = StringBuilder()
    var sbNet = StringBuilder()
    var PERMISSION_ID = 42
    private var authListener: FirebaseAuth.AuthStateListener? = null
    private var auth: FirebaseAuth? = null
    private var user: FirebaseUser? = null
    private var btnSignOut:  Button? = null
    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_maps)
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        auth = FirebaseAuth.getInstance()
        user = FirebaseAuth.getInstance().currentUser

        authListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
            var currentUser = firebaseAuth.currentUser
            if (currentUser == null) {
                startActivity(Intent(this@MapsActivity, LoginRegisterActivity::class.java))
                finish()
            }
        }



        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        btnSignOut = findViewById<Button>(R.id.btnSignOut)

        places.add(LatLng(55.754724, 37.621380))
        places.add(LatLng(55.760133, 37.618697))
        places.add(LatLng(55.764753, 37.591313))
        places.add(LatLng(55.728466, 37.604155))
        locationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager

        btnSignOut!!.setOnClickListener {
            signOut()

        }


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location : Location? ->
                // Got last known location. In some rare situations this can be null.

            }
        getLastLocation()
    }
    private fun signOut() {
        auth!!.signOut()

        startActivity(Intent(this@MapsActivity, LoginRegisterActivity::class.java))
        finish()
    }
    /**
     * Manipulates the map once available.
     * This callback is triggered when the map is ready to be used.
     * This is where we can add markers or lines, add listeners or move the camera. In this case,
     * we just add a marker near Sydney, Australia.
     * If Google Play services is not installed on the device, the user will be prompted to install
     * it inside the SupportMapFragment. This method will only be triggered once the user has
     * installed Google Play services and returned to the app.
     */

    override fun onMapReady(googleMap: GoogleMap) {

        mMap = googleMap;

        val changeMap = findViewById(R.id.switch1) as Switch
        val cl = findViewById(R.id.button3) as Button
        // Add a marker in Sydney and move the camera


        mMap.addMarker(MarkerOptions().position(sydney).title("Marker in Sydney"))
        mMap.addMarker(MarkerOptions().position(a).title("ГУМ"))
        mMap.addMarker(MarkerOptions().position(b).title("ХЗ"))



        // mMap.setMyLocationEnabled(true)

        cl.setOnClickListener{
            mMap.addMarker(MarkerOptions().position(LatLng(latitude,longitude)).title("Я"))
            val cameraPosition = CameraPosition.Builder()
                .target(LatLng( latitude,longitude))
                .zoom(16f)
                .bearing(45f)
                .tilt(20f)
                .build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            mMap.animateCamera(cameraUpdate)

        }
        val zoomLevel = 16.0f
        mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(LatLng(latitude,longitude), zoomLevel))
        mMap.setIndoorEnabled(false)
        changeMap.setOnCheckedChangeListener { buttonView, isChecked ->
            if (isChecked) {
                // The switch is enabled/checked
                mMap.setMapType(GoogleMap.MAP_TYPE_SATELLITE)

                // Change the app background color

            } else {
                // The switch is disabled
                mMap.setMapType(GoogleMap.MAP_TYPE_NORMAL)

            }
        }
    }

    @SuppressLint("MissingPermission")
    private fun getLastLocation() {
        if (checkPermissions()) {
            if (isLocationEnabled()) {

                fusedLocationClient.lastLocation.addOnCompleteListener(this) { task ->
                    var location: Location? = task.result
                    if (location == null) {
                        requestNewLocationData()
                    } else {
                        latitude = location.latitude
                        longitude = location.longitude
                    }
                }
            } else {
                Toast.makeText(this, "Turn on location", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                startActivity(intent)
            }
        } else {
            requestPermissions()
        }
    }

    @SuppressLint("MissingPermission")
    private fun requestNewLocationData() {
        var mLocationRequest = LocationRequest()
        mLocationRequest.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest.interval = 0
        mLocationRequest.fastestInterval = 0
        mLocationRequest.numUpdates = 1

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        fusedLocationClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            var mLastLocation: Location = locationResult.lastLocation
            latitude= mLastLocation.latitude
            longitude = mLastLocation.longitude
        }
    }

    private fun isLocationEnabled(): Boolean {
        var locationManager: LocationManager = getSystemService(Context.LOCATION_SERVICE) as LocationManager
        return locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER) || locationManager.isProviderEnabled(
            LocationManager.NETWORK_PROVIDER
        )
    }

    private fun checkPermissions(): Boolean {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED &&
            ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            return true
        }
        return false
    }

    private fun requestPermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION),
            PERMISSION_ID
        )
    }


    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_ID) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                getLastLocation()
            }
        }
    }



    fun onClickTest(view: View) {
        if(sum==0){
            val cameraPosition = CameraPosition.Builder()
                .target(a)
                .zoom(16f)
                .bearing(45f)
                .tilt(20f)
                .build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            mMap.animateCamera(cameraUpdate)
            sum=1
        }
        else if(sum==1){
            val cameraPosition = CameraPosition.Builder()
                .target(b)
                .zoom(16f)
                .bearing(45f)
                .tilt(20f)
                .build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            mMap.animateCamera(cameraUpdate)
            sum=2
        }
        else{
            val cameraPosition = CameraPosition.Builder()
                .target(sydney)
                .zoom(16f)
                .bearing(45f)
                .tilt(20f)
                .build()
            val cameraUpdate = CameraUpdateFactory.newCameraPosition(cameraPosition)
            mMap.animateCamera(cameraUpdate)
            sum=0
        }

    }






}


private fun <E> ArrayList<E>.add(element: LatLng) {

}



