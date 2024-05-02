package com.bluell.roomdecoration.interiordesign.data.models.dto

data class DTOUpScale(
    val face_enhance: Boolean,
    val key: String,
    val scale: Int,
    val url: String,
    val token: String,
    val type:String?="B",
    var endpoint:String?="v5/super_resolution",
    val webhook: String?="http://edecator.com/Romify/V1/webhook.php"
)