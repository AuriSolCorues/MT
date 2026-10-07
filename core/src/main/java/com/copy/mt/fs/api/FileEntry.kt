package com.copy.mt.fs.api

/** 文件系统条目（由 fs 层产出）。 */
data class FileEntry(
    val name: String,
    val path: String,
    val isDir: Boolean,
    val size: Long,
    val modifiedAt: Long,
)
