package com.copy.mt.fs.api

/** 后端可用状态。 */
data class BackendStatus(val mode: AccessMode, val available: Boolean, val reason: String? = null)
