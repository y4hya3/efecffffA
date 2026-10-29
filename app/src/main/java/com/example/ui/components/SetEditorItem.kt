package com.example.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowDownward
import androidx.compose.material.icons.filled.ArrowUpward
import androidx.compose.material.icons.filled.DeleteOutline
import androidx.compose.material.icons.filled.DragHandle
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HandSide
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.HandLeftColor
import com.example.ui.theme.HandRightColor
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.viewmodel.DraftSet

@Composable
fun SetEditorItem(
    index: Int,
    totalSets: Int,
    set: DraftSet,
    onToggleHand: () -> Unit,
    onIncrementReps: (Int) -> Unit,
    onSetRepsDirect: (Int) -> Unit,
    onSetWeightDirect: (String) -> Unit,
    onMoveUp: () -> Unit,
    onMoveDown: () -> Unit,
    onDelete: () -> Unit,
    modifier: Modifier = Modifier
) {
    var showRepTypeDialog by remember { mutableStateOf(false) }
    var showWeightDialog by remember { mutableStateOf(false) }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
            .fillMaxWidth()
            .border(
                1.dp,
                if (set.hand == HandSide.LEFT) HandLeftColor.copy(alpha = 0.35f) else HandRightColor.copy(alpha = 0.35f),
                RoundedCornerShape(16.dp)
            )
            .testTag("set_editor_item_${set.tempId}")
    ) {
        Column(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp)
        ) {
            // Header Row: Set index, Hand Selector Chip, Reorder Buttons & Delete
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // Set # Badge
                    Box(
                        modifier = Modifier
                            .size(28.dp)
                            .clip(CircleShape)
                            .background(Color(0xFF222B3D)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = "#${index + 1}",
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.Bold,
                            color = SlateLight
                        )
                    }

                    Spacer(modifier = Modifier.width(8.dp))

                    // Hand Selector Chip (Left vs Right)
                    Surface(
                        shape = RoundedCornerShape(20.dp),
                        color = if (set.hand == HandSide.LEFT) HandLeftColor.copy(alpha = 0.2f) else HandRightColor.copy(alpha = 0.2f),
                        modifier = Modifier
                            .clip(RoundedCornerShape(20.dp))
                            .clickable { onToggleHand() }
                            .border(
                                1.dp,
                                if (set.hand == HandSide.LEFT) HandLeftColor else HandRightColor,
                                RoundedCornerShape(20.dp)
                            )
                            .testTag("hand_toggle_button_${set.tempId}")
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .clip(CircleShape)
                                    .background(if (set.hand == HandSide.LEFT) HandLeftColor else HandRightColor)
                            )
                            Spacer(modifier = Modifier.width(6.dp))
                            Text(
                                text = if (set.hand == HandSide.LEFT) "LEFT HAND" else "RIGHT HAND",
                                style = MaterialTheme.typography.labelSmall,
                                fontWeight = FontWeight.Bold,
                                color = if (set.hand == HandSide.LEFT) HandLeftColor else HandRightColor,
                                letterSpacing = 0.8.sp
                            )
                        }
                    }
                }

                // Reorder Nudges & Actions
                Row(verticalAlignment = Alignment.CenterVertically) {
                    if (index > 0) {
                        IconButton(
                            onClick = onMoveUp,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("set_move_up_${set.tempId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowUpward,
                                contentDescription = "Move set up",
                                tint = SlateMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }
                    if (index < totalSets - 1) {
                        IconButton(
                            onClick = onMoveDown,
                            modifier = Modifier
                                .size(32.dp)
                                .testTag("set_move_down_${set.tempId}")
                        ) {
                            Icon(
                                imageVector = Icons.Default.ArrowDownward,
                                contentDescription = "Move set down",
                                tint = SlateMuted,
                                modifier = Modifier.size(18.dp)
                            )
                        }
                    }

                    // Drag handle visual indicator
                    Icon(
                        imageVector = Icons.Default.DragHandle,
                        contentDescription = "Reorder handle",
                        tint = Color(0xFF4A5568),
                        modifier = Modifier
                            .size(22.dp)
                            .padding(horizontal = 2.dp)
                    )

                    Spacer(modifier = Modifier.width(4.dp))

                    IconButton(
                        onClick = onDelete,
                        modifier = Modifier
                            .size(32.dp)
                            .testTag("delete_set_button_${set.tempId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.DeleteOutline,
                            contentDescription = "Delete set",
                            tint = Color(0xFFFF5252),
                            modifier = Modifier.size(20.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Body Row: Resistance / Weight Chip & Rep Counter
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Resistance / Weight / Gripper Level Chip
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF10141D),
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .clickable { showWeightDialog = true }
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .testTag("set_weight_chip_${set.tempId}")
                ) {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 8.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = SlateMuted,
                            modifier = Modifier.size(16.dp)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Column {
                            Text(
                                text = "RESISTANCE",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = SlateMuted
                            )
                            Text(
                                text = set.weightOrLevel,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                        }
                        Spacer(modifier = Modifier.width(4.dp))
                        Icon(
                            imageVector = Icons.Default.Edit,
                            contentDescription = "Edit weight",
                            tint = SlateMuted,
                            modifier = Modifier.size(12.dp)
                        )
                    }
                }

                // Repetitions Stepper [-] [ 12 Reps ] [+]
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(Color(0xFF10141D))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                        .padding(2.dp)
                ) {
                    // Decrease Rep Button
                    IconButton(
                        onClick = { onIncrementReps(-1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("rep_decrease_button_${set.tempId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Remove,
                            contentDescription = "Decrease repetition",
                            tint = FlameOrange,
                            modifier = Modifier.size(18.dp)
                        )
                    }

                    // Tappable Rep Number to Type Direct
                    Box(
                        modifier = Modifier
                            .clickable { showRepTypeDialog = true }
                            .padding(horizontal = 10.dp, vertical = 6.dp)
                            .testTag("rep_value_display_${set.tempId}"),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                text = "${set.reps}",
                                style = MaterialTheme.typography.titleMedium,
                                fontWeight = FontWeight.Bold,
                                color = SlateLight
                            )
                            Text(
                                text = "REPS",
                                style = MaterialTheme.typography.labelSmall,
                                fontSize = 9.sp,
                                color = SlateMuted
                            )
                        }
                    }

                    // Increase Rep Button
                    IconButton(
                        onClick = { onIncrementReps(1) },
                        modifier = Modifier
                            .size(36.dp)
                            .testTag("rep_increase_button_${set.tempId}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Add,
                            contentDescription = "Increase repetition",
                            tint = ElectricCyan,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }

    // Direct Rep typing dialog
    if (showRepTypeDialog) {
        var repInputText by remember { mutableStateOf(set.reps.toString()) }
        AlertDialog(
            onDismissRequest = { showRepTypeDialog = false },
            title = {
                Text("Set Repetitions", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Enter the exact number of reps completed:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    OutlinedTextField(
                        value = repInputText,
                        onValueChange = { repInputText = it.filter { ch -> ch.isDigit() } },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("direct_rep_input_field")
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val parsed = repInputText.toIntOrNull() ?: set.reps
                        onSetRepsDirect(parsed)
                        showRepTypeDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = FlameOrange),
                    modifier = Modifier.testTag("save_rep_input_button")
                ) {
                    Text("Save Reps")
                }
            },
            dismissButton = {
                TextButton(onClick = { showRepTypeDialog = false }) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }

    // Resistance / Gripper Level dialog
    if (showWeightDialog) {
        var weightInputText by remember { mutableStateOf(set.weightOrLevel) }
        val quickPresets = listOf("30 kg", "40 kg", "50 kg", "60 kg", "75 kg", "CoC #1", "CoC #2", "Pinch 25kg")
        AlertDialog(
            onDismissRequest = { showWeightDialog = false },
            title = {
                Text("Resistance / Gripper Level", fontWeight = FontWeight.Bold)
            },
            text = {
                Column {
                    Text(
                        "Choose a preset or type your resistance:",
                        style = MaterialTheme.typography.bodyMedium,
                        color = SlateMuted
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    OutlinedTextField(
                        value = weightInputText,
                        onValueChange = { weightInputText = it },
                        singleLine = true,
                        placeholder = { Text("e.g. 55 kg or CoC #1.5") },
                        modifier = Modifier
                            .fillMaxWidth()
                            .testTag("weight_input_field")
                    )
                    Spacer(modifier = Modifier.height(12.dp))
                    Text("Quick Presets:", style = MaterialTheme.typography.labelSmall, color = SlateMuted)
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPresets.take(4).forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (weightInputText == preset) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF1E2433),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { weightInputText = preset }
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (weightInputText == preset) ElectricCyan else SlateLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                    Spacer(modifier = Modifier.height(6.dp))
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(6.dp)
                    ) {
                        quickPresets.drop(4).forEach { preset ->
                            Surface(
                                shape = RoundedCornerShape(8.dp),
                                color = if (weightInputText == preset) ElectricCyan.copy(alpha = 0.2f) else Color(0xFF1E2433),
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable { weightInputText = preset }
                            ) {
                                Text(
                                    text = preset,
                                    style = MaterialTheme.typography.labelSmall,
                                    color = if (weightInputText == preset) ElectricCyan else SlateLight,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 6.dp)
                                )
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        onSetWeightDirect(weightInputText.ifBlank { "40 kg" })
                        showWeightDialog = false
                    },
                    colors = ButtonDefaults.buttonColors(containerColor = ElectricCyan),
                    modifier = Modifier.testTag("save_weight_button")
                ) {
                    Text("Set Resistance", color = Color.Black)
                }
            },
            dismissButton = {
                TextButton(onClick = { showWeightDialog = false }) {
                    Text("Cancel", color = SlateMuted)
                }
            }
        )
    }
}
