package com.example.kidsenglishlab.ui.screens

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.automirrored.filled.Undo
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Brush
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.FormatColorFill
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RemoveRedEye
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsenglishlab.audio.SoundEffectsService
import com.example.kidsenglishlab.audio.SpeechService
import com.example.kidsenglishlab.data.CategoryType
import com.example.kidsenglishlab.data.ColorOption
import com.example.kidsenglishlab.data.ColorPalette
import com.example.kidsenglishlab.data.ThemeConfig
import com.example.kidsenglishlab.data.WordItem
import com.example.kidsenglishlab.data.WordsData
import com.example.kidsenglishlab.ui.components.ConfettiEffect
import com.example.kidsenglishlab.ui.components.PuzzleVectorCanvas
import com.example.kidsenglishlab.ui.components.detectTappedPart

enum class StudioTool {
    BUCKET, // Boya Kovası - Tıklanan alanı tek tıkla doldurur
    BRUSH,  // Fırça - Serbest çizim
    ERASER  // Silgi - Parçaları temizler
}

sealed class StudioAction {
    data class BucketFill(val part: String, val previousColor: String?) : StudioAction()
    data class FreehandStroke(val stroke: DrawingPath) : StudioAction()
}

data class DrawingPath(
    val points: List<Offset>,
    val color: Color,
    val strokeWidth: Float
)

@Composable
fun ColoringStudioScreen(
    theme: ThemeConfig,
    initialWord: WordItem?,
    soundService: SoundEffectsService,
    speechService: SpeechService,
    onNavigateBack: () -> Unit,
    onGoToGame: (WordItem) -> Unit
) {
    var selectedCategory by remember { mutableStateOf(CategoryType.ALL) }
    val filteredWords = remember(selectedCategory) {
        if (selectedCategory == CategoryType.ALL) WordsData.words
        else WordsData.words.filter { it.category == selectedCategory }
    }

    var currentWord by remember { mutableStateOf(initialWord ?: WordsData.words.first()) }
    var selectedPaintColor by remember { mutableStateOf("#EF4444") }
    var currentTool by remember { mutableStateOf(StudioTool.BUCKET) }
    var showGuide by remember { mutableStateOf(false) }
    var showMagicConfetti by remember { mutableStateOf(false) }

    val userPartsColor = remember(currentWord.id) { mutableStateMapOf<String, String>() }
    val drawingPaths = remember(currentWord.id) { mutableStateListOf<DrawingPath>() }
    val actionHistory = remember(currentWord.id) { mutableStateListOf<StudioAction>() }
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }

    fun handleFillPart(partKey: String) {
        val previousColor = userPartsColor[partKey]
        if (currentTool == StudioTool.ERASER) {
            userPartsColor.remove(partKey)
            actionHistory.add(StudioAction.BucketFill(partKey, previousColor))
            soundService.playPopTone()
        } else {
            userPartsColor[partKey] = selectedPaintColor
            actionHistory.add(StudioAction.BucketFill(partKey, previousColor))
            soundService.playPaintSplashTone()
            val colorObj = ColorPalette.colors.find { it.hex.equals(selectedPaintColor, ignoreCase = true) }
            if (colorObj != null) {
                speechService.speakColor(colorObj.nameEn, colorObj.nameTr)
            }
        }
    }

    fun handleMagicPaint() {
        val palette = listOf("#EF4444", "#3B82F6", "#10B981", "#F59E0B", "#8B5CF6", "#EC4899", "#14B8A6", "#06B6D4", "#F97316")
        val parts = (currentWord.defaultParts.keys + listOf("head", "body", "ears", "eyes", "feet", "details", "tail", "wings", "background", "beak", "snout", "tongue", "cheeks", "paws", "wheels", "windows")).distinct()
        parts.forEachIndexed { idx, p ->
            val clr = palette[idx % palette.size]
            userPartsColor[p] = clr
        }
        soundService.playFanfare()
        showMagicConfetti = true
        speechService.speakWordPair(currentWord.english, currentWord.turkish)
    }

    fun handleUndo() {
        val lastAction = actionHistory.removeLastOrNull()
        if (lastAction != null) {
            when (lastAction) {
                is StudioAction.BucketFill -> {
                    if (lastAction.previousColor != null) {
                        userPartsColor[lastAction.part] = lastAction.previousColor
                    } else {
                        userPartsColor.remove(lastAction.part)
                    }
                }
                is StudioAction.FreehandStroke -> {
                    drawingPaths.remove(lastAction.stroke)
                }
            }
            soundService.playPopTone()
        } else if (drawingPaths.isNotEmpty()) {
            drawingPaths.removeLastOrNull()
            soundService.playPopTone()
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .testTag("coloring_studio_screen")
    ) {
        val isLandscapeOrTablet = maxWidth >= 650.dp || (maxWidth > maxHeight && maxHeight < 600.dp)

        if (isLandscapeOrTablet) {
            // Adaptive Landscape / Tablet Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Column: Top Bar + Canvas
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    StudioTopBar(
                        currentWord = currentWord,
                        showGuide = showGuide,
                        onNavigateBack = onNavigateBack,
                        onSpeakWord = { speechService.speakWordPair(currentWord.english, currentWord.turkish) },
                        onToggleGuide = {
                            showGuide = !showGuide
                            soundService.playPopTone()
                        },
                        onMagicPaint = { handleMagicPaint() }
                    )

                    StudioCanvasWithNav(
                        currentWord = currentWord,
                        filteredWords = filteredWords,
                        currentTool = currentTool,
                        showGuide = showGuide,
                        selectedPaintColor = selectedPaintColor,
                        userPartsColor = userPartsColor,
                        drawingPaths = drawingPaths,
                        canvasSize = 175.dp,
                        onSelectWord = { w ->
                            currentWord = w
                            soundService.playPopTone()
                            speechService.speakEnglish(w.english)
                        },
                        onFillPart = { handleFillPart(it) },
                        onDrawStroke = { stroke ->
                            drawingPaths.add(stroke)
                            actionHistory.add(StudioAction.FreehandStroke(stroke))
                        },
                        onStartStroke = { soundService.playPaintSplashTone() }
                    )
                }

                // Right Column: Word Strip + Tools + Palette
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    StudioWordSelectorStrip(
                        words = filteredWords,
                        currentWord = currentWord,
                        onSelectWord = { w ->
                            currentWord = w
                            soundService.playPopTone()
                            speechService.speakEnglish(w.english)
                        }
                    )

                    StudioToolsRow(
                        currentTool = currentTool,
                        currentWord = currentWord,
                        onSelectTool = { tool ->
                            currentTool = tool
                            soundService.playPopTone()
                        },
                        onUndo = { handleUndo() },
                        onClear = {
                            drawingPaths.clear()
                            userPartsColor.clear()
                            actionHistory.clear()
                            soundService.playWrongTone()
                        },
                        onPlayWord = { onGoToGame(currentWord) }
                    )

                    StudioColorPaletteCard(
                        selectedPaintColor = selectedPaintColor,
                        onColorSelected = { c ->
                            selectedPaintColor = c.hex
                            soundService.playPopTone()
                            speechService.speakColor(c.nameEn, c.nameTr)
                        }
                    )
                }
            }
        } else {
            // Standard Portrait Phone Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                StudioTopBar(
                    currentWord = currentWord,
                    showGuide = showGuide,
                    onNavigateBack = onNavigateBack,
                    onSpeakWord = { speechService.speakWordPair(currentWord.english, currentWord.turkish) },
                    onToggleGuide = {
                        showGuide = !showGuide
                        soundService.playPopTone()
                    },
                    onMagicPaint = { handleMagicPaint() }
                )

                StudioWordSelectorStrip(
                    words = filteredWords,
                    currentWord = currentWord,
                    onSelectWord = { w ->
                        currentWord = w
                        soundService.playPopTone()
                        speechService.speakEnglish(w.english)
                    }
                )

                StudioCanvasWithNav(
                    currentWord = currentWord,
                    filteredWords = filteredWords,
                    currentTool = currentTool,
                    showGuide = showGuide,
                    selectedPaintColor = selectedPaintColor,
                    userPartsColor = userPartsColor,
                    drawingPaths = drawingPaths,
                    canvasSize = 200.dp,
                    onSelectWord = { w ->
                        currentWord = w
                        soundService.playPopTone()
                        speechService.speakEnglish(w.english)
                    },
                    onFillPart = { handleFillPart(it) },
                    onDrawStroke = { stroke ->
                        drawingPaths.add(stroke)
                        actionHistory.add(StudioAction.FreehandStroke(stroke))
                    },
                    onStartStroke = { soundService.playPaintSplashTone() }
                )

                StudioToolsRow(
                    currentTool = currentTool,
                    currentWord = currentWord,
                    onSelectTool = { tool ->
                        currentTool = tool
                        soundService.playPopTone()
                    },
                    onUndo = { handleUndo() },
                    onClear = {
                        drawingPaths.clear()
                        userPartsColor.clear()
                        actionHistory.clear()
                        soundService.playWrongTone()
                    },
                    onPlayWord = { onGoToGame(currentWord) }
                )

                StudioColorPaletteCard(
                    selectedPaintColor = selectedPaintColor,
                    onColorSelected = { c ->
                        selectedPaintColor = c.hex
                        soundService.playPopTone()
                        speechService.speakColor(c.nameEn, c.nameTr)
                    }
                )
            }
        }

        if (showMagicConfetti) {
            ConfettiEffect()
        }
    }
}

@Composable
private fun StudioTopBar(
    currentWord: WordItem,
    showGuide: Boolean,
    onNavigateBack: () -> Unit,
    onSpeakWord: () -> Unit,
    onToggleGuide: () -> Unit,
    onMagicPaint: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .testTag("studio_back_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color(0xFF334155))
        }

        // Word Header Title
        Surface(
            onClick = onSpeakWord,
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0))
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentWord.emoji} ${currentWord.english} (${currentWord.turkish})",
                    fontWeight = FontWeight.Black,
                    fontSize = 14.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Icon(
                    Icons.Default.VolumeUp,
                    contentDescription = "Seslendir",
                    tint = Color(0xFF059669),
                    modifier = Modifier.size(18.dp)
                )
            }
        }

        // Magic Fill & Guide Tools
        Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
            IconButton(
                onClick = onToggleGuide,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (showGuide) Color(0xFFFEF3C7) else Color.White)
                    .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    Icons.Default.RemoveRedEye,
                    contentDescription = "Kılavuz",
                    tint = if (showGuide) Color(0xFFD97706) else Color(0xFF64748B),
                    modifier = Modifier.size(19.dp)
                )
            }

            IconButton(
                onClick = onMagicPaint,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3E8FF))
                    .border(1.5.dp, Color(0xFFD8B4FE), RoundedCornerShape(12.dp))
                    .testTag("magic_paint_button")
            ) {
                Icon(
                    Icons.Default.AutoAwesome,
                    contentDescription = "Sihirli Boya",
                    tint = Color(0xFFA855F7),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun StudioWordSelectorStrip(
    words: List<WordItem>,
    currentWord: WordItem,
    onSelectWord: (WordItem) -> Unit
) {
    LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.spacedBy(5.dp)
    ) {
        items(words) { w ->
            val isSelected = w.id == currentWord.id
            Surface(
                onClick = { onSelectWord(w) },
                shape = RoundedCornerShape(14.dp),
                color = if (isSelected) Color(0xFFA855F7) else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    1.5.dp,
                    if (isSelected) Color(0xFF7E22CE) else Color(0xFFE2E8F0)
                ),
                modifier = Modifier.padding(vertical = 2.dp)
            ) {
                Row(
                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(text = w.emoji, fontSize = 15.sp)
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(
                        text = w.english,
                        fontSize = 12.sp,
                        fontWeight = FontWeight.Bold,
                        color = if (isSelected) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioCanvasWithNav(
    currentWord: WordItem,
    filteredWords: List<WordItem>,
    currentTool: StudioTool,
    showGuide: Boolean,
    selectedPaintColor: String,
    userPartsColor: Map<String, String>,
    drawingPaths: List<DrawingPath>,
    canvasSize: androidx.compose.ui.unit.Dp,
    onSelectWord: (WordItem) -> Unit,
    onFillPart: (String) -> Unit,
    onDrawStroke: (DrawingPath) -> Unit,
    onStartStroke: () -> Unit
) {
    val currentIndex = filteredWords.indexOfFirst { it.id == currentWord.id }.let { if (it == -1) 0 else it }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Previous Item Triangle Button (Left Arrow)
        IconButton(
            onClick = {
                val prevIndex = if (currentIndex > 0) currentIndex - 1 else filteredWords.size - 1
                if (filteredWords.isNotEmpty()) {
                    onSelectWord(filteredWords[prevIndex])
                }
            },
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFFD8B4FE), CircleShape)
                .shadow(3.dp, CircleShape)
                .testTag("studio_prev_button")
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = "Önceki Nesne",
                tint = Color(0xFF7E22CE),
                modifier = Modifier.size(24.dp)
            )
        }

        Spacer(modifier = Modifier.width(8.dp))

        // Center Artwork Canvas Card
        StudioCanvasCard(
            currentWord = currentWord,
            currentTool = currentTool,
            showGuide = showGuide,
            selectedPaintColor = selectedPaintColor,
            userPartsColor = userPartsColor,
            drawingPaths = drawingPaths,
            canvasSize = canvasSize,
            onFillPart = onFillPart,
            onDrawStroke = onDrawStroke,
            onStartStroke = onStartStroke
        )

        Spacer(modifier = Modifier.width(8.dp))

        // Next Item Triangle Button (Right Arrow)
        IconButton(
            onClick = {
                val nextIndex = if (currentIndex < filteredWords.size - 1) currentIndex + 1 else 0
                if (filteredWords.isNotEmpty()) {
                    onSelectWord(filteredWords[nextIndex])
                }
            },
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFA855F7))
                .border(2.dp, Color(0xFF7E22CE), CircleShape)
                .shadow(3.dp, CircleShape)
                .testTag("studio_next_button")
        ) {
            Icon(
                Icons.AutoMirrored.Filled.ArrowForward,
                contentDescription = "Sonraki Nesne",
                tint = Color.White,
                modifier = Modifier.size(24.dp)
            )
        }
    }
}

@Composable
private fun StudioCanvasCard(
    currentWord: WordItem,
    currentTool: StudioTool,
    showGuide: Boolean,
    selectedPaintColor: String,
    userPartsColor: Map<String, String>,
    drawingPaths: List<DrawingPath>,
    canvasSize: androidx.compose.ui.unit.Dp,
    onFillPart: (String) -> Unit,
    onDrawStroke: (DrawingPath) -> Unit,
    onStartStroke: () -> Unit
) {
    var currentPathPoints by remember { mutableStateOf<List<Offset>>(emptyList()) }
    val activeColor = if (currentTool == StudioTool.ERASER) Color.White else ColorPalette.colors.find { it.hex.equals(selectedPaintColor, ignoreCase = true) }?.color ?: Color.Red
    val brushWidth = if (currentTool == StudioTool.ERASER) 32f else 16f

    Card(
        modifier = Modifier
            .size(canvasSize)
            .aspectRatio(1f)
            .padding(2.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White),
        border = androidx.compose.foundation.BorderStroke(3.dp, Color(0xFFD8B4FE)),
        elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            // Vector Art Base with Outlines
            PuzzleVectorCanvas(
                word = currentWord,
                revealedIndices = currentWord.english.indices.toList(),
                userColors = userPartsColor,
                isCompleted = true,
                showGuide = showGuide,
                outlineOnly = true,
                onPartClick = { part -> onFillPart(part) }
            )

            // Interactive Touch Canvas Layer (Supports Tap to Flood-Fill & Drag to Brush)
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .pointerInput(currentWord.id, currentTool, selectedPaintColor) {
                        if (currentTool == StudioTool.BUCKET) {
                            detectTapGestures { offset ->
                                val tappedPart = detectTappedPart(currentWord, offset, size.width.toFloat(), size.height.toFloat())
                                onFillPart(tappedPart)
                            }
                        } else {
                            detectDragGestures(
                                onDragStart = { offset ->
                                    currentPathPoints = listOf(offset)
                                    onStartStroke()
                                },
                                onDrag = { change, _ ->
                                    change.consume()
                                    currentPathPoints = currentPathPoints + change.position
                                },
                                onDragEnd = {
                                    if (currentPathPoints.isNotEmpty()) {
                                        val newPath = DrawingPath(
                                            points = currentPathPoints,
                                            color = activeColor,
                                            strokeWidth = brushWidth
                                        )
                                        onDrawStroke(newPath)
                                        currentPathPoints = emptyList()
                                    }
                                }
                            )
                        }
                    }
            ) {
                drawingPaths.forEach { dp ->
                    if (dp.points.size > 1) {
                        val path = Path().apply {
                            moveTo(dp.points.first().x, dp.points.first().y)
                            for (i in 1 until dp.points.size) {
                                lineTo(dp.points[i].x, dp.points[i].y)
                            }
                        }
                        drawPath(
                            path = path,
                            color = dp.color,
                            style = Stroke(
                                width = dp.strokeWidth,
                                cap = StrokeCap.Round,
                                join = StrokeJoin.Round
                            )
                        )
                    }
                }

                if (currentPathPoints.size > 1) {
                    val path = Path().apply {
                        moveTo(currentPathPoints.first().x, currentPathPoints.first().y)
                        for (i in 1 until currentPathPoints.size) {
                            lineTo(currentPathPoints[i].x, currentPathPoints[i].y)
                        }
                    }
                    drawPath(
                        path = path,
                        color = activeColor,
                        style = Stroke(
                            width = brushWidth,
                            cap = StrokeCap.Round,
                            join = StrokeJoin.Round
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun StudioToolsRow(
    currentTool: StudioTool,
    currentWord: WordItem,
    onSelectTool: (StudioTool) -> Unit,
    onUndo: () -> Unit,
    onClear: () -> Unit,
    onPlayWord: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 1. Bucket tool
        val isBucketSelected = currentTool == StudioTool.BUCKET
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(if (isBucketSelected) Color(0xFF7E22CE) else Color(0xFF94A3B8))
                .clickable { onSelectTool(StudioTool.BUCKET) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isBucketSelected) Color(0xFFA855F7) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.FormatColorFill,
                    contentDescription = "Boya Kovası",
                    tint = if (isBucketSelected) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 2. Brush tool
        val isBrushSelected = currentTool == StudioTool.BRUSH
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(if (isBrushSelected) Color(0xFF7E22CE) else Color(0xFF94A3B8))
                .clickable { onSelectTool(StudioTool.BRUSH) }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (isBrushSelected) Color(0xFFA855F7) else Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.Brush,
                    contentDescription = "Fırça",
                    tint = if (isBrushSelected) Color.White else Color(0xFF64748B),
                    modifier = Modifier.size(22.dp)
                )
            }
        }

        // 3. Undo Tool
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFF94A3B8))
                .clickable { onUndo() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF1F5F9)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.AutoMirrored.Filled.Undo, contentDescription = "Geri Al", tint = Color(0xFF64748B), modifier = Modifier.size(22.dp))
            }
        }

        // 4. Clear Tool
        Box(
            modifier = Modifier
                .size(42.dp)
                .shadow(2.dp, RoundedCornerShape(12.dp))
                .clip(RoundedCornerShape(12.dp))
                .background(Color(0xFFDC2626))
                .clickable { onClear() }
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(38.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFFEE2E2)),
                contentAlignment = Alignment.Center
            ) {
                Icon(Icons.Default.Delete, contentDescription = "Temizle", tint = Color(0xFFEF4444), modifier = Modifier.size(22.dp))
            }
        }

        // 5. Play Game Button
        Box(
            modifier = Modifier
                .height(44.dp)
                .shadow(3.dp, RoundedCornerShape(14.dp))
                .clip(RoundedCornerShape(14.dp))
                .background(Color(0xFFB45309))
                .clickable { onPlayWord() }
        ) {
            Box(
                modifier = Modifier
                    .height(39.dp)
                    .clip(RoundedCornerShape(14.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFBBF24), Color(0xFFF59E0B))
                        )
                    )
                    .padding(horizontal = 12.dp),
                contentAlignment = Alignment.Center
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.PlayArrow, contentDescription = null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Kelimeyi Oyna", fontWeight = FontWeight.Black, color = Color.White, fontSize = 13.sp)
                }
            }
        }
    }
}

@Composable
private fun StudioColorPaletteCard(
    selectedPaintColor: String,
    onColorSelected: (ColorOption) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(18.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.95f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFD8B4FE))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 8.dp, vertical = 5.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎨 BOYAMA PALETİ / COLOR PALETTE",
                fontSize = 10.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF64748B)
            )
            Spacer(modifier = Modifier.height(3.dp))

            // Row 1 - Centered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorPalette.colors.take(7).forEach { c ->
                    val isSelected = selectedPaintColor.equals(c.hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 32.dp else 26.dp)
                            .clip(CircleShape)
                            .background(c.color)
                            .border(
                                if (isSelected) 3.dp else 1.5.dp,
                                if (isSelected) Color(0xFFA855F7) else Color.White,
                                CircleShape
                            )
                            .clickable { onColorSelected(c) }
                    )
                }
            }

            Spacer(modifier = Modifier.height(3.dp))

            // Row 2 - Centered
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                ColorPalette.colors.drop(7).forEach { c ->
                    val isSelected = selectedPaintColor.equals(c.hex, ignoreCase = true)
                    Box(
                        modifier = Modifier
                            .padding(horizontal = 3.dp)
                            .size(if (isSelected) 32.dp else 26.dp)
                            .clip(CircleShape)
                            .background(c.color)
                            .border(
                                if (isSelected) 3.dp else 1.5.dp,
                                if (isSelected) Color(0xFFA855F7) else Color.White,
                                CircleShape
                            )
                            .clickable { onColorSelected(c) }
                    )
                }
            }
        }
    }
}
