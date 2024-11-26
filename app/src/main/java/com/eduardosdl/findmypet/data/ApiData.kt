package com.eduardosdl.findmypet.data

import com.google.gson.annotations.SerializedName

data class ApiData(
    val time: String,
    @SerializedName("value")
    val content: Double,
    val variable: String,
)