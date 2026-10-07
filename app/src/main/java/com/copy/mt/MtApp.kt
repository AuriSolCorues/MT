/**
 * 职责：应用入口——API<33 在启动时用持久化的语言 tag 包裹 base context；
 *       API33+ 由系统 per-app locale（LocaleManager）自动生效，无需处理。
 */
package com.copy.mt

import android.app.Application
import android.content.Context
import android.os.Build
import com.copy.mt.data.config.AppLocale
import com.copy.mt.data.config.readLocaleTagBlocking

class MtApp : Application() {
    override fun attachBaseContext(base: Context) {
        if (Build.VERSION.SDK_INT < 33) {
            // attachBaseContext 早于任何协程作用域，DataStore 无同步 API，
            // 冷启动仅此一次 runBlocking 读取，属于可接受的兜底。
            AppLocale.cachedTag = readLocaleTagBlocking(base)
        }
        super.attachBaseContext(AppLocale.wrap(base))
    }
}
