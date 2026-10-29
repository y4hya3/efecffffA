package com.example.ui.screens

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.FlashOn
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.PanTool
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.GripperEquipment
import com.example.data.model.HandSide
import com.example.data.model.SeriesWithSets
import com.example.data.model.UserProfile
import com.example.ui.components.HandComparisonBar
import com.example.ui.components.RestTimerBar
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.HandLeftColor
import com.example.ui.theme.HandRightColor
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@Composable
fun HomeScreen(
    userProfile: UserProfile,
    seriesList: List<SeriesWithSets>,
    grippers: List<GripperEquipment>,
    restSecondsRemaining: Int,
    restTotalSeconds: Int,
    isTimerRunning: Boolean,
    onStartRestPreset: (Int) -> Unit,
    onPauseTimer: () -> Unit,
    onResumeTimer: () -> Unit,
    onResetTimer: () -> Unit,
    onAddTimerSeconds: (Int) -> Unit,
    onQuickStartSession: (String?) -> Unit,
    onStartNewSeries: (String) -> Unit,
    onLogQuickSqueeze: (HandSide) -> Unit,
    onEditSeries: (SeriesWithSets) -> Unit,
    onDeleteSeries: (Long) -> Unit,
    onUpdateAthleteName: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showNameDialog by remember { mutableStateOf(false) }

    // Aggregate today's reps
    val todayKey = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    val todaySeries = seriesList.filter {
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date(it.series.createdAt)) == todayKey
    }
    val todayReps = todaySeries.sumOf { it.totalReps }
    val todayLeftReps = todaySeries.sumOf { it.leftReps }
    val todayRightReps = todaySeries.sumOf { it.rightReps }

    LazyColumn(
        modifier = modifier
            .fillMaxSize()
            .padding(horizontal = 16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        item {
            Spacer(modifier = Modifier.height(8.dp))

            // Greeting Header Row: "Welcome, [Name]" + Rank
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .clickable { showNameDialog = true }
                            .testTag("welcome_name_header")
                    ) {
                        Text(
                            text = "Welcome, ${userProfile.name}",
                            style = MaterialTheme.typography.displayMedium,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit Name",
                            tint = SlateMuted,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                    Text(
                        text = "${userProfile.rankTitle} • Pure Handgrip Training",
                        style = MaterialTheme.typography.bodyMedium,
                        color = ElectricCyan
                    )
                }
            }
        }

        // QUICK-START HERO CARD
        item {
            Surface(
                shape = RoundedCornerShape(22.dp),
                color = Color.Transparent,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(
                        1.dp,
                        Brush.horizontalGradient(listOf(FlameOrange, ElectricCyan)),
                        RoundedCornerShape(22.dp)
                    )
                    .testTag("quick_start_hero_card")
            ) {
                Box(
                    modifier = Modifier
                        .background(
                            Brush.linearGradient(
                                colors = listOf(Color(0xFF261208), Color(0xFF0F1E28), Color(0xFF131822))
                            )
                        )
                        .padding(20.dp)
                ) {
                    Column {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Surface(
                                    shape = RoundedCornerShape(8.dp),
                                    color = FlameOrange.copy(alpha = 0.2f)
                                ) {
                                    Text(
                                        text = "READY TO TRAIN",
                                        style = MaterialTheme.typography.labelSmall,
                                        fontWeight = FontWeight.Bold,
                                        color = FlameOrange,
                                        letterSpacing = 1.2.sp,
                                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                                    )
                                }
                                Spacer(modifier = Modifier.height(6.dp))
                                Text(
                                    text = "Handgrip Session",
                                    style = MaterialTheme.typography.titleLarge,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateLight
                                )
                                Text(
                                    text = "Log bilateral crush sets & track endurance",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = SlateMuted
                                )
                            }

                            Box(
                                modifier = Modifier
                                    .size(50.dp)
                                    .clip(CircleShape)
                                    .background(ElectricCyan.copy(alpha = 0.15f))
                                    .border(1.dp, ElectricCyan, CircleShape),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(
                                    imageVector = Icons.Default.PanTool,
                                    contentDescription = "Handgrip",
                                    tint = ElectricCyan,
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.height(16.dp))

                        // Large Quick Start Button
                        Button(
                            onClick = { onQuickStartSession(null) },
                            colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                            shape = RoundedCornerShape(14.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp)
                                .testTag("quick_start_session_button")
                        ) {
                            Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                            Spacer(modifier = Modifier.width(8.dp))
                            Text(
                                text = "QUICK-START GRIP SESSION",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = Color.White,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }
            }
        }

        // LIVE INSTANT HAND SQUEEZE COUNTERS (Functional 1-tap logging while holding a gripper)
        item {
            Surface(
                shape = RoundedCornerShape(18.dp),
                color = DarkSurfaceElevated,
                modifier = Modifier
                    .fillMaxWidth()
                    .border(1.dp, DarkBorder, RoundedCornerShape(18.dp))
                    .testTag("live_squeeze_logger_card")
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            text = "INSTANT SQUEEZE LOG",
                            style = MaterialTheme.typography.labelSmall,
                            color = SlateMuted,
                            letterSpacing = 1.sp
                        )
                        Text(
                            text = "Tap when crushing gripper",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = ElectricCyan
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(10.dp)
                    ) {
                        // Left Hand Squeeze Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = HandLeftColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HandLeftColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onLogQuickSqueeze(HandSide.LEFT) }
                                .testTag("quick_squeeze_left_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PanTool, contentDescription = null, tint = HandLeftColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "+1 LEFT", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HandLeftColor)
                                    Text(text = "Squeeze Rep", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = SlateMuted)
                                }
                            }
                        }

                        // Right Hand Squeeze Button
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = HandRightColor.copy(alpha = 0.15f),
                            border = androidx.compose.foundation.BorderStroke(1.dp, HandRightColor.copy(alpha = 0.6f)),
                            modifier = Modifier
                                .weight(1f)
                                .clip(RoundedCornerShape(14.dp))
                                .clickable { onLogQuickSqueeze(HandSide.RIGHT) }
                                .testTag("quick_squeeze_right_button")
                        ) {
                            Row(
                                modifier = Modifier.padding(vertical = 12.dp, horizontal = 10.dp),
                                horizontalArrangement = Arrangement.Center,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Icon(Icons.Default.PanTool, contentDescription = null, tint = HandRightColor, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(8.dp))
                                Column {
                                    Text(text = "+1 RIGHT", style = MaterialTheme.typography.labelMedium, fontWeight = FontWeight.Bold, color = HandRightColor)
                                    Text(text = "Squeeze Rep", style = MaterialTheme.typography.labelSmall, fontSize = 9.sp, color = SlateMuted)
                                }
                            }
                        }
                    }
                }
            }
        }

        // Rest Stopwatch Widget
        item {
            RestTimerBar(
                secondsRemaining = restSecondsRemaining,
                totalSeconds = restTotalSeconds,
                isRunning = isTimerRunning,
                onStartPreset = onStartRestPreset,
                onPause = onPauseTimer,
                onResume = onResumeTimer,
                onReset = onResetTimer,
                onAddSeconds = onAddTimerSeconds
            )
        }

        // Today's Bilateral Forearm Balance Bar
        item {
            Column {
                Text(
                    text = "TODAY'S FOREARM SYMMETRY",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(6.dp))
                HandComparisonBar(
                    leftReps = todayLeftReps,
                    rightReps = todayRightReps
                )
            }
        }

        // Gripper Selection Carousel
        item {
            Column {
                Text(
                    text = "TRAIN BY GRIPPER",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted,
                    letterSpacing = 1.2.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    items(grippers) { gripper ->
                        Surface(
                            shape = RoundedCornerShape(14.dp),
                            color = DarkSurfaceElevated,
                            modifier = Modifier
                                .clip(RoundedCornerShape(14.dp))
                                .border(1.dp, DarkBorder, RoundedCornerShape(14.dp))
                                .clickable { onQuickStartSession("${gripper.name} (${gripper.ratingOrWeight})") }
                                .testTag("gripper_quick_chip_${gripper.id}")
                        ) {
                            Column(
                                modifier = Modifier.padding(horizontal = 14.dp, vertical = 10.dp)
                            ) {
                                Text(
                                    text = gripper.name,
                                    style = MaterialTheme.typography.labelMedium,
                                    fontWeight = FontWeight.Bold,
                                    color = SlateLight
                                )
                                Text(
                                    text = "Rating: ${gripper.ratingOrWeight}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = ElectricCyan
                                )
                            }
                        }
                    }
                }
            }
        }

        // OVERVIEW OF RECENT WORKOUT SESSIONS Header
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = "SESSION OVERVIEW",
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "Recent Handgrip Workouts",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight
                    )
                }
                Text(
                    text = "${seriesList.size} Total",
                    style = MaterialTheme.typography.labelSmall,
                    color = ElectricCyan
                )
            }
        }

        // Recent Series List Cards
        if (seriesList.isEmpty()) {
            item {
                Surface(
                    shape = RoundedCornerShape(16.dp),
                    color = DarkSurface,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                ) {
                    Column(
                        horizontalAlignment = Alignment.CenterHorizontally,
                        modifier = Modifier.padding(24.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.PanTool,
                            contentDescription = null,
                            tint = SlateMuted,
                            modifier = Modifier.size(44.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No grip sessions recorded yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateLight
                        )
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(
                            text = "Tap 'Quick-Start Grip Session' to begin tracking!",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }
            }
        } else {
            items(seriesList, key = { it.series.id }) { seriesItem ->
                SeriesCard(
                    seriesWithSets = seriesItem,
                    onEdit = { onEditSeries(seriesItem) },
                    onDelete = { onDeleteSeries(seriesItem.series.id) }
                )
            }
        }

        // Space at bottom for floating curved navigation bar
        item {
            Spacer(modifier = Modifier.height(88.dp))
        }
    }

    // Athlete Name Dialog
    if (showNameDialog) {
        var inputName by remember { mutableStateOf(userProfile.name) }
        AlertDialog(
            onDismissRequest = { showNameDialog = false },
            title = { Text("Your Athlete Name", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Set your name to personalize your grip dashboard:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = inputName,
                        onValueChange = { inputName = it },
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("athlete_name_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (inputName.isNotBlank()) {
                            onUpdateAthleteName(inputName.trim())
                        }
                        showNameDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier.testTag("save_athlete_name_button")
                ) {
                    Text("Save", color = Color.Black, fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showNameDialog = false }) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}

@Composable
fun SeriesCard(
    seriesWithSets: SeriesWithSets,
    onEdit: () -> Unit,
    onDelete: () -> Unit
) {
    var showMenu by remember { mutableStateOf(false) }
    val dateFormat = remember { SimpleDateFormat("MMM d, h:mm a", Locale.getDefault()) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurface,
        modifier = Modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .clickable { onEdit() }
            .testTag("series_card_${seriesWithSets.series.id}")
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            // Card Top Row: Series Title, Date, Menu
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column {
                    Text(
                        text = seriesWithSets.series.name,
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight
                    )
                    Text(
                        text = dateFormat.format(Date(seriesWithSets.series.createdAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = SlateMuted
                    )
                }

                Box {
                    IconButton(
                        onClick = { showMenu = true },
                        modifier = Modifier.testTag("series_card_menu_${seriesWithSets.series.id}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.MoreVert,
                            contentDescription = "Options",
                            tint = SlateMuted
                        )
                    }
                    DropdownMenu(
                        expanded = showMenu,
                        onDismissRequest = { showMenu = false }
                    ) {
                        DropdownMenuItem(
                            text = { Text("Edit Series") },
                            onClick = {
                                showMenu = false
                                onEdit()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.Edit, contentDescription = null, tint = SlateLight)
                            }
                        )
                        DropdownMenuItem(
                            text = { Text("Delete Series", color = Color(0xFFFF5252)) },
                            onClick = {
                                showMenu = false
                                onDelete()
                            },
                            leadingIcon = {
                                Icon(Icons.Default.DeleteOutline, contentDescription = null, tint = Color(0xFFFF5252))
                            }
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Volume Stats: Sets, Total Reps, L/R Breakdown Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Sets Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E2433)
                ) {
                    Text(
                        text = "${seriesWithSets.sets.size} Sets",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                // Total Reps Badge
                Surface(
                    shape = RoundedCornerShape(8.dp),
                    color = Color(0xFF1E2433)
                ) {
                    Text(
                        text = "${seriesWithSets.totalReps} Reps Total",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Left Hand Reps
                Text(
                    text = "L: ${seriesWithSets.leftReps}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HandLeftColor
                )

                Text(
                    text = "•",
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )

                // Right Hand Reps
                Text(
                    text = "R: ${seriesWithSets.rightReps}",
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.Bold,
                    color = HandRightColor
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Sets horizontal preview chips
            LazyRow(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                items(seriesWithSets.sortedSets) { set ->
                    Surface(
                        shape = RoundedCornerShape(8.dp),
                        color = if (set.hand == HandSide.LEFT) HandLeftColor.copy(alpha = 0.12f) else HandRightColor.copy(alpha = 0.12f),
                        border = androidx.compose.foundation.BorderStroke(
                            0.5.dp,
                            if (set.hand == HandSide.LEFT) HandLeftColor.copy(alpha = 0.4f) else HandRightColor.copy(alpha = 0.4f)
                        )
                    ) {
                        Text(
                            text = "${if (set.hand == HandSide.LEFT) "L" else "R"} ${set.weightOrLevel} × ${set.reps}",
                            style = MaterialTheme.typography.labelSmall,
                            fontSize = 10.sp,
                            color = if (set.hand == HandSide.LEFT) HandLeftColor else HandRightColor,
                            modifier = Modifier.padding(horizontal = 6.dp, vertical = 3.dp)
                        )
                    }
                }
            }

            if (seriesWithSets.series.notes.isNotBlank()) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = seriesWithSets.series.notes,
                    style = MaterialTheme.typography.bodySmall,
                    color = SlateMuted,
                    maxLines = 2
                )
            }
        }
    }
}

