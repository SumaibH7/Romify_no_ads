package com.bluell.roomdecoration.interiordesign.data.models.response

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "prompts")
data class promptModel(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val catId:Int,
    val promptName:String,
)
