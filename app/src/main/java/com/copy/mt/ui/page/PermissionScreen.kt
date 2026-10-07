/**
 * 职责：全存储权限门槛页——无权限时不进主界面，只显示说明 + 去授权。
 * 无状态 page：onGrant 由调用方接系统设置/运行时申请。
 */
package com.copy.mt.ui.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.copy.mt.R
import androidx.compose.ui.res.stringResource
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

@Composable
fun PermissionScreen(onGrant: () -> Unit = {}) {
    Column(
        Modifier.fillMaxSize().padding(32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(stringResource(R.string.perm_title), style = MaterialTheme.typography.titleLarge)
        Text(
            stringResource(R.string.perm_desc),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(top = 12.dp, bottom = 24.dp),
        )
        Button(onClick = onGrant) { Text(stringResource(R.string.perm_grant)) }
    }
}

@Preview(name = "权限门槛", showBackground = true, widthDp = 360, heightDp = 720)
@Composable
private fun PermissionScreenPreview() {
    MtTheme(ThemeMode.DARK) { PermissionScreen() }
}
