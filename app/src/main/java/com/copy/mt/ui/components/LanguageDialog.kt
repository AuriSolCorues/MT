/**
 * 职责：语言选择弹窗——无状态，跟随系统 / 简体中文 / English 三选一。
 */
package com.copy.mt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.R
import com.copy.mt.data.config.AppLocale
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

/** 单个语言选项行：名称 + 选中态对钩（选中色用强调色，未选中透明占位保持对齐）。 */
@Composable
private fun LanguageOption(name: String, selected: Boolean, onClick: () -> Unit) {
    val c = LocalMtColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 4.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(name, color = c.fg, fontSize = 15.sp, modifier = Modifier.weight(1f))
        Text("✓", color = if (selected) c.accent else Color.Transparent, fontSize = 15.sp)
    }
}

/**
 * 语言选择弹窗。
 * @param current 当前生效 tag（""=跟随系统 / "zh-CN" / "en"）
 * @param onSelect 选中回调，参数为 tag
 */
@Composable
fun LanguageDialog(
    current: String,
    onSelect: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    MtDialog(
        title = stringResource(R.string.lang_dialog_title),
        onDismiss = onDismiss,
    ) {
        Column {
            LanguageOption(
                name = stringResource(R.string.val_follow_system),
                selected = current == AppLocale.FOLLOW_SYSTEM,
            ) { onSelect(AppLocale.FOLLOW_SYSTEM) }
            LanguageOption(
                name = stringResource(R.string.lang_zh),
                selected = current == AppLocale.ZH_CN,
            ) { onSelect(AppLocale.ZH_CN) }
            LanguageOption(
                name = stringResource(R.string.lang_en),
                selected = current == AppLocale.EN,
            ) { onSelect(AppLocale.EN) }
        }
    }
}

@Preview(name = "语言选择弹窗", showBackground = true)
@Composable
private fun LanguageDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        LanguageDialog(current = AppLocale.ZH_CN, onSelect = {}, onDismiss = {})
    }
}
