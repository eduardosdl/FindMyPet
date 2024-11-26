package com.eduardosdl.findmypet

import com.eduardosdl.findmypet.data.ApiResponse
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.GET
import retrofit2.http.Header

interface ApiService {

    @GET("/data?variable[]=latitude&variable[]=longitude&query=last_item")
    suspend fun getLocation(@Header("Device-Token") deviceToken: String = ""): ApiResponse

    @GET("/data?variable[]=frequency")
    suspend fun getHeartRateHistory(@Header("Device-Token") deviceToken: String = ""): ApiResponse

    object RetrofitInstance {
        val apiService: ApiService by lazy {
            Retrofit.Builder()
                .baseUrl("https://api.tago.io")
                .addConverterFactory(GsonConverterFactory.create())
                .build()
                .create(ApiService::class.java)
        }
    }
}