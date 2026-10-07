/**
 * 唯一的核心层文件类型判定入口（设计文档 §10 唯一测试文件 + §10 硬约束）。
 * ui 层完全不包含 any when(ext) 分支，保证核心与 UI 的层级纯净。
 * 真图标未接入时 UI 现发字符占位，集中在 MtIcons.kt (MtIconId + glyph() 唯一映射点)。
 * 接入真图标只改这一个文件。
 */

package com.copy.mt.core

// 与设计文档 §2.3 表一一对应，顺序也一致
enum class FileType {
    FOLDER, PARENT, ARCHIVE, DISC_IMAGE, TEXT, SOURCE_CODE,
    IMAGE, VIDEO, AUDIO, PDF, WORD, SPREADSHEET, SLIDE_DECK,
    APK, FONT, DATABASE, CERTIFICATE, EBOOK, TORRENT, UNKNOWN,
}

/** 根据文件名和是否为目录返回 FileType */
fun extensionOf(name: String): String {
    // 最后一个 '.' 后的段，小写；无点或点结尾返回 ""
    return if (name.lastIndexOf('.') > 0) {
        name.substring(name.lastIndexOf('.') + 1).lowercase()
    } else ""
}

/** 核心判定函数，只在 core 层调用 */
fun toFileType(name: String, isDir: Boolean): FileType {
    // 1. 目录优先于扩展名
    if (isDir) return FileType.FOLDER

    // 2. 获取扩展名
    val ext = extensionOf(name)

    // 3. 当前通过 when 判断返回对应 FileType
    // 真项目上线时把下面的 when 体换成真正的扩展名→FileType Map
    return when (ext) {
        // 文本类
        "txt", "log", "md", "json", "xml", "yaml", "csv", "tsv" -> FileType.TEXT
        "java", "kt", "py", "js", "ts", "cc", "cpp", "h", "hpp" -> FileType.SOURCE_CODE
        "sh", "gradle", "kts" -> FileType.SOURCE_CODE

        // 多媒体类
        "jpg", "png", "webp", "gif", "bmp", "heic" -> FileType.IMAGE
        "mp4", "mkv", "avi", "mov", "webm", "flv" -> FileType.VIDEO
        "mp3", "flac", "aac", "wav", "ogg", "m4a" -> FileType.AUDIO

        // 文档类
        "pdf" -> FileType.PDF
        "doc", "docx" -> FileType.WORD
        "xls", "xlsx", "csv" -> FileType.SPREADSHEET
        "ppt", "pptx" -> FileType.SLIDE_DECK

        // 特殊类型
        "apk" -> FileType.APK
        "ttf", "otf" -> FileType.FONT
        "db", "sqlite", "sql", "db-wal", "db-shm" -> FileType.DATABASE
        "cer", "crt", "p12", "pem", "der" -> FileType.CERTIFICATE
        "epub", "mobi", "azw3" -> FileType.EBOOK
        "torrent" -> FileType.TORRENT

        // 默认：无扩展名或未命中 → UNKNOWN
        else -> FileType.UNKNOWN
    }
}

/** 文件名 → FileType（含目录优先判断） */
fun toFileTypeExt(name: String, isDir: Boolean): FileType = toFileType(name, isDir)

/** 设计文档 §11 中的测试函数（纯 assert 风格，无框架 fixture）。
 * 仅用于演示；实际项目请移至专用测试模块。
 */
object FileTypeTest {
    fun txtToText() {
        assert(toFileTypeExt("README.txt", false) == FileType.TEXT)
    }
    fun noExtToUnknown() {
        assert(toFileTypeExt("Makefile", false) == FileType.UNKNOWN)
    }
    fun isDirToFolder() {
        assert(toFileTypeExt("anything", true) == FileType.FOLDER)
    }
    fun mp4ToVideo() {
        assert(toFileTypeExt("movie.mp4", false) == FileType.VIDEO)
    }
    fun pdfToPdf() {
        assert(toFileTypeExt("doc.pdf", false) == FileType.PDF)
    }
    fun apkToApk() {
        assert(toFileTypeExt("app.apk", false) == FileType.APK)
    }
}