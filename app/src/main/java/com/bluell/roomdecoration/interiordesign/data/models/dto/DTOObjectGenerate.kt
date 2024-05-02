package com.bluell.roomdecoration.interiordesign.data.models.dto


data class DTOObjectGenerate(
    var key:String?=null,
    var prompt:String?=null,
    var originalPrompt:String?=null,
    var promptsBuilder:String="",
    var init_image: String?=null,
    var mask_image: String?=null,
    var model_id:String?="sdxl",
    var model_name:String="SDXL",
    var instance_prompt:String="",
    var negative_prompt:String?="",
    var exact_Nprompt:String?="",
    var width:String?="1024",
    var height:String?="1024",
    var canvasPos:Int=0,
    var samples:String="4",
    var scheduler:String="UniPCMultistepScheduler",
    var num_inference_steps:String="21",
    var steps:String=num_inference_steps,
    var safety_checker:String="yes",
    var enhance_prompt:String="no",
    var seed:String?=null,
    var guidance_scale:Double=7.0,
    var strength:Double?=1.0,
    var multi_lingual:String="yes",
    var panorama:String="no",
    var self_attention:String="yes",
    var upscale:String="yes",
    var embeddings_model:String?=null,
    var webhook:String?="http://edecator.com/Romify/webhook.php",
    var track_id:String?=null,
    var guess_mode:String?="no",
    var token:String?="",
    var endpoint:String?="",
    var type:String?="a",
    var vae:String?=null,
){
    @Transient
    val dtoPropertyContainer= DTOPropertyContainer()
}
object DTOGenSingleton {
    private var myData: DTOObjectGenerate? = null

    fun getInstance(): DTOObjectGenerate {
        if (myData == null) {
            myData = DTOObjectGenerate()
        }
        return myData!!
    }
    fun resetInstance() {
        myData = null
    }
}
