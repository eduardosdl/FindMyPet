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
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.eduardosdl.findmypet.data.HeartRateData
import com.eduardosdl.findmypet.data.LocationData
import com.eduardosdl.findmypet.data.PetStatusData
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
import com.patrykandpatrick.vico.compose.common.component.rememberShapeComponent
import com.patrykandpatrick.vico.compose.common.component.rememberTextComponent
import com.patrykandpatrick.vico.compose.common.dimensions
import com.patrykandpatrick.vico.compose.common.fill
import com.patrykandpatrick.vico.core.cartesian.axis.HorizontalAxis
import com.patrykandpatrick.vico.core.cartesian.axis.VerticalAxis
import com.patrykandpatrick.vico.core.cartesian.data.CartesianChartModelProducer
import com.patrykandpatrick.vico.core.cartesian.data.CartesianValueFormatter
import com.patrykandpatrick.vico.core.cartesian.data.lineSeries
import com.patrykandpatrick.vico.core.common.shape.CorneredShape
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext


class MainActivity : ComponentActivity() {
    private val viewModel = MainViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            FindMyPetTheme {
                MainRoute(viewModel)
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
fun MainRoute(
    viewModel: MainViewModel
) {
    val petState by viewModel.petStatusData.collectAsState()

    when (petState) {
        is ViewModelState.Idle -> {}

        is ViewModelState.Loading -> {
            Toast.makeText(
                LocalContext.current,
                "Buscando informações...",
                Toast.LENGTH_SHORT
            ).show()
        }

        is ViewModelState.Success -> {
            val petData = (petState as ViewModelState.Success<PetStatusData>).data

            if (petData.location == null || petData.heartRate == null) {
                Toast.makeText(
                    LocalContext.current,
                    "Nenhuma informação encontrada",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                MainScreen(
                    location = petData.location,
                    heartRate = petData.heartRate
                )
            }
        }

        is ViewModelState.Error -> {
            Toast.makeText(
                LocalContext.current,
                (petState as ViewModelState.Error).message,
                Toast.LENGTH_SHORT
            ).show()
        }
    }

}

@Composable
fun MainScreen(
    location: LocationData,
    heartRate: HeartRateData
) {
    val pet = LatLng(location.latitude, location.longitude)
    val markerPetState = rememberMarkerState(position = pet)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(pet, 15f)
    }

    val modelProducer = remember { CartesianChartModelProducer() }
    val bottomFormatter = remember {
        CartesianValueFormatter { _, x, _ ->
            heartRate.time.reversed()[x.toInt() % heartRate.time.size]
        }
    }

    LaunchedEffect(Unit) {
        val frequencies = heartRate.frequency.reversed()

        withContext(Dispatchers.Default) {
            modelProducer.runTransaction {
                lineSeries { series(frequencies) }
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
                        startAxis =
                        VerticalAxis.rememberStart(
                            guideline = null,
                            horizontalLabelPosition = VerticalAxis.HorizontalLabelPosition.Inside,
                            titleComponent =
                            rememberTextComponent(
                                color = MaterialTheme.colorScheme.onPrimary,
                                margins = dimensions(end = 4.dp),
                                padding = dimensions(8.dp, 2.dp),
                                background = rememberShapeComponent(
                                    fill(MaterialTheme.colorScheme.primary),
                                    CorneredShape.Pill
                                ),
                            ),
                            title = "Frequência",
                        ),
                        bottomAxis =
                        HorizontalAxis.rememberBottom(
                            valueFormatter = bottomFormatter,
                            labelRotationDegrees = 270f,

                            itemPlacer = remember { HorizontalAxis.ItemPlacer.aligned() },
                            titleComponent =
                            rememberTextComponent(
                                color = MaterialTheme.colorScheme.onSecondary,
                                margins = dimensions(top = 16.dp),
                                padding = dimensions(8.dp, 2.dp),
                                background =
                                rememberShapeComponent(
                                    fill(MaterialTheme.colorScheme.secondary),
                                    CorneredShape.Pill
                                ),
                            ),
                            title = "Tempo",
                        ),
                    ),
                    modelProducer = modelProducer,
                )
            }
        }
    }
}
