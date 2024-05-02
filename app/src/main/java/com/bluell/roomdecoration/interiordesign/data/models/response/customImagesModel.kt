package com.bluell.roomdecoration.interiordesign.data.models.response

data class customImagesModel(
    var output:String,
    val id: Int,
    val meta: Meta,
    val status: String,
    val eta: Double?,
    val message: String?,
    var isSelected:Boolean?=false

){

}