package com.example.data.db

import androidx.room.TypeConverter
import com.example.data.model.HandSide

class Converters {
    @TypeConverter
    fun fromHandSide(value: HandSide): String = value.name

    @TypeConverter
    fun toHandSide(value: String): HandSide = try {
        HandSide.valueOf(value)
    } catch (_: Exception) {
        HandSide.RIGHT
    }
}
