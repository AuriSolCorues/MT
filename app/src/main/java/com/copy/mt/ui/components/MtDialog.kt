/**
 * 职责：基础弹窗——标题 + 内容槽 + 按钮行（1–3 键）。
 */
package com.copy.mt.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

data class MtDialogButton(val text: String, val onClick: () -> Unit)

@Composable
fun MtDialog(
    title: String? = null,
    buttons: List<MtDialogButton> = emptyList(),
    onDismiss: () -> Unit = {},
    content: @Composable () -> Unit = {},
) {
    val c = LocalMtColors.current
    Dialog(onDismissRequest = onDismiss, properties = DialogProperties(usePlatformDefaultWidth = false)) {
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = c.menuBg,
            modifier = Modifier.width(342.dp),
        ) {
            Column(Modifier.padding(20.dp)) {
                if (title != null) {
                    Text(title, color = c.menuFg, fontSize = 18.sp, modifier = Modifier.padding(bottom = 12.dp))
                }
                content()
                if (buttons.isNotEmpty()) {
                    Row(
                        Modifier.fillMaxWidth().padding(top = 20.dp),
                        horizontalArrangement = Arrangement.End,
                    ) {
                        buttons.forEach { b ->
                            MtButton(b.text, b.onClick)
                        }
                    }
                }
            }
        }
    }
}

@Preview(name = "基础弹窗", showBackground = true)
@Composable
private fun MtDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        MtDialog(
            title = "新建",
            buttons = listOf(MtDialogButton("取消", {}), MtDialogButton("文件", {}), MtDialogButton("文件夹", {})),
            onDismiss = {},
        ) {
            MtTextField("", {}, hint = "名称")
        }
    }
}
