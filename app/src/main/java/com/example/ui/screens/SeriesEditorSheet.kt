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
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.SyncAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.SheetState
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.HandSide
import com.example.ui.components.SetEditorItem
import com.example.ui.theme.DarkBg
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurface
import com.example.ui.theme.ElectricCyan
import com.example.ui.theme.FlameOrange
import com.example.ui.theme.HandLeftColor
import com.example.ui.theme.HandRightColor
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted
import com.example.ui.viewmodel.ActiveSeriesDraft

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SeriesEditorSheet(
    draft: ActiveSeriesDraft,
    onUpdateName: (String) -> Unit,
    onUpdateNotes: (String) -> Unit,
    onAddSet: (HandSide) -> Unit,
    onRemoveSet: (Long) -> Unit,
    onIncrementReps: (Long, Int) -> Unit,
    onSetRepsDirect: (Long, Int) -> Unit,
    onSetWeightDirect: (Long, String) -> Unit,
    onToggleHand: (Long) -> Unit,
    onMoveUp: (Long) -> Unit,
    onMoveDown: (Long) -> Unit,
    onSave: () -> Unit,
    onDismiss: () -> Unit,
    sheetState: SheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
) {
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = DarkBg,
        dragHandle = null,
        modifier = Modifier.fillMaxSize().testTag("series_editor_modal_sheet")
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp, vertical = 16.dp)
        ) {
            // Top Bar: Dismiss, Title, and Save CTA
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .size(38.dp)
                        .clip(CircleShape)
                        .background(Color(0xFF1E2433))
                        .testTag("dismiss_editor_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Close,
                        contentDescription = "Close editor",
                        tint = SlateLight
                    )
                }

                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(
                        text = if (draft.id == 0L) "NEW SERIES" else "EDIT SERIES",
                        style = MaterialTheme.typography.labelSmall,
                        color = FlameOrange,
                        letterSpacing = 1.2.sp
                    )
                    Text(
                        text = "${draft.sets.size} Sets • ${draft.sets.sumOf { it.reps }} Reps",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = SlateLight
                    )
                }

                Button(
                    onClick = onSave,
                    enabled = draft.sets.isNotEmpty(),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = FlameOrange,
                        disabledContainerColor = Color(0xFF2E384D)
                    ),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.testTag("save_series_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Check,
                        contentDescription = null,
                        tint = Color.White,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("Save", fontWeight = FontWeight.Bold)
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Series Name Input
            OutlinedTextField(
                value = draft.name,
                onValueChange = onUpdateName,
                label = { Text("Series Name") },
                placeholder = { Text("e.g. Captains of Crush Pyramid") },
                singleLine = true,
                shape = RoundedCornerShape(14.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedBorderColor = FlameOrange,
                    unfocusedBorderColor = DarkBorder,
                    focusedContainerColor = DarkSurface,
                    unfocusedContainerColor = DarkSurface,
                    focusedTextColor = SlateLight,
                    unfocusedTextColor = SlateLight
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("series_name_input_field")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Quick Add Set Chips
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // Add Left Hand Set Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HandLeftColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, HandLeftColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    OutlinedButton(
                        onClick = { onAddSet(HandSide.LEFT) },
                        border = null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_left_set_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = HandLeftColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Left", color = HandLeftColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Add Right Hand Set Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = HandRightColor.copy(alpha = 0.15f),
                    modifier = Modifier
                        .weight(1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, HandRightColor.copy(alpha = 0.6f), RoundedCornerShape(12.dp))
                ) {
                    OutlinedButton(
                        onClick = { onAddSet(HandSide.RIGHT) },
                        border = null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_right_set_button")
                    ) {
                        Icon(imageVector = Icons.Default.Add, contentDescription = null, tint = HandRightColor, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Right", color = HandRightColor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }

                // Add Bilateral Pair (L + R) Button
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = Color(0xFF19202F),
                    modifier = Modifier
                        .weight(1.1f)
                        .clip(RoundedCornerShape(12.dp))
                        .border(1.dp, DarkBorder, RoundedCornerShape(12.dp))
                ) {
                    OutlinedButton(
                        onClick = {
                            onAddSet(HandSide.RIGHT)
                            onAddSet(HandSide.LEFT)
                        },
                        border = null,
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.testTag("add_bilateral_pair_button")
                    ) {
                        Icon(imageVector = Icons.Default.SyncAlt, contentDescription = null, tint = SlateLight, modifier = Modifier.size(16.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text("+ Both", color = SlateLight, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                    }
                }
            }

            Spacer(modifier = Modifier.height(14.dp))

            // Sets List
            if (draft.sets.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.FitnessCenter,
                            contentDescription = null,
                            tint = SlateMuted,
                            modifier = Modifier.size(48.dp)
                        )
                        Spacer(modifier = Modifier.height(10.dp))
                        Text(
                            text = "No sets added yet",
                            style = MaterialTheme.typography.titleMedium,
                            color = SlateLight
                        )
                        Text(
                            text = "Tap '+ Left' or '+ Right' above to add your first set",
                            style = MaterialTheme.typography.bodyMedium,
                            color = SlateMuted
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .weight(1f)
                        .testTag("sets_editor_list"),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    itemsIndexed(
                        items = draft.sets,
                        key = { _, item -> item.tempId }
                    ) { index, setItem ->
                        SetEditorItem(
                            index = index,
                            totalSets = draft.sets.size,
                            set = setItem,
                            onToggleHand = { onToggleHand(setItem.tempId) },
                            onIncrementReps = { delta -> onIncrementReps(setItem.tempId, delta) },
                            onSetRepsDirect = { newReps -> onSetRepsDirect(setItem.tempId, newReps) },
                            onSetWeightDirect = { newWeight -> onSetWeightDirect(setItem.tempId, newWeight) },
                            onMoveUp = { onMoveUp(setItem.tempId) },
                            onMoveDown = { onMoveDown(setItem.tempId) },
                            onDelete = { onRemoveSet(setItem.tempId) }
                        )
                    }
                }
            }
        }
    }
}
