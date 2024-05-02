package com.bluell.roomdecoration.interiordesign.data.models.dto


data class DTOvariations(
    val endpoint: String = "v3/img2img",
    var prompt: String ?= "",
    var negative_prompt: String?=null,
    var init_image: String ?= null,
    val width: String = "512",
    val height: String = "512",
    val samples: String = "4",
    var num_inference_steps: String = "30",
    val safety_checker: String = "no",
    var enhance_prompt: String = "no",
    var guidance_scale: Double = 7.5,
    var strength: Double = 0.7,
    val seed: String?= null,
    var token:String?="",
    val type:String = "a",
    val base64: String = "no",
    var model_id:String?="sdxl",
    var webhook:String?="http://edecator.com/Romify/webhook.php",
    val track_id: String? = null
)

object DTOVarSingelton {
    private var myData: DTOvariations? = null

    fun getInstance(): DTOvariations {
        if (myData == null) {
            myData = DTOvariations()
        }
        return myData!!
    }
    fun resetInstance() {
        myData = null
    }
}
