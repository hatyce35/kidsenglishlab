package com.example.kidsenglishlab.data

import androidx.compose.ui.graphics.Color

data class ColorOption(
    val nameEn: String,
    val nameTr: String,
    val hex: String,
    val color: Color
)

object ColorPalette {
    val colors = listOf(
        ColorOption("Red", "Kırmızı", "#EF4444", Color(0xFFEF4444)),
        ColorOption("Orange", "Turuncu", "#F97316", Color(0xFFF97316)),
        ColorOption("Yellow", "Sarı", "#FACC15", Color(0xFFFACC15)),
        ColorOption("Green", "Yeşil", "#22C55E", Color(0xFF22C55E)),
        ColorOption("Sky Blue", "Açık Mavi", "#38BDF8", Color(0xFF38BDF8)),
        ColorOption("Blue", "Mavi", "#3B82F6", Color(0xFF3B82F6)),
        ColorOption("Purple", "Mor", "#A855F7", Color(0xFFA855F7)),
        ColorOption("Pink", "Pembe", "#EC4899", Color(0xFFEC4899)),
        ColorOption("Brown", "Kahverengi", "#854D0E", Color(0xFF854D0E)),
        ColorOption("White", "Beyaz", "#FFFFFF", Color(0xFFFFFFFF)),
        ColorOption("Gray", "Gri", "#94A3B8", Color(0xFF94A3B8)),
        ColorOption("Black", "Siyah", "#1E293B", Color(0xFF1E293B)),
        ColorOption("Gold", "Altın", "#FDE047", Color(0xFFFDE047))
    )
}
