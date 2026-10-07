package com.copy.mt.fs.impl

import com.copy.mt.fs.api.FileEntry
import com.copy.mt.fs.api.FileSystem
import java.io.File
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/** 本地文件系统实现：直接读磁盘。 */
class LocalFs : FileSystem {
    override suspend fun list(path: String): List<FileEntry> = withContext(Dispatchers.IO) {
        val dir = File(path)
        val files = dir.listFiles()
            ?: return@withContext emptyList()
        files.asSequence()
            .map { f ->
                FileEntry(
                    name = f.name,
                    path = f.absolutePath,
                    isDir = f.isDirectory,
                    size = if (f.isDirectory) 0L else f.length(),
                    modifiedAt = f.lastModified(),
                )
            }
            .sortedWith(compareByDescending<FileEntry> { it.isDir }.thenBy { it.name.lowercase() })
            .toList()
    }
}
