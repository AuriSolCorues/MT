/**
 * 职责：设置页——顶栏 + 分组行；灰项（Root/su/自启）不可点。
 */
package com.copy.mt.ui.page

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.copy.mt.R
import com.copy.mt.data.config.AppLocale
import com.copy.mt.data.config.saveLocaleTagAsync
import com.copy.mt.ui.components.LanguageDialog
import com.copy.mt.ui.components.MtDivider
import com.copy.mt.ui.components.MtGroup
import com.copy.mt.ui.components.MtRow
import com.copy.mt.ui.components.MtSwitchRow
import com.copy.mt.ui.components.MtTopBar
import com.copy.mt.ui.platform.LocalActivity
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

/** 语言行的 value：写死映射，保证选中语言后名称不随界面语言翻译。 */
@Composable
private fun localeLabel(tag: String): String = when (tag) {
    AppLocale.ZH_CN -> "简体中文"
    AppLocale.EN -> "English"
    else -> stringResource(R.string.val_follow_system)
}

@Composable
fun SettingsScreen(
    onBack: () -> Unit = {},
    onThemeClick: () -> Unit = {},
) {
    val context = LocalContext.current
    val activity = LocalActivity.current
    var showLangDialog by remember { mutableStateOf(false) }
    // 当前生效语言 tag（每次重组读取，选中后 recreate 使其立即反映新值）
    var localeTag by remember { mutableStateOf(AppLocale.currentTag(context)) }

    /** 选中语言：持久化 + 即时生效 + 重建界面。 */
    fun selectLanguage(tag: String) {
        showLangDialog = false
        saveLocaleTagAsync(context, tag)
        AppLocale.apply(context, tag)
        localeTag = tag
        // API33+ 系统通常自行重建；这里统一 recreate 兜底确保立即刷新
        activity?.recreate()
    }

    Column(Modifier.fillMaxSize().background(LocalMtColors.current.surface)) {
        MtTopBar(title = stringResource(R.string.settings_title), onBack = onBack)
        Column(Modifier.fillMaxWidth().weight(1f).verticalScroll(rememberScrollState())) {
            // 启动
            MtGroup(stringResource(R.string.grp_startup)) {
                MtRow(stringResource(R.string.set_root), value = stringResource(R.string.val_off), enabled = false)
                MtDivider()
                MtSwitchRow(stringResource(R.string.set_shell), true, {})
                MtDivider()
                MtRow(stringResource(R.string.set_su), enabled = false, value = "")
                MtDivider()
                MtRow(stringResource(R.string.set_start_left), value = stringResource(R.string.val_home))
                MtDivider()
                MtRow(stringResource(R.string.set_start_right), value = stringResource(R.string.val_home))
                MtDivider()
                MtRow(stringResource(R.string.set_autostart), value = stringResource(R.string.val_unset), enabled = false)
            }
            // 外观
            MtGroup(stringResource(R.string.grp_appearance)) {
                MtRow(stringResource(R.string.set_theme), value = stringResource(R.string.val_dark) + " >", onClick = onThemeClick)
                MtDivider()
                MtSwitchRow(stringResource(R.string.set_follow_system), false, {})
                MtDivider()
                MtRow(stringResource(R.string.set_launcher), value = stringResource(R.string.val_light) + " >")
                MtDivider()
                MtRow(stringResource(R.string.set_name_lines), value = "2")
                MtDivider()
                MtSwitchRow(stringResource(R.string.set_hide_perm), false, {})
                MtDivider()
                MtRow(stringResource(R.string.set_datetime), value = "yyyy-MM-dd HH:mm:ss")
            }
            // 常规
            MtGroup(stringResource(R.string.grp_general)) {
                MtSwitchRow(stringResource(R.string.set_backup), true, {})
                MtDivider()
                MtSwitchRow(stringResource(R.string.set_keep_time), true, {})
                MtDivider()
                MtSwitchRow(stringResource(R.string.set_favorites), true, {})
                MtDivider()
                MtRow(stringResource(R.string.set_menu_sort), value = stringResource(R.string.val_drag_sort))
                MtDivider()
                MtRow(
                    stringResource(R.string.set_lang),
                    value = localeLabel(localeTag) + " >",
                    onClick = { showLangDialog = true },
                )
            }
            // 其余组（占位）
            MtGroup(stringResource(R.string.grp_trash)) { MtRow(stringResource(R.string.set_trash_days), value = "30") }
            MtGroup(stringResource(R.string.grp_install)) { MtRow(stringResource(R.string.set_install_allow), value = stringResource(R.string.val_follow)) }
            MtGroup(stringResource(R.string.grp_bookmark_bar)) { MtRow(stringResource(R.string.set_bottom_bar), value = stringResource(R.string.val_5keys)) }
            MtGroup(stringResource(R.string.grp_external)) { MtRow(stringResource(R.string.set_sdcard), value = "/") }
            MtGroup(stringResource(R.string.grp_other)) { MtRow(stringResource(R.string.set_about), value = "2.6.9 >") }
        }
    }
    if (showLangDialog) {
        LanguageDialog(current = localeTag, onSelect = ::selectLanguage, onDismiss = { showLangDialog = false })
    }
}

@Preview(name = "设置页", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun SettingsScreenPreview() {
    MtTheme(ThemeMode.DARK) { SettingsScreen() }
}
