/**
 * 职责：左抽屉——296dp 面板 + 遮罩；头部（与主界面顶栏同色）+ 可折叠组（存储/标签/工具）。
 * 主界面内覆盖层：scrim 后面可见主界面，面板左滑入。
 */
package com.copy.mt.ui.components.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.ui.theme.LocalMtColors
import androidx.compose.ui.res.stringResource
import com.copy.mt.R
import com.copy.mt.ui.components.MtIconId
import com.copy.mt.ui.components.MtIcons
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

private data class DrawerNav(val glyph: String, val name: String, val sub: String, val path: String?)
private data class DrawerTool(val glyph: String, val name: String)

@Composable
private fun storageNavs(): List<DrawerNav> = listOf(
    DrawerNav(MtIcons.chromeGlyph(MtIconId.DRAWER_ROOT), stringResource(R.string.storage_root),
        stringResource(R.string.storage_usage, "929.62M", "0B"), "/"),
    DrawerNav(MtIcons.chromeGlyph(MtIconId.DRAWER_INTERNAL), stringResource(R.string.storage_internal),
        stringResource(R.string.storage_usage, "34.91G", "183.57G"), "/storage/emulated/0/"),
)

@Composable
private fun bookmarkNavs(): List<DrawerNav> = listOf(
    DrawerNav(MtIcons.chromeGlyph(MtIconId.DRAWER_BOOKMARK), stringResource(R.string.storage_internal), "/storage/emulated/0", "/storage/emulated/0/"),
)

@Composable
private fun toolItems(): List<DrawerTool> = listOf(
    DrawerTool(MtIcons.chromeGlyph(MtIconId.DRAWER_TRASH), stringResource(R.string.tool_trash)),
    DrawerTool(MtIcons.chromeGlyph(MtIconId.DRAWER_PLUGINS), stringResource(R.string.tool_plugins)),
    DrawerTool(MtIcons.chromeGlyph(MtIconId.DRAWER_APPS), stringResource(R.string.tool_apps)),
    DrawerTool(MtIcons.chromeGlyph(MtIconId.DRAWER_EDITOR), stringResource(R.string.tool_editor)),
    DrawerTool(MtIcons.chromeGlyph(MtIconId.DRAWER_MORE_TOOLS), stringResource(R.string.tool_more)),
)

@Composable
fun DrawerPanel(
    visible: Boolean,
    onDismiss: () -> Unit,
    onNavigate: (String) -> Unit = {},
    onDayNight: () -> Unit = {},
    onMore: () -> Unit = {},
    onTool: (String) -> Unit = {},
) {
    val c = LocalMtColors.current
    Box(Modifier.fillMaxSize()) {
        AnimatedVisibility(visible, enter = fadeIn(tween(220)), exit = fadeOut(tween(220))) {
            MtScrim(onClick = onDismiss)
        }
        AnimatedVisibility(
            visible = visible,
            enter = slideInHorizontally(animationSpec = tween(220)) { -it },
            exit = slideOutHorizontally(animationSpec = tween(220)) { -it },
            modifier = Modifier.align(Alignment.CenterStart),
        ) {
            Column(
                Modifier.width(296.dp).fillMaxHeight().background(c.menuBg),
            ) {
                // 头部：与主界面顶栏同色（bar）
                Column(Modifier.fillMaxWidth().background(c.bar).windowInsetsPadding(WindowInsets.statusBars)) {
                    Row(
                        Modifier.fillMaxWidth().height(56.dp).padding(horizontal = 12.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Column(Modifier.weight(1f)) {
                            Text("MTM", color = c.iglyph, fontSize = 20.sp)
                            Text(stringResource(R.string.drawer_subtitle), color = c.fg2, fontSize = 11.sp)
                        }
                        DrawerHeadBtn(MtIcons.chromeGlyph(MtIconId.DRAWER_DAY)) { onDayNight() }
                        DrawerHeadBtn(MtIcons.chromeGlyph(MtIconId.DRAWER_MORE)) { onMore() }
                    }
                }
                Box(Modifier.fillMaxWidth().height(1.dp).background(c.divider))

                // 可折叠分组（默认展开；旋转不丢）
                var storageOpen by rememberSaveable { mutableStateOf(true) }
                var bookmarksOpen by rememberSaveable { mutableStateOf(true) }
                var toolsOpen by rememberSaveable { mutableStateOf(true) }

                Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
                    DrawerCollapsibleGroup(stringResource(R.string.group_storage), storageOpen, { storageOpen = !storageOpen }) {
                        storageNavs().forEach { n -> DrawerNavRow(n, onNavigate) }
                    }
                    DrawerCollapsibleGroup(stringResource(R.string.group_bookmarks), bookmarksOpen, { bookmarksOpen = !bookmarksOpen }) {
                        bookmarkNavs().forEach { n -> DrawerNavRow(n, onNavigate) }
                    }
                    DrawerCollapsibleGroup(stringResource(R.string.group_tools), toolsOpen, { toolsOpen = !toolsOpen }) {
                        toolItems().forEach { t ->
                            DrawerNavRow(DrawerNav(t.glyph, t.name, "", null), onNavigate) { onTool(t.name) }
                        }
                    }
                }
            }
        }
    }
}

/** 可折叠组头：整行可点，展开时才渲染内容。 */
@Composable
private fun DrawerCollapsibleGroup(
    title: String,
    expanded: Boolean,
    onToggle: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalMtColors.current
    Column(Modifier.fillMaxWidth()) {
        Row(
            Modifier
                .fillMaxWidth()
                .clickable(onClick = onToggle)
                .padding(start = 16.dp, top = 12.dp, bottom = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(if (expanded) "▾" else "▸", color = c.fg2, fontSize = 12.sp)
            Spacer(Modifier.width(4.dp))
            Text(title, color = c.accent, fontSize = 14.sp)
        }
        if (expanded) {
            Column(Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Composable
private fun DrawerHeadBtn(glyph: String, onClick: () -> Unit) {
    Box(Modifier.size(40.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(glyph, color = LocalMtColors.current.iglyph, fontSize = 18.sp)
    }
}

@Composable
private fun DrawerNavRow(n: DrawerNav, onNavigate: (String) -> Unit, onClick: (() -> Unit)? = null) {
    val c = LocalMtColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .height(50.dp)
            .clickable { onClick?.invoke() ?: n.path?.let(onNavigate) }
            .padding(horizontal = 16.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(n.glyph, color = c.fg2, fontSize = 15.sp)
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(n.name, color = c.menuFg, fontSize = 15.sp)
            if (n.sub.isNotEmpty()) Text(n.sub, color = c.fg2, fontSize = 11.sp)
        }
    }
}

@Preview(name = "抽屉", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun DrawerPanelPreview() {
    MtTheme(ThemeMode.DARK) {
        Box(Modifier.background(LocalMtColors.current.surface)) {
            DrawerPanel(visible = true, onDismiss = {})
        }
    }
}
