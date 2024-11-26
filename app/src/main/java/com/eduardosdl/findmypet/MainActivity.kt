package com.eduardosdl.findmypet

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import com.eduardosdl.findmypet.data.ApiResponse
import com.eduardosdl.findmypet.data.LocationData
import com.eduardosdl.findmypet.ui.theme.FindMyPetTheme
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapType
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.rememberCameraPositionState
import com.google.maps.android.compose.rememberMarkerState
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModel
import com.patrykandpatrick.vico.core.cartesian.data.LineCartesianLayerModel


class MainActivity : ComponentActivity() {
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindMyPetTheme {
                MainScreen(viewModel)
            }
        }
    }

    override fun onStart() {
        super.onStart()
        viewModel.getLocation()
        viewModel.getHeartRateHistory()
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel
) {
    val locationState by viewModel.location.collectAsState()
    val heartRateState by viewModel.heartRate.collectAsState()

    when(locationState) {
        is ViewModelState.Idle -> {}

        is ViewModelState.Loading -> {
            Toast.makeText(
                LocalContext.current,
                "Buscando informações...",
                Toast.LENGTH_SHORT
            ).show()
        }

        is ViewModelState.Success -> {
            val location = (locationState as ViewModelState.Success<LocationData>).data
            MainContent(
                location
            )
        }

        is ViewModelState.Error -> {
            Toast.makeText(
                LocalContext.current,
                (locationState as ViewModelState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }
}

@Composable
fun MainContent(
    location: LocationData,
) {
    val pet = LatLng(location.latitude, location.longitude)
    val markerPetState = rememberMarkerState(position = pet)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pet, 15f)
    }

    Scaffold { innerPadding ->
        Box(modifier = Modifier
            .fillMaxWidth()
            .fillMaxHeight(0.6f)
            .padding(innerPadding)) {
            GoogleMap(
                modifier = Modifier.matchParentSize(),
                cameraPositionState = cameraPositionState,
                properties = MapProperties(mapType = MapType.SATELLITE),
                uiSettings = MapUiSettings(zoomControlsEnabled = true)
            ) {
                Marker(state = markerPetState)
            }
        }

        Box(
            modifier = Modifier.fillMaxSize()
        ) {
            CartesianChartModel(
                LineCartesianLayerModel.build {
                    series(1, 8, 3, 7)
                    series(y = listOf(6, 1, 9, 3))
                    series(x = listOf(1, 2, 3, 4), y = listOf(2, 5, 3, 4))
                },
            )
        }
    }
}
