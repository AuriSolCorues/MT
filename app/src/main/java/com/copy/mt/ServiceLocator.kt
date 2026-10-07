package com.copy.mt

import com.copy.mt.core.data.FsFileRepository
import com.copy.mt.core.repository.FileRepository
import com.copy.mt.fs.impl.LocalFs

/** 组合根：装配 fs/core/plugin 实现并注入页面（手动 DI）。 */
object ServiceLocator {
    val fileRepository: FileRepository by lazy { FsFileRepository(LocalFs()) }
}
