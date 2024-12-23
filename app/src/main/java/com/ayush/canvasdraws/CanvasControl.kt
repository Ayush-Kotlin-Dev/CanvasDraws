package com.ayush.canvasdraws

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Remove
import androidx.compose.material.icons.outlined.FormatBold
import androidx.compose.material.icons.outlined.FormatItalic
import androidx.compose.material.icons.outlined.FormatUnderlined
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.util.fastForEach
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@Composable
fun ColumnScope.CanvasControls(
    selectedColor: Color,
    colors: List<Color>,
    onSelectColor: (Color) -> Unit,
    onClearCanvas: () -> Unit,
    onAddText: () -> Unit,
    selectedTextElement: TextElement?, 
    onUpdateTextStyle: (
        color: Color?,
        fontSize: Float?,
        isBold: Boolean?,
        isItalic: Boolean?,
        isUnderline: Boolean?
    ) -> Unit,
    onDeleteText: () -> Unit, 
    modifier: Modifier = Modifier
) {
    var showClearConfirmation by remember { mutableStateOf(false) }
    var clearConfirmationTimer by remember { mutableStateOf<Job?>(null) }
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    if (selectedTextElement == null) {
        // Show color controls when no text is selected
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally)
        ) {
            colors.fastForEach { color ->
                val isSelected = selectedColor == color
                Box(
                    modifier = Modifier
                        .graphicsLayer {
                            val scale = if(isSelected) 1.2f else 1f
                            scaleX = scale
                            scaleY = scale
                        }
                        .size(32.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = 2.dp,
                            color = if(selectedColor == color) Color.Black else Color.Transparent,
                            shape = CircleShape
                        )
                        .clickable { onSelectColor(color) }
                        .semantics {
                            contentDescription = "Select color $color"
                        }
                )
            }
        }
    } else {
        // Show text controls when text is selected
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                IconButton(
                    onClick = {
                        onUpdateTextStyle(
                            null,
                            (selectedTextElement.fontSize - 2f).coerceAtLeast(12f),
                            null, null, null
                        )
                    }
                ) {
                    Icon(Icons.Default.Remove, "Decrease font size")
                }
                Text(
                    text = "${selectedTextElement.fontSize.toInt()}",
                    style = MaterialTheme.typography.bodyMedium
                )
                IconButton(
                    onClick = {
                        onUpdateTextStyle(
                            null,
                            (selectedTextElement.fontSize + 2f).coerceAtMost(32f),
                            null, null, null
                        )
                    }
                ) {
                    Icon(Icons.Default.Add, "Increase font size")
                }
            }

            // Text style controls
            Row(
                modifier = Modifier
                    .horizontalScroll(rememberScrollState()) 
            ) {
                IconButton(
                    onClick = { onUpdateTextStyle(null, null, !selectedTextElement.isBold, null, null) }
                ) {
                    Icon(
                        Icons.Outlined.FormatBold,
                        contentDescription = "Bold",
                        tint = if (selectedTextElement.isBold) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(
                    onClick = { onUpdateTextStyle(null, null, null, !selectedTextElement.isItalic, null) }
                ) {
                    Icon(
                        Icons.Outlined.FormatItalic,
                        contentDescription = "Italic",
                        tint = if (selectedTextElement.isItalic) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(
                    onClick = { onUpdateTextStyle(null, null, null, null, !selectedTextElement.isUnderline) }
                ) {
                    Icon(
                        Icons.Outlined.FormatUnderlined,
                        contentDescription = "Underline",
                        tint = if (selectedTextElement.isUnderline) MaterialTheme.colorScheme.primary else Color.Gray
                    )
                }
                IconButton(
                    onClick = onDeleteText,
                    colors = IconButtonDefaults.iconButtonColors(
                        contentColor = MaterialTheme.colorScheme.error
                    )
                ) {
                    Icon(Icons.Default.Delete, "Delete text")
                }
            }
        }
    }

    // Add SnackbarHost to show messages
    SnackbarHost(
        hostState = snackbarHostState,
        modifier = Modifier.align(Alignment.CenterHorizontally)
    )

    // Action buttons row
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Button(
            onClick = onAddText,
            modifier = Modifier.weight(1f)
        ) {
            Text("Add Text")
        }
        Button(
            onClick = {
                if (showClearConfirmation) {
                    // Second click - actually clear
                    clearConfirmationTimer?.cancel()
                    showClearConfirmation = false
                    onClearCanvas()
                } else {
                    // First click - show confirmation
                    showClearConfirmation = true
                    scope.launch {
                        snackbarHostState.showSnackbar(
                            message = "Click again to clear canvas",
                            duration = SnackbarDuration.Short
                        )
                    }
                    // Reset after 3 seconds
                    clearConfirmationTimer?.cancel()
                    clearConfirmationTimer = scope.launch {
                        delay(3000)
                        showClearConfirmation = false
                    }
                }
            },
            colors = ButtonDefaults.buttonColors(
                containerColor = if (showClearConfirmation) 
                    MaterialTheme.colorScheme.errorContainer 
                else MaterialTheme.colorScheme.error
            ),
            modifier = Modifier.weight(1f)
        ) {
            Text(
                if (showClearConfirmation) "Click to Confirm" else "Clear All",
                color = if (showClearConfirmation) 
                    MaterialTheme.colorScheme.onErrorContainer 
                else MaterialTheme.colorScheme.onError
            )
        }
    }
}