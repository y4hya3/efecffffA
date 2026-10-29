package com.example.ui.components

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.DarkBorder
import com.example.ui.theme.DarkSurfaceElevated
import com.example.ui.theme.HandLeftColor
import com.example.ui.theme.HandRightColor
import com.example.ui.theme.SlateLight
import com.example.ui.theme.SlateMuted

@Composable
fun HandComparisonBar(
    leftReps: Int,
    rightReps: Int,
    modifier: Modifier = Modifier
) {
    val total = leftReps + rightReps
    val leftFraction = if (total > 0) leftReps.toFloat() / total.toFloat() else 0.5f
    val animatedLeftFraction by animateFloatAsState(targetValue = leftFraction, label = "leftRatio")

    val leftPct = (leftFraction * 100).toInt()
    val rightPct = 100 - leftPct

    val balanceMessage = when {
        total == 0 -> "No volume logged yet"
        kotlin.math.abs(leftPct - rightPct) <= 6 -> "Balanced Symmetry (±3%)"
        leftPct > rightPct -> "Left Hand Dominant (+${leftPct - rightPct}%)"
        else -> "Right Hand Dominant (+${rightPct - leftPct}%)"
    }

    Surface(
        shape = RoundedCornerShape(16.dp),
        color = DarkSurfaceElevated,
        modifier = modifier
            .fillMaxWidth()
            .border(1.dp, DarkBorder, RoundedCornerShape(16.dp))
            .testTag("hand_comparison_bar")
    ) {
        Column(
            modifier = Modifier.padding(14.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(HandLeftColor))
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = "Left: $leftReps ($leftPct%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HandLeftColor
                    )
                }

                Text(
                    text = balanceMessage,
                    style = MaterialTheme.typography.labelSmall,
                    color = SlateMuted
                )

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        text = "Right: $rightReps ($rightPct%)",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = HandRightColor
                    )
                    Spacer(modifier = Modifier.width(6.dp))
                    Box(modifier = Modifier.size(8.dp).clip(CircleShape).background(HandRightColor))
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // Dual colored pill bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(10.dp)
                    .clip(RoundedCornerShape(5.dp))
                    .background(HandRightColor)
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .fillMaxWidth(animatedLeftFraction)
                        .background(HandLeftColor)
                )
            }
        }
    }
}
