/**
 * 职责：集中图标层——占位字符的唯一映射点。
 * UI 恒发枚举（MtIconId / FileKind），字符只是占位；后续接入真图标
 * （icon_filetype_* 矢量按包入库 + 运行期查表换包）时只改本文件。
 * 见设计文档 §2.3（文件类型图标映射 20 类）/ §8.11（图标包切换）。
 */
package com.copy.mt.ui.components

/** 图标语义 id：与具体字形/资源解耦，占位字符只映射到这里。 */
enum class MtIconId {
    // 文件类型（列表项；对应 §2.3 的 icon_filetype_folder / _unknown / _folder_up）
    FOLDER, FILE, UP,

    // 顶栏
    MENU, MORE,

    // 底栏
    NAV_BACK, NAV_FORWARD, NAV_NEW, NAV_SYNC, NAV_UP,

    // 抽屉
    DRAWER_DAY, DRAWER_MORE, DRAWER_ROOT, DRAWER_INTERNAL, DRAWER_BOOKMARK,
    DRAWER_TRASH, DRAWER_PLUGINS, DRAWER_APPS, DRAWER_EDITOR, DRAWER_MORE_TOOLS,

    // 长按面板
    PANEL_COPY, PANEL_MOVE, PANEL_DELETE, PANEL_RENAME, PANEL_TOOLS,
    PANEL_ZIP, PANEL_PROPS, PANEL_SHARE, PANEL_OPEN_WITH, PANEL_BOOKMARK,
}

object MtIcons {
    /** 唯一映射点：占位字符只允许出现在这个 when 里。 */
    private fun glyph(id: MtIconId): String = when (id) {
        MtIconId.FOLDER -> "▣" // TODO(icon): 待接入 icon_filetype_folder 真图标（设计文档 §2.3/§8.11）
        MtIconId.FILE -> "▤" // TODO(icon): 待接入 icon_filetype_unknown 真图标（设计文档 §2.3/§8.11）
        MtIconId.UP -> "↥" // TODO(icon): 待接入 icon_filetype_folder_up 真图标（设计文档 §2.3/§8.11）
        MtIconId.MENU -> "☰" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.MORE -> "⋮" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.NAV_BACK -> "←" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.NAV_FORWARD -> "→" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.NAV_NEW -> "＋" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.NAV_SYNC -> "⇄" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.NAV_UP -> "↑" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_DAY -> "☀" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_MORE -> "⋮" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_ROOT -> "⌂" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_INTERNAL -> "▤" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_BOOKMARK -> "☆" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_TRASH -> "🗑" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_PLUGINS -> "⧉" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_APPS -> "▣" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_EDITOR -> "✎" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.DRAWER_MORE_TOOLS -> "⋯" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_COPY -> "⧉" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_MOVE -> "⇄" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_DELETE -> "🗑" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_RENAME -> "✎" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_TOOLS -> "⚙" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_ZIP -> "🗜" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_PROPS -> "ⓘ" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_SHARE -> "↗" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_OPEN_WITH -> "▷" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
        MtIconId.PANEL_BOOKMARK -> "☆" // TODO(icon): 待接入 icon_filetype_* 真图标（设计文档 §2.3/§8.11）
    }

    /** 列表项文件类型图标；未来 §2.3 扩展名判定启用后在此接 FileType / 图标包查表。 */
    fun fileTypeGlyph(kind: FileKind): String = when (kind) {
        FileKind.FOLDER -> glyph(MtIconId.FOLDER)
        FileKind.UP -> glyph(MtIconId.FOLDER) // 暂用文件夹图标，真图标后续接入
        FileKind.FILE -> glyph(MtIconId.FILE) // 通用文件（默认兜底）
        FileKind.ARCHIVE -> glyph(MtIconId.FILE) // 暂用文件图标，真图标后续接入
        FileKind.DISC_IMAGE -> glyph(MtIconId.FILE)
        FileKind.TEXT -> glyph(MtIconId.FILE)
        FileKind.SOURCE_CODE -> glyph(MtIconId.FILE)
        FileKind.IMAGE -> glyph(MtIconId.FILE) // 暂用文件图标，真图标后续接入
        FileKind.VIDEO -> glyph(MtIconId.FILE)
        FileKind.AUDIO -> glyph(MtIconId.FILE)
        FileKind.PDF -> glyph(MtIconId.FILE)
        FileKind.WORD -> glyph(MtIconId.FILE)
        FileKind.SPREADSHEET -> glyph(MtIconId.FILE)
        FileKind.SLIDE_DECK -> glyph(MtIconId.FILE)
        FileKind.APK -> glyph(MtIconId.FILE) // APK 有额外的安装角标
        FileKind.FONT -> glyph(MtIconId.FILE)
        FileKind.DATABASE -> glyph(MtIconId.FILE)
        FileKind.CERTIFICATE -> glyph(MtIconId.FILE)
        FileKind.EBOOK -> glyph(MtIconId.FILE)
        FileKind.TORRENT -> glyph(MtIconId.FILE)
        FileKind.UNKNOWN -> glyph(MtIconId.FILE)
    }

    /** 界面部件（chrome）图标入口。 */
    fun chromeGlyph(id: MtIconId): String = glyph(id)
}