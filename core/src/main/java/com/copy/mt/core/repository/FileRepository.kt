package com.copy.mt.core.repository

import com.copy.mt.core.model.FileItem

/** 文件仓库接口（骨架）。 */
interface FileRepository {
    suspend fun list(path: String): List<FileItem>
}
