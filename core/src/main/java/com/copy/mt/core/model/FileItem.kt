package com.copy.mt.core.model

/** 列表项模型（骨架；字段见设计文档 §13.3）。 */
data class FileItem(
    val name: String,
    val path: String,
    val isDir: Boolean,
    val size: Long,
    val modifiedAt: Long,
)
