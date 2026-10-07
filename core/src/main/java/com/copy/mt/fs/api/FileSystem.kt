package com.copy.mt.fs.api

/** 文件系统抽象。实现见 fs/impl、fs/access。 */
interface FileSystem {
    suspend fun list(path: String): List<FileEntry>
}
