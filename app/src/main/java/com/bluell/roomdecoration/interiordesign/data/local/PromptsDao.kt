package com.bluell.roomdecoration.interiordesign.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.bluell.roomdecoration.interiordesign.data.models.response.promptModel

@Dao
interface PromptsDao {
    @Insert
    fun insert(promptModel: promptModel):Long

    @Query("SELECT * FROM prompts p JOIN category c ON p.catId=c.id WHERE p.catId=:catId ")
    fun getPromptsAgainstCat(catId: Int):List<promptModel>

    @Query("SELECT * FROM prompts")
    fun getAllPrompts():List<promptModel>
}