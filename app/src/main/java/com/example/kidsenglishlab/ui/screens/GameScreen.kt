package com.example.kidsenglishlab.ui.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Lightbulb
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsenglishlab.audio.SoundEffectsService
import com.example.kidsenglishlab.audio.SpeechService
import com.example.kidsenglishlab.data.ThemeConfig
import com.example.kidsenglishlab.data.WordItem
import com.example.kidsenglishlab.ui.components.ConfettiEffect
import com.example.kidsenglishlab.ui.components.PuzzleVectorCanvas
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlin.random.Random

@Composable
fun GameScreen(
    theme: ThemeConfig,
    activeWords: List<WordItem>,
    currentIndex: Int,
    soundService: SoundEffectsService,
    speechService: SpeechService,
    onNavigateBack: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenVoices: () -> Unit,
    onOpenCollection: () -> Unit,
    onOpenStudio: () -> Unit,
    onWordCompleted: (WordItem) -> Unit,
    onSelectWordIndex: (Int) -> Unit
) {
    if (activeWords.isEmpty()) return

    val currentWord = activeWords[currentIndex.coerceIn(0, activeWords.size - 1)]
    var revealedIndices by remember(currentWord.id) { mutableStateOf<List<Int>>(emptyList()) }
    val userColors = remember(currentWord.id) { mutableStateMapOf<String, String>() }
    var shakingLetter by remember { mutableStateOf<Char?>(null) }
    var isWordDone by remember(currentWord.id) { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    // Scrambled keyboard letters: current word letters + random distractors
    val keyboardLetters = remember(currentWord.id) {
        val wordChars = currentWord.english.toList()
        val allAlphabet = ('A'..'Z').filter { !wordChars.contains(it) }
        val targetCount = when {
            wordChars.size <= 4 -> 8
            wordChars.size <= 6 -> 10
            wordChars.size <= 8 -> 12
            else -> 14
        }
        val needed = targetCount - wordChars.size
        val distractors = allAlphabet.shuffled().take(needed)
        (wordChars + distractors).shuffled(Random(currentWord.id.hashCode()))
    }

    val isAllRevealed = currentWord.english.indices.all { revealedIndices.contains(it) }

    LaunchedEffect(isAllRevealed) {
        if (isAllRevealed && !isWordDone) {
            isWordDone = true
            soundService.playFanfare()
            speechService.speakWordPair(currentWord.english, currentWord.turkish)
            delay(1200)
            onWordCompleted(currentWord)
        }
    }

    fun handleLetterPress(letter: Char) {
        speechService.speakLetter(letter)
        if (currentWord.english.contains(letter)) {
            soundService.playPopTone()
            val newIndices = mutableListOf<Int>().apply { addAll(revealedIndices) }
            currentWord.english.forEachIndexed { idx, c ->
                if (c == letter && !newIndices.contains(idx)) {
                    newIndices.add(idx)
                }
            }
            revealedIndices = newIndices
        } else {
            soundService.playWrongTone()
            shakingLetter = letter
            coroutineScope.launch {
                delay(400)
                if (shakingLetter == letter) shakingLetter = null
            }
        }
    }

    fun handleHint() {
        if (isAllRevealed) return
        val unrevealed = currentWord.english.indices.filter { !revealedIndices.contains(it) }
        if (unrevealed.isNotEmpty()) {
            val nextIdx = unrevealed.first()
            val targetChar = currentWord.english[nextIdx]
            soundService.playHintTone()
            val newIndices = mutableListOf<Int>().apply { addAll(revealedIndices) }
            currentWord.english.forEachIndexed { idx, c ->
                if (c == targetChar && !newIndices.contains(idx)) {
                    newIndices.add(idx)
                }
            }
            revealedIndices = newIndices
            speechService.speakLetter(targetChar)
        }
    }

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .testTag("game_screen")
    ) {
        val isLandscapeOrTablet = maxWidth >= 650.dp || (maxWidth > maxHeight && maxHeight < 600.dp)

        if (isLandscapeOrTablet) {
            // Adaptive Landscape & Tablet 2-Pane Side-by-Side Layout
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pane: Navigation, Word Translation, Canvas, and Letter Slots
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.SpaceBetween
                ) {
                    GameTopBar(
                        currentIndex = currentIndex,
                        totalWords = activeWords.size,
                        difficulty = currentWord.difficulty,
                        isAllRevealed = isAllRevealed,
                        onNavigateBack = onNavigateBack,
                        onOpenCollection = onOpenCollection,
                        onOpenStudio = onOpenStudio,
                        onHint = { handleHint() }
                    )

                    TurkishTranslationPill(
                        currentWord = currentWord,
                        onSpeak = { speechService.speakTurkish(currentWord.turkish) }
                    )

                    ArtworkCanvasWithNav(
                        currentWord = currentWord,
                        currentIndex = currentIndex,
                        totalWords = activeWords.size,
                        isWordDone = isWordDone,
                        revealedIndices = revealedIndices,
                        userColors = userColors,
                        canvasSize = 160.dp,
                        onSelectWordIndex = onSelectWordIndex,
                        onPieceClick = { pieceIdx ->
                            if (!revealedIndices.contains(pieceIdx)) {
                                val targetChar = currentWord.english.getOrNull(pieceIdx)
                                if (targetChar != null) {
                                    handleLetterPress(targetChar)
                                }
                            }
                        }
                    )

                    SenBoyamakIsterMisinButton(
                        onClick = onOpenStudio
                    )

                    WordProgressSlots(
                        currentWord = currentWord,
                        revealedIndices = revealedIndices,
                        onSlotClick = { char, isFound ->
                            if (isFound) {
                                speechService.speakWordPair(currentWord.english, currentWord.turkish)
                            } else {
                                handleLetterPress(char)
                            }
                        }
                    )
                }

                // Right Pane: Scrambled Keyboard Grid
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxHeight(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    KeyboardLettersGrid(
                        keyboardLetters = keyboardLetters,
                        currentWord = currentWord,
                        revealedIndices = revealedIndices,
                        shakingLetter = shakingLetter,
                        columnsCount = if (keyboardLetters.size >= 12) 6 else 5,
                        keyHeight = 46.dp,
                        onKeyPress = { letter -> handleLetterPress(letter) }
                    )
                }
            }
        } else {
            // Standard Portrait Phone Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 6.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                GameTopBar(
                    currentIndex = currentIndex,
                    totalWords = activeWords.size,
                    difficulty = currentWord.difficulty,
                    isAllRevealed = isAllRevealed,
                    onNavigateBack = onNavigateBack,
                    onOpenCollection = onOpenCollection,
                    onOpenStudio = onOpenStudio,
                    onHint = { handleHint() }
                )

                Spacer(modifier = Modifier.height(2.dp))

                TurkishTranslationPill(
                    currentWord = currentWord,
                    onSpeak = { speechService.speakTurkish(currentWord.turkish) }
                )

                Spacer(modifier = Modifier.height(2.dp))

                // Middle and Bottom Section - Shifted 10% (~24dp) upwards with exact 5dp spacing
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .offset(y = (-24).dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    ArtworkCanvasWithNav(
                        currentWord = currentWord,
                        currentIndex = currentIndex,
                        totalWords = activeWords.size,
                        isWordDone = isWordDone,
                        revealedIndices = revealedIndices,
                        userColors = userColors,
                        canvasSize = 185.dp,
                        onSelectWordIndex = onSelectWordIndex,
                        onPieceClick = { pieceIdx ->
                            if (!revealedIndices.contains(pieceIdx)) {
                                val targetChar = currentWord.english.getOrNull(pieceIdx)
                                if (targetChar != null) {
                                    handleLetterPress(targetChar)
                                }
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(4.dp))

                    SenBoyamakIsterMisinButton(
                        onClick = onOpenStudio
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    WordProgressSlots(
                        currentWord = currentWord,
                        revealedIndices = revealedIndices,
                        onSlotClick = { char, isFound ->
                            if (isFound) {
                                speechService.speakWordPair(currentWord.english, currentWord.turkish)
                            } else {
                                handleLetterPress(char)
                            }
                        }
                    )

                    Spacer(modifier = Modifier.height(5.dp))

                    KeyboardLettersGrid(
                        keyboardLetters = keyboardLetters,
                        currentWord = currentWord,
                        revealedIndices = revealedIndices,
                        shakingLetter = shakingLetter,
                        columnsCount = when {
                            keyboardLetters.size >= 14 -> 7
                            keyboardLetters.size >= 12 -> 6
                            keyboardLetters.size >= 10 -> 5
                            else -> 4
                        },
                        keyHeight = 44.dp,
                        onKeyPress = { letter -> handleLetterPress(letter) }
                    )
                }
            }
        }

        if (isWordDone) {
            ConfettiEffect()
        }
    }
}

@Composable
private fun GameTopBar(
    currentIndex: Int,
    totalWords: Int,
    difficulty: Int,
    isAllRevealed: Boolean,
    onNavigateBack: () -> Unit,
    onOpenCollection: () -> Unit,
    onOpenStudio: () -> Unit,
    onHint: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Back Button
        IconButton(
            onClick = onNavigateBack,
            modifier = Modifier
                .size(40.dp)
                .clip(RoundedCornerShape(14.dp))
                .background(Color.White)
                .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(14.dp))
                .testTag("back_button")
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri", tint = Color(0xFF334155))
        }

        // Word Counter & Stars
        Surface(
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0)),
            shadowElevation = 1.dp
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "${currentIndex + 1} / $totalWords",
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B)
                )
                Spacer(modifier = Modifier.width(6.dp))
                Text(
                    text = "⭐".repeat(difficulty.coerceAtMost(4)),
                    fontSize = 11.sp
                )
            }
        }

        // Shortcuts: Trophy, Studio, Hint
        Row(
            horizontalArrangement = Arrangement.spacedBy(6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(
                onClick = onOpenCollection,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFFDE68A), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.EmojiEvents, contentDescription = "Koleksiyon", tint = Color(0xFFF59E0B), modifier = Modifier.size(19.dp))
            }

            IconButton(
                onClick = onOpenStudio,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFF3E8FF))
                    .border(1.5.dp, Color(0xFFD8B4FE), RoundedCornerShape(12.dp))
            ) {
                Icon(Icons.Default.Palette, contentDescription = "Boyama", tint = Color(0xFFA855F7), modifier = Modifier.size(19.dp))
            }

            IconButton(
                onClick = onHint,
                enabled = !isAllRevealed,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(if (!isAllRevealed) Color(0xFFFEF3C7) else Color(0xFFF1F5F9))
                    .border(1.5.dp, Color(0xFFFCD34D), RoundedCornerShape(12.dp))
                    .testTag("hint_button")
            ) {
                Icon(
                    Icons.Default.Lightbulb,
                    contentDescription = "İpucu",
                    tint = if (!isAllRevealed) Color(0xFFD97706) else Color(0xFF94A3B8),
                    modifier = Modifier.size(19.dp)
                )
            }
        }
    }
}

@Composable
private fun TurkishTranslationPill(
    currentWord: WordItem,
    onSpeak: () -> Unit
) {
    Surface(
        onClick = onSpeak,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFFD1FAE5),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFF6EE7B7)),
        modifier = Modifier.padding(vertical = 2.dp)
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${currentWord.difficultyLabel} • ${currentWord.turkish}",
                fontWeight = FontWeight.Black,
                fontSize = 13.sp,
                color = Color(0xFF065F46)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Icon(
                Icons.Default.VolumeUp,
                contentDescription = "Türkçe Dinle",
                tint = Color(0xFF059669),
                modifier = Modifier.size(18.dp)
            )
        }
    }
}

@Composable
private fun ArtworkCanvasWithNav(
    currentWord: WordItem,
    currentIndex: Int,
    totalWords: Int,
    isWordDone: Boolean,
    revealedIndices: List<Int>,
    userColors: Map<String, String>,
    canvasSize: androidx.compose.ui.unit.Dp,
    onSelectWordIndex: (Int) -> Unit,
    onPieceClick: (Int) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Prev Word Arrow
        IconButton(
            onClick = {
                if (currentIndex > 0) onSelectWordIndex(currentIndex - 1)
                else onSelectWordIndex(totalWords - 1)
            },
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color.White)
                .border(2.dp, Color(0xFFFDE68A), CircleShape)
                .shadow(3.dp, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Önceki", tint = Color(0xFF78350F))
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Artwork Canvas Card (Pure White Canvas)
        Card(
            modifier = Modifier
                .size(canvasSize)
                .aspectRatio(1f)
                .padding(2.dp),
            shape = RoundedCornerShape(24.dp),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            border = androidx.compose.foundation.BorderStroke(
                3.dp,
                if (isWordDone) Color(0xFF10B981) else Color(0xFFFDE68A)
            ),
            elevation = CardDefaults.cardElevation(defaultElevation = 5.dp)
        ) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                PuzzleVectorCanvas(
                    word = currentWord,
                    revealedIndices = revealedIndices,
                    userColors = userColors,
                    isCompleted = isWordDone,
                    onPieceClick = onPieceClick
                )
            }
        }

        Spacer(modifier = Modifier.width(10.dp))

        // Next Word Arrow
        IconButton(
            onClick = {
                if (currentIndex < totalWords - 1) onSelectWordIndex(currentIndex + 1)
                else onSelectWordIndex(0)
            },
            modifier = Modifier
                .size(42.dp)
                .clip(CircleShape)
                .background(Color(0xFFF59E0B))
                .border(2.dp, Color(0xFFD97706), CircleShape)
                .shadow(3.dp, CircleShape)
        ) {
            Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Sonraki", tint = Color.White)
        }
    }
}

@Composable
private fun SenBoyamakIsterMisinButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        color = Color(0xFF8B5CF6),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFC084FC)),
        shadowElevation = 3.dp,
        modifier = modifier.testTag("game_open_studio_button")
    ) {
        Row(
            modifier = Modifier.padding(horizontal = 14.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            Icon(
                Icons.Default.Palette,
                contentDescription = "Boyama Atölyesi",
                tint = Color(0xFFFDE047),
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = "Sen boyamak ister misin?",
                fontSize = 12.sp,
                fontWeight = FontWeight.ExtraBold,
                color = Color.White
            )
        }
    }
}

@Composable
private fun WordProgressSlots(
    currentWord: WordItem,
    revealedIndices: List<Int>,
    onSlotClick: (char: Char, isFound: Boolean) -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        currentWord.english.forEachIndexed { index, char ->
            val isFound = revealedIndices.contains(index)
            val slotSize = when {
                currentWord.english.length >= 8 -> 32.dp
                currentWord.english.length >= 6 -> 38.dp
                else -> 44.dp
            }

            Surface(
                onClick = { onSlotClick(char, isFound) },
                shape = RoundedCornerShape(12.dp),
                color = if (isFound) Color(0xFFF59E0B) else Color.White,
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    if (isFound) Color(0xFFD97706) else Color(0xFFCBD5E1)
                ),
                modifier = Modifier
                    .padding(horizontal = 3.dp)
                    .size(slotSize)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = if (isFound) char.toString() else "_",
                        fontSize = if (currentWord.english.length >= 8) 15.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFound) Color.White else Color(0xFF94A3B8)
                    )
                }
            }
        }
    }
}

@Composable
private fun KeyboardLettersGrid(
    keyboardLetters: List<Char>,
    currentWord: WordItem,
    revealedIndices: List<Int>,
    shakingLetter: Char?,
    columnsCount: Int,
    keyHeight: androidx.compose.ui.unit.Dp,
    onKeyPress: (Char) -> Unit
) {
    LazyVerticalGrid(
        columns = GridCells.Fixed(columnsCount),
        horizontalArrangement = Arrangement.spacedBy(5.dp),
        verticalArrangement = Arrangement.spacedBy(5.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        items(keyboardLetters) { letter ->
            val isFound = currentWord.english.contains(letter) &&
                    currentWord.english.mapIndexed { i, c -> Pair(i, c) }
                        .filter { it.second == letter }
                        .all { revealedIndices.contains(it.first) }
            val isShaking = shakingLetter == letter

            Surface(
                onClick = { onKeyPress(letter) },
                shape = RoundedCornerShape(12.dp),
                color = when {
                    isFound -> Color(0xFF10B981)
                    isShaking -> Color(0xFFEF4444)
                    else -> Color.White
                },
                border = androidx.compose.foundation.BorderStroke(
                    2.dp,
                    when {
                        isFound -> Color(0xFF059669)
                        isShaking -> Color(0xFFDC2626)
                        else -> Color(0xFFE2E8F0)
                    }
                ),
                shadowElevation = 2.dp,
                modifier = Modifier
                    .height(keyHeight)
                    .testTag("key_$letter")
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Text(
                        text = letter.toString(),
                        fontSize = 19.sp,
                        fontWeight = FontWeight.Black,
                        color = if (isFound || isShaking) Color.White else Color(0xFF1E293B)
                    )
                }
            }
        }
    }
}
