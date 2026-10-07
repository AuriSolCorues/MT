/**
 * 职责：MTM 三套内置色板常量（浅 / 深 / AMOLED）。色值见设计文档 §2。
 * 由 ui/theme/Theme.kt 的 resolveTheme 引用。
 */
package com.copy.mt.ui.theme

import androidx.compose.ui.graphics.Color

// 浅色
val MtLightBar = Color(0xFF303030)
val MtLightSurface = Color(0xFFFAFAFA)
val MtLightSurface2 = Color(0xFFF0F0F0)
val MtLightIframe = Color(0xFF2B2B2B)
val MtLightIglyph = Color(0xFFFFFFFF)
val MtLightSel = Color(0xFF65C0DF)
val MtLightAccent = Color(0xFF42A5F5)
val MtLightAccentSoft = Color(0xFFB3DCFC)
val MtLightFg = Color(0xFF111111)
val MtLightFg2 = Color(0xFF646464)
val MtLightFg3 = Color(0xFF939393)
val MtLightDisabled = Color(0xFFC8C8C8)
val MtLightMenuBg = Color(0xFFFFFFFF)
val MtLightMenuFg = Color(0xFF111111)
val MtLightDivider = Color(0xFF939393)
val MtLightScrim = Color(0x73000000)

// 深色
val MtDarkBar = Color(0xFF151515)
val MtDarkSurface = Color(0xFF303030)
val MtDarkSurface2 = Color(0xFF383838)
val MtDarkIframe = Color(0xFF151515)
val MtDarkIglyph = Color(0xFFCDCDCD)
val MtDarkSel = Color(0xFF214252)
val MtDarkAccent = Color(0xFF42A5F5)
val MtDarkAccentSoft = Color(0xFFB3DCFC)
val MtDarkFg = Color(0xFFC5C5C5)
val MtDarkFg2 = Color(0xFFA9A9A9)
val MtDarkFg3 = Color(0xFF8A8A8A)
val MtDarkDisabled = Color(0xFF4E4E4E)
val MtDarkMenuBg = Color(0xFF424242)
val MtDarkMenuFg = Color(0xFFE0E0E0)
val MtDarkDivider = Color(0xFF1C1C1C)
val MtDarkScrim = Color(0x99000000)

// AMOLED
val MtAmoledBar = Color(0xFF000000)
val MtAmoledSurface = Color(0xFF000000)
val MtAmoledSurface2 = Color(0xFF0E0E0E)
val MtAmoledIframe = Color(0xFF2E3A40)
val MtAmoledIglyph = Color(0xFFCDCDCD)
val MtAmoledSel = Color(0xFF173F50)
val MtAmoledAccent = Color(0xFF64B5F6)
val MtAmoledAccentSoft = Color(0xFFB3DCFC)
val MtAmoledFg = Color(0xFFEEEEEE)
val MtAmoledFg2 = Color(0xFF9A9A9A)
val MtAmoledFg3 = Color(0xFF6E6E6E)
val MtAmoledDisabled = Color(0xFF3A3A3A)
val MtAmoledMenuBg = Color(0xFF000000)
val MtAmoledMenuFg = Color(0xFFEEEEEE)
val MtAmoledDivider = Color(0xFF1C1C1C)
val MtAmoledScrim = Color(0xCC000000)

// 共用
val MtWarn = Color(0xFFD66620)
val MtChip = Color(0xFFE6CB73)
