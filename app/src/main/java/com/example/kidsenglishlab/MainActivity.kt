package com.example.kidsenglishlab

import android.content.Context
import android.content.SharedPreferences
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowInsetsController
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import com.example.kidsenglishlab.audio.SoundEffectsService
import com.example.kidsenglishlab.audio.SpeechService
import com.example.kidsenglishlab.data.AppThemes
import com.example.kidsenglishlab.data.CategoryType
import com.example.kidsenglishlab.data.ThemeId
import com.example.kidsenglishlab.data.UserProfile
import com.example.kidsenglishlab.data.WordItem
import com.example.kidsenglishlab.data.WordsData
import com.example.kidsenglishlab.ui.components.CompletionCelebrationDialog
import com.example.kidsenglishlab.ui.components.FirstLaunchWelcomeDialog
import com.example.kidsenglishlab.ui.components.SettingsDialog
import com.example.kidsenglishlab.ui.components.ThemeSelectionDialog
import com.example.kidsenglishlab.ui.components.VoiceSelectionDialog
import com.example.kidsenglishlab.ui.screens.CollectionScreen
import com.example.kidsenglishlab.ui.screens.ColoringStudioScreen
import com.example.kidsenglishlab.ui.screens.GameScreen
import com.example.kidsenglishlab.ui.screens.HomeScreen
import com.example.kidsenglishlab.ui.theme.KidsEnglishLabTheme
import org.json.JSONArray

enum class Screen {
    HOME, GAME, STUDIO, COLLECTION
}

class MainActivity : ComponentActivity() {
    private lateinit var soundService: SoundEffectsService
    private lateinit var speechService: SpeechService

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        hideSystemBars()

        soundService = SoundEffectsService()
        speechService = SpeechService(this)

        val prefs = getSharedPreferences("kids_english_lab_prefs", Context.MODE_PRIVATE)
        val (initialProfiles, initialActiveId) = loadProfiles(prefs)
        val initialHasRegistered = prefs.getBoolean("has_registered_player", false)
        val initialTheme = try {
            ThemeId.valueOf(prefs.getString("theme_id", ThemeId.HONEY.name) ?: ThemeId.HONEY.name)
        } catch (_: Exception) {
            ThemeId.HONEY
        }

        setContent {
            KidsEnglishLabTheme {
                Surface(
                    modifier = Modifier.fillMaxSize()
                ) {
                    KidsEnglishLabApp(
                        soundService = soundService,
                        speechService = speechService,
                        initialProfiles = initialProfiles,
                        initialActiveProfileId = initialActiveId,
                        initialHasRegistered = initialHasRegistered,
                        initialTheme = initialTheme,
                        onSaveProfiles = { profiles, activeId ->
                            saveProfiles(prefs, profiles, activeId)
                        },
                        onSaveHasRegistered = { hasRegistered ->
                            prefs.edit().putBoolean("has_registered_player", hasRegistered).apply()
                        },
                        onSaveTheme = { themeId ->
                            prefs.edit().putString("theme_id", themeId.name).apply()
                        }
                    )
                }
            }
        }
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        if (hasFocus) {
            hideSystemBars()
        }
    }

    override fun onResume() {
        super.onResume()
        hideSystemBars()
    }

    private fun hideSystemBars() {
        val windowInsetsController = WindowCompat.getInsetsController(window, window.decorView)
        windowInsetsController.systemBarsBehavior =
            WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
        windowInsetsController.hide(WindowInsetsCompat.Type.systemBars())
    }

    private fun loadProfiles(prefs: SharedPreferences): Pair<List<UserProfile>, String> {
        val jsonStr = prefs.getString("user_profiles_json", null)
        val activeId = prefs.getString("active_profile_id", "profile_1") ?: "profile_1"
        if (!jsonStr.isNullOrEmpty()) {
            try {
                val arr = JSONArray(jsonStr)
                val list = mutableListOf<UserProfile>()
                for (i in 0 until arr.length()) {
                    list.add(UserProfile.fromJson(arr.getJSONObject(i)))
                }
                if (list.isNotEmpty()) {
                    val validId = if (list.any { it.id == activeId }) activeId else list.first().id
                    return Pair(list, validId)
                }
            } catch (_: Exception) { }
        }

        // Fallback or migration from legacy completed_words
        val legacyCompleted = prefs.getStringSet("completed_words", emptySet()) ?: emptySet()
        val defaultList = listOf(
            UserProfile(
                id = "profile_1",
                name = "Küçük Kaşif",
                avatar = "🦁",
                completedWords = legacyCompleted
            )
        )
        return Pair(defaultList, "profile_1")
    }

    private fun saveProfiles(prefs: SharedPreferences, profiles: List<UserProfile>, activeId: String) {
        val arr = JSONArray()
        profiles.forEach { arr.put(it.toJson()) }
        prefs.edit()
            .putString("user_profiles_json", arr.toString())
            .putString("active_profile_id", activeId)
            .apply()
    }

    override fun onDestroy() {
        super.onDestroy()
        speechService.shutdown()
        soundService.release()
    }
}

@Composable
fun KidsEnglishLabApp(
    soundService: SoundEffectsService,
    speechService: SpeechService,
    initialProfiles: List<UserProfile>,
    initialActiveProfileId: String,
    initialHasRegistered: Boolean,
    initialTheme: ThemeId,
    onSaveProfiles: (List<UserProfile>, String) -> Unit,
    onSaveHasRegistered: (Boolean) -> Unit,
    onSaveTheme: (ThemeId) -> Unit
) {
    var currentScreen by remember { mutableStateOf(Screen.HOME) }
    var selectedThemeId by remember { mutableStateOf(initialTheme) }
    var profiles by remember { mutableStateOf(initialProfiles) }
    var activeProfileId by remember { mutableStateOf(initialActiveProfileId) }
    var hasRegistered by remember { mutableStateOf(initialHasRegistered) }

    var isWelcomeModalOpen by remember { mutableStateOf(!initialHasRegistered) }
    var isSettingsModalOpen by remember { mutableStateOf(false) }
    var isThemeModalOpen by remember { mutableStateOf(false) }
    var isVoiceModalOpen by remember { mutableStateOf(false) }
    var completedModalWord by remember { mutableStateOf<WordItem?>(null) }
    var soundEnabled by remember { mutableStateOf(true) }

    var selectedDifficulty by remember { mutableIntStateOf(0) }
    var selectedCategory by remember { mutableStateOf(CategoryType.ALL) }
    var currentWordIndex by remember { mutableIntStateOf(0) }
    var studioTargetWord by remember { mutableStateOf<WordItem?>(null) }

    val activeProfile = profiles.find { it.id == activeProfileId } ?: profiles.firstOrNull() ?: UserProfile.defaultProfile()
    val completedWordIds = activeProfile.completedWords

    val currentTheme = AppThemes.getTheme(selectedThemeId)

    val activeWords = remember(selectedCategory, selectedDifficulty) {
        WordsData.words.filter { w ->
            (selectedCategory == CategoryType.ALL || w.category == selectedCategory) &&
                    (selectedDifficulty == 0 || w.difficulty == selectedDifficulty)
        }
    }

    fun updateAndSaveProfiles(newProfiles: List<UserProfile>, newActiveId: String = activeProfileId) {
        profiles = newProfiles
        activeProfileId = newActiveId
        onSaveProfiles(newProfiles, newActiveId)
    }

    fun startWordGame(index: Int) {
        currentWordIndex = index.coerceIn(0, (activeWords.size - 1).coerceAtLeast(0))
        currentScreen = Screen.GAME
        soundService.playPopTone()
    }

    fun handleWordCompleted(word: WordItem) {
        val updatedCompleted = activeProfile.completedWords + word.id
        val updatedProfile = activeProfile.copy(completedWords = updatedCompleted)
        val updatedList = profiles.map { if (it.id == activeProfile.id) updatedProfile else it }
        updateAndSaveProfiles(updatedList, activeProfileId)
        completedModalWord = word
    }

    when (currentScreen) {
        Screen.HOME -> {
            HomeScreen(
                theme = currentTheme,
                activeWords = activeWords,
                completedWordIds = completedWordIds,
                activeProfile = activeProfile,
                selectedDifficulty = selectedDifficulty,
                selectedCategory = selectedCategory,
                soundEnabled = soundEnabled,
                onSelectDifficulty = { lvl ->
                    selectedDifficulty = lvl
                    soundService.playPopTone()
                },
                onSelectCategory = { cat ->
                    selectedCategory = cat
                    soundService.playPopTone()
                },
                onToggleSound = {
                    soundEnabled = !soundEnabled
                    soundService.isSoundEnabled = soundEnabled
                    speechService.isEnabled = soundEnabled
                },
                onOpenThemes = { isThemeModalOpen = true },
                onOpenVoices = { isVoiceModalOpen = true },
                onOpenSettings = { isSettingsModalOpen = true },
                onOpenCollection = {
                    currentScreen = Screen.COLLECTION
                    soundService.playPopTone()
                },
                onStartGame = {
                    startWordGame(0)
                },
                onOpenStudio = {
                    studioTargetWord = activeWords.firstOrNull() ?: WordsData.words.first()
                    currentScreen = Screen.STUDIO
                    soundService.playPopTone()
                }
            )
        }
        Screen.GAME -> {
            GameScreen(
                theme = currentTheme,
                activeWords = activeWords,
                currentIndex = currentWordIndex,
                soundService = soundService,
                speechService = speechService,
                onNavigateBack = {
                    currentScreen = Screen.HOME
                    soundService.playPopTone()
                },
                onOpenThemes = { isThemeModalOpen = true },
                onOpenVoices = { isVoiceModalOpen = true },
                onOpenCollection = {
                    currentScreen = Screen.COLLECTION
                    soundService.playPopTone()
                },
                onOpenStudio = {
                    studioTargetWord = activeWords.getOrNull(currentWordIndex)
                    currentScreen = Screen.STUDIO
                    soundService.playPopTone()
                },
                onWordCompleted = { word ->
                    handleWordCompleted(word)
                },
                onSelectWordIndex = { newIdx ->
                    currentWordIndex = newIdx
                    soundService.playPopTone()
                }
            )
        }
        Screen.STUDIO -> {
            ColoringStudioScreen(
                theme = currentTheme,
                initialWord = studioTargetWord,
                soundService = soundService,
                speechService = speechService,
                onNavigateBack = {
                    currentScreen = Screen.HOME
                    soundService.playPopTone()
                },
                onGoToGame = { word ->
                    val idx = activeWords.indexOfFirst { it.id == word.id }
                    if (idx != -1) {
                        startWordGame(idx)
                    } else {
                        selectedCategory = CategoryType.ALL
                        selectedDifficulty = 0
                        val allIdx = WordsData.words.indexOfFirst { it.id == word.id }
                        startWordGame(if (allIdx != -1) allIdx else 0)
                    }
                }
            )
        }
        Screen.COLLECTION -> {
            CollectionScreen(
                theme = currentTheme,
                completedWordIds = completedWordIds,
                speechService = speechService,
                onNavigateBack = {
                    currentScreen = Screen.HOME
                    soundService.playPopTone()
                },
                onPlayWord = { word ->
                    val idx = activeWords.indexOfFirst { it.id == word.id }
                    if (idx != -1) {
                        startWordGame(idx)
                    } else {
                        selectedCategory = CategoryType.ALL
                        selectedDifficulty = 0
                        val allIdx = WordsData.words.indexOfFirst { it.id == word.id }
                        startWordGame(if (allIdx != -1) allIdx else 0)
                    }
                },
                onColorWord = { word ->
                    studioTargetWord = word
                    currentScreen = Screen.STUDIO
                    soundService.playPopTone()
                }
            )
        }
    }

    // 1. First-Launch Welcome Player Dialog (Opens only once on first run)
    FirstLaunchWelcomeDialog(
        isOpen = isWelcomeModalOpen,
        onSaveProfile = { name, avatar ->
            val updated = activeProfile.copy(name = name, avatar = avatar)
            val updatedList = profiles.map { if (it.id == activeProfile.id) updated else it }
            updateAndSaveProfiles(updatedList, activeProfile.id)
            hasRegistered = true
            isWelcomeModalOpen = false
            onSaveHasRegistered(true)
            soundService.playSuccessTone()
            speechService.speakTurkish("Hoş geldin $name! Kelime macerasına başlayalım!")
        }
    )

    // 2. Settings & Multi-Profile Dialog
    SettingsDialog(
        isOpen = isSettingsModalOpen,
        profiles = profiles,
        activeProfileId = activeProfileId,
        onSelectProfile = { newId ->
            activeProfileId = newId
            onSaveProfiles(profiles, newId)
            soundService.playPopTone()
            val newActive = profiles.find { it.id == newId }
            if (newActive != null) {
                speechService.speakTurkish("Merhaba ${newActive.name}!")
            }
        },
        onAddProfile = { name, avatar ->
            if (profiles.size < 3) {
                val newProfile = UserProfile(
                    id = "profile_${System.currentTimeMillis()}",
                    name = name,
                    avatar = avatar,
                    completedWords = emptySet()
                )
                val newList = profiles + newProfile
                updateAndSaveProfiles(newList, newProfile.id)
                soundService.playSuccessTone()
                speechService.speakTurkish("Yeni profil oluşturuldu: $name!")
            }
        },
        onUpdateProfile = { id, name, avatar ->
            val updatedList = profiles.map {
                if (it.id == id) it.copy(name = name, avatar = avatar) else it
            }
            updateAndSaveProfiles(updatedList, activeProfileId)
            soundService.playPopTone()
        },
        onDeleteProfile = { idToDelete ->
            if (profiles.size > 1) {
                val newList = profiles.filterNot { it.id == idToDelete }
                val newActiveId = if (activeProfileId == idToDelete) newList.first().id else activeProfileId
                updateAndSaveProfiles(newList, newActiveId)
                soundService.playPopTone()
            }
        },
        onResetProgress = { profileIdToReset ->
            val updatedList = profiles.map {
                if (it.id == profileIdToReset) it.copy(completedWords = emptySet()) else it
            }
            updateAndSaveProfiles(updatedList, activeProfileId)
            soundService.playWrongTone()
            speechService.speakTurkish("İlerleme sıfırlandı!")
        },
        onOpenThemes = { isThemeModalOpen = true },
        onOpenVoices = { isVoiceModalOpen = true },
        onDismiss = { isSettingsModalOpen = false }
    )

    // 3. Theme Selection Modal
    ThemeSelectionDialog(
        isOpen = isThemeModalOpen,
        currentThemeId = selectedThemeId,
        onSelectTheme = { tid ->
            selectedThemeId = tid
            onSaveTheme(tid)
            soundService.playPopTone()
        },
        onDismiss = { isThemeModalOpen = false }
    )

    // 4. Voice Selection Modal
    VoiceSelectionDialog(
        isOpen = isVoiceModalOpen,
        selectedVoiceName = speechService.selectedVoiceName,
        availableVoices = speechService.getAvailableVoices(),
        onSelectVoice = { voice ->
            speechService.setVoice(voice)
            soundService.playPopTone()
        },
        onTestVoice = {
            speechService.speakWordPair("Hello", "Merhaba")
        },
        onDismiss = { isVoiceModalOpen = false }
    )

    // 5. Completion Celebration Dialog
    completedModalWord?.let { word ->
        CompletionCelebrationDialog(
            isOpen = true,
            word = word,
            isLastWord = currentWordIndex >= activeWords.size - 1,
            onSpeak = {
                speechService.speakWordPair(word.english, word.turkish)
            },
            onReplay = {
                completedModalWord = null
                startWordGame(currentWordIndex)
            },
            onGoToStudio = {
                completedModalWord = null
                studioTargetWord = word
                currentScreen = Screen.STUDIO
                soundService.playPopTone()
            },
            onNext = {
                completedModalWord = null
                if (currentWordIndex < activeWords.size - 1) {
                    startWordGame(currentWordIndex + 1)
                } else {
                    currentScreen = Screen.HOME
                }
            },
            onDismiss = { completedModalWord = null }
        )
    }
}
