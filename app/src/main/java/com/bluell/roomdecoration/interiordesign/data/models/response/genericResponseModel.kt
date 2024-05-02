package com.bluell.roomdecoration.interiordesign.data.models.response

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.bluell.roomdecoration.interiordesign.data.local.converters.MetaConverter
import com.bluell.roomdecoration.interiordesign.data.local.converters.OutputConverter

@Entity(tableName = "creations")
data class genericResponseModel(
    @PrimaryKey(autoGenerate = false)
    var id: Int ?= null,
    @TypeConverters(MetaConverter::class)
    val meta: Meta,
    @TypeConverters(OutputConverter::class)
    var output: List<String>? = null,
    @TypeConverters(OutputConverter::class)
    var future_links: List<String>? = null,
    val status: String,
    val eta: Double?,
    val message: String?,
    var endpoint:String?,
    var isSelected:Boolean?=false,
    var fetch_result:String ?= null,
    var isFavorite:Boolean ? =false
)
