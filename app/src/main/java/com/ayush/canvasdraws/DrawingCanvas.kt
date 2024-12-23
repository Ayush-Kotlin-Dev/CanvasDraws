package com.ayush.canvasdraws

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.IntSize
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.util.fastForEach
import kotlin.math.abs
import kotlin.math.roundToInt

@Composable
fun DrawingCanvas(
    paths: List<PathData>,
    currentPath: PathData?,
    textElements: List<TextElement>,
    selectedTextId: String?,
    onAction: (DrawingAction) -> Unit,
    modifier: Modifier = Modifier
) {
    var canvasSize by remember { mutableStateOf(IntSize.Zero) }

    Box(modifier = modifier
        .clipToBounds()
        .background(Color.White)
        .onSizeChanged { canvasSize = it }
        .pointerInput(true) {
            detectTapGestures(
                onTap = { offset ->
                    // Check if we clicked on any text
                    val clickedOnText = textElements.any { textElement ->
                        val textPosition = IntOffset(
                            textElement.position.x.roundToInt(),
                            textElement.position.y.roundToInt()
                        )
                        val clickPosition = IntOffset(
                            offset.x.roundToInt(),
                            offset.y.roundToInt()
                        )
                        // Make hit box more proportional to text size
                        val xDiff = (clickPosition.x - textPosition.x)
                        val yDiff = (clickPosition.y - textPosition.y)
                        xDiff in 0..((textElement.text.length * textElement.fontSize / 2).toInt()) &&
                                yDiff.toFloat() in (-textElement.fontSize/2)..(textElement.fontSize/2)
                    }

                    if (!clickedOnText) {
                        // If we didn't click on text, deselect any selected text
                        // and draw a point
                        onAction(DrawingAction.OnSelectText(null))
                        onAction(DrawingAction.OnNewPathStart)
                        onAction(DrawingAction.OnDraw(offset))
                        onAction(DrawingAction.OnPathEnd)
                    }
                }
            )
        }
    ) {
        Canvas(
            modifier = Modifier
                .fillMaxSize()
                .pointerInput(true) {
                    detectDragGestures(
                        onDragStart = {
                            // Deselect text when starting to draw
                            onAction(DrawingAction.OnSelectText(null))
                            onAction(DrawingAction.OnNewPathStart)
                        },
                        onDragEnd = { onAction(DrawingAction.OnPathEnd) },
                        onDrag = { change, _ -> onAction(DrawingAction.OnDraw(change.position)) },
                        onDragCancel = { onAction(DrawingAction.OnPathEnd) }
                    )
                }
        ){
            paths.fastForEach { pathData ->
                drawPath(path = pathData.path, color = pathData.color)
            }
            currentPath?.let {
                drawPath(path = it.path, color = it.color)
            }
        }

        // Draw text elements
        textElements.forEach { textElement ->
            val isSelected = textElement.id == selectedTextId
            var offsetX by remember { mutableStateOf(textElement.position.x) }
            var offsetY by remember { mutableStateOf(textElement.position.y) }
            var isDragging by remember { mutableStateOf(false) }

            Text(
                text = textElement.text,
                color = textElement.color,
                fontSize = textElement.fontSize.sp,
                fontWeight = if (textElement.isBold) FontWeight.Bold else FontWeight.Normal,
                fontStyle = if (textElement.isItalic) FontStyle.Italic else FontStyle.Normal,
                textDecoration = if (textElement.isUnderline) TextDecoration.Underline else TextDecoration.None,
                modifier = Modifier
                    .offset { IntOffset(offsetX.roundToInt(), offsetY.roundToInt()) }
                    .pointerInput(textElement.id) {
                        detectDragGestures(
                            onDragStart = { isDragging = true },
                            onDragEnd = {
                                isDragging = false
                                onAction(DrawingAction.OnMoveText(
                                    id = textElement.id,
                                    newPosition = Offset(offsetX, offsetY)
                                ))
                            }
                        ) { change, dragAmount ->
                            change.consume()
                            offsetX += dragAmount.x
                            offsetY += dragAmount.y
                        }
                    }
                    .clickable {
                        onAction(DrawingAction.OnSelectText(textElement.id))
                    }
                    // Optionally show border only when dragging or selected
                    .then(
                        if (isDragging || isSelected) {
                            Modifier.border(0.5.dp, Color.Gray.copy(alpha = 0.5f))
                        } else Modifier
                    )
                    .semantics {
                        contentDescription = "Editable text: ${textElement.text}"
                    }
            )
        }
    }

    LaunchedEffect(canvasSize) {
        if (canvasSize != IntSize.Zero) {
            onAction(DrawingAction.OnCanvasSizeChanged(
                width = canvasSize.width,
                height = canvasSize.height
            ))
        }
    }
}

private fun DrawScope.drawPath(
    path: List<Offset>,
    color: Color,
    thickness: Float = 10f
) {
    val smoothedPath = Path().apply {
        if(path.isNotEmpty()) {
            moveTo(path.first().x, path.first().y)

            val smoothness = 5
            for(i in 1..path.lastIndex) {
                val from = path[i - 1]
                val to = path[i]
                val dx = abs(from.x - to.x)
                val dy = abs(from.y - to.y)
                if(dx >= smoothness || dy >= smoothness) {
                    quadraticTo(
                        x1 = (from.x + to.x) / 2f,
                        y1 = (from.y + to.y) / 2f,
                        x2 = to.x,
                        y2 = to.y
                    )
                }
            }
        }
    }
    drawPath(
        path = smoothedPath,
        color = color,
        style = Stroke(
            width = thickness,
            cap = StrokeCap.Round,
            join = StrokeJoin.Round
        )
    )
}
