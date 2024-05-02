package com.bluell.roomdecoration.interiordesign.data.models.response


data class Meta(
    val H: Int,
    val W: Int,
    val enable_attention_slicing: String,
    val file_prefix: String,
    val guidance_scale: Double,
    val model: String,
    var n_samples: Int?=0,
    val negative_prompt: String,
    val outdir: String,
    val prompt: String,
    val revision: String,
    val safetychecker: String,
    val seed: Long,
    val steps: Int,
    val vae: String,
    var height: String?="",
    var width: String?="",
    var init_image: String?="",
    var mask_image: String?="",
    var instance_prompt: String?="",
    var samples: String?="",
    var type: String?="",
    var mId: String?="",
    var exactNegativePrompt: String?="",
)