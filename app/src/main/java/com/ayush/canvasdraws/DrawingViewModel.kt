package com.ayush.canvasdraws
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.lifecycle.ViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

data class DrawingState(
    val selectedColor: Color = Color.Black,
    val currentPath: PathData? = null,
    val paths: List<PathData> = emptyList(),
    val textElements: List<TextElement> = emptyList(),
    val selectedTextId: String? = null,
    val canvasWidth: Int = 0,
    val canvasHeight: Int = 0
)


val allColors = listOf(
    Color.Black,
    Color.Red,
    Color.Blue,
    Color.Green,
    Color.Yellow,
    Color.Magenta,
    Color.Cyan,
)

data class PathData(
    val id: String,
    val color: Color,
    val path: List<Offset>
)



sealed interface DrawingAction {
    data object OnNewPathStart: DrawingAction
    data class OnDraw(val offset: Offset): DrawingAction
    data object OnPathEnd: DrawingAction
    data class OnSelectColor(val color: Color): DrawingAction
    data object OnClearCanvasClick: DrawingAction

    // Text Actions
    data object OnAddTextClick : DrawingAction
    data class OnUpdateText(val id: String, val newText: String) : DrawingAction
    data class OnUpdateTextStyle(
        val id: String,
        val color: Color? = null,
        val fontSize: Float? = null,
        val isBold: Boolean? = null,
        val isItalic: Boolean? = null,
        val isUnderline: Boolean? = null
    ) : DrawingAction
    data class OnSelectText(val id: String?) : DrawingAction
    data class OnMoveText(val id: String, val newPosition: Offset) : DrawingAction
    data class OnDeleteText(val id: String) : DrawingAction
    data class OnCanvasSizeChanged(val width: Int, val height: Int) : DrawingAction




}

data class TextElement(
    val id: String,
    val text: String,
    val position: Offset,
    val color: Color = Color.Black,
    val fontSize: Float = 16f,
    val isBold: Boolean = false,
    val isItalic: Boolean = false,
    val isUnderline: Boolean = false
)

class DrawingViewModel: ViewModel() {

    private val _state = MutableStateFlow(DrawingState())
    val state = _state.asStateFlow()

    fun onAction(action: DrawingAction) {
        when(action) {
            DrawingAction.OnClearCanvasClick -> onClearCanvasClick()
            is DrawingAction.OnDraw -> onDraw(action.offset)
            DrawingAction.OnNewPathStart -> onNewPathStart()
            DrawingAction.OnPathEnd -> onPathEnd()
            is DrawingAction.OnSelectColor -> onSelectColor(action.color)
            DrawingAction.OnAddTextClick -> onAddTextClick()
            is DrawingAction.OnUpdateText -> onUpdateText(action.id, action.newText)
            is DrawingAction.OnUpdateTextStyle -> onUpdateTextStyle(
                id = action.id,
                color = action.color,
                fontSize = action.fontSize,
                isBold = action.isBold,
                isItalic = action.isItalic,
                isUnderline = action.isUnderline
            )
            is DrawingAction.OnSelectText -> onSelectText(action.id)
            is DrawingAction.OnMoveText -> onMoveText(action.id, action.newPosition)
            is DrawingAction.OnDeleteText -> onDeleteText(action.id)
            is DrawingAction.OnCanvasSizeChanged -> onCanvasSizeChanged(action.width, action.height)


        }
    }

    private fun onSelectColor(color: Color) {
        _state.update { it.copy(
            selectedColor = color
        ) }
    }

    private fun onPathEnd() {
        val currentPathData = state.value.currentPath ?: return
        _state.update { it.copy(
            currentPath = null,
            paths = it.paths + currentPathData
        ) }
    }

    private fun onNewPathStart() {
        _state.update { it.copy(
            currentPath = PathData(
                id = System.currentTimeMillis().toString(),
                color = it.selectedColor,
                path = emptyList()
            ),
            selectedTextId = null
        ) }
    }

    private fun onDraw(offset: Offset) {
        val currentPathData = state.value.currentPath ?: return
        _state.update { it.copy(
            currentPath = currentPathData.copy(
                path = currentPathData.path + offset
            )
        ) }
    }

    private fun onClearCanvasClick() {
        _state.update { it.copy(
            currentPath = null,
            paths = emptyList(),
//            textElements = emptyList(),  TODO if want to clear text elements also on clear canvas
//            selectedTextId = null
        ) }
    }

    // Text Actions
    private fun onAddTextClick() {
        val centerX = state.value.canvasWidth / 2f
        val centerY = state.value.canvasHeight / 2f

        val newText = TextElement(
            id = System.currentTimeMillis().toString(),
            text = "New Text",
            position = Offset(centerX, centerY)
        )

        _state.update { it.copy(
            textElements = it.textElements + newText,
            selectedTextId = newText.id
        ) }
    }


    private fun onUpdateText(id: String, newText: String) {
        _state.update { state ->
            val updatedElements = state.textElements.map { element ->
                if (element.id == id) element.copy(text = newText) else element
            }
            state.copy(textElements = updatedElements)
        }
    }

    private fun onUpdateTextStyle(
        id: String,
        color: Color?,
        fontSize: Float?,
        isBold: Boolean?,
        isItalic: Boolean?,
        isUnderline: Boolean?
    ) {
        _state.update { state ->
            val updatedElements = state.textElements.map { element ->
                if (element.id == id) {
                    element.copy(
                        color = color ?: element.color,
                        fontSize = fontSize ?: element.fontSize,
                        isBold = isBold ?: element.isBold,
                        isItalic = isItalic ?: element.isItalic,
                        isUnderline = isUnderline ?: element.isUnderline
                    )
                } else element
            }
            state.copy(textElements = updatedElements)
        }
    }

    private fun onMoveText(id: String, newPosition: Offset) {
        _state.update { state ->
            val updatedElements = state.textElements.map { element ->
                if (element.id == id) {
                    element.copy(position = newPosition)
                } else element
            }
            state.copy(textElements = updatedElements)
        }
    }
    private fun onSelectText(id: String?) {
        _state.update { it.copy(selectedTextId = id) }
    }
    private fun onDeleteText(id: String) {
        _state.update { state ->
            state.copy(
                textElements = state.textElements.filter { it.id != id },
                selectedTextId = null
            )
        }
    }
    private fun onCanvasSizeChanged(width: Int, height: Int) {
        _state.update { it.copy(
            canvasWidth = width,
            canvasHeight = height
        ) }
    }


}