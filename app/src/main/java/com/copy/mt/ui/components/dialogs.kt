/**
 * 职责：对话框群——新建 / 重命名 / 跳转 / 主题颜色 / 提取设置 / 搜索。
 *       全部为普通 @Composable，内部经 MtDialog 渲染；业务回调由调用方传入，组件内不做业务。
 */
@file:JvmName("MtDialogs")
package com.copy.mt.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Checkbox
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.platform.LocalClipboardManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextRange
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.R
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

/**
 * 对话框按钮行：右对齐，支持逐键禁用。
 * MtDialog 的 buttons 参数不带 enabled，故需要禁用态的对话框在内容槽里自绘此行（样式与基座一致）。
 */
@Composable
private fun DialogActions(buttons: List<Triple<String, Boolean, () -> Unit>>) {
    Row(
        Modifier.fillMaxWidth().padding(top = 20.dp),
        horizontalArrangement = Arrangement.End,
    ) {
        buttons.forEach { (text, enabled, onClick) ->
            MtButton(text, onClick, enabled = enabled)
        }
    }
}

/** 1. 新建：输入名称；为空时「文件 / 文件夹」禁用。 */
@Composable
fun NewEntryDialog(
    onCreateFolder: (String) -> Unit,
    onCreateFile: (String) -> Unit,
    onDismiss: () -> Unit,
    title: String = stringResource(R.string.dlg_new),
) {
    var name by remember { mutableStateOf("") }
    val valid = name.isNotBlank()
    MtDialog(title = title, onDismiss = onDismiss) {
        MtTextField(name, { name = it }, hint = stringResource(R.string.dlg_name))
        DialogActions(
            listOf(
                Triple(stringResource(R.string.common_cancel), true, onDismiss),
                Triple(stringResource(R.string.dlg_file), valid) { onCreateFile(name) },
                Triple(stringResource(R.string.dlg_folder), valid) { onCreateFolder(name) },
            )
        )
    }
}

/** 2. 重命名：预填原名并全选；与原名相同或为空时禁用确定，重名提示错误（组件内判定）。 */
@Composable
fun RenameDialog(
    currentName: String,
    onRename: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalMtColors.current
    // TextFieldValue 携带初始选区：全选原名，输入即覆盖
    var value by remember {
        mutableStateOf(TextFieldValue(currentName, TextRange(0, currentName.length)))
    }
    val duplicated = currentName.isNotEmpty() && value.text == currentName
    val valid = value.text.isNotBlank() && !duplicated
    MtDialog(title = stringResource(R.string.dlg_rename), onDismiss = onDismiss) {
        BasicTextField(
            value = value,
            onValueChange = { value = it },
            singleLine = true,
            cursorBrush = SolidColor(c.accent),
            textStyle = TextStyle(color = c.fg, fontSize = 14.sp),
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Done),
            modifier = Modifier.fillMaxWidth(),
        )
        if (duplicated) {
            Text(
                stringResource(R.string.dlg_name_exists),
                color = c.warn,
                fontSize = 11.sp,
                modifier = Modifier.padding(top = 6.dp),
            )
        }
        DialogActions(
            listOf(
                Triple(stringResource(R.string.common_cancel), true, onDismiss),
                Triple(stringResource(R.string.common_ok), valid) { onRename(value.text) },
            )
        )
    }
}

/** 3. 跳转：输入路径；粘贴 = 读剪贴板填入（不预填、不全选，SPEC B9）。 */
@Composable
fun JumpPathDialog(
    onConfirm: (String) -> Unit,
    onDismiss: () -> Unit,
) {
    var path by remember { mutableStateOf("") }
    val clipboard = LocalClipboardManager.current
    MtDialog(title = stringResource(R.string.dlg_jump), onDismiss = onDismiss) {
        MtTextField(path, { path = it })
        DialogActions(
            listOf(
                Triple(stringResource(R.string.dlg_paste), true) {
                    clipboard.getText()?.let { path = it.text }
                },
                Triple(stringResource(R.string.common_cancel), true, onDismiss),
                Triple(stringResource(R.string.common_ok), true) { onConfirm(path) },
            )
        )
    }
}

/** 主题模式 → 文案资源。 */
private fun modeLabelRes(mode: ThemeMode): Int = when (mode) {
    ThemeMode.FOLLOW_SYSTEM -> R.string.mode_follow
    ThemeMode.LIGHT -> R.string.mode_light
    ThemeMode.DARK -> R.string.mode_dark
    ThemeMode.AMOLED -> R.string.mode_amoled
}

/** 4. 主题颜色：四选一单选；确定才回调 onSelect。 */
@Composable
fun ThemeDialog(
    current: ThemeMode,
    onSelect: (ThemeMode) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalMtColors.current
    var selected by remember { mutableStateOf(current) }
    MtDialog(title = stringResource(R.string.dlg_theme), onDismiss = onDismiss) {
        Column {
            ThemeMode.values().forEach { mode ->
                Row(
                    Modifier.fillMaxWidth().clickable { selected = mode },
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    RadioButton(selected = selected == mode, onClick = null)
                    Text(stringResource(modeLabelRes(mode)), color = c.fg, fontSize = 15.sp)
                }
            }
        }
        DialogActions(
            listOf(
                Triple(stringResource(R.string.common_cancel), true, onDismiss),
                Triple(stringResource(R.string.common_ok), true) { onSelect(selected) },
            )
        )
    }
}

/** 5. 提取设置：存放路径 + 命名表达式 + 签名校验开关；`{ }` 键向表达式尾插入占位符（stub）。 */
@Composable
fun ExtractSettingsDialog(
    path: String,
    namePattern: String,
    signCheck: Boolean,
    onConfirm: (path: String, pattern: String, signCheck: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalMtColors.current
    var path by remember { mutableStateOf(path) }
    var pattern by remember { mutableStateOf(namePattern) }
    var sign by remember { mutableStateOf(signCheck) }
    MtDialog(title = stringResource(R.string.dlg_extract), onDismiss = onDismiss) {
        Text(stringResource(R.string.dlg_extract_path), color = c.fg2, fontSize = 12.sp)
        MtTextField(path, { path = it })
        Text(
            stringResource(R.string.dlg_name_pattern),
            color = c.fg2,
            fontSize = 12.sp,
            modifier = Modifier.padding(top = 12.dp),
        )
        MtTextField(pattern, { pattern = it }, hint = "{A}_{V}.apk")
        Row(
            Modifier.fillMaxWidth().clickable { sign = !sign }.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = sign, onCheckedChange = null)
            Text(stringResource(R.string.dlg_sign_check), color = c.fg, fontSize = 14.sp)
        }
        DialogActions(
            listOf(
                Triple(stringResource(R.string.common_cancel), true, onDismiss),
                Triple("{ }", true) { pattern += "{  }" },
                Triple(stringResource(R.string.common_ok), true) { onConfirm(path, pattern, sign) },
            )
        )
    }
}

/** 6. 搜索：关键词 + 「搜索子目录」勾选（默认勾选）；关键词为空时「搜索」禁用。 */
@Composable
fun SearchDialog(
    includeSubDirs: Boolean = true,
    onSearch: (query: String, includeSubDirs: Boolean) -> Unit,
    onDismiss: () -> Unit,
) {
    val c = LocalMtColors.current
    var query by remember { mutableStateOf("") }
    var includeSub by remember { mutableStateOf(includeSubDirs) }
    MtDialog(title = stringResource(R.string.dlg_search), onDismiss = onDismiss) {
        MtTextField(query, { query = it })
        Row(
            Modifier.fillMaxWidth().clickable { includeSub = !includeSub }.padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Checkbox(checked = includeSub, onCheckedChange = null)
            Text(stringResource(R.string.dlg_include_sub), color = c.fg, fontSize = 14.sp)
        }
        DialogActions(
            listOf(
                Triple(stringResource(R.string.common_cancel), true, onDismiss),
                Triple(stringResource(R.string.dlg_search), query.isNotBlank()) {
                    onSearch(query, includeSub)
                },
            )
        )
    }
}

// ---------------- 预览 ----------------

@Preview(name = "新建", showBackground = true)
@Composable
private fun NewEntryDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        NewEntryDialog(onCreateFolder = {}, onCreateFile = {}, onDismiss = {})
    }
}

@Preview(name = "重命名", showBackground = true)
@Composable
private fun RenameDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        RenameDialog(currentName = "app.apk", onRename = {}, onDismiss = {})
    }
}

@Preview(name = "跳转", showBackground = true)
@Composable
private fun JumpPathDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        JumpPathDialog(onConfirm = {}, onDismiss = {})
    }
}

@Preview(name = "主题颜色", showBackground = true)
@Composable
private fun ThemeDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        ThemeDialog(current = ThemeMode.FOLLOW_SYSTEM, onSelect = {}, onDismiss = {})
    }
}

@Preview(name = "提取设置", showBackground = true)
@Composable
private fun ExtractSettingsDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        ExtractSettingsDialog(path = "/sdcard/App", namePattern = "", signCheck = false, onConfirm = { _, _, _ -> }, onDismiss = {})
    }
}

@Preview(name = "搜索", showBackground = true)
@Composable
private fun SearchDialogPreview() {
    MtTheme(ThemeMode.DARK) {
        SearchDialog(onSearch = { _, _ -> }, onDismiss = {})
    }
}
