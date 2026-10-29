package com.example.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
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
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.ChartDataPoint
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.HandLeftColor
import com.example.ui.theme.HandRightColor
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted

@Composable
fun VisualProgressChart(
    points: List<ChartDataPoint>,
    selectedHorizon: String,
    onHorizonSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var selectedIndex by remember(points, selectedHorizon) { mutableStateOf<Int?>(null) }

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(20.dp))
            .testTag("visual_progress_chart_card")
    ) {
        Column(
            modifier = Modifier.padding(18.dp)
        ) {
            // Header Row: Title & Horizon Selector Tabs
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "BILATERAL PROGRESSION",
                        style = MaterialTheme.typography.labelSmall,
                        color = ElectricCyan,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Repetition Trends",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight
                    )
                }

                // Time Horizon Tabs [ Week | Month | Year ]
                Row(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF0F131C))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(3.dp)
                ) {
                    listOf("Week", "Month", "Year").forEach { horizon ->
                        val isSelected = horizon.equals(selectedHorizon, ignoreCase = true)
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(9.dp))
                                .background(if (isSelected) ElectricCyan else Color.Transparent)
                                .clickable {
                                    selectedIndex = null
                                    onHorizonSelected(horizon)
                                }
                                .padding(horizontal = 10.dp, vertical = 6.dp)
                                .testTag("horizon_tab_${horizon.lowercase()}")
                        ) {
                            Text(
                                text = horizon,
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                color = if (isSelected) Color.Black else SlateMuted
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Legend & Highlight Info Row
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Legend
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(HandLeftColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Left Hand", style = MaterialTheme.typography.labelSmall, color = SlateLight)

                    Spacer(modifier = Modifier.width(16.dp))

                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .clip(CircleShape)
                            .background(HandRightColor)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Right Hand", style = MaterialTheme.typography.labelSmall, color = SlateLight)
                }

                // Selected point readout
                selectedIndex?.let { idx ->
                    if (idx in points.indices) {
                        val pt = points[idx]
                        Text(
                            text = "${pt.label}: L ${pt.leftReps} / R ${pt.rightReps} reps",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Canvas Chart Area
            if (points.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Text("No workout data recorded yet for this horizon", color = SlateMuted)
                }
            } else {
                val maxReps = points.maxOf { maxOf(it.leftReps, it.rightReps) }.coerceAtLeast(10)

                Box(modifier = Modifier.fillMaxWidth().height(180.dp)) {
                    Canvas(
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(180.dp)
                            .pointerInput(points) {
                                detectTapGestures { tapOffset ->
                                    val count = points.size
                                    if (count > 0) {
                                        val segmentWidth = size.width / count
                                        val index = (tapOffset.x / segmentWidth).toInt().coerceIn(0, count - 1)
                                        selectedIndex = index
                                    }
                                }
                            }
                    ) {
                        val w = size.width
                        val h = size.height - 30.dp.toPx() // bottom margin for labels
                        val pointCount = points.size
                        val stepX = if (pointCount > 1) w / (pointCount - 1) else w / 2

                        // Draw Grid Lines (3 horizontal lines)
                        for (i in 0..2) {
                            val y = h * (i / 2f)
                            drawLine(
                                color = Color(0xFF222B3D),
                                start = Offset(0f, y),
                                end = Offset(w, y),
                                strokeWidth = 1.dp.toPx()
                            )
                        }

                        // Compute Coordinates
                        val leftOffsets = mutableListOf<Offset>()
                        val rightOffsets = mutableListOf<Offset>()

                        points.forEachIndexed { i, p ->
                            val x = if (pointCount > 1) i * stepX else w / 2
                            val leftY = h - (p.leftReps.toFloat() / maxReps * h)
                            val rightY = h - (p.rightReps.toFloat() / maxReps * h)
                            leftOffsets.add(Offset(x, leftY))
                            rightOffsets.add(Offset(x, rightY))
                        }

                        // Draw Left Hand Path & Area Fill
                        if (leftOffsets.isNotEmpty()) {
                            val leftPath = Path().apply {
                                moveTo(leftOffsets.first().x, leftOffsets.first().y)
                                for (i in 1 until leftOffsets.size) {
                                    val prev = leftOffsets[i - 1]
                                    val curr = leftOffsets[i]
                                    val midX = (prev.x + curr.x) / 2
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }

                            // Gradient Area
                            val leftAreaPath = Path().apply {
                                addPath(leftPath)
                                lineTo(leftOffsets.last().x, h)
                                lineTo(leftOffsets.first().x, h)
                                close()
                            }
                            drawPath(
                                path = leftAreaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(HandLeftColor.copy(alpha = 0.25f), Color.Transparent),
                                    startY = 0f,
                                    endY = h
                                )
                            )

                            // Stroke
                            drawPath(
                                path = leftPath,
                                color = HandLeftColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw Right Hand Path & Area Fill
                        if (rightOffsets.isNotEmpty()) {
                            val rightPath = Path().apply {
                                moveTo(rightOffsets.first().x, rightOffsets.first().y)
                                for (i in 1 until rightOffsets.size) {
                                    val prev = rightOffsets[i - 1]
                                    val curr = rightOffsets[i]
                                    val midX = (prev.x + curr.x) / 2
                                    cubicTo(midX, prev.y, midX, curr.y, curr.x, curr.y)
                                }
                            }

                            val rightAreaPath = Path().apply {
                                addPath(rightPath)
                                lineTo(rightOffsets.last().x, h)
                                lineTo(rightOffsets.first().x, h)
                                close()
                            }
                            drawPath(
                                path = rightAreaPath,
                                brush = Brush.verticalGradient(
                                    colors = listOf(HandRightColor.copy(alpha = 0.25f), Color.Transparent),
                                    startY = 0f,
                                    endY = h
                                )
                            )

                            drawPath(
                                path = rightPath,
                                color = HandRightColor,
                                style = Stroke(width = 3.dp.toPx(), cap = StrokeCap.Round)
                            )
                        }

                        // Draw Data Point Dots
                        leftOffsets.forEachIndexed { i, offset ->
                            drawCircle(
                                color = if (selectedIndex == i) Color.White else HandLeftColor,
                                radius = if (selectedIndex == i) 6.dp.toPx() else 4.dp.toPx(),
                                center = offset
                            )
                        }
                        rightOffsets.forEachIndexed { i, offset ->
                            drawCircle(
                                color = if (selectedIndex == i) Color.White else HandRightColor,
                                radius = if (selectedIndex == i) 6.dp.toPx() else 4.dp.toPx(),
                                center = offset
                            )
                        }
                    }

                    // Bottom X-Axis Text Labels
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .align(Alignment.BottomCenter)
                            .padding(top = 155.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        points.forEachIndexed { idx, p ->
                            Text(
                                text = p.label,
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 10.sp,
                                color = if (selectedIndex == idx) SlateLight else SlateMuted,
                                fontWeight = if (selectedIndex == idx) FontWeight.Bold else FontWeight.Normal
                            )
                        }
                    }
                }
            }
        }
    }
}
