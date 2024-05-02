package com.bluell.roomdecoration.interiordesign.data.models.dto

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName ="project_art" )
data class DTOProjectsArt(
    @PrimaryKey(autoGenerate = true)
    val Id:Int=0,
    val projectId:Int,
    val artId:Int
)
