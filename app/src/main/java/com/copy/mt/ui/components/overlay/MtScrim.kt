/**
 * 职责：覆盖层基础件——遮罩。
 */
package com.copy.mt.ui.components.overlay

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

@Composable
fun MtScrim(onClick: () -> Unit) {
    Box(Modifier.fillMaxSize().background(LocalMtColors.current.scrim).clickable(onClick = onClick))
}

@Preview(name = "遮罩", showBackground = true, widthDp = 200, heightDp = 200)
@Composable
private fun MtScrimPreview() {
    MtTheme(ThemeMode.DARK) { MtScrim({}) }
}
