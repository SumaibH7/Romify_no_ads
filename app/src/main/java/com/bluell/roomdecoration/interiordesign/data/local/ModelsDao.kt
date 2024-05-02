package com.bluell.roomdecoration.interiordesign.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bluell.roomdecoration.interiordesign.data.models.response.getAllModelsResponse


@Dao
interface ModelsDao {
@Insert(onConflict = OnConflictStrategy.REPLACE)
fun insert(getAllModelsResponse: getAllModelsResponse)

@Query("SELECT * FROM allmodels LIMIT 100000 OFFSET 11")
fun getAllModels():List<getAllModelsResponse>

@Query("SELECT * FROM allmodels LIMIT 11")
fun getLimitedModels():List<getAllModelsResponse>

}