package com.bluell.roomdecoration.interiordesign.data.models.dto

import androidx.lifecycle.MutableLiveData

class DTOPropertyContainer {
    val modelName: MutableLiveData<String> = MutableLiveData("See All")
    val num_inference_steps: MutableLiveData<String> = MutableLiveData("50")
    val sizeName: MutableLiveData<String> = MutableLiveData("Select Canvas")
    val prompts: MutableLiveData<String> = MutableLiveData("")

}