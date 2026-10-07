/**
 * 职责：§10.1 弹层动效（全项目唯一入口）——
 *   rememberScaleInFrom / rememberScaleOutTo / rememberFadeSpec / MtPopupDialog。
 * ⋮ 菜单（7.1）与长按面板（7.2）共用 MtPopupDialog，仅 pivot 与时长不同：
 *   菜单 pivot=(1f,0f) 右上角展开；面板 pivot=(0.592f,0.615f) 偏左下炸开。
 * 关键约束（§10.1）：动效参数不散落各屏幕；新增弹层复用本组件，不自己写 EnterTransition。
 * 59.2% / 61.5% 是原作者调出来的手感魔数（§6.7 明确要求），不要改成 50%/50%。
 */
@file:JvmName("MtPopup")
package com.copy.mt.ui.components.overlay

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.copy.mt.ui.theme.LocalMtColors

/**
 * §6.7/§10.1 弹层进入：scale 0→1 + alpha，duration=200ms，FastOutSlowInEasing。
 * pivot 决定缩放轴心（菜单右上角 (1f,0f)，面板偏左下 (0.592f,0.615f)）。
 */
@Composable
fun rememberScaleInFrom(
    pivot: TransformOrigin,
    duration: Int = 200,
    interpolator: Easing = FastOutSlowInEasing,
): EnterTransition =
    scaleIn(
        initialScale = 0f,
        transformOrigin = pivot,
        animationSpec = tween(duration, easing = interpolator),
    ) + fadeIn(animationSpec = tween(duration, easing = interpolator))

/**
 * §6.7/§10.1 弹层退出：仅淡 alpha 1→0（不回缩 scale），duration=150ms。
 * pivot 仅为签名对称而保留：§6.7 规定退出只淡 alpha、不回缩。
 */
@Composable
fun rememberScaleOutTo(
    pivot: TransformOrigin,
    duration: Int = 150,
): ExitTransition = fadeOut(animationSpec = tween(duration))

/** §10.1 双向淡入淡出：duration=150ms。返回 (enter, exit)。 */
@Composable
fun rememberFadeSpec(duration: Int = 150): Pair<EnterTransition, ExitTransition> =
    fadeIn(animationSpec = tween(duration)) to fadeOut(animationSpec = tween(duration))

/**
 * §10.1 共用弹层壳：透明 scrim（dimAmount=0 仍接收点击→点外部关闭）+ 内容 scale 弹出。
 * 外层 AnimatedVisibility 管 scrim 淡入淡出（也保证退出动画播完再移出组合，
 * 修旧版「if (!visible) return」导致退出动画被跳过的问题）；
 * 内层 AnimatedVisibility 管内容 scaleIn / 淡出（§6.7 退出只淡 alpha、不回缩）。
 *
 * @param visible 是否显示（由调用方状态驱动；进/出动画自动播）
 * @param pivot 缩放轴心（菜单 (1f,0f)，面板 (0.592f,0.615f)）
 * @param dimAmount scrim 黑度，0=完全透明（背景不变暗，§6.7 要求 setDimAmount(0f)）
 * @param yOffset 整体上移量（面板 -7dp；菜单锚顶栏传 0dp）
 * @param alignment 内容对齐（菜单 TopEnd，面板 Center）
 * @param shape Surface 圆角（菜单 2dp，面板 8dp）
 * @param surfaceModifier 透传给内部 Surface（菜单 196dp+锚顶避让状态栏，面板 320dp）
 */
@Composable
fun MtPopupDialog(
    visible: Boolean,
    pivot: TransformOrigin,
    dimAmount: Float = 0f,
    yOffset: Dp = (-7).dp,
    alignment: Alignment = Alignment.Center,
    shape: Shape = RoundedCornerShape(8.dp),
    elevation: Dp = 12.dp,
    surfaceModifier: Modifier = Modifier,
    onDismissRequest: () -> Unit,
    content: @Composable ColumnScope.() -> Unit,
) {
    val c = LocalMtColors.current
    // 外层：scrim 生命周期（淡入淡出 150ms）。dimAmount=0 时 scrim 透明但仍可点。
    AnimatedVisibility(
        visible = visible,
        enter = fadeIn(animationSpec = tween(150)),
        exit = fadeOut(animationSpec = tween(150)),
    ) {
        Box(
            Modifier
                .fillMaxSize()
                .background(Color.Black.copy(alpha = dimAmount))
                .clickable(onClick = onDismissRequest),
            contentAlignment = alignment,
        ) {
            // 内层：内容 scale 弹出 / 淡出。offset 统一上移 yOffset（面板 -7dp）。
            AnimatedVisibility(
                visible = visible,
                enter = rememberScaleInFrom(pivot),
                exit = rememberScaleOutTo(pivot),
                modifier = Modifier.offset(y = yOffset),
            ) {
                Surface(
                    shape = shape,
                    color = c.menuBg,
                    shadowElevation = elevation,
                    modifier = surfaceModifier,
                ) {
                    Column(content = content)
                }
            }
        }
    }
}
