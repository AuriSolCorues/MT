package com.copy.mt

import android.content.Intent
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import androidx.activity.ComponentActivity
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import com.copy.mt.data.config.AppLocale
import com.copy.mt.navigation.Route
import com.copy.mt.ui.page.BrowserScreen
import com.copy.mt.ui.page.BrowserViewModel
import com.copy.mt.ui.page.PermissionScreen
import com.copy.mt.ui.page.SettingsScreen
import com.copy.mt.ui.platform.LocalActivity
import com.copy.mt.ui.platform.StorageAccess
import com.copy.mt.ui.components.ThemeDialog
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode

/** 全应用唯一 Activity：权限门槛 + 主界面壳。 */
class MainActivity : ComponentActivity() {
    override fun attachBaseContext(newBase: Context) {
        // API<33 的 Activity 资源不继承 Application 的包裹，须在自身 base 上再包一层；
        // 缓存 tag 已由 MtApp.attachBaseContext 在更早时机载入。API33+ 此处原样透传。
        super.attachBaseContext(AppLocale.wrap(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val context = LocalContext.current
            var granted by remember { mutableStateOf(StorageAccess.granted(context)) }

            val permissionLauncher = rememberLauncherForActivityResult(
                ActivityResultContracts.RequestMultiplePermissions()
            ) { granted = StorageAccess.granted(context) }

            // 从系统设置返回时重新检查
            DisposableEffect(Unit) {
                val observer = LifecycleEventObserver { _, event ->
                    if (event == Lifecycle.Event.ON_RESUME) granted = StorageAccess.granted(context)
                }
                lifecycle.addObserver(observer)
                onDispose { lifecycle.removeObserver(observer) }
            }

            fun requestAccess() {
                if (StorageAccess.needsSettingsScreen) {
                    val intent = Intent(
                        Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION,
                        Uri.parse("package:${context.packageName}"),
                    )
                    runCatching { context.startActivity(intent) }
                } else {
                    permissionLauncher.launch(StorageAccess.runtimePermissions())
                }
            }

            var themeMode by remember { mutableStateOf(ThemeMode.DARK) }
            var themeDialogOpen by remember { mutableStateOf(false) }

            MtTheme(mode = themeMode) {
                CompositionLocalProvider(LocalActivity provides this) {
                Surface(modifier = Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                    if (granted) {
                        val vm: BrowserViewModel = viewModel()
                        val ui by vm.state.collectAsStateWithLifecycle()
                        var page by remember { mutableStateOf<Route>(Route.Browser) }
                        when (page) {
                            Route.Settings -> {
                                SettingsScreen(
                                    onBack = { page = Route.Browser },
                                    onThemeClick = { themeDialogOpen = true },
                                )
                                if (themeDialogOpen) {
                                    ThemeDialog(
                                        current = themeMode,
                                        onSelect = { themeMode = it; themeDialogOpen = false },
                                        onDismiss = { themeDialogOpen = false },
                                    )
                                }
                            }
                            else -> BrowserScreen(
                                left = ui.left,
                                right = ui.right,
                                active = ui.active,
                                onActivate = vm::activate,
                                onUp = vm::up,
                                onSync = vm::sync,
                                onRefresh = vm::refresh,
                                onOpenPath = { side, path -> vm.open(side, path) },
                                onOpenSettings = { page = Route.Settings },
                                onItemClick = { side, item ->
                                    if (item.name == "..") vm.up(side)
                                    else if (item.isDir) vm.open(side, item.path)
                                },
                            )
                        }
                    } else {
                        PermissionScreen(onGrant = ::requestAccess)
                    }
                }
                }
            }
        }
    }
}
