package com.ayush.canvasdraws

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.ayush.canvasdraws.ui.theme.CanvasDrawsTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            CanvasDrawsTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    val context = LocalContext.current
                    val viewModel = viewModel<DrawingViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    var showTextEditor by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Box(
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        ) {
                            DrawingCanvas(
                                paths = state.paths,
                                currentPath = state.currentPath,
                                textElements = state.textElements,
                                selectedTextId = state.selectedTextId,
                                onAction = { action ->
                                    if (action is DrawingAction.OnSelectText) {
                                        showTextEditor = action.id
                                    }
                                    viewModel.onAction(action)
                                },
                                modifier = Modifier.fillMaxSize()
                            )

                            // Overlay the undo/redo controls
                            CanvasTopControls(
                                canUndo = state.canUndo,
                                canRedo = state.canRedo,
                                onUndo = {
                                    VibratorService.vibrate(context, VibratorService.VibrationPattern.Click)
                                    viewModel.onAction(DrawingAction.OnUndo)
                                },
                                onRedo = {
                                    VibratorService.vibrate(context, VibratorService.VibrationPattern.Click)
                                    viewModel.onAction(DrawingAction.OnRedo)
                                },
                                modifier = Modifier
                                    .align(Alignment.TopEnd)
                                    .padding(8.dp)
                            )
                        }

                        CanvasControls(
                            selectedColor = state.selectedColor,
                            colors = allColors,
                            onSelectColor = {
                                viewModel.onAction(DrawingAction.OnSelectColor(it))
                            },
                            onClearCanvas = {
                                viewModel.onAction(DrawingAction.OnClearCanvasClick)
                            },
                            onAddText = {
                                viewModel.onAction(DrawingAction.OnAddTextClick)
                            },
                            selectedTextElement = state.selectedTextId?.let { id ->
                                state.textElements.find { it.id == id }
                            },
                            onUpdateTextStyle = { color, fontSize, isBold, isItalic, isUnderline , fontFamily ->
                                state.selectedTextId?.let { id ->
                                    viewModel.onAction(
                                        DrawingAction.OnUpdateTextStyle(
                                            id = id,
                                            color = color,
                                            fontSize = fontSize,
                                            isBold = isBold,
                                            isItalic = isItalic,
                                            isUnderline = isUnderline,
                                            fontFamily =  fontFamily

                                        )
                                    )
                                }
                            },
                            onDeleteText = {
                                state.selectedTextId?.let { id ->
                                    viewModel.onAction(DrawingAction.OnDeleteText(id))
                                }
                            }
                        )

                        showTextEditor?.let { textId ->
                            state.textElements.find { it.id == textId }?.let { textElement ->
                                SimpleTextEditDialog(
                                    initialText = textElement.text,
                                    onDismiss = {
                                        showTextEditor = null
                                    },
                                    onConfirm = { newText ->
                                        viewModel.onAction(DrawingAction.OnUpdateText(textId, newText))
                                        showTextEditor = null
                                    }
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

//TODO
// 1. Settings option ( Add text will add text on Desired location or on center of screen automatically)
// 2. Save data in local storage