package com.example.service

import android.content.Context
import android.os.Bundle
import android.speech.tts.TextToSpeech
import android.speech.tts.UtteranceProgressListener
import android.util.Log
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import java.util.Locale

class TtsManager(private val context: Context) : TextToSpeech.OnInitListener {

    private val TAG = "TtsManager"
    private var tts: TextToSpeech? = null

    private val _isReady = MutableStateFlow(false)
    val isReady: StateFlow<Boolean> = _isReady.asStateFlow()

    private val _isSpeaking = MutableStateFlow(false)
    val isSpeaking: StateFlow<Boolean> = _isSpeaking.asStateFlow()

    private val _lastSpoken = MutableStateFlow("")
    val lastSpoken: StateFlow<String> = _lastSpoken.asStateFlow()

    private var currentLanguage: String = "ar"
    private var currentSpeed: Float = 0.85f // Default comfortable speed for elderly users

    init {
        tts = TextToSpeech(context.applicationContext, this)
    }

    override fun onInit(status: Int) {
        if (status == TextToSpeech.SUCCESS) {
            _isReady.value = true
            setLanguage(currentLanguage)
            setSpeechRate(currentSpeed)

            tts?.setOnUtteranceProgressListener(object : UtteranceProgressListener() {
                override fun onStart(utteranceId: String?) {
                    _isSpeaking.value = true
                }

                override fun onDone(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                @Deprecated("Deprecated in Java")
                override fun onError(utteranceId: String?) {
                    _isSpeaking.value = false
                }

                override fun onError(utteranceId: String?, errorCode: Int) {
                    Log.w(TAG, "TTS Error code: $errorCode")
                    _isSpeaking.value = false
                }
            })
        } else {
            Log.e(TAG, "TTS Initialization failed with status: $status")
            _isReady.value = false
        }
    }

    fun setLanguage(language: String) {
        currentLanguage = language
        if (!_isReady.value) return

        val locale = when (language) {
            "ar" -> Locale.forLanguageTag("ar")
            "zh" -> Locale.SIMPLIFIED_CHINESE
            else -> Locale.ENGLISH
        }

        val result = tts?.setLanguage(locale)
        if (result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED) {
            Log.w(TAG, "Language $language is not supported or missing data, trying fallback")
            // Try specific country locales as fallback
            when (language) {
                "ar" -> tts?.setLanguage(Locale.forLanguageTag("ar-SA"))
                "zh" -> tts?.setLanguage(Locale.SIMPLIFIED_CHINESE)
                else -> tts?.setLanguage(Locale.US)
            }
        }
    }

    fun setSpeechRate(rate: Float) {
        currentSpeed = rate.coerceIn(0.5f, 1.5f)
        tts?.setSpeechRate(currentSpeed)
    }

    fun speak(text: String, onDone: (() -> Unit)? = null) {
        val trimmed = text.trim()
        if (trimmed.isEmpty()) return

        _lastSpoken.value = trimmed
        setLanguage(currentLanguage)
        setSpeechRate(currentSpeed)

        val params = Bundle().apply {
            putString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, "sawti_speech_${System.currentTimeMillis()}")
        }

        tts?.speak(trimmed, TextToSpeech.QUEUE_FLUSH, params, params.getString(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID))
    }

    fun stop() {
        tts?.stop()
        _isSpeaking.value = false
    }

    fun repeat() {
        val last = _lastSpoken.value
        if (last.isNotBlank()) {
            speak(last)
        }
    }

    fun shutdown() {
        tts?.stop()
        tts?.shutdown()
        tts = null
        _isReady.value = false
        _isSpeaking.value = false
    }
}
