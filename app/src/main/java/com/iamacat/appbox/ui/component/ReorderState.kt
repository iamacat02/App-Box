package com.iamacat.appbox.ui.component

import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue

/**
 * Minimal drag-to-reorder helper for a LazyColumn. Items are tracked by their
 * stable list key so the callbacks survive recomposition while dragging.
 */
@Stable
class ReorderState(
    private val listState: LazyListState,
    private val onMove: (fromIndex: Int, toIndex: Int) -> Unit
) {
    var draggingKey by mutableStateOf<Any?>(null)
        private set

    var dragOffset by mutableFloatStateOf(0f)
        private set

    fun onDragStart(key: Any) {
        draggingKey = key
        dragOffset = 0f
    }

    fun onDrag(deltaY: Float) {
        val key = draggingKey ?: return
        dragOffset += deltaY

        val dragged = listState.layoutInfo.visibleItemsInfo
            .firstOrNull { it.key == key } ?: return
        val draggedStart = dragged.offset + dragOffset
        val draggedEnd = draggedStart + dragged.size

        val target = listState.layoutInfo.visibleItemsInfo.firstOrNull { info ->
            info.key != key && (info.offset + info.size / 2f) in draggedStart..draggedEnd
        } ?: return

        onMove(dragged.index, target.index)
        dragOffset += (dragged.offset - target.offset).toFloat()
    }

    fun onDragEnd() {
        draggingKey = null
        dragOffset = 0f
    }

    fun isDragging(key: Any): Boolean = draggingKey == key

    fun offsetFor(key: Any): Float = if (draggingKey == key) dragOffset else 0f
}

@Composable
fun rememberReorderState(
    listState: LazyListState,
    onMove: (fromIndex: Int, toIndex: Int) -> Unit
): ReorderState {
    val currentOnMove by rememberUpdatedState(onMove)
    return remember(listState) {
        ReorderState(listState) { from, to -> currentOnMove(from, to) }
    }
}
