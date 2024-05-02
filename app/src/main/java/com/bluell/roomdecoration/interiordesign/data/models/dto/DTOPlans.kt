package com.bluell.roomdecoration.interiordesign.data.models.dto

data class DTOPlans(
    val planName:String,
    val planPrice:String,
    val isMandatory:Boolean,
    val isFeatured:Boolean,
    val gems:Int,
    var isEnabled:Boolean
) {
}
