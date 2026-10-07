/**
 * 职责：已安装应用页（APK 提取页，仿 MT 管理器 图13）——顶栏 + 用户/系统 Tab + 应用卡片列表；
 * 多选态：标题计数 + 底部操作条（提取/卸载/分享/属性）+ 右侧 4 个 FAB 占位。
 * 当前为静态 mock + stub 回调；接 VM 后由调用方传入数据。
 */
package com.copy.mt.ui.page

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.R
import com.copy.mt.ui.components.MtDivider
import com.copy.mt.ui.components.MtIconId
import com.copy.mt.ui.components.MtIcons
import com.copy.mt.ui.components.MtTopBar
import com.copy.mt.ui.components.overlay.MenuPopup
import com.copy.mt.ui.components.overlay.MenuItem
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

/** 应用条目（mock 形态；接 VM 后由模型替代）。 */
data class ApkItem(
    val name: String, // 应用名
    val version: String, // 版本号
    val sizeLabel: String, // 大小（已格式化文本）
    val packageName: String, // 包名（同时作列表 key / 选中 key）
    val isSystem: Boolean = false, // 是否系统应用
)

/** mock 数据：用户应用 3 个。 */
private val MockUserApps = listOf(
    ApkItem("微信", "8.0.77", "280M", "com.tencent.mm"),
    ApkItem("Telegram", "12.9.2", "70M", "org.telegram.messenger"),
    ApkItem("APKPure", "3.20.7901", "50M", "com.apkpure.aegon"),
)

/** mock 数据：系统应用 2 个。 */
private val MockSystemApps = listOf(
    ApkItem("Android 系统", "15", "1.2G", "com.android.system", isSystem = true),
    ApkItem("OPPO 服务", "14.1.2", "96M", "com.oppo.service", isSystem = true),
)

/** Tab 枚举。 */
private enum class AppsTab { USER, SYSTEM }

/**
 * 已安装应用页入口（无状态 page：数据由参数传入，交互状态组件内 remember 自治）。
 * @param initialMulti / initialSel 仅作预览种子状态，业务侧不用传。
 */
@Composable
fun ApksScreen(
    userApps: List<ApkItem> = MockUserApps,
    systemApps: List<ApkItem> = MockSystemApps,
    onBack: () -> Unit = {},
    onItemClick: (ApkItem) -> Unit = {},
    onOpenSettings: () -> Unit = {},
    initialMulti: Boolean = false,
    initialSel: Set<String> = emptySet(),
) {
    val c = LocalMtColors.current
    var tab by remember { mutableStateOf(AppsTab.USER) }
    var multi by remember { mutableStateOf(initialMulti) }
    var sel by remember { mutableStateOf(initialSel) }
    var menuOpen by remember { mutableStateOf(false) }

    val list = if (tab == AppsTab.USER) userApps else systemApps

    Box(Modifier.fillMaxSize().background(c.surface)) {
        Column(Modifier.fillMaxSize()) {
            // 顶栏：多选态标题变「已选: N」；返回键在多选态先退多选
            MtTopBar(
                title = if (multi) stringResource(R.string.apps_selected, sel.size)
                else stringResource(R.string.apps_title),
                onBack = {
                    if (multi) {
                        multi = false
                        sel = emptySet()
                    } else {
                        onBack()
                    }
                },
                actions = {
                    // 搜索（TODO(stub): 搜索功能未实现）
                    Box(Modifier.size(48.dp).clickable { /* TODO(stub): 搜索 */ }, contentAlignment = Alignment.Center) {
                        Text("🔍", color = c.iglyph, fontSize = 18.sp) // TODO(icon): 待接入搜索真图标
                    }
                    // ⋮ 菜单
                    Box(Modifier.size(48.dp).clickable { menuOpen = true }, contentAlignment = Alignment.Center) {
                        Text(MtIcons.chromeGlyph(MtIconId.MORE), color = c.iglyph, fontSize = 20.sp)
                    }
                },
            )

            // Tab 行：用户应用 | 系统应用，各占半屏 48dp，选中下划线 accent
            Row(Modifier.fillMaxWidth().height(48.dp).background(c.bar)) {
                AppsTabItem(stringResource(R.string.apps_tab_user), tab == AppsTab.USER, Modifier.weight(1f)) { tab = AppsTab.USER }
                AppsTabItem(stringResource(R.string.apps_tab_system), tab == AppsTab.SYSTEM, Modifier.weight(1f)) { tab = AppsTab.SYSTEM }
            }

            // 列表
            if (list.isEmpty()) {
                Box(Modifier.weight(1f).fillMaxWidth(), contentAlignment = Alignment.Center) {
                    Text(stringResource(R.string.apps_empty), color = c.fg3, fontSize = 13.sp)
                }
            } else {
                LazyColumn(Modifier.weight(1f).fillMaxWidth()) {
                    items(list, key = { it.packageName }) { app ->
                        ApkCard(
                            app = app,
                            multi = multi,
                            selected = app.packageName in sel,
                            onClick = {
                                if (multi) {
                                    // 多选态：单击=切换选中；全不选则退出多选
                                    sel = if (app.packageName in sel) sel - app.packageName else sel + app.packageName
                                    if (sel.isEmpty()) multi = false
                                } else {
                                    onItemClick(app) // TODO(stub): 打开应用详情
                                }
                            },
                            onLongClick = {
                                if (!multi) {
                                    multi = true
                                    sel = setOf(app.packageName)
                                } else {
                                    sel = if (app.packageName in sel) sel - app.packageName else sel + app.packageName
                                }
                            },
                        )
                        MtDivider()
                    }
                }
            }

            // 多选态底部操作条：提取 / 卸载 / 分享 / 属性
            if (multi) {
                MultiActionBar()
            }
        }

        // 多选态右侧 4 个 FAB 占位（竖排，底距 12dp；48dp 操作条 + 12dp 间隙）
        if (multi) {
            FabColumn(Modifier.align(Alignment.BottomEnd).padding(end = 12.dp, bottom = 60.dp))
        }

        // ⋮ 菜单：排序方式 / 全选 / 设置
        if (menuOpen) {
            MenuPopup(
                visible = true,
                onDismiss = { menuOpen = false },
                items = listOf(
                    MenuItem("sort_by", stringResource(R.string.menu_sort_by)), // TODO(stub): 排序未实现
                    MenuItem("select_all", stringResource(R.string.menu_select_all)),
                    MenuItem("settings", stringResource(R.string.menu_settings)),
                ),
                onAction = { item ->
                    menuOpen = false
                    when (item.id) {
                        "select_all" -> sel = list.map { it.packageName }.toSet() // 全选当前 tab
                        "settings" -> onOpenSettings()
                        // "sort_by" TODO(stub)
                    }
                },
            )
        }
    }
}

/** Tab 单项：文本居中 + 选中底部 2dp accent 下划线。 */
@Composable
private fun AppsTabItem(text: String, selected: Boolean, modifier: Modifier = Modifier, onClick: () -> Unit) {
    val c = LocalMtColors.current
    Column(modifier.clickable(onClick = onClick), horizontalAlignment = Alignment.CenterHorizontally) {
        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
            Text(
                text,
                color = if (selected) c.fg else c.fg2,
                fontSize = 15.sp,
                fontWeight = if (selected) androidx.compose.ui.text.font.FontWeight.Medium else androidx.compose.ui.text.font.FontWeight.Normal,
            )
        }
        Box(Modifier.fillMaxWidth().height(2.dp).background(if (selected) c.accent else Color.Transparent))
    }
}

/** 应用卡片：勾选圈(多选) + 图标位 28dp + 名称 15sp + 版本·大小/包名 11sp 灰。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun ApkCard(
    app: ApkItem,
    multi: Boolean,
    selected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit,
) {
    val c = LocalMtColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(if (selected) c.accent.copy(alpha = 0.18f) else c.surface) // 选中态天蓝
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (multi) {
            // 多选态左侧勾选圈
            Box(
                Modifier
                    .size(20.dp)
                    .clip(CircleShape)
                    .background(if (selected) c.accent else Color.Transparent)
                    .border(
                        width = 1.5.dp,
                        color = if (selected) c.accent else c.fg3,
                        shape = CircleShape,
                    ),
                contentAlignment = Alignment.Center,
            ) {
                if (selected) Text("✓", color = Color.White, fontSize = 12.sp)
            }
            Spacer(Modifier.width(10.dp))
        }
        // 图标位：28dp 圆角框占位（TODO(icon): 待接入应用真实图标）
        Box(
            Modifier
                .size(28.dp)
                .clip(RoundedCornerShape(5.dp))
                .background(c.iframe),
            contentAlignment = Alignment.Center,
        ) {
            Text("▣", color = c.iglyph, fontSize = 13.sp) // TODO(icon): 占位字形，待替换应用图标
        }
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(app.name, color = c.fg, fontSize = 15.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
            Text(
                "${app.version} · ${app.sizeLabel}",
                color = c.fg2,
                fontSize = 11.sp,
                maxLines = 1,
            )
            Text(app.packageName, color = c.fg2, fontSize = 11.sp, maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
    }
}

/** 多选态底部操作条：4 个平铺按钮，48dp 高（业务 TODO stub）。 */
@Composable
private fun MultiActionBar() {
    val c = LocalMtColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .background(c.bar)
            .padding(vertical = 0.dp)
            .height(48.dp),
    ) {
        listOf(
            stringResource(R.string.apps_extract),
            stringResource(R.string.apps_uninstall),
            stringResource(R.string.apps_share),
            stringResource(R.string.apps_props),
        ).forEach { label ->
            Box(
                Modifier
                    .weight(1f)
                    .height(48.dp)
                    .clickable { /* TODO(stub): $label 业务未实现 */ },
                contentAlignment = Alignment.Center,
            ) {
                Text(label, color = c.fg, fontSize = 14.sp)
            }
        }
    }
}

/** 多选态右侧 FAB 列：4 个 50dp 圆形 Surface + 字形占位（业务 TODO stub）。 */
@Composable
private fun FabColumn(modifier: Modifier = Modifier) {
    val c = LocalMtColors.current
    // core 没有的字形用占位字符；提取/卸载待接真图标
    val fabs = listOf(
        "⤓" to stringResource(R.string.apps_extract), // TODO(icon): 提取占位
        "🗑" to stringResource(R.string.apps_uninstall), // TODO(icon): 卸载占位
        MtIcons.chromeGlyph(MtIconId.PANEL_SHARE) to stringResource(R.string.apps_share),
        MtIcons.chromeGlyph(MtIconId.PANEL_PROPS) to stringResource(R.string.apps_props),
    )
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        fabs.forEach { (glyph, label) ->
            Surface(
                shape = CircleShape,
                color = c.accent,
                shadowElevation = 4.dp,
                modifier = Modifier.size(50.dp).clickable { /* TODO(stub): $label 业务未实现 */ },
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(glyph, color = Color.White, fontSize = 20.sp)
                }
            }
        }
    }
}

@Preview(name = "已安装应用-深色", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ApksScreenDarkPreview() {
    MtTheme(ThemeMode.DARK) { ApksScreen() }
}

@Preview(name = "已安装应用-浅色", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ApksScreenLightPreview() {
    MtTheme(ThemeMode.LIGHT) { ApksScreen() }
}

@Preview(name = "已安装应用-多选态", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ApksScreenMultiPreview() {
    MtTheme(ThemeMode.DARK) {
        ApksScreen(
            initialMulti = true,
            initialSel = setOf("com.tencent.mm", "org.telegram.messenger"),
        )
    }
}

@Preview(name = "已安装应用-空列表", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun ApksScreenEmptyPreview() {
    MtTheme(ThemeMode.DARK) { ApksScreen(userApps = emptyList(), systemApps = emptyList()) }
}
