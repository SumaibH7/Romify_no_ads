package com.bluell.roomdecoration.interiordesign.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOProjectsArt
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel

@Dao
interface ProjectArtDao {

    @Insert
    fun insert(dtoProjectsArt: DTOProjectsArt):Long

    @Query("SELECT * FROM project_art pa JOIN creations c ON pa.artId=c.id WHERE pa.projectId=:projectId ")
    fun getAllProjectArts(projectId: Int):List<genericResponseModel>

}