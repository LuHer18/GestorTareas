package com.example.gestortareas

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions

// MapActivity muestra el mapa de Google y ubica al usuario en su posición actual.
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    private lateinit var googleMap: GoogleMap
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnBackMap: Button

    companion object {
        private const val LOCATION_PERMISSION_REQUEST_CODE = 1001
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_map)

        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)
        btnBackMap = findViewById(R.id.btnBackMap)

        btnBackMap.setOnClickListener {
            finish()
        }

        val mapFragment = supportFragmentManager
            .findFragmentById(R.id.mapFragment) as SupportMapFragment

        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map
        checkLocationPermission()
    }

    private fun checkLocationPermission() {
        val permissionGranted = ActivityCompat.checkSelfPermission(
            this,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED

        if (permissionGranted) {
            showCurrentLocation()
        } else {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showCurrentLocation() {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        googleMap.isMyLocationEnabled = true

        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    googleMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title("Tu ubicación actual")
                    )

                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f)
                    )
                } else {
                    Toast.makeText(
                        this,
                        "No se pudo obtener la ubicación actual",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    "Error al obtener la ubicación",
                    Toast.LENGTH_LONG
                ).show()
            }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == LOCATION_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                showCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    "Permiso de ubicación denegado",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}