package com.example.data.model

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation

enum class HandSide {
    LEFT,
    RIGHT
}

@Entity(tableName = "user_profile")
data class UserProfile(
    @PrimaryKey val id: Int = 1,
    val name: String = "Alex",
    val preferredUnit: String = "kg",
    val goalRepsPerDay: Int = 60,
    val rankTitle: String = "Crush Grip Specialist",
    val defaultRestSeconds: Int = 60
)

@Entity(tableName = "grip_series")
data class GripSeries(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis(),
    val notes: String = ""
)

@Entity(
    tableName = "grip_sets",
    foreignKeys = [
        ForeignKey(
            entity = GripSeries::class,
            parentColumns = ["id"],
            childColumns = ["seriesId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["seriesId"])]
)
data class GripSet(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val seriesId: Long,
    val hand: HandSide,
    val weightOrLevel: String = "40 kg",
    val reps: Int = 10,
    val orderIndex: Int = 0,
    val isCompleted: Boolean = true
)

data class SeriesWithSets(
    @Embedded val series: GripSeries,
    @Relation(
        parentColumn = "id",
        entityColumn = "seriesId"
    )
    val sets: List<GripSet>
) {
    val totalReps: Int get() = sets.sumOf { it.reps }
    val leftReps: Int get() = sets.filter { it.hand == HandSide.LEFT }.sumOf { it.reps }
    val rightReps: Int get() = sets.filter { it.hand == HandSide.RIGHT }.sumOf { it.reps }
    val sortedSets: List<GripSet> get() = sets.sortedBy { it.orderIndex }
}

data class BestDayRecord(
    val dateString: String,
    val timestamp: Long,
    val totalReps: Int
)

data class HandRecord(
    val hand: HandSide,
    val maxRepsInSingleSet: Int,
    val bestResistance: String,
    val totalLifetimeReps: Int
)

data class ChartDataPoint(
    val label: String,
    val leftReps: Int,
    val rightReps: Int,
    val timestamp: Long
)

data class HeatmapDay(
    val dateString: String,
    val dayOfWeek: Int, // 1 to 7
    val dayOfMonth: Int,
    val monthLabel: String,
    val repsCount: Int,
    val intensityTier: Int // 0, 1, 2, 3, 4
)

data class GripperEquipment(
    val id: String,
    val name: String,
    val ratingOrWeight: String,
    val type: String // "Torsion Spring", "Adjustable Spring", "Pinch Gripper"
)
