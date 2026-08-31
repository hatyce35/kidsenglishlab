package com.example.kidsenglishlab.data

import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color

enum class ThemeId {
    HONEY, PINK, BLUE, NATURE, PURPLE, PEACH, NIGHT
}

data class ThemeConfig(
    val id: ThemeId,
    val name: String,
    val tag: String,
    val icon: String,
    val primaryColor: Color,
    val secondaryColor: Color,
    val backgroundBrush: Brush,
    val cardBackground: Color,
    val textColor: Color,
    val subTextColor: Color,
    val isDark: Boolean = false
)

object AppThemes {
    val themes = listOf(
        ThemeConfig(
            id = ThemeId.HONEY,
            name = "Güneş & Bal",
            tag = "Varsayılan 🍯",
            icon = "☀️",
            primaryColor = Color(0xFFF59E0B),
            secondaryColor = Color(0xFFFBBF24),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFFFEF3C7), Color(0xFFFFFBEB), Color(0xFFFDE68A))
            ),
            cardBackground = Color(0xFFFFFFFF),
            textColor = Color(0xFF1E293B),
            subTextColor = Color(0xFF475569)
        ),
        ThemeConfig(
            id = ThemeId.PINK,
            name = "Masalsı Pembe",
            tag = "Kızlar İçin Özel 🌸",
            icon = "🌸",
            primaryColor = Color(0xFFEC4899),
            secondaryColor = Color(0xFFF472B6),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFFFCE7F3), Color(0xFFFFF1F2), Color(0xFFFBCFE8))
            ),
            cardBackground = Color(0xFFFFFFFF),
            textColor = Color(0xFF831843),
            subTextColor = Color(0xFF9D174D)
        ),
        ThemeConfig(
            id = ThemeId.BLUE,
            name = "Gökyüzü Mavi",
            tag = "Erkekler için Özel 🚀",
            icon = "🚀",
            primaryColor = Color(0xFF0284C7),
            secondaryColor = Color(0xFF38BDF8),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFFE0F2FE), Color(0xFFF0F9FF), Color(0xFFBAE6FD))
            ),
            cardBackground = Color(0xFFFFFFFF),
            textColor = Color(0xFF0C4A6E),
            subTextColor = Color(0xFF0369A1)
        ),
        ThemeConfig(
            id = ThemeId.NATURE,
            name = "Zümrüt Doğa",
            tag = "Doğa & Macera 🍀",
            icon = "🍀",
            primaryColor = Color(0xFF10B981),
            secondaryColor = Color(0xFF34D399),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFFD1FAE5), Color(0xFFF0FDF4), Color(0xFFA7F3D0))
            ),
            cardBackground = Color(0xFFFFFFFF),
            textColor = Color(0xFF064E3B),
            subTextColor = Color(0xFF047857)
        ),
        ThemeConfig(
            id = ThemeId.PURPLE,
            name = "Sihirli Mor",
            tag = "Lavanta & Büyü 🔮",
            icon = "🔮",
            primaryColor = Color(0xFFA855F7),
            secondaryColor = Color(0xFFC084FC),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFFF3E8FF), Color(0xFFFAF5FF), Color(0xFFE9D5FF))
            ),
            cardBackground = Color(0xFFFFFFFF),
            textColor = Color(0xFF581C87),
            subTextColor = Color(0xFF6B21A8)
        ),
        ThemeConfig(
            id = ThemeId.PEACH,
            name = "Tatlı Şeftali",
            tag = "Mercan & Enerji 🍑",
            icon = "🍑",
            primaryColor = Color(0xFFF97316),
            secondaryColor = Color(0xFFFB923C),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFFFFEDD5), Color(0xFFFFF7ED), Color(0xFFFED7AA))
            ),
            cardBackground = Color(0xFFFFFFFF),
            textColor = Color(0xFF7C2D12),
            subTextColor = Color(0xFF9A3412)
        ),
        ThemeConfig(
            id = ThemeId.NIGHT,
            name = "Gece Macerası",
            tag = "Uzay & Yıldızlar 🌙",
            icon = "🌙",
            primaryColor = Color(0xFFFBBF24),
            secondaryColor = Color(0xFF818CF8),
            backgroundBrush = Brush.verticalGradient(
                listOf(Color(0xFF0F172A), Color(0xFF1E1B4B), Color(0xFF020617))
            ),
            cardBackground = Color(0xFF1E293B),
            textColor = Color(0xFFF8FAFC),
            subTextColor = Color(0xFFCBD5E1),
            isDark = true
        )
    )

    fun getTheme(id: ThemeId): ThemeConfig = themes.find { it.id == id } ?: themes[0]
}
