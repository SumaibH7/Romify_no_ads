package com.bluell.roomdecoration.interiordesign.data.models.response

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "upscaled")
data class UpscaleResponseDatabase (
    @PrimaryKey(autoGenerate = false)
    val id: Int,
    val arrayID:Int,
    var indexs: Int,
    var output:String

)