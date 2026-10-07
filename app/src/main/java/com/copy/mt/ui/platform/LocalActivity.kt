/**
 * 职责：CompositionLocal 形式的当前 Activity，供页面深层重组内调用 recreate() 等实例方法。
 */
package com.copy.mt.ui.platform

import android.app.Activity
import androidx.compose.runtime.staticCompositionLocalOf

/** 无宿主时为 null（如 Preview 环境）。 */
val LocalActivity = staticCompositionLocalOf<Activity?> { null }
