/**
 * 职责：长按操作面板——320×268.7dp 居中，2 列 × 5 行，顶部提示条；scale 弹出、点外部/Esc 关闭。
 */
package com.copy.mt.ui.components.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
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
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.R
import com.copy.mt.ui.components.MtIconId
import com.copy.mt.ui.components.MtIcons
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

data class PanelItem(val id: String, val label: String, val glyph: String = "", val arrow: Boolean = false)

/** 面板 10 项（默认）。 */
@Composable
fun DefaultPanelItems(leftArrow: Boolean = false): List<PanelItem> = listOf(
    PanelItem("copy", stringResource(R.string.panel_copy).let { if (leftArrow) it.replace("->","<-") else it }, MtIcons.chromeGlyph(MtIconId.PANEL_COPY), arrow = true),
    PanelItem("move", stringResource(R.string.panel_move).let { if (leftArrow) it.replace("->","<-") else it }, MtIcons.chromeGlyph(MtIconId.PANEL_MOVE), arrow = true),
    PanelItem("delete", stringResource(R.string.panel_delete), MtIcons.chromeGlyph(MtIconId.PANEL_DELETE)),
    PanelItem("rename", stringResource(R.string.panel_rename), MtIcons.chromeGlyph(MtIconId.PANEL_RENAME)),
    PanelItem("tools", stringResource(R.string.panel_tools), MtIcons.chromeGlyph(MtIconId.PANEL_TOOLS)),
    PanelItem("zip", stringResource(R.string.panel_zip), MtIcons.chromeGlyph(MtIconId.PANEL_ZIP)),
    PanelItem("props", stringResource(R.string.panel_props), MtIcons.chromeGlyph(MtIconId.PANEL_PROPS)),
    PanelItem("share", stringResource(R.string.panel_share), MtIcons.chromeGlyph(MtIconId.PANEL_SHARE)),
    PanelItem("open_with", stringResource(R.string.panel_open_with), MtIcons.chromeGlyph(MtIconId.PANEL_OPEN_WITH)),
    PanelItem("bookmark", stringResource(R.string.panel_bookmark), MtIcons.chromeGlyph(MtIconId.PANEL_BOOKMARK)),
)

@Composable
fun ActionPanel(
    visible: Boolean = true,
    items: List<PanelItem> = DefaultPanelItems(),
    tip: String = stringResource(R.string.panel_tip),
    onAction: (PanelItem) -> Unit = {},
    onDismiss: () -> Unit = {},
) {
    val c = LocalMtColors.current
    // §6.7/§10.1：复用 MtPopupDialog——pivot=(0.592f,0.615f) 偏左下炸开，
    // scale 0→1 + alpha 200ms FastOutSlowInEasing；退出仅淡 alpha 150ms；
    // dimAmount=0（setDimAmount(0f) 背景不变暗）+ yOffset=-7dp（整体上移 7dp）。
    MtPopupDialog(
        visible = visible,
        pivot = TransformOrigin(0.592f, 0.615f),
        yOffset = (-7).dp,
        alignment = Alignment.Center,
        shape = RoundedCornerShape(8.dp),
        surfaceModifier = Modifier.width(320.dp),
        onDismissRequest = onDismiss,
    ) {
        // 提示条
        Row(
            Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(tip, color = c.fg3, fontSize = 11.sp, modifier = Modifier.weight(1f))
            Text("✕", color = c.fg3, fontSize = 14.sp, modifier = Modifier.clickable(onClick = onDismiss))
        }
        Box(Modifier.fillMaxWidth().height(0.5.dp).background(c.divider))
        // 2 列 × 5 行
        items.chunked(2).forEach { rowItems ->
            Row(Modifier.fillMaxWidth()) {
                rowItems.forEach { item ->
                    PanelCell(item, Modifier.weight(1f), onClick = { onAction(item) })
                }
                if (rowItems.size == 1) Spacer(Modifier.weight(1f))
            }
        }
    }
}

@Composable
private fun PanelCell(item: PanelItem, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalMtColors.current
    Row(
        modifier
            .height(48.dp)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (item.glyph.isNotEmpty()) {
            Text(item.glyph, color = if (item.arrow) c.accent else c.fg, fontSize = 15.sp)
            Spacer(Modifier.width(10.dp))
        }
        Text(
            item.label,
            color = if (item.arrow) c.accent else c.fg,
            fontSize = 15.sp,
        )
    }
}

@Preview(name = "长按面板", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ActionPanelPreview() {
    MtTheme(ThemeMode.DARK) { ActionPanel() }
}
