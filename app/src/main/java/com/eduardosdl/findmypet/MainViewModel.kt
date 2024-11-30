package com.eduardosdl.findmypet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduardosdl.findmypet.data.ApiResponse
import com.eduardosdl.findmypet.data.HeartRateData
import com.eduardosdl.findmypet.data.LocationData
import com.eduardosdl.findmypet.data.PetStatusData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.TimeZone

class MainViewModel : ViewModel() {
    private val apiService = ApiService.RetrofitInstance.apiService

    private val _petStatusData = MutableStateFlow<ViewModelState<PetStatusData>>(ViewModelState.Idle)
    val petStatusData: StateFlow<ViewModelState<PetStatusData>> = _petStatusData

    fun getLocation() {
        viewModelScope.launch {
            _petStatusData.value = ViewModelState.Loading
            try {
                val response = apiService.getLocation()
                val locationData = parseApiLocationData(response)

                val currentData = (_petStatusData.value as? ViewModelState.Success)?.data ?: PetStatusData()
                _petStatusData.value = ViewModelState.Success(currentData.copy(location = locationData))
            } catch (e: Exception) {
                _petStatusData.value = ViewModelState.Error("Houve um erro ao buscar localização")
                Log.e("MainViewModel", "Error: ${e.message}", e)
            }
        }
    }

    fun getHeartRateHistory() {
        viewModelScope.launch {
            _petStatusData.value = ViewModelState.Loading
            try {
                val response = apiService.getHeartRateHistory()
                val heartRateData = parseApiHeartRateDate(response)

                val currentData = (_petStatusData.value as? ViewModelState.Success)?.data ?: PetStatusData()
                _petStatusData.value = ViewModelState.Success(currentData.copy(heartRate = heartRateData))
            } catch (e: Exception) {
                _petStatusData.value = ViewModelState.Error("Houve um erro ao buscar histórico de batimentos cardíacos")
                Log.e("MainViewModel", "Error: ${e.message}", e)
            }
        }
    }

    private fun parseApiLocationData(response: ApiResponse): LocationData {
        val location = LocationData(0.0, 0.0, response.result.firstOrNull()?.time ?: "")

        response.result.forEach {
            when (it.variable) {
                "latitude" -> location.latitude = it.content
                "longitude" -> location.longitude = it.content
            }
        }

        return location
    }

    private fun parseApiHeartRateDate(response: ApiResponse): HeartRateData {
        val heartRate = mutableListOf<Int>()
        val time = mutableListOf<String>()

        val inputFormatter = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'", Locale.US)
        inputFormatter.timeZone = TimeZone.getTimeZone("UTC")

        val outputFormatter = SimpleDateFormat("dd/MM HH:mm", Locale.US)
        outputFormatter.timeZone = TimeZone.getTimeZone("UTC")

        response.result.forEach { status ->
            heartRate.add(status.content.toInt())

            val date = inputFormatter.parse(status.time)
            val formattedDate = date?.let { outputFormatter.format(it) } ?: ""

            time.add(formattedDate)
        }

        return HeartRateData(heartRate, time)
    }
}