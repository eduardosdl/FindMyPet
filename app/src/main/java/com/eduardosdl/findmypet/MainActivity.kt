package com.eduardosdl.findmypet

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
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
import com.patrykandpatrick.vico.compose.cartesian.CartesianChartHost
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberBottom
import com.patrykandpatrick.vico.compose.cartesian.axis.rememberStart
import com.patrykandpatrick.vico.compose.cartesian.layer.rememberLineCartesianLayer
import com.patrykandpatrick.vico.compose.cartesian.rememberCartesianChart
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlin.random.Random


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

    when (locationState) {
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

    val modelProducer = remember { CartesianChartModelProducer() }

    LaunchedEffect(Unit) {
        val hours = (0..9).map { it.toFloat() }
        val frequencies = listOf(30, 45, 60, 75, 80, 65, 50, 85, 90, 100)

        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries {
                    series(frequencies, frequencies.map { Random.nextFloat() * 15 })
                }
            }
        }
    }

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.6f)
            ) {
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
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(0.4f)
                    .padding(16.dp)
            ) {
                CartesianChartHost(
                    chart = rememberCartesianChart(
                        rememberLineCartesianLayer(),
                        startAxis = VerticalAxis.rememberStart(title = "Frequência"),
                        bottomAxis = HorizontalAxis.rememberBottom(title = "Horas"),
                    ),
                    modelProducer = modelProducer,
                )
            }
        }
    }
}
