/**
 * 职责：右上 ⋮ 弹出菜单——196dp，项 48dp，锚右上，scale 弹出。
 */
package com.copy.mt.ui.components.overlay

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.R
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

data class MenuItem(val id: String, val label: String, val enabled: Boolean = true)

@Composable
fun DefaultMenuItems(): List<MenuItem> = listOf(
    MenuItem("refresh", stringResource(R.string.menu_refresh)),
    MenuItem("search", stringResource(R.string.menu_search)),
    MenuItem("filter", stringResource(R.string.menu_filter)),
    MenuItem("sort", stringResource(R.string.menu_sort)),
    MenuItem("terminal", stringResource(R.string.menu_terminal), enabled = false),
    MenuItem("hidden", stringResource(R.string.menu_hidden)),
    MenuItem("bookmark", stringResource(R.string.menu_bookmark)),
    MenuItem("set_home", stringResource(R.string.menu_set_home)),
    MenuItem("swap", stringResource(R.string.menu_swap)),
    MenuItem("settings", stringResource(R.string.menu_settings)),
    MenuItem("exit", stringResource(R.string.menu_exit)),
)

@Composable
fun MenuPopup(
    visible: Boolean,
    onDismiss: () -> Unit,
    items: List<MenuItem> = DefaultMenuItems(),
    onAction: (MenuItem) -> Unit = {},
) {
    val c = LocalMtColors.current
    // §6.7/§10.1：复用 MtPopupDialog——pivot=(1f,0f) 右上角展开，scale 0→1 + alpha，
    // 200ms FastOutSlowInEasing；退出仅淡 alpha 150ms。yOffset=0dp（锚顶栏不上移）。
    MtPopupDialog(
        visible = visible,
        pivot = TransformOrigin(1f, 0f),
        yOffset = 0.dp,
        alignment = Alignment.TopEnd,
        shape = RoundedCornerShape(2.dp),
        surfaceModifier = Modifier
            .padding(top = 48.dp)
            .windowInsetsPadding(WindowInsets.statusBars)
            .width(196.dp),
        onDismissRequest = onDismiss,
    ) {
        items.forEach { item ->
            Row(
                Modifier
                    .fillMaxWidth()
                    .height(48.dp)
                    .clickable(enabled = item.enabled) { onAction(item) }
                    .padding(horizontal = 16.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    item.label,
                    color = if (item.enabled) c.menuFg else c.disabled,
                    fontSize = 16.sp,
                )
            }
        }
    }
}

@Preview(name = "⋮ 菜单", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun MenuPopupPreview() {
    MtTheme(ThemeMode.DARK) { MenuPopup(visible = true, onDismiss = {}) }
}
