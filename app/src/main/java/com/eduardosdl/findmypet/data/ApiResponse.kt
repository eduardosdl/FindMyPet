package com.eduardosdl.findmypet.data

data class ApiResponse(
    val status: Boolean,
    val result: List<ApiData>
)