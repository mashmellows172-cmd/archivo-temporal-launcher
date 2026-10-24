package com.archivotemporal.launcher.character

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.semantics.clearAndSetSemantics
import com.archivotemporal.launcher.preferences.MotionMode
import kotlinx.coroutines.delay

private val ClockOrange = Color(0xFFC85A24)
private val ClockShade = Color(0xFF9D3F20)
private val Ink = Color(0xFF30201D)
private val Cream = Color(0xFFFFE8C2)
private val Glove = Color(0xFFFFF6E9)
private val Shoe = Color(0xFF38241F)

/** A decorative, non-interactive clock guide. Its entire footprint is controlled by [modifier]. */
@Composable
fun MissMinutesCanvas(state: MissMinutesState, modifier: Modifier = Modifier) {
    val position = remember { Animatable(state.targetX) }
    val walk = remember { Animatable(0f) }
    val breathe = remember { Animatable(0f) }
    val blink = remember { Animatable(1f) }
    val pose = remember { Animatable(0f) }

    LaunchedEffect(state.targetX, state.motionMode, state.transitionMillis, state.phase) {
        if (state.phase == MissMinutesPhase.Notifying) {
            return@LaunchedEffect
        } else if (state.motionMode == MotionMode.REDUCED) {
            position.snapTo(state.targetX)
        } else if (state.phase != MissMinutesPhase.Moving) {
            position.snapTo(state.targetX)
            return@LaunchedEffect
        } else {
            position.animateTo(
                state.targetX,
                tween(
                    durationMillis = state.transitionMillis.coerceAtLeast(1),
                    easing = LinearEasing,
                ),
            )
        }
    }
    LaunchedEffect(state.motionMode, state.phase) {
        if (state.motionMode == MotionMode.FULL && state.phase == MissMinutesPhase.Moving) {
            walk.animateTo(
                1f,
                infiniteRepeatable(tween(320, easing = LinearEasing), RepeatMode.Reverse),
            )
        } else {
            walk.snapTo(0f)
        }
    }
    LaunchedEffect(state.motionMode, state.phase) {
        val targetPose = when (state.phase) {
            MissMinutesPhase.Pointing -> 1f
            MissMinutesPhase.Confirming -> 0.7f
            MissMinutesPhase.Notifying -> -1f
            else -> 0f
        }
        if (state.motionMode == MotionMode.REDUCED) {
            pose.snapTo(targetPose)
        } else {
            pose.animateTo(targetPose, tween(180))
        }
    }
    LaunchedEffect(state.motionMode) {
        if (state.motionMode == MotionMode.FULL) {
            breathe.animateTo(
                1f,
                infiniteRepeatable(tween(3_400, easing = LinearEasing), RepeatMode.Reverse),
            )
        } else {
            breathe.snapTo(0f)
        }
    }
    LaunchedEffect(state.motionMode) {
        if (state.motionMode == MotionMode.FULL) {
            while (true) {
                delay(3_200)
                blink.animateTo(0.12f, tween(85))
                delay(90)
                blink.animateTo(1f, tween(115))
            }
        } else {
            blink.snapTo(1f)
        }
    }

    Canvas(modifier = modifier.clearAndSetSemantics { }) {
        drawMissMinutes(
            size = size,
            position = position.value,
            phase = state.phase,
            walk = walk.value,
            breathe = breathe.value,
            eyeOpen = blink.value,
            pose = pose.value,
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawMissMinutes(
    size: androidx.compose.ui.geometry.Size,
    position: Float,
    phase: MissMinutesPhase,
    walk: Float,
    breathe: Float,
    eyeOpen: Float,
    pose: Float,
) {
    val unit = (size.minDimension * 0.105f).coerceIn(28f, 82f)
    val sideMargin = unit * 1.2f
    val availableWidth = (size.width - sideMargin * 2f).coerceAtLeast(0f)
    val centerX = if (availableWidth == 0f) size.width / 2f
    else sideMargin + position.coerceIn(0f, 1f) * availableWidth
    val center = Offset(centerX, size.height * 0.67f)
    val radius = unit * 0.57f
    val stroke = unit * 0.075f
    val bodyScale = 1f + breathe * 0.012f
    val walking = phase == MissMinutesPhase.Moving
    val pointing = phase == MissMinutesPhase.Pointing
    val confirming = phase == MissMinutesPhase.Confirming
    val notifying = phase == MissMinutesPhase.Notifying

    // Shoe shadows ground the figure without adding a separate moving layout element.
    drawOval(
        color = Ink.copy(alpha = 0.14f),
        topLeft = Offset(center.x - unit * 0.56f, center.y + unit * 1.17f),
        size = androidx.compose.ui.geometry.Size(unit * 1.12f, unit * 0.18f),
    )

    val leftLegAngle = if (walking) -25f + walk * 50f else 8f
    val rightLegAngle = if (walking) 25f - walk * 50f else -8f
    drawLeg(center, unit, stroke, -1f, leftLegAngle)
    drawLeg(center, unit, stroke, 1f, rightLegAngle)

    val leftArmAngle = -12f - pose * 25f
    val rightArmAngle = 12f + pose * 25f
    drawArm(center, unit, stroke, -1f, leftArmAngle, pointing)
    drawArm(center, unit, stroke, 1f, rightArmAngle, notifying)

    // Clock body, with a tiny squash/stretch and hand-drawn outer ring.
    drawCircle(
        color = ClockShade,
        radius = radius * bodyScale,
        center = center + Offset(0f, unit * 0.035f),
    )
    drawCircle(color = ClockOrange, radius = radius * bodyScale, center = center)
    drawCircle(
        color = Ink,
        radius = radius * bodyScale,
        center = center,
        style = Stroke(width = stroke, cap = StrokeCap.Round),
    )

    // Twelve dark clock ticks, shortened around the expressive face.
    repeat(12) { index ->
        val angle = Math.toRadians(index * 30.0 - 90.0)
        val outer = Offset(center.x + kotlin.math.cos(angle).toFloat() * radius * 0.83f,
            center.y + kotlin.math.sin(angle).toFloat() * radius * 0.83f)
        val inner = Offset(center.x + kotlin.math.cos(angle).toFloat() * radius * 0.70f,
            center.y + kotlin.math.sin(angle).toFloat() * radius * 0.70f)
        drawLine(Ink, inner, outer, strokeWidth = stroke * 0.48f, cap = StrokeCap.Round)
    }

    val eyeY = center.y - unit * 0.16f
    val eyeSpacing = unit * 0.19f
    val eyeRadiusX = unit * 0.105f
    val eyeRadiusY = unit * (0.14f * eyeOpen.coerceAtLeast(0.08f))
    listOf(-1f, 1f).forEach { side ->
        val eye = Offset(center.x + eyeSpacing * side, eyeY)
        drawOval(Glove, Offset(eye.x - eyeRadiusX, eye.y - eyeRadiusY),
            androidx.compose.ui.geometry.Size(eyeRadiusX * 2f, eyeRadiusY * 2f))
        val gaze = ((position - 0.5f) * 2f).coerceIn(-1f, 1f) * unit * 0.025f
        drawCircle(Ink, unit * 0.042f, eye + Offset(gaze, unit * 0.015f))
        drawCircle(Color.White, unit * 0.012f, eye + Offset(gaze - unit * 0.012f, -unit * 0.01f))
    }

    // Brow arcs and a changing mouth make the clock read as a character, not a dial.
    val browY = eyeY - unit * 0.20f
    drawLine(Ink, Offset(center.x - unit * 0.29f, browY), Offset(center.x - unit * 0.10f, browY - unit * 0.025f),
        strokeWidth = stroke * 0.5f, cap = StrokeCap.Round)
    drawLine(Ink, Offset(center.x + unit * 0.10f, browY - unit * 0.025f), Offset(center.x + unit * 0.29f, browY),
        strokeWidth = stroke * 0.5f, cap = StrokeCap.Round)
    val mouth = Path().apply {
        moveTo(center.x - unit * 0.13f, center.y + unit * 0.18f)
        cubicTo(center.x - unit * 0.06f, center.y + unit * 0.28f,
            center.x + unit * 0.06f, center.y + unit * 0.28f,
            center.x + unit * 0.13f, center.y + unit * 0.18f)
    }
    drawPath(mouth, Ink, style = Stroke(width = stroke * 0.55f, cap = StrokeCap.Round))
    if (confirming || notifying) {
        drawOval(
            color = Ink,
            topLeft = Offset(center.x - unit * 0.075f, center.y + unit * 0.13f),
            size = androidx.compose.ui.geometry.Size(unit * 0.15f, unit * 0.17f),
        )
        drawOval(
            color = ClockShade,
            topLeft = Offset(center.x - unit * 0.045f, center.y + unit * 0.215f),
            size = androidx.compose.ui.geometry.Size(unit * 0.09f, unit * 0.06f),
        )
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawLeg(
    center: Offset,
    unit: Float,
    stroke: Float,
    side: Float,
    angle: Float,
) {
    val hip = center + Offset(side * unit * 0.20f, unit * 0.46f)
    rotate(degrees = angle, pivot = hip) {
        val knee = hip + Offset(side * unit * 0.05f, unit * 0.36f)
        val ankle = knee + Offset(side * unit * 0.06f, unit * 0.27f)
        drawLine(Ink, hip, ankle, strokeWidth = stroke * 0.74f, cap = StrokeCap.Round)
        drawLine(ClockOrange, hip, ankle, strokeWidth = stroke * 0.42f, cap = StrokeCap.Round)
        drawLine(Ink, ankle, ankle + Offset(side * unit * 0.27f, unit * 0.015f),
            strokeWidth = stroke * 0.9f, cap = StrokeCap.Round)
        drawLine(Shoe, ankle + Offset(side * unit * 0.08f, 0f),
            ankle + Offset(side * unit * 0.34f, 0f), strokeWidth = stroke * 1.75f, cap = StrokeCap.Round)
    }
}

private fun androidx.compose.ui.graphics.drawscope.DrawScope.drawArm(
    center: Offset,
    unit: Float,
    stroke: Float,
    side: Float,
    angle: Float,
    point: Boolean,
) {
    val shoulder = center + Offset(side * unit * 0.47f, -unit * 0.03f)
    rotate(degrees = angle * side, pivot = shoulder) {
        val elbow = shoulder + Offset(side * unit * 0.37f, unit * 0.09f)
        val wrist = elbow + Offset(side * unit * 0.28f, -unit * 0.02f)
        val hand = wrist + Offset(side * unit * if (point) 0.17f else 0.12f, -unit * 0.02f)
        drawLine(Ink, shoulder, wrist, strokeWidth = stroke * 0.68f, cap = StrokeCap.Round)
        drawLine(ClockOrange, shoulder, wrist, strokeWidth = stroke * 0.37f, cap = StrokeCap.Round)
        drawCircle(Glove, radius = unit * 0.12f, center = wrist)
        drawCircle(Ink, radius = unit * 0.12f, center = wrist, style = Stroke(stroke * 0.35f))
        drawLine(Ink, wrist, hand, strokeWidth = stroke * 0.36f, cap = StrokeCap.Round)
        if (point) {
            drawLine(Ink, hand, hand + Offset(side * unit * 0.11f, -unit * 0.12f),
                strokeWidth = stroke * 0.34f, cap = StrokeCap.Round)
        } else {
            drawCircle(Glove, radius = unit * 0.105f, center = hand)
            drawCircle(Ink, radius = unit * 0.105f, center = hand, style = Stroke(stroke * 0.34f))
        }
    }
}
