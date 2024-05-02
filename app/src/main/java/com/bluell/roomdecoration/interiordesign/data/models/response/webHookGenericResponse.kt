package com.bluell.roomdecoration.interiordesign.data.models.response

data class webHookGenericResponse(
    val id: Int,
    val output: List<String>,
    val status: String,
    val track_id: String,
    val webhook_type: String
){}