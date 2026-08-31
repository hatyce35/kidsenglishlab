package com.example.kidsenglishlab.audio

import android.content.Context
import android.os.Handler
import android.os.Looper
import android.speech.tts.TextToSpeech
import java.util.Locale
import java.util.concurrent.Executors

class SpeechService(context: Context) : TextToSpeech.OnInitListener {
    private var tts: TextToSpeech? = null
    private var isInitialized = false
    var isEnabled: Boolean = true
    var selectedVoiceName: String = "Auto (Default)"
    private val mainHandler = Handler(Looper.getMainLooper())
    private val executor = Executors.newSingleThreadExecutor()

    init {
        executor.execute {
            try {
                // Initialize TTS safely on background thread
                val appContext = context.applicationContext
                mainHandler.post {
                    try {
                        tts = TextToSpeech(appContext, this)
                    } catch (_: Exception) {}
                }
            } catch (_: Exception) {
                // Ignore initialization failure
            }
        }
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            isInitialized = true
            try {
                tts?.setSpeechRate(0.88f)
                tts?.setPitch(1.12f)
                tts?.language = Locale.US
            } catch (_: Exception) {}
        }
    }

    fun getAvailableVoices(): List<String> {
        val list = mutableListOf("Auto (Default)")
        if (isInitialized && tts != null) {
            try {
                val voices = tts?.voices
                if (voices != null) {
                    for (v in voices) {
                        if (v.locale.language == "en" || v.locale.language == "tr") {
                            list.add(v.name)
                        }
                    }
                }
            } catch (_: Exception) {}
        }
        return list.distinct()
    }

    fun setVoice(voiceName: String) {
        selectedVoiceName = voiceName
        if (isInitialized && tts != null && voiceName != "Auto (Default)") {
            try {
                val found = tts?.voices?.find { it.name == voiceName }
                if (found != null) {
                    tts?.voice = found
                }
            } catch (_: Exception) {}
        }
    }

    fun speakEnglish(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isEnabled || !isInitialized || tts == null) return
        try {
            tts?.language = Locale.US
            tts?.speak(text, queueMode, null, "en_$text")
        } catch (_: Exception) {}
    }

    fun speakTurkish(text: String, queueMode: Int = TextToSpeech.QUEUE_FLUSH) {
        if (!isEnabled || !isInitialized || tts == null) return
        try {
            tts?.language = Locale("tr", "TR")
            tts?.speak(text, queueMode, null, "tr_$text")
        } catch (_: Exception) {}
    }

    fun speakWordPair(english: String, turkish: String) {
        if (!isEnabled || !isInitialized || tts == null) return
        speakEnglish(english, TextToSpeech.QUEUE_FLUSH)
        // Queue Turkish translation slightly after
        try {
            tts?.playSilentUtterance(350, TextToSpeech.QUEUE_ADD, null)
            tts?.language = Locale("tr", "TR")
            tts?.speak(turkish, TextToSpeech.QUEUE_ADD, null, "tr_$turkish")
        } catch (_: Exception) {}
    }

    fun speakLetter(char: Char) {
        if (!isEnabled || !isInitialized || tts == null) return
        speakEnglish(char.toString())
    }

    fun speakColor(nameEn: String, nameTr: String) {
        if (!isEnabled || !isInitialized || tts == null) return
        speakEnglish(nameEn, TextToSpeech.QUEUE_FLUSH)
        try {
            tts?.playSilentUtterance(250, TextToSpeech.QUEUE_ADD, null)
            tts?.language = Locale("tr", "TR")
            tts?.speak(nameTr, TextToSpeech.QUEUE_ADD, null, "tr_$nameTr")
        } catch (_: Exception) {}
    }

    fun shutdown() {
        try {
            tts?.stop()
            tts?.shutdown()
        } catch (_: Exception) {}
    }
}
