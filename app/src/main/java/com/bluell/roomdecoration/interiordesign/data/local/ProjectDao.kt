package com.bluell.roomdecoration.interiordesign.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOProject

@Dao
interface ProjectDao {
    @Insert
    fun insert(dtoProject: DTOProject): Long

    @Query("SELECT * FROM projects")
    fun getAllProjects(): List<DTOProject>

    @Query("SELECT id FROM projects WHERE projectName=:projectName LIMIT 1" )
    fun getProjectId(projectName:String):Int
}