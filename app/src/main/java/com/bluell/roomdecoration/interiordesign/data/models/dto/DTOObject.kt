package com.bluell.roomdecoration.interiordesign.data.models.dto

data class DTOObject(
    var token:String ?= null,
    var init_image:String ?= null,
    var prompt:String ?= null,
    var steps:String ?= null,
    var guidance_scale:String ?= null,
    var endpoint:String ?= null,
    var negative_prompt:String?="",
    var type:String?="a"
)
object DTOSingleton {
    private var myData: DTOObject? = null

    fun getInstance(): DTOObject {
        if (myData == null) {
            myData = DTOObject()
        }
        return myData!!
    }
    fun resetInstance() {
        myData = null
    }
}
