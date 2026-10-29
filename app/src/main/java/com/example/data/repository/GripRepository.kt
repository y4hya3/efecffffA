package com.example.data.repository

import com.example.data.db.GripDao
import com.example.data.model.BestDayRecord
import com.example.data.model.ChartDataPoint
import com.example.data.model.GripSeries
import com.example.data.model.GripSet
import com.example.data.model.HandRecord
import com.example.data.model.HandSide
import com.example.data.model.HeatmapDay
import com.example.data.model.SeriesWithSets
import com.example.data.model.UserProfile
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

data class AnalyticsSummary(
    val bestDay: BestDayRecord?,
    val leftRecord: HandRecord,
    val rightRecord: HandRecord,
    val chartPoints: List<ChartDataPoint>,
    val heatmapDays: List<HeatmapDay>,
    val totalRepsAllTime: Int,
    val leftRepsAllTime: Int,
    val rightRepsAllTime: Int
)

class GripRepository(private val dao: GripDao) {

    val allSeries: Flow<List<SeriesWithSets>> = dao.getAllSeriesWithSets()
    val userProfile: Flow<UserProfile?> = dao.getUserProfile()

    suspend fun initializeStarterDataIfEmpty() = withContext(Dispatchers.IO) {
        if (dao.getSeriesCount() == 0) {
            // Seed default profile
            dao.saveUserProfile(
                UserProfile(
                    id = 1,
                    name = "Alex",
                    preferredUnit = "kg",
                    goalRepsPerDay = 60
                )
            )

            val now = System.currentTimeMillis()
            val oneDay = 86_400_000L

            // 1. Today's session: CoC Crush Pyramid
            val series1Id = dao.insertSeries(
                GripSeries(
                    name = "CoC Crush Pyramid",
                    createdAt = now - (2 * 3600_000L),
                    notes = "Explosive concentric squeeze, 2 sec hold at close"
                )
            )
            dao.insertSets(
                listOf(
                    GripSet(seriesId = series1Id, hand = HandSide.RIGHT, weightOrLevel = "40 kg", reps = 15, orderIndex = 0),
                    GripSet(seriesId = series1Id, hand = HandSide.LEFT, weightOrLevel = "40 kg", reps = 14, orderIndex = 1),
                    GripSet(seriesId = series1Id, hand = HandSide.RIGHT, weightOrLevel = "50 kg", reps = 12, orderIndex = 2),
                    GripSet(seriesId = series1Id, hand = HandSide.LEFT, weightOrLevel = "50 kg", reps = 10, orderIndex = 3),
                    GripSet(seriesId = series1Id, hand = HandSide.RIGHT, weightOrLevel = "60 kg", reps = 8, orderIndex = 4),
                    GripSet(seriesId = series1Id, hand = HandSide.LEFT, weightOrLevel = "60 kg", reps = 7, orderIndex = 5)
                )
            )

            // 2. Yesterday's session: Pinch Grip Endurance
            val series2Id = dao.insertSeries(
                GripSeries(
                    name = "Pinch Grip Endurance",
                    createdAt = now - oneDay,
                    notes = "Two-hand pinch block holds & high rep resets"
                )
            )
            dao.insertSets(
                listOf(
                    GripSet(seriesId = series2Id, hand = HandSide.RIGHT, weightOrLevel = "35 kg", reps = 20, orderIndex = 0),
                    GripSet(seriesId = series2Id, hand = HandSide.LEFT, weightOrLevel = "35 kg", reps = 18, orderIndex = 1),
                    GripSet(seriesId = series2Id, hand = HandSide.RIGHT, weightOrLevel = "45 kg", reps = 14, orderIndex = 2),
                    GripSet(seriesId = series2Id, hand = HandSide.LEFT, weightOrLevel = "45 kg", reps = 12, orderIndex = 3)
                )
            )

            // 3. 3 days ago: Max Peak Day (100+ reps)
            val series3Id = dao.insertSeries(
                GripSeries(
                    name = "Forearm Burnout Series",
                    createdAt = now - (3 * oneDay),
                    notes = "Extensor / flexor conditioning volume"
                )
            )
            dao.insertSets(
                listOf(
                    GripSet(seriesId = series3Id, hand = HandSide.RIGHT, weightOrLevel = "30 kg", reps = 25, orderIndex = 0),
                    GripSet(seriesId = series3Id, hand = HandSide.LEFT, weightOrLevel = "30 kg", reps = 25, orderIndex = 1),
                    GripSet(seriesId = series3Id, hand = HandSide.RIGHT, weightOrLevel = "40 kg", reps = 20, orderIndex = 2),
                    GripSet(seriesId = series3Id, hand = HandSide.LEFT, weightOrLevel = "40 kg", reps = 20, orderIndex = 3),
                    GripSet(seriesId = series3Id, hand = HandSide.RIGHT, weightOrLevel = "50 kg", reps = 16, orderIndex = 4),
                    GripSet(seriesId = series3Id, hand = HandSide.LEFT, weightOrLevel = "50 kg", reps = 15, orderIndex = 5)
                )
            )

            // 4. Historical entries for realistic heatmap & trend curves
            val pastDays = listOf(5, 7, 8, 11, 14, 18, 21, 28, 35, 42, 50, 60)
            pastDays.forEach { daysAgo ->
                val pastId = dao.insertSeries(
                    GripSeries(
                        name = "Heavy Gripper Routine",
                        createdAt = now - (daysAgo * oneDay),
                        notes = "Progressive overload session"
                    )
                )
                val baseReps = (8..18).random()
                dao.insertSets(
                    listOf(
                        GripSet(seriesId = pastId, hand = HandSide.RIGHT, weightOrLevel = "45 kg", reps = baseReps + 2, orderIndex = 0),
                        GripSet(seriesId = pastId, hand = HandSide.LEFT, weightOrLevel = "45 kg", reps = baseReps, orderIndex = 1),
                        GripSet(seriesId = pastId, hand = HandSide.RIGHT, weightOrLevel = "55 kg", reps = baseReps - 2, orderIndex = 2),
                        GripSet(seriesId = pastId, hand = HandSide.LEFT, weightOrLevel = "55 kg", reps = baseReps - 3, orderIndex = 3)
                    )
                )
            }
        }
    }

    suspend fun saveSeriesWithSets(series: GripSeries, sets: List<GripSet>): Long = withContext(Dispatchers.IO) {
        val seriesId = if (series.id == 0L) {
            dao.insertSeries(series)
        } else {
            dao.updateSeries(series)
            dao.deleteSetsBySeriesId(series.id)
            series.id
        }

        val indexedSets = sets.mapIndexed { index, gripSet ->
            gripSet.copy(seriesId = seriesId, orderIndex = index)
        }
        dao.insertSets(indexedSets)
        seriesId
    }

    suspend fun deleteSeries(seriesId: Long) = withContext(Dispatchers.IO) {
        dao.deleteSeriesById(seriesId)
    }

    suspend fun updateProfile(
        name: String,
        unit: String,
        goalReps: Int,
        rankTitle: String = "Crush Grip Specialist",
        defaultRestSeconds: Int = 60
    ) = withContext(Dispatchers.IO) {
        dao.saveUserProfile(
            UserProfile(
                id = 1,
                name = name,
                preferredUnit = unit,
                goalRepsPerDay = goalReps,
                rankTitle = rankTitle,
                defaultRestSeconds = defaultRestSeconds
            )
        )
    }

    fun computeAnalytics(allSeries: List<SeriesWithSets>, horizon: String): AnalyticsSummary {
        val dateFormat = SimpleDateFormat("MMM d, yyyy", Locale.getDefault())
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

        // Calculate all-time reps
        var totalAllTime = 0
        var leftAllTime = 0
        var rightAllTime = 0

        var maxLeftRepsSingleSet = 0
        var bestLeftResistance = "0 kg"
        var maxRightRepsSingleSet = 0
        var bestRightResistance = "0 kg"

        // Group reps by day
        val dailyMap = mutableMapOf<String, Pair<Long, Int>>() // dayKey -> (timestamp, totalReps)

        allSeries.forEach { seriesWithSets ->
            val dayKey = dayKeyFormat.format(Date(seriesWithSets.series.createdAt))
            val currentDaySum = dailyMap[dayKey]?.second ?: 0
            val addedReps = seriesWithSets.totalReps
            dailyMap[dayKey] = Pair(seriesWithSets.series.createdAt, currentDaySum + addedReps)

            seriesWithSets.sets.forEach { set ->
                totalAllTime += set.reps
                if (set.hand == HandSide.LEFT) {
                    leftAllTime += set.reps
                    if (set.reps > maxLeftRepsSingleSet) {
                        maxLeftRepsSingleSet = set.reps
                        bestLeftResistance = set.weightOrLevel
                    }
                } else {
                    rightAllTime += set.reps
                    if (set.reps > maxRightRepsSingleSet) {
                        maxRightRepsSingleSet = set.reps
                        bestRightResistance = set.weightOrLevel
                    }
                }
            }
        }

        // Best Day computation
        val bestDayEntry = dailyMap.maxByOrNull { it.value.second }
        val bestDay = bestDayEntry?.let { entry ->
            BestDayRecord(
                dateString = dateFormat.format(Date(entry.value.first)),
                timestamp = entry.value.first,
                totalReps = entry.value.second
            )
        }

        val leftRecord = HandRecord(
            hand = HandSide.LEFT,
            maxRepsInSingleSet = maxLeftRepsSingleSet,
            bestResistance = bestLeftResistance.ifEmpty { "N/A" },
            totalLifetimeReps = leftAllTime
        )

        val rightRecord = HandRecord(
            hand = HandSide.RIGHT,
            maxRepsInSingleSet = maxRightRepsSingleSet,
            bestResistance = bestRightResistance.ifEmpty { "N/A" },
            totalLifetimeReps = rightAllTime
        )

        // Compute chart points based on Horizon: "Week", "Month", "Year"
        val chartPoints = generateChartPoints(allSeries, horizon)

        // Compute heatmap (last 16 weeks / ~112 days)
        val heatmapDays = generateHeatmapDays(dailyMap)

        return AnalyticsSummary(
            bestDay = bestDay,
            leftRecord = leftRecord,
            rightRecord = rightRecord,
            chartPoints = chartPoints,
            heatmapDays = heatmapDays,
            totalRepsAllTime = totalAllTime,
            leftRepsAllTime = leftAllTime,
            rightRepsAllTime = rightAllTime
        )
    }

    private fun generateChartPoints(allSeries: List<SeriesWithSets>, horizon: String): List<ChartDataPoint> {
        val points = mutableListOf<ChartDataPoint>()
        val cal = Calendar.getInstance()

        when (horizon.uppercase()) {
            "WEEK" -> {
                // Past 7 days
                val dayFormat = SimpleDateFormat("EEE", Locale.getDefault())
                val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())

                for (i in 6 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.DAY_OF_YEAR, -i)
                    val key = dayKeyFormat.format(c.time)
                    val label = dayFormat.format(c.time)

                    var leftReps = 0
                    var rightReps = 0

                    allSeries.forEach { seriesWithSets ->
                        val sKey = dayKeyFormat.format(Date(seriesWithSets.series.createdAt))
                        if (sKey == key) {
                            seriesWithSets.sets.forEach { set ->
                                if (set.hand == HandSide.LEFT) leftReps += set.reps
                                else rightReps += set.reps
                            }
                        }
                    }
                    points.add(ChartDataPoint(label = label, leftReps = leftReps, rightReps = rightReps, timestamp = c.timeInMillis))
                }
            }
            "MONTH" -> {
                // Past 4 weeks (weekly buckets)
                val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                for (w in 3 downTo 0) {
                    val startCal = Calendar.getInstance()
                    startCal.add(Calendar.DAY_OF_YEAR, -(w * 7 + 6))
                    val endCal = Calendar.getInstance()
                    endCal.add(Calendar.DAY_OF_YEAR, -(w * 7))

                    val label = "W${4 - w}"
                    var leftReps = 0
                    var rightReps = 0

                    allSeries.forEach { seriesWithSets ->
                        val setDate = seriesWithSets.series.createdAt
                        if (setDate >= startCal.timeInMillis && setDate <= endCal.timeInMillis + 86400000L) {
                            seriesWithSets.sets.forEach { set ->
                                if (set.hand == HandSide.LEFT) leftReps += set.reps
                                else rightReps += set.reps
                            }
                        }
                    }
                    points.add(ChartDataPoint(label = label, leftReps = leftReps, rightReps = rightReps, timestamp = endCal.timeInMillis))
                }
            }
            "YEAR" -> {
                // Past 6-12 months
                val monthFormat = SimpleDateFormat("MMM", Locale.getDefault())
                val monthKeyFormat = SimpleDateFormat("yyyy-MM", Locale.getDefault())

                for (m in 5 downTo 0) {
                    val c = Calendar.getInstance()
                    c.add(Calendar.MONTH, -m)
                    val key = monthKeyFormat.format(c.time)
                    val label = monthFormat.format(c.time)

                    var leftReps = 0
                    var rightReps = 0

                    allSeries.forEach { seriesWithSets ->
                        val sKey = monthKeyFormat.format(Date(seriesWithSets.series.createdAt))
                        if (sKey == key) {
                            seriesWithSets.sets.forEach { set ->
                                if (set.hand == HandSide.LEFT) leftReps += set.reps
                                else rightReps += set.reps
                            }
                        }
                    }
                    points.add(ChartDataPoint(label = label, leftReps = leftReps, rightReps = rightReps, timestamp = c.timeInMillis))
                }
            }
        }
        return points
    }

    private fun generateHeatmapDays(dailyMap: Map<String, Pair<Long, Int>>): List<HeatmapDay> {
        val days = mutableListOf<HeatmapDay>()
        val dayKeyFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        val monthLabelFormat = SimpleDateFormat("MMM", Locale.getDefault())

        // 16 weeks = 112 days
        val totalDays = 112
        val cal = Calendar.getInstance()

        // Find max reps in any single day for dynamic tier scaling
        val maxReps = dailyMap.values.maxOfOrNull { it.second }?.coerceAtLeast(40) ?: 40

        for (i in (totalDays - 1) downTo 0) {
            val c = Calendar.getInstance()
            c.add(Calendar.DAY_OF_YEAR, -i)
            val key = dayKeyFormat.format(c.time)
            val reps = dailyMap[key]?.second ?: 0

            // Intensity 0..4
            val tier = when {
                reps == 0 -> 0
                reps <= maxReps * 0.25f -> 1
                reps <= maxReps * 0.50f -> 2
                reps <= maxReps * 0.75f -> 3
                else -> 4
            }

            days.add(
                HeatmapDay(
                    dateString = key,
                    dayOfWeek = c.get(Calendar.DAY_OF_WEEK),
                    dayOfMonth = c.get(Calendar.DAY_OF_MONTH),
                    monthLabel = monthLabelFormat.format(c.time),
                    repsCount = reps,
                    intensityTier = tier
                )
            )
        }
        return days
    }
}
