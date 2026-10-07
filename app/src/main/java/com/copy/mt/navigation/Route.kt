package com.copy.mt.navigation

/** 页面路由定义（骨架；镜像 account 的 AppPage 写法）。 */
sealed interface Route {
    data object PermissionGate : Route
    data object Browser : Route
    data object Apks : Route
    data object Storage : Route
    data object Settings : Route
    data object Plugins : Route
    data object Logs : Route
    data object AccessMode : Route
    data object Theme : Route
    data object Language : Route
}
