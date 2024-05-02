package com.bluell.roomdecoration.interiordesign.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOCategory

@Dao
interface CategoryDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(dtoCategory: DTOCategory)

    @Query("SELECT * FROM category")
    fun getAllCategories():List<DTOCategory>

}