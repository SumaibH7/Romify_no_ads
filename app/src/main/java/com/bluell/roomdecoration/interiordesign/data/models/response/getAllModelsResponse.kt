package com.bluell.roomdecoration.interiordesign.data.models.response

import androidx.annotation.DrawableRes
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "AllModels")
data class getAllModelsResponse(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val created_at: String?,
    val description: String?,
    val instance_prompt: String?,
    val model_id: String?,
    val model_name: String?,
    val screenshots: String?,
    val status: String?,
    @DrawableRes val drawable: Int?
)