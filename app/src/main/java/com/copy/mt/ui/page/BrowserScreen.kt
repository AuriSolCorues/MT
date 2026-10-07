/**
 * 职责：主界面（双栏文件浏览）——顶栏 + 双栏 + 底栏；
 *       动效：水滴下拉 / 级联渐入 / 边缘阴影折叠 / 横滑选中 / 底栏整组切换 / 同步图标交叉淡变；
 *       覆盖层：抽屉 / ⋮菜单 / 长按面板 / 新建对话框。
 */
package com.copy.mt.ui.page

import android.app.Activity
import android.widget.Toast
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBars
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.runtime.mutableLongStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.R
import com.copy.mt.core.model.FileItem
import com.copy.mt.ui.components.FileKind
import com.copy.mt.ui.components.MtFileRow
import com.copy.mt.ui.components.BottomBarCrossfade
import com.copy.mt.ui.components.BottomBarMode
import com.copy.mt.ui.components.CascadeItem
import com.copy.mt.ui.components.ShadowEdges
import com.copy.mt.ui.components.ShadowSide
import com.copy.mt.ui.components.SwipeSelectBox
import com.copy.mt.ui.components.SyncCrossfadeIcon
import com.copy.mt.ui.components.WaterDropRefresh
import com.copy.mt.ui.components.overlay.ActionPanel
import com.copy.mt.ui.components.overlay.DefaultMenuItems
import com.copy.mt.ui.components.overlay.DrawerPanel
import com.copy.mt.ui.components.overlay.MenuPopup
import com.copy.mt.ui.components.NewEntryDialog
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

private val timeFmt = SimpleDateFormat("yy-MM-dd HH:mm", Locale.getDefault())

private fun fmtTime(ms: Long): String = if (ms <= 0) "" else timeFmt.format(Date(ms))

private fun fmtSize(bytes: Long): String {
    if (bytes < 1024) return "$bytes B"
    val kb = bytes / 1024.0
    if (kb < 1024) return String.format(Locale.US, "%.2f K", kb)
    val mb = kb / 1024.0
    if (mb < 1024) return String.format(Locale.US, "%.2f M", mb)
    return String.format(Locale.US, "%.2f G", mb / 1024.0)
}

private fun kindOf(item: FileItem): FileKind = when {
    item.name == ".." -> FileKind.UP
    item.isDir -> FileKind.FOLDER
    else -> FileKind.FILE
}

@Composable
fun BrowserScreen(
    left: PaneState = PaneState(),
    right: PaneState = PaneState(),
    active: Side = Side.LEFT,
    onActivate: (Side) -> Unit = {},
    onUp: (Side) -> Unit = {},
    onOpenPath: (String) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    onItemClick: (Side, FileItem) -> Unit = { _, _ -> },
    onSync: () -> Unit = {},
    onRefresh: (Side) -> Unit = {},
) {
    val c = LocalMtColors.current
    val context = LocalContext.current

    // ---- 覆盖层 / 交互状态（页面局部；ViewModel 承载方式仍后定） ----
    var drawerOpen by remember { mutableStateOf(false) }
    var menuOpen by remember { mutableStateOf(false) }
    var panelItem by remember { mutableStateOf<FileItem?>(null) }
    var panelSide by remember { mutableStateOf<Side?>(null) }
    var newEntryOpen by remember { mutableStateOf(false) }
    var multi by remember { mutableStateOf(false) }
    var sel by remember { mutableStateOf(setOf<String>()) }   // key = path + name

    val activePane = if (active == Side.LEFT) left else right
    fun rows(p: PaneState) = p.items

    fun selKey(side: Side, item: FileItem) = "${side.name}:${item.path}/${item.name}"

    fun toggleSel(side: Side, item: FileItem) {
        val k = selKey(side, item)
        sel = if (k in sel) sel - k else sel + k
        if (sel.isEmpty()) multi = false
    }

    fun exitMulti() { multi = false; sel = emptySet() }

    // 根目录判定：无「..」即在根（ViewModel.open 仅在非根追加 ..）
    fun isAtRoot(side: Side): Boolean =
        (if (side == Side.LEFT) left else right).items.none { it.name == ".." }

    // 统一返回（§8.6 / 底栏 ↑ 语义）：
    //   浮层优先（新建框 → 多选 → 面板 → 菜单 → 抽屉）→ 激活栏上一级 →
    //   根目录时 toast「再点一次退出」，1.8s 内再按即退出
    var lastBackAt by remember { mutableLongStateOf(0L) }
    fun handleBack(): Boolean {
        when {
            newEntryOpen -> { newEntryOpen = false; return true }
            multi -> { exitMulti(); return true }
            panelItem != null -> { panelItem = null; return true }
            menuOpen -> { menuOpen = false; return true }
            drawerOpen -> { drawerOpen = false; return true }
        }
        // 无浮层：激活栏上一级；根目录双按退出
        if (isAtRoot(active)) {
            val now = System.currentTimeMillis()
            if (now - lastBackAt <= 1800L) {
                (context as? Activity)?.finish()
            } else {
                lastBackAt = now
                Toast.makeText(context, context.getString(R.string.press_again_to_exit), Toast.LENGTH_SHORT).show()
            }
        } else {
            onUp(active)
        }
        return true
    }

    // 系统返回键 = 统一返回
    BackHandler { handleBack() }

    fun openWithStub(item: FileItem) {
        Toast.makeText(context, context.getString(R.string.open_with_stub), Toast.LENGTH_SHORT).show()
    }

    Box(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().background(c.surface)) {
            MtToolbar(
                path = activePane.path,
                items = activePane.items,
                selectedCount = if (multi) sel.size else 0,
                multi = multi,
                onMenu = { drawerOpen = true },
                onPathClick = {},
                onMore = { menuOpen = true },
            )
            Row(Modifier.weight(1f).fillMaxWidth()) {
                FilePane(
                    pane = left, active = active == Side.LEFT, side = Side.LEFT,
                    multi = multi, sel = sel, panelItem = panelItem, panelSide = panelSide,
                    modifier = Modifier.weight(1f),
                    dirIsLeftToRight = active == Side.LEFT,
                    onActivate = onActivate,
                    onItemClick = { side, item ->
                        if (item.name == "..") { onUp(side); return@FilePane }
                        if (multi) { toggleSel(side, item); return@FilePane }
                        if (item.isDir) onOpenPath(item.path) else openWithStub(item)
                    },
                    onIconTap = { side, item ->
                        // 点图标 = 进多选并选中该项
                        multi = true
                        onActivate(side)
                        val k = selKey(side, item)
                        sel = sel + k
                    },
                    onRefresh = onRefresh,
                    onUp = onUp,
                    onLongPress = { side, item ->
                        panelSide = side; panelItem = item
                    },
                    onSwipeSelect = { side, item ->
                        multi = true
                        val k = selKey(side, item)
                        sel = sel + k
                    },
                    epoch = left.items.hashCode(),
                )
                Box(Modifier.width(1.dp).fillMaxSize().background(c.divider))
                FilePane(
                    pane = right, active = active == Side.RIGHT, side = Side.RIGHT,
                    multi = multi, sel = sel, panelItem = panelItem, panelSide = panelSide,
                    modifier = Modifier.weight(1f),
                    dirIsLeftToRight = active == Side.LEFT,
                    onActivate = onActivate,
                    onItemClick = { side, item ->
                        if (item.name == "..") { onUp(side); return@FilePane }
                        if (multi) { toggleSel(side, item); return@FilePane }
                        if (item.isDir) onOpenPath(item.path) else openWithStub(item)
                    },
                    onIconTap = { side, item ->
                        multi = true
                        onActivate(side)
                        sel = sel + selKey(side, item)
                    },
                    onUp = onUp,
                    onLongPress = { side, item ->
                        panelSide = side; panelItem = item
                    },
                    onSwipeSelect = { side, item ->
                        multi = true
                        sel = sel + selKey(side, item)
                    },
                    onRefresh = onRefresh,
                    epoch = right.items.hashCode(),
                )
            }
            BottomBar(
                mode = if (multi) BottomBarMode.MULTI else BottomBarMode.NORMAL,
                active = active,
                canBack = activePane.items.isNotEmpty(),
                canForward = false,
                onNav = { id ->
                    when (id) {
                        "back" -> onUp(active)          // ← 栏内历史后退（历史栈待实现，暂走上退）
                        "forward" -> {}                 // TODO: 前进历史栈
                        "new" -> newEntryOpen = true
                        "sync" -> onSync()
                        "up" -> handleBack()            // ↑ 系统返回：浮层→上退→根目录双按退出
                    }
                },
                onMultiAction = { id ->
                    val items = activePane.items.filter { it.name != ".." }
                    when (id) {
                        "all" -> sel = items.map { it.path + it.name }.toSet()
                        "invert" -> {
                            val keys = items.map { it.path + it.name }
                            sel = keys.filterNot { it in sel }.toSet()
                        }
                        "clear" -> exitMulti()
                        "cond" -> Toast.makeText(context, "条件选择（待实现）", Toast.LENGTH_SHORT).show()
                        "tbd" -> {}
                    }
                },
            )
        }

        // 抽屉（主界面变暗 + 296dp 滑入）
        DrawerPanel(
            visible = drawerOpen,
            onDismiss = { drawerOpen = false },
            onNavigate = { path -> drawerOpen = false; onOpenPath(path) },
            onDayNight = { /* 主题切换入口，后续接设置 */ },
            onMore = { drawerOpen = false; menuOpen = true },
            onTool = { name ->
                drawerOpen = false
                if (name == "已安装应用" || name == "Installed apps") onOpenSettings() // 占位：后续接 APK 页
            },
        )

        // ⋮ 菜单
        MenuPopup(
            visible = menuOpen,
            items = DefaultMenuItems(),
            onDismiss = { menuOpen = false },
            onAction = { item ->
                menuOpen = false
                when (item.id) {
                    "settings" -> onOpenSettings()
                    "refresh" -> { /* 刷新走 VM，后续接 */ }
                    "swap" -> onActivate(if (active == Side.LEFT) Side.RIGHT else Side.LEFT)
                }
            },
        )

        // 长按面板（常驻组合，visible 驱动进/出动画；panelItem 仅作显示开关）
        ActionPanel(
            visible = panelItem != null,
            onAction = { panelItem = null },
            onDismiss = { panelItem = null },
        )

        // 新建对话框
        if (newEntryOpen) {
            NewEntryDialog(
                onCreateFolder = { name -> newEntryOpen = false; Toast.makeText(context, name, Toast.LENGTH_SHORT).show() },
                onCreateFile = { name -> newEntryOpen = false; Toast.makeText(context, name, Toast.LENGTH_SHORT).show() },
                onDismiss = { newEntryOpen = false },
            )
        }
    }
}

@Composable
private fun MtToolbar(
    path: String,
    items: List<FileItem>,
    selectedCount: Int,
    multi: Boolean,
    onMenu: () -> Unit,
    onPathClick: () -> Unit,
    onMore: () -> Unit,
) {
    val c = LocalMtColors.current
    val folders = items.count { it.isDir && it.name != ".." }
    val files = items.count { !it.isDir }
    val summary = stringResource(R.string.browser_stat, folders, files)
    val selText = if (multi && selectedCount > 0)
        "  " + stringResource(R.string.browser_selected, selectedCount) else ""
    Row(
        Modifier
            .fillMaxWidth()
            .background(c.bar)
            .windowInsetsPadding(WindowInsets.statusBars)
            .height(56.dp)
            .padding(horizontal = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        GlyphBtn("☰", stringResource(R.string.cd_menu), onMenu)
        Column(Modifier.weight(1f).padding(horizontal = 8.dp).clickable(onClick = onPathClick)) {
            Text(path, color = Color.White, fontSize = 18.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(summary + selText, color = Color.White.copy(alpha = 0.7f), fontSize = 11.sp, maxLines = 1)
        }
        GlyphBtn("⋮", stringResource(R.string.cd_more), onMore)
    }
}

@Composable
private fun GlyphBtn(glyph: String, desc: String, onClick: () -> Unit) {
    Box(Modifier.size(48.dp).clickable(onClick = onClick), contentAlignment = Alignment.Center) {
        Text(glyph, color = LocalMtColors.current.iglyph, fontSize = 20.sp)
    }
}

@Composable
private fun FilePane(
    pane: PaneState,
    active: Boolean,
    side: Side,
    modifier: Modifier = Modifier,
    multi: Boolean,
    sel: Set<String>,
    panelItem: FileItem?,
    panelSide: Side?,
    dirIsLeftToRight: Boolean,
    onActivate: (Side) -> Unit,
    onItemClick: (Side, FileItem) -> Unit,
    onIconTap: (Side, FileItem) -> Unit,
    onUp: (Side) -> Unit,
    onLongPress: (Side, FileItem) -> Unit,
    onSwipeSelect: (Side, FileItem) -> Unit,
    onRefresh: (Side) -> Unit,
    epoch: Int,
) {
    val c = LocalMtColors.current
    val listState = rememberLazyListState()
    // 滚动本栏 = 激活本栏（§8.6 触发点⑤）：首可见项索引变化即视为「在操作这一栏」
    var lastIndex by remember { mutableStateOf(listState.firstVisibleItemIndex) }
    LaunchedEffect(listState) {
        snapshotFlow { listState.firstVisibleItemIndex to listState.isScrollInProgress }
            .collect { (index, scrolling) ->
                if (scrolling && index != lastIndex) { lastIndex = index; onActivate(side) }
            }
    }
    Box(modifier.background(c.surface)) {
        WaterDropRefresh(
            isLeftToRight = dirIsLeftToRight,
            listState = listState,
            onRefresh = { onRefresh(side) },
            onPullStart = { onActivate(side) },
            // 接通刷新状态机：pane.loading 为 true 时是刷新中，false 时解锁 finishRefresh。
            // 旧版没传此参数（恒 false），导致 LaunchedEffect 永不触发 finishRefresh，
            // state.isRefreshing 卡死为 true，下拉刷新只触发一次就锁死。
            isRefreshing = pane.loading,
            modifier = Modifier.fillMaxSize(),
        ) {
            LazyColumn(Modifier.fillMaxSize(), state = listState) {
                itemsIndexed(pane.items, key = { _, it -> it.path + it.name }) { index, item ->
                    val key = item.path + item.name
                    val selected = key in sel
                    CascadeItem(epoch = epoch, index = index) {
                        SwipeSelectBox(
                            selected = selected,
                            enabled = item.name != ".." && !selected,
                            onSelected = { onSwipeSelect(side, item) },
                            modifier = Modifier.fillMaxWidth(),
                        ) {
                            MtFileRow(
                                name = item.name,
                                meta = if (item.isDir) "" else "${fmtTime(item.modifiedAt)}   ${fmtSize(item.size)}",
                                kind = kindOf(item),
                                selected = selected || (panelItem == item && panelSide == side),
                                onClick = { onActivate(side); onItemClick(side, item) },
                                onLongClick = { onActivate(side); onLongPress(side, item) },
                                onIconTap = if (item.name != "..") ({ onIconTap(side, item) }) else null,
                            )
                        }
                    }
                }
            }
        }
        // 非激活栏折叠阴影：右栏阴影画在左缘，左栏画在右缘
        ShadowEdges(
            side = side,
            active = active,
            modifier = Modifier.fillMaxSize(),
        )
    }
}

@Composable
private fun BottomBar(
    mode: BottomBarMode,
    active: Side,
    canBack: Boolean,
    canForward: Boolean,
    onNav: (String) -> Unit,
    onMultiAction: (String) -> Unit,
) {
    val c = LocalMtColors.current
    BottomBarCrossfade(
        target = mode,
        normal = {
            Row(
                Modifier.fillMaxWidth().background(c.bar).windowInsetsPadding(WindowInsets.navigationBars).height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val keys = listOf("back", "forward", "new", "sync", "up")
                keys.forEach { id ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        when (id) {
                            "sync" -> SyncCrossfadeIcon(active = active)
                            else -> {
                                val glyph = when (id) {
                                    "back" -> "←"; "forward" -> "→"; "new" -> "＋"; else -> "↑"
                                }
                                Text(glyph, color = c.fg2, fontSize = 20.sp, fontWeight = FontWeight.Medium,
                                    modifier = Modifier.clickable { onNav(id) })
                            }
                        }
                    }
                }
            }
        },
        multi = {
            Row(
                Modifier.fillMaxWidth().background(c.bar).windowInsetsPadding(WindowInsets.navigationBars).height(64.dp),
                horizontalArrangement = Arrangement.SpaceEvenly,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                val actions = listOf(
                    "all" to stringResource(R.string.multi_all),
                    "invert" to stringResource(R.string.multi_invert),
                    "clear" to stringResource(R.string.multi_clear),
                    "cond" to stringResource(R.string.multi_cond),
                    "tbd" to stringResource(R.string.multi_tbd),
                )
                actions.forEach { (id, label) ->
                    Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                        Text(label, color = c.fg2, fontSize = 12.sp,
                            modifier = Modifier.clickable { onMultiAction(id) })
                    }
                }
            }
        },
    )
}

@Preview(name = "主界面-深色", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun BrowserScreenPreview() {
    MtTheme(ThemeMode.DARK) {
        BrowserScreen(
            left = PaneState(
                path = "/storage/emulated/0/",
                items = listOf(
                    FileItem("Documents", "/storage/emulated/0/Documents", true, 0, 1759300000000),
                    FileItem("Download", "/storage/emulated/0/Download", true, 0, 1759300000000),
                    FileItem("chk.png", "/storage/emulated/0/chk.png", false, 185_886, 1759730000000),
                ),
            ),
            right = PaneState(
                path = "/storage/emulated/0/",
                items = listOf(
                    FileItem("Account", "/storage/emulated/0/Account", true, 0, 1759600000000),
                    FileItem("Android", "/storage/emulated/0/Android", true, 0, 1759600000000),
                    FileItem("DCIM", "/storage/emulated/0/DCIM", true, 0, 1759600000000),
                ),
            ),
            active = Side.LEFT,
        )
    }
}
