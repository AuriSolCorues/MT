/**
 * 职责：基础控件——按钮 / 分隔线 / 设置行 / 开关 / 输入框。
 */
package com.copy.mt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

/** 文字按钮。 */
@Composable
fun MtButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    color: androidx.compose.ui.graphics.Color? = null,
    enabled: Boolean = true,
) {
    val c = LocalMtColors.current
    Text(
        text,
        color = if (enabled) (color ?: c.accent) else c.disabled,
        fontSize = 15.sp,
        modifier = modifier
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
    )
}

@Composable
fun MtDivider(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxWidth().height(0.5.dp).background(LocalMtColors.current.divider))
}

/** 设置行：标题（+可选副标题）与右侧值 / 内容槽。 */
@Composable
fun MtRow(
    title: String,
    subtitle: String? = null,
    value: String? = null,
    enabled: Boolean = true,
    onClick: (() -> Unit)? = null,
    trailing: @Composable (() -> Unit)? = null,
) {
    val c = LocalMtColors.current
    Row(
        Modifier
            .fillMaxWidth()
            .let { if (onClick != null) it.clickable(enabled = enabled, onClick = onClick) else it }
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(Modifier.weight(1f)) {
            Text(
                title,
                color = if (enabled) c.fg else c.disabled,
                fontSize = 15.sp,
            )
            if (subtitle != null) {
                Text(subtitle, color = c.fg2, fontSize = 11.sp)
            }
        }
        when {
            trailing != null -> trailing()
            value != null -> Text(value, color = c.fg2, fontSize = 13.sp, textAlign = TextAlign.End)
        }
    }
}

/** 基础开关。 */
@Composable
fun MtSwitch(checked: Boolean, onCheckedChange: (Boolean) -> Unit, enabled: Boolean = true) {
    Switch(checked = checked, onCheckedChange = onCheckedChange, enabled = enabled)
}

/** 设置开关行。 */
@Composable
fun MtSwitchRow(
    title: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit,
    subtitle: String? = null,
    enabled: Boolean = true,
) {
    MtRow(
        title = title,
        subtitle = subtitle,
        enabled = enabled,
        onClick = if (enabled) ({ onCheckedChange(!checked) }) else null,
        trailing = { MtSwitch(checked, onCheckedChange, enabled) },
    )
}

/** 基础输入框。 */
@Composable
fun MtTextField(
    value: String,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    hint: String = "",
) {
    val c = LocalMtColors.current
    Box(modifier.fillMaxWidth()) {
        if (value.isEmpty() && hint.isNotEmpty()) {
            Text(hint, color = c.fg3, fontSize = 14.sp)
        }
        BasicTextField(
            value = value,
            onValueChange = onValueChange,
            singleLine = true,
            cursorBrush = SolidColor(c.accent),
            textStyle = androidx.compose.ui.text.TextStyle(color = c.fg, fontSize = 14.sp),
            keyboardOptions = androidx.compose.foundation.text.KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
    }
}

@Preview(name = "基础件", showBackground = true, widthDp = 320)
@Composable
private fun BasicsPreview() {
    MtTheme(ThemeMode.DARK) {
        Column {
            MtRow("请求 Root 权限", enabled = false, value = "关")
            MtRow("自动启动服务", enabled = false, value = "未启用")
            MtSwitchRow("生成备份文件", true, {})
            MtDivider()
            MtTextField("", {}, hint = "输入名称")
            Row { MtButton("取消", {}); MtButton("确定", {}) }
        }
    }
}
