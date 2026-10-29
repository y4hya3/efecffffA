package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocalFireDepartment
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HeatmapDay
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.Heatmap0
import com.example.ui.theme.Heatmap1
import com.example.ui.theme.Heatmap2
import com.example.ui.theme.Heatmap3
import com.example.ui.theme.Heatmap4
import com.example.ui.theme.HeatmapPeak
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted

@Composable
fun ActivityHeatmap(
    days: List<HeatmapDay>,
    modifier: Modifier = Modifier
) {
    var selectedDay by remember(days) { mutableStateOf<HeatmapDay?>(null) }
    val scrollState = rememberScrollState()

    // Calculate total active days and streak
    val activeDaysCount = days.count { it.repsCount > 0 }
    val totalRepsLogged = days.sumOf { it.repsCount }

    // Group days into columns of 7 (each column is a week)
    val weeks = remember(days) {
        days.chunked(7)
    }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .testTag("activity_heatmap_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Title & Active Day Stats
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(RoundedCornerShape(10.dp))
                            .background(ElectricCyan.copy(alpha = 0.15f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.CalendarMonth,
                            contentDescription = "Activity Heatmap",
                            tint = ElectricCyan,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "WORKOUT CONSISTENCY",
                            style = MaterialTheme.typography.labelSmall,
                            color = ElectricCyan,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = "Activity Calendar",
                            style = MaterialTheme.typography.titleMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }
                }

                // Active days badge
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10141E),
                    modifier = Modifier.border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.LocalFireDepartment,
                            contentDescription = null,
                            tint = FlameOrange,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "$activeDaysCount Days Active",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Dynamic detail banner on tap
            selectedDay?.let { day ->
                Surface(
                    shape = RoundedCornerShape(10.dp),
                    color = Color(0xFF0F1420),
                    modifier = Modifier
                        .fillMaxWidth()
                        .border(1.dp, if (day.repsCount > 0) ElectricCyan.copy(alpha = 0.4f) else DarkBorder, RoundedCornerShape(10.dp))
                        .padding(bottom = 10.dp)
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(horizontal = 12.dp, vertical = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "${day.dateString} (${day.monthLabel} ${day.dayOfMonth})",
                            style = MaterialTheme.typography.bodySmall,
                            color = SlateMuted
                        )
                        Text(
                            text = if (day.repsCount > 0) "${day.repsCount} Reps Completed 🔥" else "Rest Day 💤",
                            style = MaterialTheme.typography.labelMedium,
                            fontWeight = FontWeight.Bold,
                            color = if (day.repsCount > 0) ElectricCyan else SlateMuted
                        )
                    }
                }
            }

            // Heatmap Grid: Horizontal Scrollable Weeks
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(scrollState)
                    .padding(vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                // Day of Week labels column (Mon, Wed, Fri)
                Column(
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                    modifier = Modifier.padding(end = 4.dp)
                ) {
                    listOf("M", "T", "W", "T", "F", "S", "S").forEachIndexed { i, label ->
                        Box(
                            modifier = Modifier.size(16.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            if (i % 2 == 0) {
                                Text(
                                    text = label,
                                    style = MaterialTheme.typography.labelSmall,
                                    fontSize = 9.sp,
                                    color = SlateMuted
                                )
                            }
                        }
                    }
                }

                // Week columns
                weeks.forEachIndexed { weekIndex, weekDays ->
                    Column(
                        verticalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        weekDays.forEach { day ->
                            val cellColor = when (day.intensityTier) {
                                0 -> Heatmap0
                                1 -> Heatmap1
                                2 -> Heatmap2
                                3 -> Heatmap3
                                4 -> Heatmap4
                                else -> HeatmapPeak
                            }

                            val isSelected = selectedDay?.dateString == day.dateString

                            Box(
                                modifier = Modifier
                                    .size(16.dp)
                                    .clip(RoundedCornerShape(3.dp))
                                    .background(cellColor)
                                    .then(
                                        if (isSelected) Modifier.border(1.5.dp, Color.White, RoundedCornerShape(3.dp))
                                        else Modifier.border(0.5.dp, Color(0xFF1E2532), RoundedCornerShape(3.dp))
                                    )
                                    .clickable { selectedDay = day }
                                    .testTag("heatmap_cell_${day.dateString}")
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Footer Legend: Less [ ■ ■ ■ ■ ■ ] More
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Total: $totalRepsLogged reps logged",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(3.dp)
                ) {
                    Text("Less", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = SlateMuted)
                    Spacer(modifier = Modifier.width(2.dp))
                    listOf(Heatmap0, Heatmap1, Heatmap2, Heatmap3, Heatmap4).forEach { color ->
                        Box(
                            modifier = Modifier
                                .size(11.dp)
                                .clip(RoundedCornerShape(2.dp))
                                .background(color)
                        )
                    }
                    Spacer(modifier = Modifier.width(2.dp))
                    Text("More", style = MaterialTheme.typography.labelSmall, fontSize = 10.sp, color = SlateMuted)
                }
            }
        }
    }
}
