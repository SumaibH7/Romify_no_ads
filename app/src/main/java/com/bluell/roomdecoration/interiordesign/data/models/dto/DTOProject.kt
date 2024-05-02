package com.bluell.roomdecoration.interiordesign.data.models.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "projects")
data class DTOProject(
    @PrimaryKey(autoGenerate = true)
    val id:Int=0,
    val projectName:String,
    val creationDate:String=System.currentTimeMillis().toString(),
) {
}