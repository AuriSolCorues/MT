/**
 * 职责：主界面双栏状态——路径、条目、激活栏；读盘走 core FileRepository。
 */
package com.copy.mt.ui.page

import android.os.Environment
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.copy.mt.ServiceLocator
import com.copy.mt.core.model.FileItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

enum class Side { LEFT, RIGHT }

data class PaneState(
    val path: String = "",
    val items: List<FileItem> = emptyList(),
    val loading: Boolean = false,
)

data class BrowserUiState(
    val left: PaneState = PaneState(),
    val right: PaneState = PaneState(),
    val active: Side = Side.LEFT,
)

class BrowserViewModel : ViewModel() {
    private val repo = ServiceLocator.fileRepository
    private val root: String = (Environment.getExternalStorageDirectory()?.absolutePath ?: "/storage/emulated/0") + "/"

    private val _state = MutableStateFlow(BrowserUiState())
    val state: StateFlow<BrowserUiState> = _state.asStateFlow()

    init {
        open(Side.LEFT, root)
        open(Side.RIGHT, root)
    }

    fun activate(side: Side) {
        _state.value = _state.value.copy(active = side)
    }

    fun open(side: Side, path: String) {
        val p = if (path.endsWith("/")) path else "$path/"
        updatePane(side, PaneState(path = p, items = pane(side).items, loading = true))
        viewModelScope.launch {
            val listed = runCatching { repo.list(p) }.getOrDefault(emptyList())
            val items = if (p.trimEnd('/') == root.trimEnd('/')) listed else {
                val parent = p.trimEnd('/').substringBeforeLast('/').ifEmpty { "/" } + "/"
                listOf(FileItem("..", parent, true, 0L, 0L)) + listed
            }
            updatePane(side, PaneState(path = p, items = items, loading = false))
        }
    }

    fun up(side: Side) {
        val cur = pane(side).path.trimEnd('/')
        if (cur == root.trimEnd('/') || cur.isEmpty()) return
        val parent = cur.substringBeforeLast('/').ifEmpty { "/" } + "/"
        open(side, parent)
    }

    fun refresh(side: Side) = open(side, pane(side).path)

    /** 双栏同步：把非激活栏路径同步成激活栏的。 */
    fun sync() {
        val a = _state.value.active
        val other = if (a == Side.LEFT) Side.RIGHT else Side.LEFT
        open(other, pane(a).path)
    }

    private fun pane(side: Side) = if (side == Side.LEFT) _state.value.left else _state.value.right

    private fun updatePane(side: Side, pane: PaneState) {
        _state.value = if (side == Side.LEFT) _state.value.copy(left = pane)
        else _state.value.copy(right = pane)
    }
}
