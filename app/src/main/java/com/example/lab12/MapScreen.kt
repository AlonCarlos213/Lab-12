package com.example.lab12

import androidx.compose.foundation.layout.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.maps.android.compose.*
import androidx.compose.material.Button
import androidx.compose.material.Text
import androidx.compose.material.DropdownMenu
import androidx.compose.material.DropdownMenuItem

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.location.Location
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.ui.platform.LocalContext
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.FusedLocationProviderClient


@Composable
fun MapScreen() {
    val context = LocalContext.current
    val fusedLocationClient = LocationServices.getFusedLocationProviderClient(context)

    // Variables para almacenar la ubicación actual y el tipo de mapa
    var currentLocation by remember { mutableStateOf(LatLng(-16.4040102, -71.559611)) } // Ubicación predeterminada (Arequipa)
    val cameraPositionState = rememberCameraPositionState {
        position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(currentLocation, 12f)
    }
    var mapType by remember { mutableStateOf(MapType.NORMAL) }

    // Solicitar permisos de ubicación
    val locationPermissionRequest = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { isGranted ->
            if (isGranted) {
                getCurrentLocation(fusedLocationClient) { location ->
                    currentLocation = LatLng(location.latitude, location.longitude)
                    cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(currentLocation, 12f)
                }
            }
        }
    )

    LaunchedEffect(Unit) {
        if (ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            locationPermissionRequest.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        } else {
            getCurrentLocation(fusedLocationClient) { location ->
                currentLocation = LatLng(location.latitude, location.longitude)
                cameraPositionState.position = com.google.android.gms.maps.model.CameraPosition.fromLatLngZoom(currentLocation, 12f)
            }
        }
    }

    Column {
        // Componente para seleccionar el tipo de mapa
        MapTypeSelector(onMapTypeSelected = { selectedType ->
            mapType = selectedType
        })

        // Mostrar el mapa con el tipo seleccionado y la ubicación actual
        Box(modifier = Modifier.fillMaxSize()) {
            GoogleMap(
                modifier = Modifier.fillMaxSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = mapType)
            ) {
                // Añadir marcador en la ubicación actual
                Marker(
                    state = rememberMarkerState(position = currentLocation),
                    icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                    title = "Ubicación actual"
                )
            }
        }
    }
}

// Función para obtener la ubicación actual
@SuppressLint("MissingPermission")
private fun getCurrentLocation(
    fusedLocationClient: FusedLocationProviderClient,
    onLocationAvailable: (Location) -> Unit
) {
    fusedLocationClient.lastLocation
        .addOnSuccessListener { location: Location? ->
            location?.let { onLocationAvailable(it) }
        }
}

@Composable
fun MapTypeSelector(onMapTypeSelected: (MapType) -> Unit) {
    var expanded by remember { mutableStateOf(false) }
    val mapTypes = listOf(
        "Normal" to MapType.NORMAL,
        "Satellite" to MapType.SATELLITE,
        "Terrain" to MapType.TERRAIN,
        "Hybrid" to MapType.HYBRID
    )

    Box(
        Modifier
            .fillMaxWidth()
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Button(onClick = { expanded = true }) {
            Text("Cambiar tipo de mapa")
        }
        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            mapTypes.forEach { (label, type) ->
                DropdownMenuItem(onClick = {
                    onMapTypeSelected(type)
                    expanded = false
                }) {
                    Text(text = label)
                }
            }
        }
    }
}

