package com.ayush.canvasdraws

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
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
import androidx.compose.ui.tooling.preview.Preview
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
                    val viewModel = viewModel<DrawingViewModel>()
                    val state by viewModel.state.collectAsStateWithLifecycle()
                    var showTextEditor by remember { mutableStateOf<String?>(null) }

                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(innerPadding),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        DrawingCanvas(
                            paths = state.paths,
                            currentPath = state.currentPath,
                            textElements = state.textElements,
                            selectedTextId = state.selectedTextId,
                            isAddingText = state.isAddingText,
                            onAction = { action ->
                                // Intercept text selection to show editor
                                if (action is DrawingAction.OnSelectText) {
                                    showTextEditor = action.id
                                }
                                viewModel.onAction(action)
                            },
                            modifier = Modifier
                                .fillMaxWidth()
                                .weight(1f)
                        )
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
                            onUpdateTextStyle = { color, fontSize, isBold, isItalic, isUnderline ->
                                state.selectedTextId?.let { id ->
                                    viewModel.onAction(
                                        DrawingAction.OnUpdateTextStyle(
                                            id = id,
                                            color = color,
                                            fontSize = fontSize,
                                            isBold = isBold,
                                            isItalic = isItalic,
                                            isUnderline = isUnderline
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
                        // Show text editor dialog when text is selected
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

