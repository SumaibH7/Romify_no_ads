package com.bluell.roomdecoration.interiordesign.data.local.converters

import androidx.room.TypeConverter
import com.bluell.roomdecoration.interiordesign.data.models.response.Meta
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class MetaConverter {
    @TypeConverter
    fun fromLoginToString(group: Meta): String {
        val gson = Gson()
        val type = object : TypeToken<Meta>() {}.type
        return gson.toJson(group, type)
    }


    @TypeConverter
    fun fromStringToLogin(value: String): Meta {
        val gson = Gson()
        val type = object : TypeToken<Meta>() {}.type
        return gson.fromJson(value, type)
    }
}