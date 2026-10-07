/**
 * 职责：跨页通用件——顶栏配色、空态。
 * 架构位置：二级页骨架 MtScreen 与各页复用；取色走 LocalMtColors。
 */
package com.copy.mt.ui.components

import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.foundation.layout.Box
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.pointer.pointerInput
import com.copy.mt.ui.theme.LocalMtColors

@Composable
internal fun mtTopBarColors() = TopAppBarDefaults.topAppBarColors(
    containerColor = LocalMtColors.current.bar,
    titleContentColor = LocalMtColors.current.iglyph,
    navigationIconContentColor = LocalMtColors.current.iglyph,
    actionIconContentColor = LocalMtColors.current.iglyph,
)

@Composable
internal fun MtEmptyState(text: String, modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Text(text, color = MaterialTheme.colorScheme.onSurfaceVariant)
    }
}

@androidx.compose.ui.tooling.preview.Preview(name = "空态", showBackground = true, widthDp = 320, heightDp = 200)
@Composable
private fun MtEmptyStatePreview() {
    com.copy.mt.ui.theme.MtTheme(com.copy.mt.ui.theme.ThemeMode.DARK) {
        MtEmptyState("暂无内容", Modifier)
    }
}

/** 长按拖动排序共用手势；每移动 48dp 通知一次方向。（复制自 account） */
internal fun Modifier.reorderDragHandle(
    key: Any,
    onMove: (Int) -> Unit,
    onDrag: (Float) -> Unit = {},
    onDragStart: () -> Unit = {},
    onDragEnd: () -> Unit = {},
    onDragCancel: () -> Unit = {}
): Modifier = pointerInput(key) {
    var distance = 0f
    detectDragGesturesAfterLongPress(
        onDragStart = {
            distance = 0f
            onDragStart()
        },
        onDrag = { change, amount ->
            change.consume()
            onDrag(amount.y)
            distance += amount.y
            while (distance > 48f) {
                onMove(1)
                distance -= 48f
            }
            while (distance < -48f) {
                onMove(-1)
                distance += 48f
            }
        },
        onDragEnd = {
            distance = 0f
            onDragEnd()
        },
        onDragCancel = {
            distance = 0f
            onDragCancel()
        }
    )
}
