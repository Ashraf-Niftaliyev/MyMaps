package com.esrefnifteliyev.mymaps.view

import android.Manifest
import android.content.Intent
import android.content.SharedPreferences
import android.content.pm.PackageManager
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.room.Room
import com.esrefnifteliyev.mymaps.R

import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.esrefnifteliyev.mymaps.databinding.ActivityMapsBinding
import com.esrefnifteliyev.mymaps.model.Place
import com.esrefnifteliyev.mymaps.roomdb.PlaceDao
import com.esrefnifteliyev.mymaps.roomdb.PlaceDatabase
import com.google.android.material.snackbar.Snackbar
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MapsActivity : AppCompatActivity(), OnMapReadyCallback, GoogleMap.OnMapLongClickListener {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding
    private lateinit var locationManager: LocationManager
    private lateinit var locationListener: LocationListener
    private lateinit var permissionLauncher: ActivityResultLauncher<String>
    private lateinit var sharedPreferences: SharedPreferences
    private var trackBoolean: Boolean? = null
    private var selectedLatitude: Double? = null
    private var selectedLongitude: Double? = null
    private lateinit var db: PlaceDatabase
    private lateinit var placeDao: PlaceDao
    private lateinit var compositeDisposable: CompositeDisposable

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        registerLauncher()

        sharedPreferences = this.getSharedPreferences("com.esrefnifteliyev.mymaps", MODE_PRIVATE)
        trackBoolean = false

        selectedLatitude = 0.0
        selectedLongitude = 0.0

        db = Room.databaseBuilder(applicationContext,PlaceDatabase::class.java,"Places")
            .build()

        placeDao = db.placeDao()


    }

    fun save(view: View){
        if (selectedLatitude!= null && selectedLongitude!= null){
            val place = Place(binding.placeName.text.toString(),selectedLatitude!!,selectedLongitude!!)
            compositeDisposable.add(
                placeDao.insert(place)
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(this::handleResponse)
            )
        }
    }

    private fun handleResponse() {
        val intent = Intent(this@MapsActivity, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
        startActivity(intent)
    }

    fun delete(view: View) {

    }




    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap
        mMap.setOnMapLongClickListener(this)

        locationManager = this.getSystemService(LOCATION_SERVICE) as LocationManager

        locationListener = object : LocationListener {
            override fun onLocationChanged(location: Location) {
                trackBoolean = sharedPreferences.getBoolean("trackBoolean", false)
                if (trackBoolean == false) {
                    val userLocation = LatLng(location.latitude, location.longitude)
                    mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(userLocation, 15f))
                    sharedPreferences.edit().putBoolean("trackBoolean", true).apply()
                }
            }

        }

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                )
            ) {
                Snackbar.make(
                    binding.root,
                    "Permission needed for location",
                    Snackbar.LENGTH_INDEFINITE
                ).setAction("Give Permission") {
                    permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
                }.show()

            } else {
                permissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
            }

        } else {
            locationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER,
                500,
                0f,
                locationListener
            )
            val lastLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
            if (lastLocation != null) {
                val lastUserLocation = LatLng(lastLocation.latitude, lastLocation.longitude)
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(lastUserLocation, 15f))
            }
            mMap.isMyLocationEnabled = true
        }

    }

    fun registerLauncher() {
        permissionLauncher =
            registerForActivityResult(ActivityResultContracts.RequestPermission()) { result ->
                if (result) {
                    if (ContextCompat.checkSelfPermission(
                            this,
                            Manifest.permission.ACCESS_FINE_LOCATION
                        ) != PackageManager.PERMISSION_GRANTED
                    ) {
                        locationManager.requestLocationUpdates(
                            LocationManager.GPS_PROVIDER,
                            500,
                            0f,
                            locationListener
                        )
                        val lastLocation =
                            locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER)
                        if (lastLocation != null) {
                            val lastUserLocation =
                                LatLng(lastLocation.latitude, lastLocation.longitude)
                            mMap.moveCamera(
                                CameraUpdateFactory.newLatLngZoom(
                                    lastUserLocation,
                                    15f
                                )
                            )
                        }
                        mMap.isMyLocationEnabled = true
                    }
                } else {
                    Toast.makeText(this@MapsActivity, "Permission needed!", Toast.LENGTH_LONG)
                        .show()
                }
            }
    }

    override fun onMapLongClick(p0: LatLng) {
        mMap.clear()

        mMap.addMarker(MarkerOptions().position(p0))

        selectedLatitude = p0.latitude
        selectedLongitude = p0.longitude
    }


    override fun onDestroy() {
        super.onDestroy()
        compositeDisposable.clear()
    }


}