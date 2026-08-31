package com.example.kidsenglishlab.ui.screens

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.EmojiEvents
import androidx.compose.material.icons.filled.Palette
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.RecordVoiceOver
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.VolumeOff
import androidx.compose.material.icons.filled.VolumeUp
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.kidsenglishlab.data.CategoryType
import com.example.kidsenglishlab.data.ThemeConfig
import com.example.kidsenglishlab.data.UserProfile
import com.example.kidsenglishlab.data.WordItem

private val LEVELS_GRID = listOf(
    listOf(0 to "Tümü (100)", 1 to "1. Seviye (3)", 2 to "2. Seviye (4)"),
    listOf(3 to "3. Seviye (5)", 4 to "4. Seviye (6)", 5 to "5. Seviye (7)"),
    listOf(6 to "6. Seviye (8)", 7 to "7. Seviye (9)", 8 to "8. Seviye (10)")
)

private val CATEGORIES_GRID = listOf(
    listOf(CategoryType.ALL, CategoryType.ANIMALS, CategoryType.FRUITS),
    listOf(CategoryType.VEHICLES, CategoryType.NATURE, CategoryType.OBJECTS)
)

@Composable
fun HomeScreen(
    theme: ThemeConfig,
    activeWords: List<WordItem>,
    completedWordIds: Set<String>,
    activeProfile: UserProfile,
    selectedDifficulty: Int,
    selectedCategory: CategoryType,
    soundEnabled: Boolean,
    onSelectDifficulty: (Int) -> Unit,
    onSelectCategory: (CategoryType) -> Unit,
    onToggleSound: () -> Unit,
    onOpenThemes: () -> Unit,
    onOpenVoices: () -> Unit,
    onOpenCollection: () -> Unit,
    onOpenSettings: () -> Unit,
    onStartGame: () -> Unit,
    onOpenStudio: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "heroBounce")
    val bounceScale by infiniteTransition.animateFloat(
        initialValue = 0.96f,
        targetValue = 1.05f,
        animationSpec = infiniteRepeatable(
            animation = tween(1200, easing = FastOutSlowInEasing),
            repeatMode = RepeatMode.Reverse
        ),
        label = "heroScale"
    )

    BoxWithConstraints(
        modifier = Modifier
            .fillMaxSize()
            .background(theme.backgroundBrush)
            .testTag("home_screen")
    ) {
        val isLandscapeOrTablet = maxWidth >= 650.dp || (maxWidth > maxHeight && maxHeight < 600.dp)

        if (isLandscapeOrTablet) {
            // Adaptive Landscape & Tablet 2-Column Split
            Row(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 14.dp, vertical = 8.dp),
                horizontalArrangement = Arrangement.spacedBy(14.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left Pane: Top bar, Hero Mascot, and Action Play buttons
                Column(
                    modifier = Modifier
                        .weight(1f)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.SpaceBetween,
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeTopBar(
                        theme = theme,
                        completedWordIds = completedWordIds,
                        activeProfile = activeProfile,
                        soundEnabled = soundEnabled,
                        onOpenCollection = onOpenCollection,
                        onOpenSettings = onOpenSettings,
                        onOpenVoices = onOpenVoices,
                        onOpenThemes = onOpenThemes,
                        onToggleSound = onToggleSound
                    )

                    HomeHeroBanner(
                        theme = theme,
                        activeProfile = activeProfile,
                        bounceScale = bounceScale,
                        compact = true
                    )

                    HomeActionButtons(
                        activeWordsCount = activeWords.size,
                        onStartGame = onStartGame,
                        onOpenStudio = onOpenStudio,
                        compact = true
                    )
                }

                // Right Pane: Level Selection & Category Selection
                Column(
                    modifier = Modifier
                        .weight(1.15f)
                        .fillMaxSize()
                        .verticalScroll(rememberScrollState()),
                    verticalArrangement = Arrangement.spacedBy(10.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    HomeLevelsSection(
                        selectedDifficulty = selectedDifficulty,
                        onSelectDifficulty = onSelectDifficulty
                    )

                    HomeCategoriesSection(
                        selectedCategory = selectedCategory,
                        onSelectCategory = onSelectCategory
                    )
                }
            }
        } else {
            // Standard Portrait Phone Layout
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 10.dp)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                HomeTopBar(
                    theme = theme,
                    completedWordIds = completedWordIds,
                    activeProfile = activeProfile,
                    soundEnabled = soundEnabled,
                    onOpenCollection = onOpenCollection,
                    onOpenSettings = onOpenSettings,
                    onOpenVoices = onOpenVoices,
                    onOpenThemes = onOpenThemes,
                    onToggleSound = onToggleSound
                )

                Spacer(modifier = Modifier.height(4.dp))

                HomeHeroBanner(
                    theme = theme,
                    activeProfile = activeProfile,
                    bounceScale = bounceScale,
                    compact = false
                )

                Spacer(modifier = Modifier.height(6.dp))

                HomeLevelsSection(
                    selectedDifficulty = selectedDifficulty,
                    onSelectDifficulty = onSelectDifficulty
                )

                Spacer(modifier = Modifier.height(6.dp))

                HomeCategoriesSection(
                    selectedCategory = selectedCategory,
                    onSelectCategory = onSelectCategory
                )

                Spacer(modifier = Modifier.height(10.dp))

                HomeActionButtons(
                    activeWordsCount = activeWords.size,
                    onStartGame = onStartGame,
                    onOpenStudio = onOpenStudio,
                    compact = false
                )
            }
        }
    }
}

@Composable
private fun HomeTopBar(
    theme: ThemeConfig,
    completedWordIds: Set<String>,
    activeProfile: UserProfile,
    soundEnabled: Boolean,
    onOpenCollection: () -> Unit,
    onOpenSettings: () -> Unit,
    onOpenVoices: () -> Unit,
    onOpenThemes: () -> Unit,
    onToggleSound: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(bottom = 6.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Left: Trophy Collection Button
        Surface(
            onClick = onOpenCollection,
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFDE68A)),
            shadowElevation = 2.dp,
            modifier = Modifier.testTag("collection_button")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Default.EmojiEvents,
                    contentDescription = "Koleksiyon",
                    tint = Color(0xFFF59E0B),
                    modifier = Modifier.size(20.dp)
                )
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = "${completedWordIds.size}/100",
                    fontWeight = FontWeight.Black,
                    fontSize = 12.sp,
                    color = Color(0xFFB45309)
                )
            }
        }

        // Center: Player Name Badge
        Surface(
            onClick = onOpenSettings,
            shape = RoundedCornerShape(16.dp),
            color = Color.White,
            border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFDBEAFE)),
            shadowElevation = 2.dp,
            modifier = Modifier
                .padding(horizontal = 4.dp)
                .testTag("player_name_badge")
        ) {
            Row(
                modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = activeProfile.avatar, fontSize = 15.sp)
                Spacer(modifier = Modifier.width(4.dp))
                Text(
                    text = activeProfile.name,
                    fontWeight = FontWeight.Black,
                    fontSize = 13.sp,
                    color = Color(0xFF1E293B),
                    maxLines = 1
                )
            }
        }

        // Right Action Tools: Voice, Theme, Sound, Settings (Gear)
        Row(
            horizontalArrangement = Arrangement.spacedBy(5.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Voice Select Button
            IconButton(
                onClick = onOpenVoices,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .testTag("voice_button")
            ) {
                Icon(Icons.Default.RecordVoiceOver, contentDescription = "Ses", tint = Color(0xFF475569), modifier = Modifier.size(19.dp))
            }

            // Theme Select Button
            IconButton(
                onClick = onOpenThemes,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .testTag("theme_button")
            ) {
                Text(text = theme.icon, fontSize = 17.sp)
            }

            // Sound Toggle Button
            IconButton(
                onClick = onToggleSound,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color.White)
                    .border(1.5.dp, Color(0xFFE2E8F0), RoundedCornerShape(12.dp))
                    .testTag("sound_button")
            ) {
                Icon(
                    if (soundEnabled) Icons.Default.VolumeUp else Icons.Default.VolumeOff,
                    contentDescription = "Ses Aç/Kapat",
                    tint = if (soundEnabled) Color(0xFF10B981) else Color(0xFF94A3B8),
                    modifier = Modifier.size(19.dp)
                )
            }

            // Settings Button
            IconButton(
                onClick = onOpenSettings,
                modifier = Modifier
                    .size(36.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(Color(0xFFEFF6FF))
                    .border(1.5.dp, Color(0xFFBFDBFE), RoundedCornerShape(12.dp))
                    .testTag("settings_button")
            ) {
                Icon(Icons.Default.Settings, contentDescription = "Ayarlar", tint = Color(0xFF2563EB), modifier = Modifier.size(19.dp))
            }
        }
    }
}

@Composable
private fun HomeHeroBanner(
    theme: ThemeConfig,
    activeProfile: UserProfile,
    bounceScale: Float,
    compact: Boolean
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = if (compact) 2.dp else 4.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.94f)),
        elevation = CardDefaults.cardElevation(defaultElevation = 3.dp),
        border = androidx.compose.foundation.BorderStroke(2.dp, Color(0xFFFDE68A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 14.dp, vertical = if (compact) 8.dp else 12.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // Mascot Avatar with Glow & 3D Effect
            Box(
                modifier = Modifier
                    .size(if (compact) 60.dp else 72.dp)
                    .scale(bounceScale)
                    .shadow(10.dp, CircleShape)
                    .clip(CircleShape)
                    .background(
                        Brush.radialGradient(
                            listOf(Color(0xFFFEF3C7), Color(0xFFFCD34D), Color(0xFFF59E0B))
                        )
                    )
                    .border(3.5.dp, Color.White, CircleShape),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = activeProfile.avatar,
                    fontSize = if (compact) 32.sp else 38.sp
                )
            }

            Spacer(modifier = Modifier.height(6.dp))

            Text(
                text = "Kids English Lab 🚀",
                fontSize = if (compact) 19.sp else 22.sp,
                fontWeight = FontWeight.Black,
                color = theme.textColor,
                letterSpacing = 0.5.sp
            )

            Text(
                text = "Harfleri birleştir, kelimeleri keşfet ve boya!",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFF64748B),
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
private fun HomeLevelsSection(
    selectedDifficulty: Int,
    onSelectDifficulty: (Int) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "🎯 SEVİYE SEÇİMİ (Harf Sayısı)",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                LEVELS_GRID.forEach { rowLevels ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowLevels.forEach { (diff, label) ->
                            val isSelected = selectedDifficulty == diff
                            Button(
                                onClick = { onSelectDifficulty(diff) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFFF59E0B) else Color(0xFFF1F5F9),
                                    contentColor = if (isSelected) Color.White else Color(0xFF334155)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = label,
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeCategoriesSection(
    selectedCategory: CategoryType,
    onSelectCategory: (CategoryType) -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(20.dp),
        colors = CardDefaults.cardColors(containerColor = Color.White.copy(alpha = 0.92f)),
        border = androidx.compose.foundation.BorderStroke(1.5.dp, Color(0xFFE2E8F0))
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp, vertical = 8.dp)
        ) {
            Text(
                text = "📂 KATEGORİLER",
                fontSize = 11.sp,
                fontWeight = FontWeight.Black,
                color = Color(0xFF64748B),
                modifier = Modifier.padding(bottom = 6.dp)
            )

            Column(
                modifier = Modifier.fillMaxWidth(),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                CATEGORIES_GRID.forEach { rowCategories ->
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = Arrangement.spacedBy(4.dp)
                    ) {
                        rowCategories.forEach { cat ->
                            val isSelected = selectedCategory == cat
                            Button(
                                onClick = { onSelectCategory(cat) },
                                modifier = Modifier
                                    .weight(1f)
                                    .height(34.dp),
                                shape = RoundedCornerShape(10.dp),
                                colors = ButtonDefaults.buttonColors(
                                    containerColor = if (isSelected) Color(0xFF3B82F6) else Color(0xFFF1F5F9),
                                    contentColor = if (isSelected) Color.White else Color(0xFF334155)
                                ),
                                contentPadding = androidx.compose.foundation.layout.PaddingValues(horizontal = 2.dp, vertical = 0.dp)
                            ) {
                                Text(
                                    text = "${cat.emoji} ${cat.title}",
                                    fontSize = 11.sp,
                                    fontWeight = if (isSelected) FontWeight.Black else FontWeight.Bold,
                                    maxLines = 1
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun HomeActionButtons(
    activeWordsCount: Int,
    onStartGame: () -> Unit,
    onOpenStudio: () -> Unit,
    compact: Boolean
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .offset(y = (-12).dp),
        verticalArrangement = Arrangement.spacedBy(if (compact) 8.dp else 12.dp)
    ) {
        // 3D Play Game Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 60.dp else 74.dp)
                .shadow(6.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFFB45309))
                .clickable { onStartGame() }
                .testTag("start_game_button")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 54.dp else 66.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFFBBF24), Color(0xFFF59E0B), Color(0xFFD97706))
                        )
                    )
                    .border(2.5.dp, Color(0xFFFEF3C7).copy(alpha = 0.8f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 36.dp else 42.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.PlayArrow,
                            contentDescription = null,
                            modifier = Modifier.size(if (compact) 26.dp else 32.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "OYUNA BAŞLA ($activeWordsCount Kelime)",
                        fontSize = if (compact) 17.sp else 20.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }

        // 3D Coloring Studio Button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(if (compact) 56.dp else 70.dp)
                .shadow(6.dp, RoundedCornerShape(22.dp))
                .clip(RoundedCornerShape(22.dp))
                .background(Color(0xFF6B21A8))
                .clickable { onOpenStudio() }
                .testTag("open_studio_button")
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(if (compact) 50.dp else 62.dp)
                    .clip(RoundedCornerShape(22.dp))
                    .background(
                        Brush.verticalGradient(
                            listOf(Color(0xFFC084FC), Color(0xFFA855F7), Color(0xFF7E22CE))
                        )
                    )
                    .border(2.5.dp, Color(0xFFF3E8FF).copy(alpha = 0.8f), RoundedCornerShape(22.dp)),
                contentAlignment = Alignment.Center
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center
                ) {
                    Box(
                        modifier = Modifier
                            .size(if (compact) 34.dp else 38.dp)
                            .clip(CircleShape)
                            .background(Color.White.copy(alpha = 0.25f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            Icons.Default.Palette,
                            contentDescription = null,
                            modifier = Modifier.size(if (compact) 22.dp else 26.dp),
                            tint = Color.White
                        )
                    }
                    Spacer(modifier = Modifier.width(10.dp))
                    Text(
                        text = "🎨 BOYAMA ATÖLYESİ",
                        fontSize = if (compact) 17.sp else 19.sp,
                        fontWeight = FontWeight.Black,
                        color = Color.White,
                        letterSpacing = 0.5.sp
                    )
                }
            }
        }
    }
}
