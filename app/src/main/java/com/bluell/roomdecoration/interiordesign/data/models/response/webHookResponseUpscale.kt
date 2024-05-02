package com.bluell.roomdecoration.interiordesign.data.models.response


data class webHookResponseUpscale(
    val id: Int,
    val output: List<String>,
    val status: String,
    val track_id: String,
    val webhook_type: String
){}