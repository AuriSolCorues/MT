/**
 * 职责：MTM 主题引擎——浅 / 深 / AMOLED 三套内置色板（或 JSONC 自定义）解析成扁平 MtColors，
 *       再折算成 Material3 ColorScheme，经 CompositionLocal 下发全树。
 * 架构位置：MainActivity 装配 MtTheme；页面从 MaterialTheme.colorScheme 或 LocalMtColors 取色。
 */
package com.copy.mt.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.staticCompositionLocalOf
import androidx.compose.ui.graphics.Color
import com.copy.mt.data.config.stripJsonComments
import com.copy.mt.data.model.SavedTheme
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json

/** 显示模式。 */
enum class ThemeMode { FOLLOW_SYSTEM, LIGHT, DARK, AMOLED }

/** 当前页面使用的完整色板。字段保持扁平，方便在 JSON 中直接修改。 */
data class MtColors(
    val bar: Color,
    val surface: Color,
    val surface2: Color,
    val iframe: Color,
    val iglyph: Color,
    val sel: Color,
    val selFg: Color,
    val accent: Color,
    val accentSoft: Color,
    val fg: Color,
    val fg2: Color,
    val fg3: Color,
    val disabled: Color,
    val menuBg: Color,
    val menuFg: Color,
    val divider: Color,
    val scrim: Color,
    val warn: Color,
    val chip: Color,
)

val MtLightPalette = MtColors(
    bar = MtLightBar, surface = MtLightSurface, surface2 = MtLightSurface2, iframe = MtLightIframe,
    iglyph = MtLightIglyph, sel = MtLightSel, selFg = Color.White, accent = MtLightAccent,
    accentSoft = MtLightAccentSoft, fg = MtLightFg, fg2 = MtLightFg2, fg3 = MtLightFg3,
    disabled = MtLightDisabled, menuBg = MtLightMenuBg, menuFg = MtLightMenuFg,
    divider = MtLightDivider, scrim = MtLightScrim, warn = MtWarn, chip = MtChip,
)

val MtDarkPalette = MtColors(
    bar = MtDarkBar, surface = MtDarkSurface, surface2 = MtDarkSurface2, iframe = MtDarkIframe,
    iglyph = MtDarkIglyph, sel = MtDarkSel, selFg = Color.White, accent = MtDarkAccent,
    accentSoft = MtDarkAccentSoft, fg = MtDarkFg, fg2 = MtDarkFg2, fg3 = MtDarkFg3,
    disabled = MtDarkDisabled, menuBg = MtDarkMenuBg, menuFg = MtDarkMenuFg,
    divider = MtDarkDivider, scrim = MtDarkScrim, warn = MtWarn, chip = MtChip,
)

val MtAmoledPalette = MtColors(
    bar = MtAmoledBar, surface = MtAmoledSurface, surface2 = MtAmoledSurface2, iframe = MtAmoledIframe,
    iglyph = MtAmoledIglyph, sel = MtAmoledSel, selFg = Color.White, accent = MtAmoledAccent,
    accentSoft = MtAmoledAccentSoft, fg = MtAmoledFg, fg2 = MtAmoledFg2, fg3 = MtAmoledFg3,
    disabled = MtAmoledDisabled, menuBg = MtAmoledMenuBg, menuFg = MtAmoledMenuFg,
    divider = MtAmoledDivider, scrim = MtAmoledScrim, warn = MtWarn, chip = MtChip,
)

val LocalMtColors = staticCompositionLocalOf { MtDarkPalette }

// ---------------- JSONC 自定义主题 ----------------

@Serializable
data class ThemeJsonColors(
    val bar: String = "#151515",
    val surface: String = "#303030",
    val surface2: String = "#383838",
    val iframe: String = "#151515",
    val iglyph: String = "#CDCDCD",
    val sel: String = "#214252",
    val selFg: String = "#FFFFFF",
    val accent: String = "#42A5F5",
    val accentSoft: String = "#B3DCFC",
    val fg: String = "#C5C5C5",
    val fg2: String = "#A9A9A9",
    val fg3: String = "#8A8A8A",
    val disabled: String = "#4E4E4E",
    val menuBg: String = "#424242",
    val menuFg: String = "#E0E0E0",
    val divider: String = "#1C1C1C",
    val scrim: String = "#99000000",
    val warn: String = "#D66620",
    val chip: String = "#E6CB73",
)

@Serializable
data class ThemeJsonDefinition(
    val version: Int = 1,
    val name: String = "自定义主题",
    val defaultMode: String = "dark",
    val colors: ThemeJsonColors = ThemeJsonColors()
)

private val themeJson = Json { ignoreUnknownKeys = true; encodeDefaults = true; prettyPrint = true }

private fun parseHex(value: String): Color? = runCatching {
    val hex = value.trim().removePrefix("#")
    require(hex.length == 6 || hex.length == 8)
    require(hex.all { it.digitToIntOrNull(16) != null })
    Color((if (hex.length == 6) 0xFF000000L else 0L) or hex.toLong(16))
}.getOrNull()

private fun ThemeJsonColors.toPalette(): MtColors? {
    val values = listOf(
        bar, surface, surface2, iframe, iglyph, sel, selFg, accent, accentSoft, fg, fg2, fg3,
        disabled, menuBg, menuFg, divider, scrim, warn, chip
    )
    val c = values.map { parseHex(it) }
    if (c.any { it == null }) return null
    val n = c.requireNoNulls()
    return MtColors(n[0], n[1], n[2], n[3], n[4], n[5], n[6], n[7], n[8], n[9], n[10], n[11], n[12], n[13], n[14], n[15], n[16], n[17], n[18])
}

fun parseThemeJson(source: String): ThemeJsonDefinition? = runCatching {
    val definition = themeJson.decodeFromString<ThemeJsonDefinition>(stripJsonComments(source))
    require(definition.version == 1)
    require(definition.defaultMode in setOf("dark", "light", "system"))
    require(definition.colors.toPalette() != null)
    definition
}.getOrNull()

fun themePaletteFromJson(source: String): MtColors? = parseThemeJson(source)?.colors?.toPalette()

/** 两个可复制示例（浅 / 深）。 */
fun defaultThemePresets(): List<SavedTheme> = listOf(
    SavedTheme("mt-dark", "MTM 深色", """{
      "version": 1,
      "name": "MTM 深色",
      "defaultMode": "dark",
      "colors": {
        "bar": "#151515", "surface": "#303030", "surface2": "#383838",
        "iframe": "#151515", "iglyph": "#CDCDCD", "sel": "#214252", "selFg": "#FFFFFF",
        "accent": "#42A5F5", "accentSoft": "#B3DCFC",
        "fg": "#C5C5C5", "fg2": "#A9A9A9", "fg3": "#8A8A8A", "disabled": "#4E4E4E",
        "menuBg": "#424242", "menuFg": "#E0E0E0", "divider": "#1C1C1C",
        "scrim": "#99000000", "warn": "#D66620", "chip": "#E6CB73"
      }
    }"""),
    SavedTheme("mt-light", "MTM 浅色", """{
      "version": 1,
      "name": "MTM 浅色",
      "defaultMode": "light",
      "colors": {
        "bar": "#303030", "surface": "#FAFAFA", "surface2": "#F0F0F0",
        "iframe": "#2B2B2B", "iglyph": "#FFFFFF", "sel": "#65C0DF", "selFg": "#FFFFFF",
        "accent": "#42A5F5", "accentSoft": "#B3DCFC",
        "fg": "#111111", "fg2": "#646464", "fg3": "#939393", "disabled": "#C8C8C8",
        "menuBg": "#FFFFFF", "menuFg": "#111111", "divider": "#939393",
        "scrim": "#73000000", "warn": "#D66620", "chip": "#E0C870"
      }
    }"""),
)

// ---------------- 装配 ----------------

private fun MtColors.toScheme(dark: Boolean) = if (dark) {
    darkColorScheme(
        primary = accent, onPrimary = Color.White, primaryContainer = sel, onPrimaryContainer = fg,
        secondary = accent, tertiary = warn,
        background = surface, surface = surface, surfaceVariant = surface2,
        onBackground = fg, onSurface = fg, onSurfaceVariant = fg2,
        error = warn, onError = Color.White, outline = divider, outlineVariant = divider,
        surfaceContainerLowest = surface, surfaceContainerLow = menuBg, surfaceContainer = menuBg,
        surfaceContainerHigh = menuBg, surfaceContainerHighest = menuBg,
    )
} else {
    lightColorScheme(
        primary = accent, onPrimary = Color.White, primaryContainer = sel, onPrimaryContainer = fg,
        secondary = accent, tertiary = warn,
        background = surface, surface = surface, surfaceVariant = surface2,
        onBackground = fg, onSurface = fg, onSurfaceVariant = fg2,
        error = warn, onError = Color.White, outline = divider, outlineVariant = divider,
        surfaceContainerLowest = surface, surfaceContainerLow = menuBg, surfaceContainer = menuBg,
        surfaceContainerHigh = menuBg, surfaceContainerHighest = menuBg,
    )
}

/** 依据模式解析出内置色板；JSONC 有效则整体覆盖（非法则回退内置）。 */
fun resolveMtColors(mode: ThemeMode, darkTheme: Boolean, customThemeJson: String): MtColors {
    val custom = themePaletteFromJson(customThemeJson)
    if (custom != null) return custom
    return when (mode) {
        ThemeMode.LIGHT -> MtLightPalette
        ThemeMode.AMOLED -> MtAmoledPalette
        ThemeMode.DARK -> MtDarkPalette
        ThemeMode.FOLLOW_SYSTEM -> if (darkTheme) MtDarkPalette else MtLightPalette
    }
}

@Composable
fun MtTheme(
    mode: ThemeMode = ThemeMode.FOLLOW_SYSTEM,
    customThemeJson: String = "",
    content: @Composable () -> Unit,
) {
    val systemDark = isSystemInDarkTheme()
    val dark = when (mode) {
        ThemeMode.LIGHT -> false
        ThemeMode.DARK, ThemeMode.AMOLED -> true
        ThemeMode.FOLLOW_SYSTEM -> systemDark
    }
    val palette = resolveMtColors(mode, systemDark, customThemeJson)
    MaterialTheme(
        colorScheme = palette.toScheme(dark),
        typography = Typography,
    ) {
        CompositionLocalProvider(LocalMtColors provides palette) { content() }
    }
}