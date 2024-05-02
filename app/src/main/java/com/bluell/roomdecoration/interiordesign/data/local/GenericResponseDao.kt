package com.bluell.roomdecoration.interiordesign.data.local

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel

@Dao
interface GenericResponseDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun saveData(genericResponseModel: genericResponseModel):Long

    @Query("SELECT * FROM creations WHERE (:includeAll = 1 OR endpoint =:point) ORDER BY id DESC")
    fun getAllCreations(includeAll:Int,point:String):List<genericResponseModel>

    @Query("SELECT * FROM creations WHERE isFavorite=1 AND endpoint =:point  ORDER BY id DESC")
    fun getAllfavorites(point:String):List<genericResponseModel>

    @Query("SELECT * FROM creations WHERE (:includeAll = 1 OR endpoint =:point) ORDER BY id DESC")
    fun getAllCreationsLive(includeAll:Int,point:String):LiveData<List<genericResponseModel>>

    @Query("SELECT * FROM creations WHERE isFavorite=1 AND endpoint =:point  ORDER BY id DESC")
    fun getAllFavoritesLive(point:String):LiveData<List<genericResponseModel>>

    @Query("SELECT * FROM creations WHERE id=:Id LIMIT 1")
    fun getCreationsById(Id:Int):LiveData<genericResponseModel>

    @Query("SELECT * FROM creations WHERE id=:Id LIMIT 1")
    fun getCreationsByIdNotLive(Id:Int): genericResponseModel

    @Update()
    fun UpdateData(genericResponseModel: genericResponseModel)

    @Query("UPDATE creations SET output=:output WHERE id=:Id")
    fun UpdateOutput(output:List<String>,Id: Int)

    @Delete
    fun deleteCreation(genericResponseModel: genericResponseModel):Int

    @Query("DELETE FROM creations WHERE (:includeAll = 1 OR endpoint =:point)")
    fun deleteAllCreations(includeAll:Int,point:String)


    @Query("UPDATE creations SET isFavorite = 0 WHERE isFavorite = 1")
    suspend fun updateFavoritesToFalse()

    @Query("DELETE FROM creations WHERE isFavorite=1")
    fun deleteFavHistory()


}