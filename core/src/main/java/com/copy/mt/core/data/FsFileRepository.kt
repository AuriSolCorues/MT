package com.copy.mt.core.data

import com.copy.mt.core.model.FileItem
import com.copy.mt.core.repository.FileRepository
import com.copy.mt.fs.api.FileSystem

/** FileRepository 的 fs 桥接实现：FileEntry → FileItem。 */
class FsFileRepository(private val fs: FileSystem) : FileRepository {
    override suspend fun list(path: String): List<FileItem> =
        fs.list(path).map {
            FileItem(
                name = it.name,
                path = it.path,
                isDir = it.isDir,
                size = it.size,
                modifiedAt = it.modifiedAt,
            )
        }
}
