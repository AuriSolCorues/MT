package com.copy.mt.core.model

/** 粘贴/复制请求（骨架）。 */
data class PasteRequest(val mode: String, val paths: List<String>, val target: String)
