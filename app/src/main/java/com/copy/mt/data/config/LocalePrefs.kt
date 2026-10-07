/**
 * 职责：应用内语言——DataStore 持久化 tag（""=跟随系统 / "zh-CN" / "en"），
 *       并封装两条生效路径：API33+ LocaleManager per-app locale；API<33 Configuration 包裹。
 */
package com.copy.mt.data.config

import android.app.LocaleManager
import android.content.Context
import android.content.res.Configuration
import android.os.Build
import android.os.LocaleList
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.util.Locale
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

/** 进程级单例 DataStore。 */
private val Context.localeStore by preferencesDataStore(name = "locale")

private val KEY_LOCALE_TAG = stringPreferencesKey("locale_tag")

// recreate() 会取消 Activity 级协程作用域，持久化须挂进程级作用域才能保证落盘。
private val localeWriteScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

/** 语言 tag 约定与生效逻辑。 */
object AppLocale {
    const val FOLLOW_SYSTEM = ""
    const val ZH_CN = "zh-CN"
    const val EN = "en"

    // API<33 路径的当前 tag：MtApp.attachBaseContext 启动时读入，运行期切换时更新。
    @Volatile
    var cachedTag: String = FOLLOW_SYSTEM

    /** 未知 tag 一律回退为跟随系统。 */
    fun normalize(tag: String): String = if (tag == ZH_CN || tag == EN) tag else FOLLOW_SYSTEM

    /** 当前生效 tag：API33+ 以系统 per-app locale 为准；API<33 用启动/切换后的缓存。 */
    fun currentTag(context: Context): String {
        if (Build.VERSION.SDK_INT >= 33) {
            val lm = context.getSystemService(LocaleManager::class.java) ?: return FOLLOW_SYSTEM
            val list = lm.applicationLocales
            if (list.isEmpty) return FOLLOW_SYSTEM
            return normalize(list[0].toLanguageTag())
        }
        return normalize(cachedTag)
    }

    /** 即时生效：API33+ 走 LocaleManager（系统随后自动重建界面）；API<33 只更新缓存，由调用方 recreate()。 */
    fun apply(context: Context, tag: String) {
        val t = normalize(tag)
        if (Build.VERSION.SDK_INT >= 33) {
            val lm = context.getSystemService(LocaleManager::class.java) ?: return
            lm.setApplicationLocales(
                if (t == FOLLOW_SYSTEM) LocaleList.getEmptyLocaleList()
                else LocaleList(Locale.forLanguageTag(t))
            )
        } else {
            cachedTag = t
        }
    }

    /** 按缓存 tag 包裹 Context（API<33 生效路径）；API33+ 交由系统处理，原样返回。 */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= 33) return base
        val config = Configuration(base.resources.configuration)
        val tag = normalize(cachedTag)
        if (tag == FOLLOW_SYSTEM) {
            // 复位默认 locale，避免切回跟随系统后格式化仍用旧语言
            Locale.setDefault(config.locales[0])
            return base
        }
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        config.setLocales(LocaleList(locale))
        return base.createConfigurationContext(config)
    }
}

/** 持久化语言 tag：API<33 供下次启动读取；API33+ 系统自持 per-app locale，此处仅作记录。 */
fun saveLocaleTagAsync(context: Context, tag: String) {
    val app = context.applicationContext
    localeWriteScope.launch { app.localeStore.edit { it[KEY_LOCALE_TAG] = AppLocale.normalize(tag) } }
}

/**
 * 同步读取语言 tag——仅供 MtApp.attachBaseContext 使用：该时机早于任何协程作用域，
 * DataStore 无同步 API，冷启动仅此一次 runBlocking 小读取，阻塞可接受。
 */
fun readLocaleTagBlocking(context: Context): String =
    runBlocking { context.localeStore.data.first()[KEY_LOCALE_TAG] ?: AppLocale.FOLLOW_SYSTEM }
