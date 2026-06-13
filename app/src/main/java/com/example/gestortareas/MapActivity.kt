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

// MapActivity muestra un mapa de Google y centra la cámara en la ubicación actual del usuario.
// Esta pantalla combina tres elementos importantes de Android: mapas, permisos en tiempo
// de ejecución y obtención de ubicación mediante Google Play Services.
class MapActivity : AppCompatActivity(), OnMapReadyCallback {

    // Referencia al mapa una vez que Google Maps termina de inicializarlo.
    private lateinit var googleMap: GoogleMap

    // Cliente recomendado por Google para obtener la última ubicación conocida del dispositivo.
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var btnBackMap: Button

    companion object {
        // Código usado para reconocer la respuesta del permiso de ubicación.
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

        // La carga del mapa es asíncrona: onMapReady se ejecuta cuando el mapa ya está listo.
        mapFragment.getMapAsync(this)
    }

    override fun onMapReady(map: GoogleMap) {
        googleMap = map

        // No se puede acceder a la ubicación hasta que el mapa exista y el permiso esté concedido.
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
            // Desde Android 6.0 los permisos peligrosos, como ubicación precisa,
            // deben solicitarse mientras la aplicación está en ejecución.
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                LOCATION_PERMISSION_REQUEST_CODE
            )
        }
    }

    private fun showCurrentLocation() {
        // Se vuelve a verificar el permiso para proteger esta función si se llama
        // desde otro punto del ciclo de vida o después de que el permiso cambie.
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            return
        }

        googleMap.isMyLocationEnabled = true

        // lastLocation devuelve la última ubicación conocida. Puede ser null si el dispositivo
        // aún no tiene una ubicación disponible o si los servicios de ubicación están desactivados.
        fusedLocationClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location != null) {
                    val currentLatLng = LatLng(location.latitude, location.longitude)

                    // Se agrega un marcador y se mueve la cámara para que el usuario vea
                    // inmediatamente su posición dentro del mapa.
                    googleMap.addMarker(
                        MarkerOptions()
                            .position(currentLatLng)
                            .title(getString(R.string.current_location_marker))
                    )

                    googleMap.moveCamera(
                        CameraUpdateFactory.newLatLngZoom(currentLatLng, 16f)
                    )
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.current_location_unavailable),
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
            .addOnFailureListener {
                Toast.makeText(
                    this,
                    getString(R.string.current_location_error),
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
                // Si el usuario concede el permiso, recién aquí se puede mostrar la ubicación.
                showCurrentLocation()
            } else {
                Toast.makeText(
                    this,
                    getString(R.string.location_permission_denied),
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}
