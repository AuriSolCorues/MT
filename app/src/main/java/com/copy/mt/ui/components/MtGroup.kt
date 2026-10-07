/**
 * 职责：基础分组——可折叠组头 + 行容器（行间用 MtDivider）。
 */
package com.copy.mt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

@Composable
fun MtGroup(
    title: String? = null,
    expandable: Boolean = false,
    expanded: Boolean = true,
    onToggle: () -> Unit = {},
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalMtColors.current
    Column(modifier.fillMaxWidth()) {
        if (title != null) {
            Row(
                Modifier
                    .fillMaxWidth()
                    .let { if (expandable) it.clickable(onClick = onToggle) else it }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                if (expandable) {
                    Text(if (expanded) "▾ " else "▸ ", color = c.fg2, fontSize = 13.sp)
                }
                Text(title, color = c.accent, fontSize = 14.sp)
            }
        }
        if (!expandable || expanded) {
            Column(Modifier.fillMaxWidth(), content = content)
        }
    }
}

@Preview(name = "分组", showBackground = true, widthDp = 320)
@Composable
private fun MtGroupPreview() {
    MtTheme(ThemeMode.DARK) {
        Column {
            MtGroup(title = "启动") {
                MtRow("启动路径 - 左窗口", value = "首页")
                MtDivider()
                MtRow("启动路径 - 右窗口", value = "首页")
            }
            MtGroup(title = "工具", expandable = true, expanded = true) {
                MtRow("已安装应用")
            }
        }
    }
}
