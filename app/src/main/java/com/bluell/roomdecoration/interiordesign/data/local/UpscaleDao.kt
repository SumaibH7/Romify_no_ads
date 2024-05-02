package com.bluell.roomdecoration.interiordesign.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bluell.roomdecoration.interiordesign.data.models.response.UpscaleResponseDatabase

@Dao
interface UpscaleDao {

    @Query("SELECT * FROM upscaled Where arrayID =:cId")
    fun getAllUpscaled(cId:Int): LiveData<List<UpscaleResponseDatabase>>


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveData(upscaleResponseDatabase: UpscaleResponseDatabase):Long

    @Query("SELECT * FROM upscaled WHERE arrayID=:Id AND indexs=:index LIMIT 1")
    fun getCreationsByIdNotLive(Id:Int,index:Int): UpscaleResponseDatabase

    @Query("SELECT * FROM upscaled WHERE arrayID=:Id LIMIT 1")
    fun getCreationsByIdNotLiveMessage(Id:Int): UpscaleResponseDatabase

    @Update()
    fun UpdateData(upscaleResponseDatabase: UpscaleResponseDatabase)

    @Query("UPDATE upscaled SET output = :output WHERE id = :id AND arrayID = :arrayId")
    fun updateOutputById(id: Int, output: String, arrayId: Int)
}