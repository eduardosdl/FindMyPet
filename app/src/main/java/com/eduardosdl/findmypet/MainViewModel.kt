package com.eduardosdl.findmypet

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.eduardosdl.findmypet.data.ApiData
import com.eduardosdl.findmypet.data.ApiResponse
import com.eduardosdl.findmypet.data.LocationData
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private val apiService = ApiService.RetrofitInstance.apiService

    private val _location = MutableStateFlow<ViewModelState<LocationData>>(ViewModelState.Idle)
    val location: MutableStateFlow<ViewModelState<LocationData>> = _location

    private val _heartRate = MutableStateFlow<ViewModelState<ApiResponse>>(ViewModelState.Idle)
    val heartRate: MutableStateFlow<ViewModelState<ApiResponse>> = _heartRate

    fun getLocation() {
        viewModelScope.launch {
            _location.value = ViewModelState.Loading

            try {
                val response = apiService.getLocation()
                val locationData = parseApiLocationData(response)
                _location.value = ViewModelState.Success(locationData)
            } catch (e: Exception) {
                _location.value = ViewModelState.Error("Houve um erro ao buscar localização")
                Log.e("MainViewModel", "Error: ${e.message}")
            }
        }
    }

    fun getHeartRateHistory() {
        viewModelScope.launch {
            _heartRate.value = ViewModelState.Loading
            try {
                val response = apiService.getHeartRateHistory()
                _heartRate.value = ViewModelState.Success(response)
            } catch (e: Exception) {
                _heartRate.value = ViewModelState.Error("Houve um erro ao buscar histórico de batimentos cardíacos")
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
}