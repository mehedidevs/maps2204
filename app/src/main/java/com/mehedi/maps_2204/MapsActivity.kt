package com.mehedi.maps_2204

import android.Manifest
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.lifecycle.MutableLiveData
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import com.google.android.gms.maps.model.PolylineOptions
import com.karumi.dexter.Dexter
import com.karumi.dexter.MultiplePermissionsReport
import com.karumi.dexter.PermissionToken
import com.karumi.dexter.listener.PermissionRequest
import com.karumi.dexter.listener.multi.MultiplePermissionsListener
import com.mehedi.maps_2204.databinding.ActivityMapsBinding
import java.io.IOException

class MapsActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var mMap: GoogleMap
    private lateinit var binding: ActivityMapsBinding

    var isPermissionGranted = MutableLiveData<Boolean>(false)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMapsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)
    }

    override fun onStart() {
        super.onStart()

        Dexter.withContext(this)
            .withPermissions(
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
            .withListener(object : MultiplePermissionsListener {
                override fun onPermissionsChecked(report: MultiplePermissionsReport) {
                    // check if all permissions are granted
                    if (report.areAllPermissionsGranted()) {

                        isPermissionGranted.postValue(true)
                    }

                    // check for permanent denial of any permission
                    if (report.isAnyPermissionPermanentlyDenied) {
                        isPermissionGranted.postValue(false)
                        // permission is denied permenantly, navigate user to app settings
                    }
                }

                override fun onPermissionRationaleShouldBeShown(
                    permissions: List<PermissionRequest>,
                    token: PermissionToken
                ) {
                    token.continuePermissionRequest()
                }
            })
            .onSameThread()
            .check()


    }


    override fun onMapReady(googleMap: GoogleMap) {
        mMap = googleMap

        isPermissionGranted.observe(this) {


            if (it) {
                val location = getUserLocation()

                val geocoder = Geocoder(this@MapsActivity)

                val sydney = LatLng(location?.latitude ?: 0.0, location?.longitude ?: 0.0)

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    geocoder.getFromLocation(
                        sydney.latitude,
                        sydney.longitude,
                        1
                    ) { address ->
                        mMap.addMarker(
                            MarkerOptions().position(sydney).title(address[0].toString())
                        )
                        mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18f))
                    }
                } else {
                    val addresses = geocoder.getFromLocation(
                        sydney.latitude,
                        sydney.longitude,
                        1

                    )
                    mMap.addMarker(
                        MarkerOptions().position(sydney).title(addresses?.get(0)?.adminArea)
                    )
                    mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(sydney, 18f))

                }


            }


        }


        // Add a marker in Sydney and move the camera

    }


    private fun getUserLocation(): Location? {
        var location: Location? = null
        var bestLocation: Location? = null

        val locationManager = getSystemService(LOCATION_SERVICE) as LocationManager


        val providers = locationManager.getProviders(true)

        for (provider in providers) {
            if (ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_FINE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                    this,
                    Manifest.permission.ACCESS_COARSE_LOCATION
                ) != PackageManager.PERMISSION_GRANTED
            ) {
                return null
            }

            location = locationManager.getLastKnownLocation(provider)
            if (location == null) {
                continue
            }

            if (bestLocation == null || location.accuracy > bestLocation.accuracy) {
                bestLocation = location
            }
            Log.d("TAG", "getUserLocation: $location ")

        }

        return bestLocation


    }


}