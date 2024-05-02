package com.bluell.roomdecoration.interiordesign.data.local

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOCategory
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOProject
import com.bluell.roomdecoration.interiordesign.data.models.dto.DTOProjectsArt
import com.bluell.roomdecoration.interiordesign.data.models.response.promptModel
import com.bluell.roomdecoration.interiordesign.data.local.converters.MetaConverter
import com.bluell.roomdecoration.interiordesign.data.local.converters.OutputConverter
import com.bluell.roomdecoration.interiordesign.data.models.response.UpscaleResponseDatabase
import com.bluell.roomdecoration.interiordesign.data.models.response.genericResponseModel
import com.bluell.roomdecoration.interiordesign.data.models.response.getAllModelsResponse

@androidx.room.Database(entities = [genericResponseModel::class, DTOProject::class, DTOProjectsArt::class, getAllModelsResponse::class, promptModel::class, DTOCategory::class, UpscaleResponseDatabase::class], version = 2)
@TypeConverters(MetaConverter::class, OutputConverter::class)
abstract class AppDatabase : RoomDatabase() {

    abstract fun genericResponseDao(): GenericResponseDao?
    abstract fun upscaleDao(): UpscaleDao?

    abstract fun projectDao(): ProjectDao
    abstract fun catsDao(): CategoryDao
    abstract fun promptsDao(): PromptsDao

    abstract fun projectArtDao(): ProjectArtDao

    abstract fun modelsDao(): ModelsDao

    companion object {
        @Volatile
        private var instance: AppDatabase? = null
        private val LOCK = Any()

        operator fun invoke(context: Context) = instance ?: synchronized(LOCK) {
            instance ?: buildDatabase(context).also { instance = it }
        }

        private fun buildDatabase(context: Context) = Room.databaseBuilder(
            context,
            AppDatabase::class.java, "romify.db"
        )
            .allowMainThreadQueries()
            .fallbackToDestructiveMigration()
            .build()
    }
}