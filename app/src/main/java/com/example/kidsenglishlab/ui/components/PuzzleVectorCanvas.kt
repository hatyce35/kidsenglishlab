package com.example.kidsenglishlab.ui.components

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.example.kidsenglishlab.data.WordItem

fun parseColor(hex: String?, fallback: Color): Color {
    if (hex.isNullOrBlank()) return fallback
    return try {
        val clean = hex.removePrefix("#")
        val colorInt = when (clean.length) {
            6 -> "FF$clean".toLong(16).toInt()
            8 -> clean.toLong(16).toInt()
            3 -> {
                val r = clean[0].toString().repeat(2)
                val g = clean[1].toString().repeat(2)
                val b = clean[2].toString().repeat(2)
                "FF$r$g$b".toLong(16).toInt()
            }
            else -> return fallback
        }
        Color(colorInt)
    } catch (_: Exception) {
        fallback
    }
}

@Composable
fun PuzzleVectorCanvas(
    word: WordItem,
    revealedIndices: List<Int>,
    userColors: Map<String, String> = emptyMap(),
    onPieceClick: ((Int) -> Unit)? = null,
    onPartClick: ((String) -> Unit)? = null,
    isCompleted: Boolean = false,
    showGuide: Boolean = false,
    outlineOnly: Boolean = false,
    modifier: Modifier = Modifier
) {
    val infiniteTransition = rememberInfiniteTransition(label = "puzzlePulse")
    val pulseScale by infiniteTransition.animateFloat(
        initialValue = 1.0f,
        targetValue = if (isCompleted) 1.04f else 1.0f,
        animationSpec = infiniteRepeatable(
            animation = tween(900, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulseScale"
    )

    val wordLen = word.english.length.coerceAtLeast(1)

    Box(
        modifier = modifier
            .fillMaxSize()
            .pointerInput(word.id, revealedIndices, onPieceClick, onPartClick) {
                detectTapGestures { offset ->
                    if (onPartClick != null) {
                        val tappedPart = detectTappedPart(word, offset, size.width.toFloat(), size.height.toFloat())
                        onPartClick(tappedPart)
                    } else if (onPieceClick != null) {
                        val pieceWidth = size.width / wordLen
                        val index = (offset.x / pieceWidth).toInt().coerceIn(0, wordLen - 1)
                        onPieceClick(index)
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val w = size.width
            val h = size.height
            val cx = w / 2f
            val cy = h / 2f

            // 1. Background clean card
            drawRoundRect(
                color = Color.White,
                cornerRadius = CornerRadius(24.dp.toPx(), 24.dp.toPx())
            )

            // 2. Draw Artwork Base (Color or Clean Outlines)
            drawWordArtwork(
                word = word,
                userColors = userColors,
                revealedRatio = if (showGuide || isCompleted) 1.0f else revealedIndices.size.toFloat() / wordLen,
                outlineOnly = outlineOnly,
                w = w,
                h = h
            )

            // 3. Draw Puzzle Grid Lines if not completed and not in coloring outline mode
            if (!isCompleted && !showGuide && !outlineOnly) {
                val pieceW = w / wordLen
                for (i in 0 until wordLen) {
                    val isRevealed = revealedIndices.contains(i)
                    val left = i * pieceW
                    val right = (i + 1) * pieceW

                    if (!isRevealed) {
                        // Dim overlay for unrevealed piece
                        drawRect(
                            color = Color(0xDDFFFFFF),
                            topLeft = Offset(left, 0f),
                            size = Size(pieceW, h)
                        )
                        // Dashed borders
                        drawRect(
                            color = Color(0xFFCBD5E1),
                            topLeft = Offset(left, 0f),
                            size = Size(pieceW, h),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    } else {
                        // Highlight outline for revealed piece
                        drawRect(
                            color = Color(0x33F59E0B),
                            topLeft = Offset(left, 0f),
                            size = Size(pieceW, h),
                            style = Stroke(width = 2.dp.toPx())
                        )
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawWordArtwork(
    word: WordItem,
    userColors: Map<String, String>,
    revealedRatio: Float,
    outlineOnly: Boolean,
    w: Float,
    h: Float
) {
    val cx = w / 2f
    val cy = h / 2f
    val scale = (w / 260f).coerceAtMost(h / 260f)

    fun c(partKey: String, defaultHex: String): Color {
        val userHex = userColors[partKey]
        if (userHex != null) {
            return parseColor(userHex, Color.White)
        }
        if (outlineOnly) {
            return Color.White
        }
        val hex = word.defaultParts[partKey] ?: defaultHex
        val base = parseColor(hex, parseColor(defaultHex, Color(0xFFF59E0B)))
        return if (revealedRatio >= 1.0f) base else base.copy(alpha = (0.35f + 0.65f * revealedRatio).coerceIn(0.1f, 1.0f))
    }

    when (word.id) {
        "cat" -> {
            // Ears
            val leftEar = Path().apply {
                moveTo(cx - 65 * scale, cy - 30 * scale)
                lineTo(cx - 45 * scale, cy - 85 * scale)
                lineTo(cx - 20 * scale, cy - 40 * scale)
                close()
            }
            drawPath(leftEar, c("ears", "#F97316"))
            drawPath(leftEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            val rightEar = Path().apply {
                moveTo(cx + 20 * scale, cy - 40 * scale)
                lineTo(cx + 45 * scale, cy - 85 * scale)
                lineTo(cx + 65 * scale, cy - 30 * scale)
                close()
            }
            drawPath(rightEar, c("ears", "#F97316"))
            drawPath(rightEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            // Head
            drawCircle(c("head", "#FB923C"), radius = 62 * scale, center = Offset(cx, cy))
            drawCircle(Color(0xFF1E293B), radius = 62 * scale, center = Offset(cx, cy), style = Stroke(4.dp.toPx()))

            // Eyes
            drawCircle(Color(0xFF1E293B), radius = 7 * scale, center = Offset(cx - 25 * scale, cy - 8 * scale))
            drawCircle(Color(0xFF1E293B), radius = 7 * scale, center = Offset(cx + 25 * scale, cy - 8 * scale))
            drawCircle(Color.White, radius = 2.5f * scale, center = Offset(cx - 27 * scale, cy - 11 * scale))
            drawCircle(Color.White, radius = 2.5f * scale, center = Offset(cx + 23 * scale, cy - 11 * scale))

            // Cheeks
            drawCircle(c("cheeks", "#FDA4AF"), radius = 8 * scale, center = Offset(cx - 38 * scale, cy + 12 * scale))
            drawCircle(c("cheeks", "#FDA4AF"), radius = 8 * scale, center = Offset(cx + 38 * scale, cy + 12 * scale))

            // Nose & Mouth
            val nose = Path().apply {
                moveTo(cx - 7 * scale, cy + 8 * scale)
                lineTo(cx + 7 * scale, cy + 8 * scale)
                lineTo(cx, cy + 16 * scale)
                close()
            }
            drawPath(nose, c("nose", "#FB7185"))
        }
        "dog" -> {
            // Body
            drawOval(c("body", "#FBBF24"), topLeft = Offset(cx - 56 * scale, cy + 20 * scale), size = Size(112 * scale, 75 * scale))
            drawOval(Color(0xFF1E293B), topLeft = Offset(cx - 56 * scale, cy + 20 * scale), size = Size(112 * scale, 75 * scale), style = Stroke(4.dp.toPx()))

            // Ears
            val leftEar = Path().apply {
                moveTo(cx - 50 * scale, cy - 40 * scale)
                cubicTo(cx - 90 * scale, cy - 30 * scale, cx - 80 * scale, cy + 40 * scale, cx - 45 * scale, cy + 20 * scale)
                close()
            }
            drawPath(leftEar, c("ears", "#D97706"))
            drawPath(leftEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            val rightEar = Path().apply {
                moveTo(cx + 50 * scale, cy - 40 * scale)
                cubicTo(cx + 90 * scale, cy - 30 * scale, cx + 80 * scale, cy + 40 * scale, cx + 45 * scale, cy + 20 * scale)
                close()
            }
            drawPath(rightEar, c("ears", "#D97706"))
            drawPath(rightEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            // Head
            drawCircle(c("head", "#FBBF24"), radius = 55 * scale, center = Offset(cx, cy - 20 * scale))
            drawCircle(Color(0xFF1E293B), radius = 55 * scale, center = Offset(cx, cy - 20 * scale), style = Stroke(4.dp.toPx()))

            // Snout
            drawOval(c("snout", "#FEF3C7"), topLeft = Offset(cx - 32 * scale, cy - 10 * scale), size = Size(64 * scale, 42 * scale))
            drawCircle(Color(0xFF1E293B), radius = 8 * scale, center = Offset(cx, cy))
            // Tongue
            drawOval(c("tongue", "#FB7185"), topLeft = Offset(cx - 8 * scale, cy + 14 * scale), size = Size(16 * scale, 18 * scale))

            // Eyes
            drawCircle(Color(0xFF1E293B), radius = 8 * scale, center = Offset(cx - 24 * scale, cy - 28 * scale))
            drawCircle(Color(0xFF1E293B), radius = 8 * scale, center = Offset(cx + 24 * scale, cy - 28 * scale))
            drawCircle(Color.White, radius = 3 * scale, center = Offset(cx - 26 * scale, cy - 31 * scale))
            drawCircle(Color.White, radius = 3 * scale, center = Offset(cx + 22 * scale, cy - 31 * scale))
        }
        "sun" -> {
            // Sun Rays
            val rayColor = c("rays", "#F59E0B")
            for (angle in 0 until 360 step 45) {
                val rad = Math.toRadians(angle.toDouble())
                val rx = cx + (68 * scale) * Math.cos(rad).toFloat()
                val ry = cy + (68 * scale) * Math.sin(rad).toFloat()
                val rx2 = cx + (92 * scale) * Math.cos(rad).toFloat()
                val ry2 = cy + (92 * scale) * Math.sin(rad).toFloat()
                drawLine(rayColor, Offset(rx, ry), Offset(rx2, ry2), strokeWidth = 8.dp.toPx(), cap = StrokeCap.Round)
            }
            // Sun Body
            drawCircle(c("body", "#FBBF24"), radius = 55 * scale, center = Offset(cx, cy))
            drawCircle(Color(0xFF1E293B), radius = 55 * scale, center = Offset(cx, cy), style = Stroke(4.dp.toPx()))

            // Face
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx - 22 * scale, cy - 6 * scale))
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx + 22 * scale, cy - 6 * scale))
            drawCircle(c("cheeks", "#FDA4AF"), radius = 8 * scale, center = Offset(cx - 34 * scale, cy + 12 * scale))
            drawCircle(c("cheeks", "#FDA4AF"), radius = 8 * scale, center = Offset(cx + 34 * scale, cy + 12 * scale))

            val smile = Path().apply {
                moveTo(cx - 18 * scale, cy + 14 * scale)
                quadraticBezierTo(cx, cy + 30 * scale, cx + 18 * scale, cy + 14 * scale)
            }
            drawPath(smile, Color(0xFF1E293B), style = Stroke(4.dp.toPx(), cap = StrokeCap.Round))
        }
        "car" -> {
            // Car Roof & Windows
            val carBody = Path().apply {
                moveTo(cx - 80 * scale, cy + 20 * scale)
                lineTo(cx - 50 * scale, cy - 30 * scale)
                lineTo(cx + 45 * scale, cy - 30 * scale)
                lineTo(cx + 80 * scale, cy + 20 * scale)
                close()
            }
            drawPath(carBody, c("body", "#3B82F6"))
            drawPath(carBody, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            // Cabin rectangle
            drawRoundRect(c("body", "#3B82F6"), topLeft = Offset(cx - 95 * scale, cy + 10 * scale), size = Size(190 * scale, 45 * scale), cornerRadius = CornerRadius(10 * scale))
            drawRoundRect(Color(0xFF1E293B), topLeft = Offset(cx - 95 * scale, cy + 10 * scale), size = Size(190 * scale, 45 * scale), cornerRadius = CornerRadius(10 * scale), style = Stroke(4.dp.toPx()))

            // Windows
            drawRoundRect(c("windows", "#BAE6FD"), topLeft = Offset(cx - 45 * scale, cy - 24 * scale), size = Size(38 * scale, 32 * scale), cornerRadius = CornerRadius(4 * scale))
            drawRoundRect(c("windows", "#BAE6FD"), topLeft = Offset(cx + 2 * scale, cy - 24 * scale), size = Size(40 * scale, 32 * scale), cornerRadius = CornerRadius(4 * scale))

            // Wheels
            drawCircle(c("wheels", "#334155"), radius = 22 * scale, center = Offset(cx - 50 * scale, cy + 55 * scale))
            drawCircle(Color(0xFF1E293B), radius = 22 * scale, center = Offset(cx - 50 * scale, cy + 55 * scale), style = Stroke(4.dp.toPx()))
            drawCircle(c("hubcaps", "#94A3B8"), radius = 10 * scale, center = Offset(cx - 50 * scale, cy + 55 * scale))

            drawCircle(c("wheels", "#334155"), radius = 22 * scale, center = Offset(cx + 50 * scale, cy + 55 * scale))
            drawCircle(Color(0xFF1E293B), radius = 22 * scale, center = Offset(cx + 50 * scale, cy + 55 * scale), style = Stroke(4.dp.toPx()))
            drawCircle(c("hubcaps", "#94A3B8"), radius = 10 * scale, center = Offset(cx + 50 * scale, cy + 55 * scale))
        }
        "apple" -> {
            // Apple Body
            val applePath = Path().apply {
                moveTo(cx, cy - 35 * scale)
                cubicTo(cx - 40 * scale, cy - 65 * scale, cx - 80 * scale, cy - 20 * scale, cx - 70 * scale, cy + 30 * scale)
                cubicTo(cx - 60 * scale, cy + 75 * scale, cx - 15 * scale, cy + 75 * scale, cx, cy + 60 * scale)
                cubicTo(cx + 15 * scale, cy + 75 * scale, cx + 60 * scale, cy + 75 * scale, cx + 70 * scale, cy + 30 * scale)
                cubicTo(cx + 80 * scale, cy - 20 * scale, cx + 40 * scale, cy - 65 * scale, cx, cy - 35 * scale)
                close()
            }
            drawPath(applePath, c("body", "#EF4444"))
            drawPath(applePath, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            // Stem
            drawLine(c("stem", "#78350F"), Offset(cx, cy - 35 * scale), Offset(cx + 5 * scale, cy - 65 * scale), strokeWidth = 6.dp.toPx(), cap = StrokeCap.Round)
            // Leaf
            val leaf = Path().apply {
                moveTo(cx + 4 * scale, cy - 50 * scale)
                quadraticBezierTo(cx + 35 * scale, cy - 65 * scale, cx + 40 * scale, cy - 45 * scale)
                quadraticBezierTo(cx + 25 * scale, cy - 35 * scale, cx + 4 * scale, cy - 50 * scale)
                close()
            }
            drawPath(leaf, c("leaf", "#22C55E"))
            drawPath(leaf, Color(0xFF1E293B), style = Stroke(2.dp.toPx()))

            // Eyes & Cheeks
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx - 24 * scale, cy + 10 * scale))
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx + 24 * scale, cy + 10 * scale))
            drawCircle(c("cheeks", "#FDA4AF"), radius = 8 * scale, center = Offset(cx - 36 * scale, cy + 22 * scale))
            drawCircle(c("cheeks", "#FDA4AF"), radius = 8 * scale, center = Offset(cx + 36 * scale, cy + 22 * scale))
        }
        "star" -> {
            val starPath = Path()
            val points = 5
            val outerR = 75 * scale
            val innerR = 35 * scale
            for (i in 0 until points * 2) {
                val r = if (i % 2 == 0) outerR else innerR
                val angle = Math.toRadians((i * 36 - 90).toDouble())
                val px = cx + r * Math.cos(angle).toFloat()
                val py = cy + r * Math.sin(angle).toFloat()
                if (i == 0) starPath.moveTo(px, py) else starPath.lineTo(px, py)
            }
            starPath.close()
            drawPath(starPath, c("body", "#FACC15"))
            drawPath(starPath, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            // Eyes & Cheeks
            drawCircle(Color(0xFF1E293B), radius = 5 * scale, center = Offset(cx - 16 * scale, cy - 4 * scale))
            drawCircle(Color(0xFF1E293B), radius = 5 * scale, center = Offset(cx + 16 * scale, cy - 4 * scale))
            drawCircle(c("cheeks", "#FB7185"), radius = 6 * scale, center = Offset(cx - 25 * scale, cy + 8 * scale))
            drawCircle(c("cheeks", "#FB7185"), radius = 6 * scale, center = Offset(cx + 25 * scale, cy + 8 * scale))
        }
        "bus" -> {
            // Bus Body Box
            drawRoundRect(c("body", "#FBBF24"), topLeft = Offset(cx - 95 * scale, cy - 40 * scale), size = Size(190 * scale, 85 * scale), cornerRadius = CornerRadius(16 * scale))
            drawRoundRect(Color(0xFF1E293B), topLeft = Offset(cx - 95 * scale, cy - 40 * scale), size = Size(190 * scale, 85 * scale), cornerRadius = CornerRadius(16 * scale), style = Stroke(4.dp.toPx()))

            // Windows Strip
            drawRoundRect(c("windows", "#BAE6FD"), topLeft = Offset(cx - 85 * scale, cy - 32 * scale), size = Size(45 * scale, 28 * scale), cornerRadius = CornerRadius(6 * scale))
            drawRoundRect(Color(0xFF1E293B), topLeft = Offset(cx - 85 * scale, cy - 32 * scale), size = Size(45 * scale, 28 * scale), cornerRadius = CornerRadius(6 * scale), style = Stroke(2.5.dp.toPx()))
            drawRoundRect(c("windows", "#BAE6FD"), topLeft = Offset(cx - 30 * scale, cy - 32 * scale), size = Size(45 * scale, 28 * scale), cornerRadius = CornerRadius(6 * scale))
            drawRoundRect(Color(0xFF1E293B), topLeft = Offset(cx - 30 * scale, cy - 32 * scale), size = Size(45 * scale, 28 * scale), cornerRadius = CornerRadius(6 * scale), style = Stroke(2.5.dp.toPx()))
            drawRoundRect(c("windows", "#BAE6FD"), topLeft = Offset(cx + 25 * scale, cy - 32 * scale), size = Size(60 * scale, 28 * scale), cornerRadius = CornerRadius(6 * scale))
            drawRoundRect(Color(0xFF1E293B), topLeft = Offset(cx + 25 * scale, cy - 32 * scale), size = Size(60 * scale, 28 * scale), cornerRadius = CornerRadius(6 * scale), style = Stroke(2.5.dp.toPx()))

            // Stripe
            drawRect(c("stripe", "#F59E0B"), topLeft = Offset(cx - 93 * scale, cy + 8 * scale), size = Size(186 * scale, 14 * scale))

            // Wheels
            drawCircle(c("wheels", "#334155"), radius = 20 * scale, center = Offset(cx - 55 * scale, cy + 45 * scale))
            drawCircle(Color(0xFF1E293B), radius = 20 * scale, center = Offset(cx - 55 * scale, cy + 45 * scale), style = Stroke(4.dp.toPx()))
            drawCircle(Color.LightGray, radius = 8 * scale, center = Offset(cx - 55 * scale, cy + 45 * scale))

            drawCircle(c("wheels", "#334155"), radius = 20 * scale, center = Offset(cx + 55 * scale, cy + 45 * scale))
            drawCircle(Color(0xFF1E293B), radius = 20 * scale, center = Offset(cx + 55 * scale, cy + 45 * scale), style = Stroke(4.dp.toPx()))
            drawCircle(Color.LightGray, radius = 8 * scale, center = Offset(cx + 55 * scale, cy + 45 * scale))
        }
        "fox" -> {
            // Ears
            val leftEar = Path().apply {
                moveTo(cx - 60 * scale, cy - 25 * scale)
                lineTo(cx - 48 * scale, cy - 85 * scale)
                lineTo(cx - 15 * scale, cy - 35 * scale)
                close()
            }
            drawPath(leftEar, c("ears", "#C2410C"))
            drawPath(leftEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            val rightEar = Path().apply {
                moveTo(cx + 15 * scale, cy - 35 * scale)
                lineTo(cx + 48 * scale, cy - 85 * scale)
                lineTo(cx + 60 * scale, cy - 25 * scale)
                close()
            }
            drawPath(rightEar, c("ears", "#C2410C"))
            drawPath(rightEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            // Head
            drawCircle(c("head", "#EA580C"), radius = 58 * scale, center = Offset(cx, cy))
            drawCircle(Color(0xFF1E293B), radius = 58 * scale, center = Offset(cx, cy), style = Stroke(4.dp.toPx()))

            // Cheeks
            val leftCheek = Path().apply {
                moveTo(cx - 55 * scale, cy)
                cubicTo(cx - 40 * scale, cy + 40 * scale, cx, cy + 45 * scale, cx, cy + 25 * scale)
                close()
            }
            drawPath(leftCheek, c("cheeks", "#FFFFFF"))
            val rightCheek = Path().apply {
                moveTo(cx + 55 * scale, cy)
                cubicTo(cx + 40 * scale, cy + 40 * scale, cx, cy + 45 * scale, cx, cy + 25 * scale)
                close()
            }
            drawPath(rightCheek, c("cheeks", "#FFFFFF"))

            // Eyes & Nose
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx - 24 * scale, cy - 8 * scale))
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx + 24 * scale, cy - 8 * scale))
            drawCircle(c("nose", "#1E293B"), radius = 8 * scale, center = Offset(cx, cy + 25 * scale))
        }
        "owl" -> {
            // Body
            drawOval(c("body", "#854D0E"), topLeft = Offset(cx - 60 * scale, cy - 65 * scale), size = Size(120 * scale, 130 * scale))
            drawOval(Color(0xFF1E293B), topLeft = Offset(cx - 60 * scale, cy - 65 * scale), size = Size(120 * scale, 130 * scale), style = Stroke(4.dp.toPx()))

            // Belly
            drawOval(c("belly", "#FEF3C7"), topLeft = Offset(cx - 40 * scale, cy - 10 * scale), size = Size(80 * scale, 65 * scale))
            drawOval(Color(0xFF1E293B), topLeft = Offset(cx - 40 * scale, cy - 10 * scale), size = Size(80 * scale, 65 * scale), style = Stroke(2.5.dp.toPx()))

            // Wings
            drawArc(c("wings", "#A16207"), startAngle = 100f, sweepAngle = 140f, useCenter = true, topLeft = Offset(cx - 75 * scale, cy - 25 * scale), size = Size(40 * scale, 80 * scale))
            drawArc(c("wings", "#A16207"), startAngle = -60f, sweepAngle = 140f, useCenter = true, topLeft = Offset(cx + 35 * scale, cy - 25 * scale), size = Size(40 * scale, 80 * scale))

            // Eyes & Beak
            drawCircle(c("eyes", "#FDE047"), radius = 18 * scale, center = Offset(cx - 24 * scale, cy - 25 * scale))
            drawCircle(Color(0xFF1E293B), radius = 18 * scale, center = Offset(cx - 24 * scale, cy - 25 * scale), style = Stroke(3.dp.toPx()))
            drawCircle(Color(0xFF1E293B), radius = 7 * scale, center = Offset(cx - 24 * scale, cy - 25 * scale))

            drawCircle(c("eyes", "#FDE047"), radius = 18 * scale, center = Offset(cx + 24 * scale, cy - 25 * scale))
            drawCircle(Color(0xFF1E293B), radius = 18 * scale, center = Offset(cx + 24 * scale, cy - 25 * scale), style = Stroke(3.dp.toPx()))
            drawCircle(Color(0xFF1E293B), radius = 7 * scale, center = Offset(cx + 24 * scale, cy - 25 * scale))

            val beak = Path().apply {
                moveTo(cx - 8 * scale, cy - 14 * scale)
                lineTo(cx + 8 * scale, cy - 14 * scale)
                lineTo(cx, cy - 2 * scale)
                close()
            }
            drawPath(beak, c("beak", "#F97316"))
        }
        "pig" -> {
            // Ears
            val leftEar = Path().apply {
                moveTo(cx - 55 * scale, cy - 30 * scale)
                lineTo(cx - 50 * scale, cy - 75 * scale)
                lineTo(cx - 20 * scale, cy - 45 * scale)
                close()
            }
            drawPath(leftEar, c("ears", "#F43F5E"))
            drawPath(leftEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            val rightEar = Path().apply {
                moveTo(cx + 20 * scale, cy - 45 * scale)
                lineTo(cx + 50 * scale, cy - 75 * scale)
                lineTo(cx + 55 * scale, cy - 30 * scale)
                close()
            }
            drawPath(rightEar, c("ears", "#F43F5E"))
            drawPath(rightEar, Color(0xFF1E293B), style = Stroke(4.dp.toPx()))

            // Head
            drawCircle(c("head", "#FDA4AF"), radius = 58 * scale, center = Offset(cx, cy))
            drawCircle(Color(0xFF1E293B), radius = 58 * scale, center = Offset(cx, cy), style = Stroke(4.dp.toPx()))

            // Snout
            drawOval(c("snout", "#FB7185"), topLeft = Offset(cx - 25 * scale, cy + 2 * scale), size = Size(50 * scale, 34 * scale))
            drawOval(Color(0xFF1E293B), topLeft = Offset(cx - 25 * scale, cy + 2 * scale), size = Size(50 * scale, 34 * scale), style = Stroke(3.dp.toPx()))
            drawCircle(Color(0xFF1E293B), radius = 4 * scale, center = Offset(cx - 10 * scale, cy + 19 * scale))
            drawCircle(Color(0xFF1E293B), radius = 4 * scale, center = Offset(cx + 10 * scale, cy + 19 * scale))

            // Eyes & Cheeks
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx - 26 * scale, cy - 14 * scale))
            drawCircle(Color(0xFF1E293B), radius = 6 * scale, center = Offset(cx + 26 * scale, cy - 14 * scale))
            drawCircle(c("cheeks", "#F43F5E"), radius = 8 * scale, center = Offset(cx - 38 * scale, cy + 8 * scale))
            drawCircle(c("cheeks", "#F43F5E"), radius = 8 * scale, center = Offset(cx + 38 * scale, cy + 8 * scale))
        }
        else -> {
            // Rich multi-part vector canvas for all words
            val primaryColor = word.color.ifEmpty { "#3B82F6" }

            // Outer frame / decorative layer
            drawCircle(c("frame", "#FEF3C7"), radius = 76 * scale, center = Offset(cx, cy))
            drawCircle(Color(0xFF1E293B), radius = 76 * scale, center = Offset(cx, cy), style = Stroke(4.dp.toPx()))

            // Accent upper/lower parts
            drawArc(c("accent", "#FDE047"), startAngle = 180f, sweepAngle = 180f, useCenter = true, topLeft = Offset(cx - 68 * scale, cy - 68 * scale), size = Size(136 * scale, 136 * scale))

            // Main Body Circle
            drawCircle(c("body", primaryColor), radius = 54 * scale, center = Offset(cx, cy))
            drawCircle(Color(0xFF1E293B), radius = 54 * scale, center = Offset(cx, cy), style = Stroke(3.5.dp.toPx()))

            // Center Belly / Badge
            drawCircle(c("belly", "#FFFFFF"), radius = 34 * scale, center = Offset(cx, cy))
            drawCircle(Color(0xFF1E293B), radius = 34 * scale, center = Offset(cx, cy), style = Stroke(2.5.dp.toPx()))

            // Cheeks
            drawCircle(c("cheeks", "#FDA4AF"), radius = 6 * scale, center = Offset(cx - 20 * scale, cy + 6 * scale))
            drawCircle(c("cheeks", "#FDA4AF"), radius = 6 * scale, center = Offset(cx + 20 * scale, cy + 6 * scale))
        }
    }
}

fun detectTappedPart(
    word: WordItem,
    offset: Offset,
    w: Float,
    h: Float
): String {
    val cx = w / 2f
    val cy = h / 2f
    val scale = (w / 260f).coerceAtMost(h / 260f)
    val dx = offset.x - cx
    val dy = offset.y - cy
    val dist = kotlin.math.sqrt(dx * dx + dy * dy)

    return when (word.id) {
        "cat" -> {
            if (dy < -20 * scale) "ears"
            else if (kotlin.math.abs(dx) > 28 * scale && kotlin.math.abs(dy) < 28 * scale) "cheeks"
            else if (dist < 18 * scale) "nose"
            else "head"
        }
        "dog" -> {
            if (kotlin.math.abs(dx) >= 35 * scale && dy >= -50 * scale && dy <= 45 * scale) "ears"
            else if (dy > 8 * scale && dy < 32 * scale && kotlin.math.abs(dx) < 18 * scale) "tongue"
            else if (dy >= -15 * scale && dy <= 22 * scale && kotlin.math.abs(dx) < 35 * scale) "snout"
            else if (dy > 20 * scale) "body"
            else "head"
        }
        "sun" -> {
            if (dist > 48 * scale) "rays"
            else if (kotlin.math.abs(dx) > 20 * scale && dy > 0) "cheeks"
            else "body"
        }
        "car" -> {
            if (dy > 30 * scale) {
                val dxL = offset.x - (cx - 50 * scale)
                val dyL = offset.y - (cy + 55 * scale)
                val dxR = offset.x - (cx + 50 * scale)
                val dyR = offset.y - (cy + 55 * scale)
                if (kotlin.math.sqrt(dxL * dxL + dyL * dyL) < 12 * scale || kotlin.math.sqrt(dxR * dxR + dyR * dyR) < 12 * scale) "hubcaps"
                else "wheels"
            } else if (dy < 0 && dy > -32 * scale && kotlin.math.abs(dx) < 50 * scale) "windows"
            else "body"
        }
        "bus" -> {
            if (dy > 25 * scale) "wheels"
            else if (dy < 0 && dy > -35 * scale) "windows"
            else if (dy >= 0 && dy < 20 * scale) "stripe"
            else "body"
        }
        "apple" -> {
            if (dy < -35 * scale) {
                if (dx > 0) "leaf" else "stem"
            } else if (kotlin.math.abs(dx) > 22 * scale && dy > 8 * scale) "cheeks"
            else "body"
        }
        "star" -> {
            if (kotlin.math.abs(dx) > 14 * scale && dy > 0 && dist < 35 * scale) "cheeks"
            else "body"
        }
        "fox" -> {
            if (dy < -20 * scale) "ears"
            else if (dist < 16 * scale) "nose"
            else if (kotlin.math.abs(dx) > 22 * scale) "cheeks"
            else "head"
        }
        "owl" -> {
            if (dist < 22 * scale && dy < 0) "beak"
            else if (kotlin.math.abs(dx) > 38 * scale) "wings"
            else if (dy > 8 * scale && dist < 35 * scale) "belly"
            else "body"
        }
        "pig" -> {
            if (dy < -25 * scale) "ears"
            else if (dist < 25 * scale) "snout"
            else if (kotlin.math.abs(dx) > 28 * scale) "cheeks"
            else "head"
        }
        else -> {
            val parts = word.defaultParts.keys.toList()
            if (parts.isEmpty()) {
                if (dist > 54 * scale) "frame"
                else if (dist > 34 * scale) "body"
                else "belly"
            } else if (parts.size == 1) {
                parts.first()
            } else {
                if (dist > 55 * scale) {
                    parts.find { it in listOf("frame", "accent", "rays", "wings", "tail", "ears", "leaf", "hubcaps") } ?: parts.last()
                } else if (dy < -25 * scale) {
                    parts.find { it in listOf("head", "ears", "stem", "roof", "eyes", "horns", "windows") } ?: parts.first()
                } else if (dy > 25 * scale) {
                    parts.find { it in listOf("body", "belly", "wheels", "paws", "legs", "stripe", "tail") } ?: parts.first()
                } else if (dist < 25 * scale) {
                    parts.find { it in listOf("snout", "nose", "beak", "windows", "tongue", "core", "belly", "head", "body") } ?: parts.first()
                } else if (kotlin.math.abs(dx) > 25 * scale) {
                    parts.find { it in listOf("cheeks", "wings", "patch", "lights", "accent", "ears") } ?: parts.first()
                } else {
                    parts.first()
                }
            }
        }
    }
}
