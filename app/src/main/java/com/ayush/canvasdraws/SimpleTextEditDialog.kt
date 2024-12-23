package com.ayush.canvasdraws

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import kotlinx.coroutines.delay

@OptIn(ExperimentalComposeUiApi::class)
@Composable
fun SimpleTextEditDialog(
    initialText: String,
    onDismiss: () -> Unit,
    onConfirm: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    // State
    var textFieldValue by remember {
        mutableStateOf(
            TextFieldValue(
                text = initialText,
                selection = TextRange(initialText.length)
            )
        )
    }
    var isError by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf("") }

    // System controllers
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current
    val focusRequester = remember { FocusRequester() }

    // Validation function
    fun validateAndSubmit() {
        val trimmedText = textFieldValue.text.trim()
        when {
            trimmedText.isEmpty() -> {
                isError = true
                errorMessage = "Text cannot be empty"
            }
            trimmedText.length > 100 -> {
                isError = true
                errorMessage = "Text is too long (max 100 characters)"
            }
            else -> {
                isError = false
                errorMessage = ""
                keyboardController?.hide()
                onConfirm(trimmedText)
            }
        }
    }

    // Handle initial focus
    LaunchedEffect(Unit) {
        try {
            delay(100)
            focusRequester.requestFocus()
            keyboardController?.show()
        } catch (e: Exception) {
            // Handle focus request failure silently
        }
    }

    // Cleanup on dismiss
    DisposableEffect(Unit) {
        onDispose {
            keyboardController?.hide()
        }
    }

    Dialog(
        onDismissRequest = {
            keyboardController?.hide()
            onDismiss()
        },
        properties = DialogProperties(
            dismissOnBackPress = true,
            dismissOnClickOutside = true,
            usePlatformDefaultWidth = false
        )
    ) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = 8.dp,
            modifier = modifier
                .fillMaxWidth(0.9f)
                .padding(horizontal = 16.dp)
        ) {
            Column(
                modifier = Modifier
                    .padding(16.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                TextField(
                    value = textFieldValue,
                    onValueChange = {
                        isError = false
                        textFieldValue = it
                    },
                    label = { Text("Edit Text") },
                    isError = isError,
                    supportingText = if (isError) {
                        { Text(errorMessage, color = MaterialTheme.colorScheme.error) }
                    } else null,
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .focusRequester(focusRequester),
                    keyboardOptions = KeyboardOptions(
                        imeAction = ImeAction.Done
                    ),
                    keyboardActions = KeyboardActions(
                        onDone = {
                            validateAndSubmit()
                        }
                    )
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    TextButton(
                        onClick = {
                            keyboardController?.hide()
                            onDismiss()
                        },
                        modifier = Modifier.weight(1f)
                    ) {
                        Text("Cancel")
                    }
                    Button(
                        onClick = { validateAndSubmit() },
                        modifier = Modifier.weight(1f),
                        enabled = textFieldValue.text.isNotBlank()
                    ) {
                        Text("Done")
                    }
                }
            }
        }
    }
}