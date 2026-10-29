package com.example.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.BestDayRecord
import com.example.data.model.HandRecord
import com.example.data.model.HandSide
import com.example.data.repository.AnalyticsSummary
import com.example.ui.components.ActivityHeatmap
import com.example.ui.components.VisualProgressChart
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.HandLeftColor
import com.example.ui.theme.HandRightColor
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted

@Composable
fun AnalyticsScreen(
    summary: AnalyticsSummary?,
    selectedHorizon: String,
    onSelectHorizon: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    if (summary == null) {
        Box(
            modifier = modifier.fillMaxSize(),
            contentAlignment = Alignment.Center
        ) {
            CircularProgressIndicator(color = ElectricCyan)
        }
        return
    }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = "GRIP ANALYTICS",
                style = MaterialTheme.typography.labelSmall,
                color = ElectricCyan,
                letterSpacing = 1.2.sp
            )
            Text(
                text = "Performance & Records",
                style = MaterialTheme.typography.displayMedium,
                fontWeight = FontWeight.Bold,
                color = SlateLight
            )
        }

        // Card 1: Best Day You Did Most Repetitions
        item {
            BestDayCard(bestDay = summary.bestDay)
        }

        // Cards 2 & 3: Best Records for Left Hand & Right Hand
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                HandRecordCard(
                    title = "LEFT HAND PR",
                    hand = HandSide.LEFT,
                    record = summary.leftRecord,
                    color = HandLeftColor,
                    modifier = Modifier.weight(1f)
                )
                HandRecordCard(
                    title = "RIGHT HAND PR",
                    hand = HandSide.RIGHT,
                    record = summary.rightRecord,
                    color = HandRightColor,
                    modifier = Modifier.weight(1f)
                )
            }
        }

        // Visual Progress Chart: Week, Month, Year
        item {
            VisualProgressChart(
                points = summary.chartPoints,
                selectedHorizon = selectedHorizon,
                onHorizonSelected = onSelectHorizon
            )
        }

        // Activity Calendar Heatmap with Cool Color Squares
        item {
            ActivityHeatmap(
                days = summary.heatmapDays
            )
        }

        // Lifetime Volume Stats Bar
        item {
            LifetimeSummaryCard(
                totalReps = summary.totalRepsAllTime,
                leftReps = summary.leftRepsAllTime,
                rightReps = summary.rightRepsAllTime
            )
        }

        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }
}

@Composable
fun BestDayCard(
    bestDay: BestDayRecord?,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, FlameOrange.copy(alpha = 0.5f), RoundedCornerShape(20.dp))
            .testTag("best_day_record_card")
    ) {
        Row(
            modifier = Modifier.padding(18.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(52.dp)
                    .clip(CircleShape)
                    .background(FlameOrange.copy(alpha = 0.2f)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = Icons.Default.EmojiEvents,
                    contentDescription = "Trophy",
                    tint = FlameOrange,
                    modifier = Modifier.size(28.dp)
                )
            }

            Spacer(modifier = Modifier.width(16.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "BEST DAY RECORD",
                    style = MaterialTheme.typography.labelSmall,
                    color = FlameOrange,
                    letterSpacing = 1.2.sp
                )
                Text(
                    text = if (bestDay != null) "${bestDay.totalReps} Repetitions" else "No Data Yet",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateLight
                )
                Text(
                    text = if (bestDay != null) "Achieved on ${bestDay.dateString}" else "Log workouts to set your record",
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted
                )
            }
        }
    }
}

@Composable
fun HandRecordCard(
    title: String,
    hand: HandSide,
    record: HandRecord,
    color: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(18.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
            .border(1.dp, color.copy(alpha = 0.4f), RoundedCornerShape(18.dp))
            .testTag("hand_record_${hand.name.lowercase()}")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = title,
                    style = MaterialTheme.typography.labelSmall,
                    color = color,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.sp
                )
                Box(
                    modifier = Modifier
                        .size(26.dp)
                        .clip(CircleShape)
                        .background(color.copy(alpha = 0.15f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.PanTool,
                        contentDescription = null,
                        tint = color,
                        modifier = Modifier.size(14.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Peak Single Set Reps
            Text(
                text = "${record.maxRepsInSingleSet} Reps",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = SlateLight
            )
            Text(
                text = "Max Set Reps",
                style = MaterialTheme.typography.labelSmall,
                color = SlateMuted
            )

            Spacer(modifier = Modifier.height(8.dp))

            // Peak Resistance & Lifetime Volume
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(
                        text = record.bestResistance,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight
                    )
                    Text(
                        text = "Peak Level",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = SlateMuted
                    )
                }

                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = "${record.totalLifetimeReps}",
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        color = color
                    )
                    Text(
                        text = "Total Reps",
                        style = MaterialTheme.typography.labelSmall,
                        fontSize = 10.sp,
                        color = SlateMuted
                    )
                }
            }
        }
    }
}

@Composable
fun LifetimeSummaryCard(
    totalReps: Int,
    leftReps: Int,
    rightReps: Int,
    modifier: Modifier = Modifier
) {
    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceAround,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$totalReps",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold,
                    color = SlateLight
                )
                Text(
                    text = "Total Reps Logged",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }

            Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$leftReps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HandLeftColor
                )
                Text(
                    text = "Left Hand",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }

            Box(modifier = Modifier.width(1.dp).height(30.dp).background(DarkBorder))

            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    text = "$rightReps",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = HandRightColor
                )
                Text(
                    text = "Right Hand",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )
            }
        }
    }
}
