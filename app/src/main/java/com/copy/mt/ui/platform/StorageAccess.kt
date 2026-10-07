/**
 * 职责：全存储访问权限判定（Android 11+ 用 MANAGE_EXTERNAL_STORAGE；以下用读写运行时权限）。
 */
package com.copy.mt.ui.platform

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import android.os.Environment
import androidx.core.content.ContextCompat

object StorageAccess {

    fun granted(context: Context): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            Environment.isExternalStorageManager()
        } else {
            ContextCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(context, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
        }

    /** API<30 需要申请的运行时权限；API>=30 返回空（走系统设置页）。 */
    fun runtimePermissions(): Array<String> =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) emptyArray()
        else arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE)

    val needsSettingsScreen: Boolean
        get() = Build.VERSION.SDK_INT >= Build.VERSION_CODES.R
}
