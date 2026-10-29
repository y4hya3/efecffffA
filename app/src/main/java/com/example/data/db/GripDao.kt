package com.example.data.db

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import androidx.room.Update
import com.example.data.model.GripSeries
import com.example.data.model.GripSet
import com.example.data.model.SeriesWithSets
import com.example.data.model.UserProfile
import kotlinx.coroutines.flow.Flow

@Dao
interface GripDao {
    @Transaction
    @Query("SELECT * FROM grip_series ORDER BY createdAt DESC")
    fun getAllSeriesWithSets(): Flow<List<SeriesWithSets>>

    @Transaction
    @Query("SELECT * FROM grip_series WHERE id = :seriesId LIMIT 1")
    fun getSeriesWithSetsById(seriesId: Long): Flow<SeriesWithSets?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSeries(series: GripSeries): Long

    @Update
    suspend fun updateSeries(series: GripSeries)

    @Query("DELETE FROM grip_series WHERE id = :seriesId")
    suspend fun deleteSeriesById(seriesId: Long)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertSets(sets: List<GripSet>)

    @Query("DELETE FROM grip_sets WHERE seriesId = :seriesId")
    suspend fun deleteSetsBySeriesId(seriesId: Long)

    @Query("SELECT * FROM user_profile WHERE id = 1 LIMIT 1")
    fun getUserProfile(): Flow<UserProfile?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun saveUserProfile(profile: UserProfile)

    @Query("SELECT COUNT(*) FROM grip_series")
    suspend fun getSeriesCount(): Int

    @Query("SELECT * FROM grip_sets")
    fun getAllSets(): Flow<List<GripSet>>
}
