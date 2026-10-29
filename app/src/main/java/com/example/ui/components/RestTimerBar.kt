package com.example.ui.components

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted

@Composable
fun RestTimerBar(
    secondsRemaining: Int,
    totalSeconds: Int,
    isRunning: Boolean,
    onStartPreset: (Int) -> Unit,
    onPause: () -> Unit,
    onResume: () -> Unit,
    onReset: () -> Unit,
    onAddSeconds: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val progress by animateFloatAsState(
        targetValue = if (totalSeconds > 0) secondsRemaining.toFloat() / totalSeconds.toFloat() else 0f,
        label = "restProgress"
    )

    Surface(
        shape = RoundedCornerShape(20.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, if (isRunning) ElectricCyan.copy(alpha = 0.5f) else DarkBorder, RoundedCornerShape(20.dp))
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) ElectricCyan.copy(alpha = 0.15f) else DarkBorder),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Timer,
                            contentDescription = "Rest Stopwatch",
                            tint = if (isRunning) ElectricCyan else SlateMuted,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Column {
                        Text(
                            text = "REST STOPWATCH",
                            style = MaterialTheme.typography.labelSmall,
                            color = if (isRunning) ElectricCyan else SlateMuted,
                            letterSpacing = 1.2.sp
                        )
                        Text(
                            text = if (isRunning) "Active Recovery" else if (secondsRemaining > 0) "Paused" else "Ready for Next Set",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.SemiBold,
                            color = SlateLight
                        )
                    }
                }

                // Presets
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    listOf(30, 60, 90).forEach { preset ->
                        Surface(
                            shape = RoundedCornerShape(8.dp),
                            color = if (totalSeconds == preset && (isRunning || secondsRemaining > 0)) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF131722),
                            modifier = Modifier.clip(RoundedCornerShape(8.dp))
                        ) {
                            TextButton(
                                onClick = { onStartPreset(preset) },
                                modifier = Modifier
                                    .height(32.dp)
                                    .padding(horizontal = 4.dp)
                                    .testTag("timer_preset_${preset}s")
                            ) {
                                Text(
                                    text = "${preset}s",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (totalSeconds == preset && (isRunning || secondsRemaining > 0)) ElectricCyan else SlateMuted
                                )
                            }
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Timer display & controls
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        contentAlignment = Alignment.Center,
                        modifier = Modifier.size(54.dp)
                    ) {
                        CircularProgressIndicator(
                            progress = { 1f },
                            modifier = Modifier.size(54.dp),
                            color = Color(0xFF222B3D),
                            strokeWidth = 4.dp
                        )
                        CircularProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.size(54.dp),
                            color = if (secondsRemaining <= 10 && secondsRemaining > 0) FlameOrange else ElectricCyan,
                            strokeWidth = 4.dp
                        )
                        val mins = secondsRemaining / 60
                        val secs = secondsRemaining % 60
                        Text(
                            text = String.format("%02d:%02d", mins, secs),
                            style = MaterialTheme.typography.labelLarge,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }

                    Spacer(modifier = Modifier.width(14.dp))

                    Column {
                        Text(
                            text = if (secondsRemaining > 0) "$secondsRemaining seconds left" else "Interval complete!",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Medium,
                            color = if (secondsRemaining == 0) FlameOrange else SlateLight
                        )
                        Text(
                            text = "Bilateral forearm recovery",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted
                        )
                    }
                }

                // Controls
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (secondsRemaining > 0) {
                        IconButton(
                            onClick = { onAddSeconds(15) },
                            modifier = Modifier.testTag("timer_add_15s")
                        ) {
                            Icon(
                                imageVector = Icons.Default.Add,
                                contentDescription = "Add 15s",
                                tint = ElectricCyan
                            )
                        }
                    }

                    IconButton(
                        onClick = {
                            if (isRunning) onPause()
                            else onResume()
                        },
                        modifier = Modifier
                            .size(42.dp)
                            .clip(CircleShape)
                            .background(if (isRunning) FlameOrange else ElectricCyan)
                            .testTag("timer_play_pause_button")
                    ) {
                        Icon(
                            imageVector = if (isRunning) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isRunning) "Pause" else "Play",
                            tint = Color.Black
                        )
                    }

                    Spacer(modifier = Modifier.width(6.dp))

                    IconButton(
                        onClick = onReset,
                        modifier = Modifier.testTag("timer_reset_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Reset Timer",
                            tint = SlateMuted
                        )
                    }
                }
            }
        }
    }
}
