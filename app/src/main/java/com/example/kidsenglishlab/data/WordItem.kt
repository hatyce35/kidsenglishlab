package com.example.kidsenglishlab.data

enum class CategoryType(val id: String, val title: String, val emoji: String) {
    ALL("all", "Tümü", "🌟"),
    ANIMALS("animals", "Hayvanlar", "🐾"),
    FRUITS("fruits", "Meyveler", "🍎"),
    VEHICLES("vehicles", "Araçlar", "🚗"),
    NATURE("nature", "Doğa", "🌳"),
    OBJECTS("objects", "Eşyalar", "🏠")
}

data class WordItem(
    val id: String,
    val english: String,
    val turkish: String,
    val gameType: String, // "coloring" or "puzzle"
    val emoji: String,
    val color: String,
    val category: CategoryType,
    val difficulty: Int, // 1 to 8
    val difficultyLabel: String,
    val funFactEn: String,
    val funFactTr: String,
    val defaultParts: Map<String, String> = emptyMap()
)
