package com.copy.mt.data.model

import kotlinx.serialization.Serializable

/** 用户保存的自定义主题（id + 显示名 + 原始 JSONC 文本）。（复制自 account 项目） */
@Serializable
data class SavedTheme(val id: String, val name: String, val json: String)
