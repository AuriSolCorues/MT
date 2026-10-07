/**
 * 职责：列表基础控件——文件/文件夹行 + 图标位。
 */
package com.copy.mt.ui.components

import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

/** 行类型——对应设计文档 §2.3 的 20 类图标映射。真图标未接入时现发字符占位。 */
enum class FileKind {
    FOLDER,           // 目录
    UP,               // 上级 ..
    FILE,             // 通用文件（默认兜底）
    ARCHIVE,          // 压缩包
    DISC_IMAGE,       // 光盘映像 (iso/img)
    TEXT,             // 文本/日志/MD/JSon/Yaml/Csv等
    SOURCE_CODE,      // Java/Kotlin/Python/Js/Sh/gradle/kts等
    IMAGE,            // Jpg/Png/Webp/Gif/Bmp/Hec
    VIDEO,            // Mp4/Mkv/Avi/Mov/Webm/Flv
    AUDIO,            // Mp3/Flac/Aac/Wav/Ogg/M4a
    PDF,              // Pdf
    WORD,             // Doc/Docx
    SPREADSHEET,      // Xls/Xlsx/Csv
    SLIDE_DECK,       // Ppt/Pptx
    APK,              // Apk
    FONT,             // Ttf/Otf
    DATABASE,         // Db/Sqlite/Sql Db-wal/Db-shm
    CERTIFICATE,      // Cer/Crt/P12/Pem/Der
    EBOOK,            // Epub/Mobi/Azw3
    TORRENT,          // Torrent
    UNKNOWN,          // 其他（无扩展名/未命中）
}

private fun glyphOf(kind: FileKind): String = MtIcons.fileTypeGlyph(kind)

/** 图标位：28dp 纯色圆角框 + 图形（可选右下角标）。 */
@Composable
fun MtIconBox(
    kind: FileKind,
    selected: Boolean = false,
    badge: String? = null,
    modifier: Modifier = Modifier,
) {
    val c = LocalMtColors.current
    Box(modifier.size(28.dp).clip(RoundedCornerShape(5.dp)).background(c.iframe)) {
        Text(
            glyphOf(kind),
            color = c.iglyph,
            fontSize = 13.sp,
            modifier = Modifier.align(Alignment.Center),
        )
        if (badge != null) {
            Text(
                badge,
                color = c.iglyph,
                fontSize = 8.sp,
                modifier = Modifier.align(Alignment.BottomEnd).padding(1.dp),
            )
        }
    }
}

/** 文件/文件夹行：图标 + 名称 + 副行（时间·大小）。 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun MtFileRow(
    name: String,
    meta: String = "",
    kind: FileKind = FileKind.FILE,
    selected: Boolean = false,
    modifier: Modifier = Modifier,
    onClick: () -> Unit = {},
    onLongClick: () -> Unit = {},
    onIconTap: (() -> Unit)? = null,
) {
    val c = LocalMtColors.current
    Row(
        modifier
            .fillMaxWidth()
            .height(40.dp)
            .background(if (selected) c.sel else c.surface)
            .combinedClickable(onClick = onClick, onLongClick = onLongClick)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(Modifier.then(if (onIconTap != null) Modifier.clickable(onClick = onIconTap) else Modifier)) {
            MtIconBox(kind, selected)
        }
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(
                name,
                color = if (selected) c.selFg else c.fg,
                fontSize = 12.sp,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            if (meta.isNotEmpty()) {
                Text(
                    meta,
                    color = if (selected) c.selFg else c.fg2,
                    fontSize = 10.sp,
                    maxLines = 1,
                )
            }
        }
    }
}

@Preview(name = "文件行", showBackground = true, widthDp = 300)
@Composable
private fun MtFileRowPreview() {
    MtTheme(ThemeMode.DARK) {
        Column {
            MtFileRow("..", "", FileKind.UP)
            MtFileRow("Documents", "26-10-01 14:28", FileKind.FOLDER)
            MtFileRow("chk.png", "26-10-06 14:14   181.53K", FileKind.FILE)
            MtFileRow("selected", "26-10-06 14:14", FileKind.FILE, selected = true)
        }
    }
}
