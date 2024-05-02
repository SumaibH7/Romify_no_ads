package com.bluell.roomdecoration.interiordesign.data.models.response

data class upScaleResponse(
    val generationTime: Double,
    val id: Int,
    var output: List<String>,
    val status: String,
    val message: String,
    val eta: String
){}