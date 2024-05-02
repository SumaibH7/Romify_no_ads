package com.bluell.roomdecoration.interiordesign.data.models.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "category")
data class DTOCategory(
    @PrimaryKey(autoGenerate = true)
    val Id:Int=0,
    val categoryName:String
)
