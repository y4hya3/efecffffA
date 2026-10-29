package com.example.data.db

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.data.model.GripSeries
import com.example.data.model.GripSet
import com.example.data.model.UserProfile

@Database(
    entities = [
        GripSeries::class,
        GripSet::class,
        UserProfile::class
    ],
    version = 2,
    exportSchema = false
)
@TypeConverters(Converters::class)
abstract class GripDatabase : RoomDatabase() {
    abstract fun gripDao(): GripDao

    companion object {
        @Volatile
        private var INSTANCE: GripDatabase? = null

        fun getInstance(context: Context): GripDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    GripDatabase::class.java,
                    "grip_forge.db"
                )
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }
    }
}
