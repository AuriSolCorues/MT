/**
 * 职责：画布层动效组件（设计文档 §6 动画规格落地）——
 *       §6.1 下拉刷新贝塞尔水滴 WaterDropRefresh
 *       §6.2 列表加载级联渐入 CascadeItem（epoch 方案）
 *       §6.4 面板边缘阴影折叠 ShadowEdges（+ shadowAlpha 三次贝塞尔缓动灰阶）
 *       §6.5 横滑选中 SwipeSelectBox（+ swipeSelectRange 范围选中）
 *       §6.6 双栏同步图标交叉淡变 SyncCrossfadeIcon
 *       §6.8 底栏整组切换 BottomBarCrossfade
 * 架构位置：ui/components 纯 Compose 实现（nestedScroll / pointerInput + Canvas），
 *           无 View 桥接、无第三方动画库。颜色取自 LocalMtColors：水滴用 bar
 *           （浅色板 #FF303030，即文档「与顶栏同色」的语义），横滑选中底用 sel
 *           （浅色板 #FF65C0DF 天蓝）。本文件不写死任何 UI 文案；无障碍描述经
 *           contentDescription 参数由调用方传入。
 */
package com.copy.mt.ui.components

import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseOut
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.animate
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.gestures.awaitEachGesture
import androidx.compose.foundation.gestures.awaitFirstDown
import androidx.compose.foundation.gestures.detectHorizontalDragGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxScope
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.nestedscroll.NestedScrollConnection
import androidx.compose.ui.input.nestedscroll.NestedScrollSource
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.IntOffset
import androidx.compose.ui.unit.Velocity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.copy.mt.ui.page.Side
import com.copy.mt.ui.theme.LocalMtColors
import com.copy.mt.ui.theme.MtTheme
import com.copy.mt.ui.theme.ThemeMode
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.math.abs
import kotlin.math.roundToInt

/** 无障碍描述挂载（可空，不写死任何文案）。 */
private fun Modifier.description(text: String?): Modifier =
    if (text == null) this else semantics { contentDescription = text }

// ═══════════════ §6.1 下拉刷新贝塞尔水滴 ★最重要 ═══════════════

/** 阻力系数：手指移 100px，内容移 40px。 */
const val PULL_RESISTANCE = 0.4f

/** 最大下拉距离（px；文档注明「注释写 dp，代码按 px」，沿用 px）。 */
const val MAX_PULL_DISTANCE = 240f

/** 触发刷新后列表滑回顶部的时长（ms）。 */
const val REFRESH_ANIMATION_DURATION = 300L

/** 水滴鼓起高度上限（px），即文档 BezierHeader.maxBulgeHeight。 */
const val MAX_BULGE_HEIGHT = 95f

/** §6.1 rawProgress = min(pull/240, 1)；moveProgress = raw²（二次 easeIn：起步慢、后段快）。 */
private fun moveProgressOf(pull: Float): Float {
    val raw = (pull / MAX_PULL_DISTANCE).coerceAtMost(1f)
    return raw * raw
}

/** §6.1 控制点横向比例：左栏 0.10 → 0.90（左→右），右栏 0.90 → 0.10（右→左）。 */
private fun controlXRatio(leftToRight: Boolean, moveProgress: Float): Float =
    if (leftToRight) 0.10f + 0.80f * moveProgress else 0.90f - 0.80f * moveProgress

/** §6.1 高度系数：moveProgress 0→0.5 时 1.4 → 2.2，0.5→1 时 2.2 → 0（中点鼓包 2.2 倍）。 */
private fun heightFactor(moveProgress: Float): Float =
    if (moveProgress <= 0.5f) 1.4f + 0.8f * (moveProgress / 0.5f)
    else 2.2f * (1f - (moveProgress - 0.5f) / 0.5f)

/** §6.1 isAtRightEnd：eps = 0.01f，控制点比例到达行程末端即视为到对侧末端。 */
private fun isAtRightEnd(leftToRight: Boolean, ratio: Float): Boolean =
    if (leftToRight) ratio >= 0.90f - 0.01f else ratio <= 0.10f + 0.01f

/**
 * 水滴刷新状态机（移植 MT2 PullRefreshLayout 的事件模型）：
 * - 增量式 dy：lastY 每帧更新（修正 MT2 原版「相对按下点累计」的语义漂移）
 * - 双标志锁：hasTriggered（防重复触发）+ isRefreshing（刷新中锁全部事件）
 * - finishRefresh()：由刷新完成方调用解锁（对应 MT2 的 finishRefresh()）
 */
private class WaterDropState(private val scope: CoroutineScope) {
    var onPullStart: () -> Unit = {}
    var onRefresh: () -> Unit = {}
    var leftToRight: Boolean = true
    var isRefreshing: Boolean = false
    var hasTriggered: Boolean = false
    var enabled: Boolean = true

    var pull by mutableFloatStateOf(0f)
    var contentOffset by mutableFloatStateOf(0f)

    /** 每帧增量驱动（手指移动多少，阻尼后水滴/内容走多少）。 */
    fun drag(deltaY: Float, atTop: Boolean) {
        if (isRefreshing || hasTriggered) return
        val next = (pull + deltaY * PULL_RESISTANCE).coerceIn(0f, MAX_PULL_DISTANCE)
        if (pull <= 0f && next > 0f) onPullStart()
        pull = next
        contentOffset = next
        val ratio = controlXRatio(leftToRight, moveProgressOf(next))
        if (!hasTriggered && (next >= MAX_PULL_DISTANCE || isAtRightEnd(leftToRight, ratio))) trigger()
    }

    /** 触发瞬间：水滴瞬间消失，列表甩到 240 再 300ms 滑回顶部；isRefreshing 锁事件直到 finishRefresh()。 */
    private fun trigger() {
        hasTriggered = true
        isRefreshing = true
        pull = 0f
        contentOffset = MAX_PULL_DISTANCE
        onRefresh()
        scope.launch {
            animate(
                MAX_PULL_DISTANCE, 0f, 0f,
                tween(REFRESH_ANIMATION_DURATION.toInt(), easing = FastOutSlowInEasing),
            ) { v, _ -> contentOffset = v }
        }
    }

    /** 未触发松手（回弹）：duration = (distance/240*450).coerceAtLeast(250)，减速曲线。 */
    fun settle() {
        if (isRefreshing || hasTriggered) return
        scope.launch {
            val distance = abs(contentOffset)
            if (distance <= 0.5f) {
                pull = 0f; contentOffset = 0f
                return@launch
            }
            val duration = (distance / MAX_PULL_DISTANCE * 450f).toInt().coerceAtLeast(250)
            animate(contentOffset, 0f, 0f, tween(duration, easing = EaseOut)) { v, _ ->
                pull = v
                contentOffset = v
            }
        }
    }

    /** 刷新完成：外部（VM 刷新结束）调用解锁。 */
    fun finishRefresh() {
        isRefreshing = false
        hasTriggered = false
        pull = 0f
        contentOffset = 0f
    }
}

/**
 * §6.1 下拉刷新（MT2 事件模型的 Compose 移植）：
 * - 容器独占手势：列表在顶时向下拖 → 本层消费全部垂直位移（等效 onInterceptTouchEvent）
 * - 增量式 dy、双标志锁、finishRefresh() 解锁
 * - 绘制照 §6.1 公式：二次贝塞尔水滴 + 内容层 translationY
 *
 * @param listState 传入被包列表的 LazyListState，用于「列表在顶」判定
 */
@Composable
fun WaterDropRefresh(
    isLeftToRight: Boolean,
    listState: LazyListState,
    onRefresh: () -> Unit,
    onFinishRefresh: () -> Unit = {},
    onPullStart: () -> Unit = {},
    modifier: Modifier = Modifier,
    isRefreshing: Boolean = false,
    enabled: Boolean = true,
    dropColor: Color = LocalMtColors.current.bar,
    contentDescription: String? = null,
    content: @Composable () -> Unit,
) {
    val scope = rememberCoroutineScope()
    val state = remember { WaterDropState(scope) }
    val density = LocalDensity.current

    // 外部 isRefreshing 变化 / 回调同步（避免手势闭包捕获过期值）
    SideEffect {
        state.leftToRight = isLeftToRight
        state.onPullStart = onPullStart
        state.onRefresh = onRefresh
        if (isRefreshing && !state.isRefreshing) state.isRefreshing = true
    }

    // 刷新数据完成（isRefreshing 由 true→false）→ 解锁并通知外部收尾
    LaunchedEffect(isRefreshing) {
        if (!isRefreshing && state.isRefreshing) {
            state.finishRefresh()
            onFinishRefresh()
        }
    }

    // §6.1 列表在顶判定：canScrollBackward=false 即在顶（无需再比对首项索引/偏移）
    fun atTop(): Boolean = !listState.canScrollBackward

    // §6.1 nestedScroll：与 LazyColumn 滚动协作——
    //   仅「列表在顶 + 向下拖」消费位移（阻尼由 state.drag 内部施加），
    //   其余场景（向上滚 / 已离顶）零消费，列表照常滚动。不再用 pointerInput 抢占手势。
    val connection = remember {
        object : NestedScrollConnection {
            override fun onPreScroll(available: Offset, source: NestedScrollSource): Offset {
                if (!enabled || state.isRefreshing || state.hasTriggered) return Offset.Zero
                if (available.y <= 0f || !atTop()) return Offset.Zero
                if (state.pull <= 0f) state.onPullStart()
                state.drag(available.y, atTop())
                return Offset(0f, available.y)
            }

            override fun onPostScroll(
                consumed: Offset,
                available: Offset,
                source: NestedScrollSource,
            ): Offset {
                // 列表消费后仍有剩余（顶到头）+ 继续向下 → 水滴接管
                if (!enabled || state.isRefreshing || state.hasTriggered) return Offset.Zero
                if (available.y <= 0f || !atTop()) return Offset.Zero
                if (state.pull <= 0f) state.onPullStart()
                state.drag(available.y, atTop())
                return Offset(0f, available.y)
            }
        }
    }

    Box(
        modifier
            .clipToBounds()
            .nestedScroll(connection)
            // 松手结算：被动等待 up，期间不消费任何位移（滚动/长按/点击全放行给子层）。
            // requireUnconsumed=true：子层已认领的手势本层不介入。
            .pointerInput(enabled) {
                if (!enabled) return@pointerInput
                awaitEachGesture {
                    val down = awaitFirstDown(requireUnconsumed = true)
                    do {
                        val event = awaitPointerEvent()
                    } while (event.changes.any { it.pressed })
                    // 松手 → 回弹结算（触发刷新的回弹由 state.drag 内部 trigger 完成）
                    state.settle()
                }
            }
            .description(contentDescription)
    ) {
        Canvas(Modifier.fillMaxSize()) {
            if (state.pull <= 0f) return@Canvas
            val baseBulge = state.pull.coerceAtMost(MAX_BULGE_HEIGHT)
            val move = moveProgressOf(state.pull)
            val cx = size.width * controlXRatio(state.leftToRight, move)
            val bulge = baseBulge * heightFactor(move)
            if (bulge <= 0f) return@Canvas
            val path = Path().apply {
                moveTo(0f, 0f)
                quadraticBezierTo(cx, bulge, size.width, 0f)
                close()
            }
            drawPath(path, dropColor, style = Fill)
        }
        Box(Modifier.fillMaxSize().graphicsLayer { translationY = state.contentOffset }) {
            content()
        }
    }
}

@Preview(name = "水滴下拉刷新", showBackground = true, widthDp = 180, heightDp = 320)
@Composable
private fun WaterDropRefreshPreview() {
    MtTheme(ThemeMode.DARK) {
        WaterDropRefresh(isLeftToRight = true, listState = LazyListState(), onRefresh = {}, onPullStart = {}) {
            LazyColumn(Modifier.fillMaxSize().background(LocalMtColors.current.surface)) {
                items((1..40).map { "item-$it" }) { name ->
                    Text(
                        name,
                        color = LocalMtColors.current.fg,
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                    )
                }
            }
        }
    }
}

// ═══════════════ §6.2 列表加载级联渐入（epoch 方案） ═══════════════

/**
 * §6.2 级联渐入：同一 epoch 内每项只播一次；数据刷新（epoch+1）后重播。
 * 可见项集体「隐藏 50ms → 淡入 100ms」，插值 CubicBezier(0.42, 0.2, 0.58, 0.8)；
 * 不做逐项错峰（原版即同帧集体淡入）。作为行内容外层容器使用：
 * `CascadeItem(epoch, index) { MtFileRow(...) }`。
 */
@Composable
fun CascadeItem(
    epoch: Int,
    index: Int,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit,
) {
    // remember(epoch) 让 epoch 变化时 played 归零、Animatable 重置，实现重播。
    var played by remember(epoch) { mutableStateOf(false) }
    val alpha = remember(epoch) { Animatable(0f) }
    LaunchedEffect(epoch) {
        // played 只做守卫、不作 key：写 key 会立刻取消本协程，动画永远到不了 1f（整列表透明）。
        if (played) return@LaunchedEffect
        played = true
        delay(50)
        alpha.animateTo(1f, tween(100, easing = CascadeInEasing))
    }
    Box(modifier.graphicsLayer { this.alpha = alpha.value }) { content() }
}

/** §6.2 PathInterpolator(0.42, 0.2, 0.58, 0.8) 的 Compose 等价。 */
val CascadeInEasing = CubicBezierEasing(0.42f, 0.2f, 0.58f, 0.8f)

// ═══════════════ §6.4 面板边缘阴影折叠 ═══════════════

/** 阴影边：内竖边（靠向激活栏一侧）+ 上边 + 下边。 */
enum class ShadowSide { LEFT_OF_RIGHT_PANE, RIGHT_OF_LEFT_PANE }

/**
 * §6.4 灰阶三次贝塞尔缓动：cubic-bezier(0.42, 0, 0.58, 1) 的分解式求值，
 * t ∈ [0,1] 黑 → 白。抽成单个函数供 24 段色标与调用方复用。
 */
fun shadowAlpha(t: Float): Float {
    val u = 1f - t
    return u * 0f + 3f * u * u * t * 0.42f + 3f * u * t * t * 0.58f + t * t * t
}

/** 24 段离散色标：黑→白 S 型灰阶（文档 COLORS/POSITIONS 表的 Compose 色标写法）。 */
private val ShadowStops: List<Pair<Float, Color>> = run {
    val steps = 24
    (0 until steps).map { i ->
        val t = i / (steps - 1).toFloat()
        val gray = (255 * shadowAlpha(t)).roundToInt()
        t to Color(red = gray, green = gray, blue = gray)
    }
}

/**
 * §6.4 面板边缘阴影：非激活栏「靠向激活栏那条边 + 上边 + 下边」的 4dp 暗色渐变，
 * 灰阶按三次贝塞尔缓动 + alpha 0.4；激活切换时 120ms alpha 交叉淡变（不硬切 visibility）。
 * 摆放：左栏在右缘放 [ShadowSide.RIGHT_OF_LEFT_PANE]，右栏在左缘放
 * [ShadowSide.LEFT_OF_RIGHT_PANE]，上/下边各自贴齐。
 */
@Composable
fun ShadowEdges(
    side: Side,
    active: Boolean,
    modifier: Modifier = Modifier,
    shadowTint: Color = Color.Black,
) {
    // 非激活栏才显示阴影；激活栏切换时 120ms 交叉淡变（非 visibility 硬切）。
    val foldAlpha by animateFloatAsState(
        targetValue = if (active) 0f else 1f,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "shadow-fold-alpha",
    )
    if (foldAlpha <= 0f) return

    // 原型 panes.css：inset 0 4px 6px / inset 0 -4px 6px / ±4px 0 6px —— 三条 4dp 内阴影，
    // 各自「贴边最深 → 向内淡出」。深端灰阶走 shadowAlpha 贝塞尔缓动，主题 alpha 0.4/0.6/0.8 由调用方 tint 传。
    // 阴影 alpha：0.4 在深色背景上太淡、用户看不出（「焦点没有」），加到 0.8。
    val stops: List<Pair<Float, Color>> =
        ShadowStops.map { (t, c0) -> t to shadowTint.copy(alpha = (1f - shadowAlpha(t)) * 0.8f) }
    // stops 的 alpha：offset=0 深(0.4)，offset=1 浅(0)。
    // 反向渐变需保留 offset 升序（Brush colorStops 要求 offset 0→1），否则解释未定义、
    // 阴影缺失（对应用户「双栏焦点没有」）。旧版用 stops.asReversed() 得到降序 offset。
    val stopsReversed: List<Pair<Float, Color>> =
        stops.mapIndexed { i, (t, _) -> t to stops[stops.size - 1 - i].second }
    val deepAtStart: Boolean = side == Side.RIGHT   // 右栏的内侧在左缘 → 深端贴条带起点
    val innerStops = if (deepAtStart) stops else stopsReversed
    val innerBrush = Brush.horizontalGradient(colorStops = innerStops.toTypedArray())
    val topBrush = Brush.verticalGradient(colorStops = stops.toTypedArray())                    // 上边：顶深
    val bottomBrush = Brush.verticalGradient(colorStops = stopsReversed.toTypedArray())  // 下边：底深

    Box(modifier.fillMaxSize().graphicsLayer { alpha = foldAlpha }) {
        Box(Modifier.fillMaxSize()) {
            Box(Modifier.fillMaxWidth().height(4.dp).background(topBrush))
            Box(
                Modifier
                    .fillMaxWidth()
                    .height(4.dp)
                    .background(bottomBrush)
                    .align(Alignment.BottomCenter),
            )
            val innerWidth = 4.dp
            Box(
                Modifier
                    .width(innerWidth)
                    .fillMaxHeight()
                    .background(innerBrush)
                    .align(if (side == Side.RIGHT) Alignment.CenterStart else Alignment.CenterEnd),
            )
        }
    }
}

@Preview(name = "面板边缘阴影", showBackground = true, widthDp = 160, heightDp = 200)
@Composable
private fun ShadowEdgesPreview() {
    MtTheme(ThemeMode.DARK) {
        Box(Modifier.size(160.dp, 200.dp).background(LocalMtColors.current.surface)) {
            ShadowEdges(side = Side.RIGHT, active = false, modifier = Modifier.fillMaxSize())
        }
    }
}

// ═══════════════ §6.5 横滑选中 + 范围选中 ═══════════════

/**
 * §6.5 横滑选中容器：根 Box 常驻天蓝选中底（露出色），内容层随手势平移最多 35dp（限位）；
 * 松手内容瞬间复位（无回弹动画）并回调 [onSelected]。滑动只平移内容层，根容器不动。
 * 颜色取色板 sel（浅色板 #FF65C0DF 天蓝，同文档）。
 */
@Composable
fun SwipeSelectBox(
    selected: Boolean,
    enabled: Boolean,
    onSelected: () -> Unit,
    modifier: Modifier = Modifier,
    maxSwipe: () -> Dp = { 35.dp },
    content: @Composable BoxScope.() -> Unit,
) {
    val c = LocalMtColors.current
    var dragX by remember { mutableFloatStateOf(0f) }
    val maxPx = with(LocalDensity.current) { maxSwipe().toPx() }
    // 选中阈值：明显位移才算横滑选中（修「过于灵敏」）；12dp ≈ 35dp 限位的 1/3。
    val selectThresholdPx = with(LocalDensity.current) { 12.dp.toPx() }
    Box(
        modifier
            .fillMaxWidth()
            .background(if (selected) c.sel else Color.Transparent)
            // §6.5 横滑选中：用 detectHorizontalDragGestures——
            //   仅「横向位移超 slop」才认领手势，纵向滚动 / 点按 / 长按全放行给子层，
            //   不再抢占 LazyColumn 滚动与 combinedClickable 的长按。
            .pointerInput(enabled, maxPx, selectThresholdPx) {
                if (!enabled) return@pointerInput
                detectHorizontalDragGestures(
                    onDragStart = { dragX = 0f },
                    onHorizontalDrag = { change, dragAmount ->
                        change.consume()
                        dragX = (dragX + dragAmount).coerceIn(-maxPx, maxPx)
                    },
                    onDragEnd = {
                        // clearView 结算：瞬间复位、无动画；位移达到阈值才计为选中。
                        val dragged = abs(dragX) >= selectThresholdPx
                        dragX = 0f
                        if (dragged) onSelected()
                    },
                    onDragCancel = { dragX = 0f },
                )
            },
    ) {
        // 内容层平移（仅画布位移，不重建布局）；负 margin 缺口问题由内容自撑满规避。
        Box(Modifier.fillMaxWidth().offset { IntOffset(dragX.roundToInt(), 0) }, content = content)
    }
}

/**
 * §6.5 范围选中纯逻辑（§⑤ selectIfNotSelected）：
 * 第一下进入选中模式并记起点；已选态下再滑另一项 → 两项之间全部选中，随后清起点可再来一轮。
 * 返回处理后的选中集合副本。`..`（第 0 项）禁滑由调用方以 enabled=false 表达（§④）。
 */
fun swipeSelectRange(
    position: Int,
    isSelectionMode: Boolean,
    firstSwipePosition: Int?,
    selected: Set<Int>,
): SwipeSelectResult {
    if (!isSelectionMode) {
        return SwipeSelectResult(selected + position, isSelectionMode = true, firstSwipePosition = position)
    }
    if (firstSwipePosition == null) {
        return SwipeSelectResult(selected + position, isSelectionMode = true, firstSwipePosition = position)
    }
    val start = minOf(firstSwipePosition, position)
    val end = maxOf(firstSwipePosition, position)
    return SwipeSelectResult(selected + (start..end).toSet(), isSelectionMode = true, firstSwipePosition = null)
}

/** swipeSelectRange 的结算结果（新选中集 + 是否进入选中模式 + 当前起点）。 */
data class SwipeSelectResult(
    val selected: Set<Int>,
    val isSelectionMode: Boolean,
    val firstSwipePosition: Int?,
)

@Preview(name = "横滑选中", showBackground = true, widthDp = 300)
@Composable
private fun SwipeSelectBoxPreview() {
    MtTheme(ThemeMode.DARK) {
        Column {
            SwipeSelectBox(selected = true, enabled = true, onSelected = {}) {
                MtFileRow("swipe-me", "26-10-06 14:14", FileKind.FILE, selected = true)
            }
            SwipeSelectBox(selected = false, enabled = false, onSelected = {}) {
                MtFileRow("..", "", FileKind.UP)
            }
        }
    }
}

// ═══════════════ §6.6 双栏同步图标交叉淡变 + §6.8 底栏整组切换 ═══════════════

/**
 * §6.6 同步图标：两个叠加箭头（文档 pathData 的 Canvas 直绘：左下 →、右上 ←），
 * 按激活栏交叉淡变 150ms 表达「哪栏激活」——左激活：左箭头 50%/右箭头 100%；右激活反之；中性全 100%。
 * 颜色取色板 iglyph（底栏图标同色）。
 */
@Composable
fun SyncCrossfadeIcon(
    active: Side,
    modifier: Modifier = Modifier,
    tint: Color = LocalMtColors.current.iglyph,
    contentDescription: String? = null,
) {
    // 三态箭头 alpha：左箭头 / 右箭头。
    val leftAlpha by animateFloatAsState(
        targetValue = if (active == Side.LEFT) 0.5f else 1f,
        animationSpec = tween(150),
        label = "sync-left-arrow-alpha",
    )
    val rightAlpha by animateFloatAsState(
        targetValue = if (active == Side.RIGHT) 0.5f else 1f,
        animationSpec = tween(150),
        label = "sync-right-arrow-alpha",
    )
    Box(
        modifier.size(24.dp).description(contentDescription),
        contentAlignment = Alignment.Center,
    ) {
        Canvas(Modifier.size(24.dp)) {
            val s = size.width / 24f // pathData 按 24dp 视口归一化
            // 左下箭头 "M6.5,11.3v3.2h7.5v2.2h-7.5v3.2L2.1,15.6z"
            val left = Path().apply {
                moveTo(6.5f * s, 11.3f * s)
                lineTo(6.5f * s, 14.5f * s)
                lineTo(14f * s, 14.5f * s)
                lineTo(14f * s, 16.7f * s)
                lineTo(6.5f * s, 16.7f * s)
                lineTo(6.5f * s, 19.9f * s)
                lineTo(2.1f * s, 15.6f * s)
                close()
            }
            // 右上箭头 "M17.5,4.7v3.2h-7.5v2.2h7.5v3.2L21.9,9z"
            val right = Path().apply {
                moveTo(17.5f * s, 4.7f * s)
                lineTo(17.5f * s, 7.9f * s)
                lineTo(10f * s, 7.9f * s)
                lineTo(10f * s, 10.1f * s)
                lineTo(17.5f * s, 10.1f * s)
                lineTo(17.5f * s, 13.3f * s)
                lineTo(21.9f * s, 9f * s)
                close()
            }
            drawPath(left, tint.copy(alpha = tint.alpha * leftAlpha))
            drawPath(right, tint.copy(alpha = tint.alpha * rightAlpha))
        }
    }
}

@Preview(name = "同步图标三态", showBackground = true, widthDp = 140)
@Composable
private fun SyncCrossfadeIconPreview() {
    MtTheme(ThemeMode.DARK) {
        Row(Modifier.padding(8.dp), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            SyncCrossfadeIcon(Side.LEFT)
            SyncCrossfadeIcon(Side.RIGHT)
            SyncCrossfadeIcon(active = Side.LEFT, tint = LocalMtColors.current.fg2) // 中性占位
        }
    }
}

/** §6.8 底栏模式：普通 5 键 ⇄ 多选 5 键。 */
enum class BottomBarMode { NORMAL, MULTI }

/**
 * §6.8 底栏整组切换：普通 5 导航键与多选操作键整组交叉淡变 120ms，不硬切 visibility。
 * 两组键的内容由调用方以 [normal] / [multi] 传入（含「全选/反选/取消选择/条件选择/待定」）。
 */
@Composable
fun BottomBarCrossfade(
    target: BottomBarMode,
    normal: @Composable () -> Unit,
    multi: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    Crossfade(
        targetState = target,
        animationSpec = tween(120, easing = FastOutSlowInEasing),
        label = "bottom-bar-crossfade",
    ) { mode ->
        Box(Modifier.fillMaxWidth()) {
            when (mode) {
                BottomBarMode.NORMAL -> normal()
                BottomBarMode.MULTI -> multi()
            }
        }
    }
}

@Preview(name = "底栏整组切换", showBackground = true, widthDp = 360, heightDp = 72)
@Composable
private fun BottomBarCrossfadePreview() {
    MtTheme(ThemeMode.DARK) {
        BottomBarCrossfade(
            target = BottomBarMode.MULTI,
            normal = {
                Row(Modifier.height(64.dp).background(LocalMtColors.current.bar)) {
                    listOf("←", "→", "＋", "⇄", "↑").forEach {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(it, color = LocalMtColors.current.fg2, fontSize = 20.sp)
                        }
                    }
                }
            },
            multi = {
                Row(Modifier.height(64.dp).background(LocalMtColors.current.bar)) {
                    listOf("全选", "反选", "取消", "条件", "待定").forEach {
                        Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                            Text(it, color = LocalMtColors.current.fg2, fontSize = 12.sp)
                        }
                    }
                }
            },
        )
    }
}
