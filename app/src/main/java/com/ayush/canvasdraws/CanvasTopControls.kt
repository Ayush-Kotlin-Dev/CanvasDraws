package com.ayush.canvasdraws
import android.annotation.SuppressLint
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Redo
import androidx.compose.material.icons.filled.Undo
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsPressedAsState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.PlainTooltip
import androidx.compose.material3.Text
import androidx.compose.material3.TooltipBox
import androidx.compose.material3.TooltipDefaults
import androidx.compose.material3.rememberTooltipState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.animation.core.animateFloatAsState
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.ui.hapticfeedback.HapticFeedback
import androidx.compose.ui.hapticfeedback.HapticFeedbackType
import androidx.compose.ui.unit.dp
@Composable
fun CanvasTopControls(
    canUndo: Boolean,
    canRedo: Boolean,
    onUndo: () -> Unit,
    onRedo: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .clip(RoundedCornerShape(8.dp))
            .background(MaterialTheme.colorScheme.surface.copy(alpha = 0.85f))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        // Undo Button
        ControlButton(
            icon = Icons.Default.Undo,
            contentDescription = "Undo",
            enabled = canUndo,
            onClick = onUndo
        )

        // Divider
        Box(
            modifier = Modifier
                .width(1.dp)
                .height(24.dp)
                .background(MaterialTheme.colorScheme.onSurface.copy(alpha = 0.12f))
        )

        // Redo Button
        ControlButton(
            icon = Icons.Default.Redo,
            contentDescription = "Redo",
            enabled = canRedo,
            onClick = onRedo
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ControlButton(
    icon: ImageVector,
    contentDescription: String,
    enabled: Boolean,
    onClick: () -> Unit
) {
    val interactionSource = remember { MutableInteractionSource() }
    val isPressed by interactionSource.collectIsPressedAsState()

    val scale by animateFloatAsState(
        targetValue = if (isPressed && enabled) 0.9f else 1f,
        label = "scale"
    )

    val alpha by animateFloatAsState(
        targetValue = if (enabled) 1f else 0.38f,
        label = "alpha"
    )

    Box(
        modifier = Modifier
            .scale(scale)
            .size(40.dp)
            .clip(CircleShape)
            .background(
                color = if (enabled && isPressed)
                    MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)
                else Color.Transparent
            )
            .clickable(
                enabled = enabled,
                onClick = onClick
            ),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = contentDescription,
            modifier = Modifier.size(24.dp),
            tint = MaterialTheme.colorScheme.primary.copy(alpha = alpha)
        )
    }

    // Tooltip
    if (enabled) {
        TooltipBox(
            positionProvider = TooltipDefaults.rememberPlainTooltipPositionProvider(),
            tooltip = {
                PlainTooltip {
                    Text(contentDescription)
                }
            },
            state = rememberTooltipState()
        ) {
            // Content that triggers the tooltip
            Box(Modifier.size(1.dp))
        }
    }
}
