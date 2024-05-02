package com.bluell.roomdecoration.interiordesign.data.local.converters

import android.util.Log
import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.JsonSyntaxException
import com.google.gson.reflect.TypeToken

class OutputConverter {
    @TypeConverter
    fun fromString(value: String?): List<String> {
        try {
            if (!value.isNullOrEmpty()) {
                val gson = Gson()
                val listType = object : TypeToken<List<String>>() {}.type
                return gson.fromJson(value, listType) ?: emptyList()
            }
        } catch (e: JsonSyntaxException) {
            Log.e("OutputConverter", "Error parsing JSON: $value", e)
        }
        return emptyList()
    }

    @TypeConverter
    fun listToString(list: List<String?>?): String {
        val gson = Gson()
        return gson.toJson(list)
    }
}


