package com.example.ui.viewmodel

import android.app.Application
import android.content.Context
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.correction.SmartCorrector
import com.example.data.db.AppDatabase
import com.example.data.model.Phrase
import com.example.data.recognition.HandwritingRecognizer
import com.example.data.recognition.HandwritingStroke
import com.example.data.recognition.RecognitionResult
import com.example.data.recognition.RecognitionSource
import com.example.data.recognition.StrokePoint
import com.example.data.repository.PhraseRepository
import com.example.service.TtsManager
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
data class SawtiUiState(
    val currentText: String = "",
    val strokes: List<HandwritingStroke> = emptyList(),
    val currentStrokePoints: List<StrokePoint> = emptyList(),
    val selectedLanguage: String = "ar", // "ar", "en", "zh"
    val isRecognizing: Boolean = false,
    val recognitionSource: RecognitionSource? = null,
    val suggestedCorrection: String? = null,
    val autoSpeakEnabled: Boolean = false,
    val isDarkMode: Boolean = false,
    val vibrationEnabled: Boolean = true,
    val speechSpeed: Float = 0.85f,
    val fontScale: Float = 1.15f, // Large readable fonts for elderly
    val isEmergencyModeActive: Boolean = false,
    val isConfirmingEmergency: Boolean = false,
    val isAddPhraseDialogOpen: Boolean = false,
    val editingPhrase: Phrase? = null,
    val isSettingsOpen: Boolean = false,
    val isOnline: Boolean = true,
    val statusMessage: String? = null
)

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class SawtiViewModel(application: Application) : AndroidViewModel(application) {

    private val db = AppDatabase.getInstance(application)
    private val repository = PhraseRepository(db.phraseDao())
    private val recognizer = HandwritingRecognizer(application)
    val ttsManager = TtsManager(application)

    private val _uiState = MutableStateFlow(SawtiUiState())
    val uiState: StateFlow<SawtiUiState> = _uiState.asStateFlow()

    private val _currentLanguageFlow = MutableStateFlow("ar")

    val quickPhrases: StateFlow<List<Phrase>> = _currentLanguageFlow
        .flatMapLatest { lang ->
            repository.getPhrases(lang)
        }
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), emptyList())

    private var autoRecognizeJob: Job? = null

    init {
        viewModelScope.launch {
            repository.checkAndSeedDefaults("ar")
            repository.checkAndSeedDefaults("en")
            repository.checkAndSeedDefaults("zh")
            _uiState.value = _uiState.value.copy(
                isOnline = recognizer.isOnline()
            )
        }
    }

    fun vibrate() {
        if (!_uiState.value.vibrationEnabled) return
        try {
            val context = getApplication<Application>()
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                val vibrator = vibratorManager?.defaultVibrator
                vibrator?.vibrate(VibrationEffect.createPredefined(VibrationEffect.EFFECT_CLICK))
            } else {
                @Suppress("DEPRECATION")
                val vibrator = context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
                @Suppress("DEPRECATION")
                vibrator?.vibrate(35)
            }
        } catch (_: Exception) {}
    }

    // Touch and Stroke handlers
    fun onStrokeStart(x: Float, y: Float) {
        autoRecognizeJob?.cancel()
        _uiState.value = _uiState.value.copy(
            currentStrokePoints = listOf(StrokePoint(x, y))
        )
    }

    fun onStrokeMove(x: Float, y: Float) {
        val current = _uiState.value.currentStrokePoints
        _uiState.value = _uiState.value.copy(
            currentStrokePoints = current + StrokePoint(x, y)
        )
    }

    fun onStrokeEnd() {
        val points = _uiState.value.currentStrokePoints
        if (points.isNotEmpty()) {
            val newStroke = HandwritingStroke(points)
            val updatedStrokes = _uiState.value.strokes + newStroke
            _uiState.value = _uiState.value.copy(
                strokes = updatedStrokes,
                currentStrokePoints = emptyList()
            )

            // Auto-recognize on 1.2s pause if Auto-Speak is enabled
            if (_uiState.value.autoSpeakEnabled) {
                autoRecognizeJob?.cancel()
                autoRecognizeJob = viewModelScope.launch {
                    delay(1200)
                    recognizeHandwriting()
                }
            }
        }
    }

    fun undoLastStroke() {
        vibrate()
        autoRecognizeJob?.cancel()
        val currentStrokes = _uiState.value.strokes
        if (currentStrokes.isNotEmpty()) {
            _uiState.value = _uiState.value.copy(
                strokes = currentStrokes.dropLast(1)
            )
        }
    }

    fun clearCanvas() {
        vibrate()
        autoRecognizeJob?.cancel()
        _uiState.value = _uiState.value.copy(
            strokes = emptyList(),
            currentStrokePoints = emptyList(),
            suggestedCorrection = null
        )
    }

    fun recognizeHandwriting(canvasWidth: Int = 800, canvasHeight: Int = 600) {
        vibrate()
        val strokes = _uiState.value.strokes
        if (strokes.isEmpty()) return

        val lang = _uiState.value.selectedLanguage

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isRecognizing = true)
            val result = recognizer.recognize(strokes, lang, canvasWidth, canvasHeight)

            val newText = if (_uiState.value.currentText.isBlank()) {
                result.recognizedText
            } else {
                "${_uiState.value.currentText} ${result.recognizedText}"
            }

            // Check for smart correction
            val correction = result.suggestedCorrection ?: SmartCorrector.checkCorrection(newText, lang)?.suggestedText

            _uiState.value = _uiState.value.copy(
                isRecognizing = false,
                currentText = newText,
                recognitionSource = result.source,
                suggestedCorrection = correction,
                isOnline = recognizer.isOnline()
            )

            // If auto-speak is enabled, speak immediately!
            if (_uiState.value.autoSpeakEnabled && newText.isNotBlank()) {
                val textToSpeak = correction ?: newText
                ttsManager.speak(textToSpeak)
            }
        }
    }

    fun applySuggestedCorrection() {
        vibrate()
        val suggestion = _uiState.value.suggestedCorrection ?: return
        _uiState.value = _uiState.value.copy(
            currentText = suggestion,
            suggestedCorrection = null
        )
        if (_uiState.value.autoSpeakEnabled) {
            ttsManager.speak(suggestion)
        }
    }

    fun dismissCorrection() {
        vibrate()
        _uiState.value = _uiState.value.copy(suggestedCorrection = null)
    }

    fun triggerSmartCorrectionCheck() {
        vibrate()
        val text = _uiState.value.currentText
        val lang = _uiState.value.selectedLanguage
        val correction = SmartCorrector.checkCorrection(text, lang)
        if (correction != null) {
            _uiState.value = _uiState.value.copy(
                suggestedCorrection = correction.suggestedText
            )
        } else {
            // Check if user text already correct or show friendly hint
            _uiState.value = _uiState.value.copy(
                statusMessage = when (lang) {
                    "ar" -> "النص يبدو صحيحاً"
                    "zh" -> "文本看起来正确"
                    else -> "Text looks correct"
                }
            )
            viewModelScope.launch {
                delay(2000)
                _uiState.value = _uiState.value.copy(statusMessage = null)
            }
        }
    }

    fun onTextChange(newText: String) {
        val lang = _uiState.value.selectedLanguage
        val correction = SmartCorrector.checkCorrection(newText, lang)?.suggestedText
        _uiState.value = _uiState.value.copy(
            currentText = newText,
            suggestedCorrection = correction
        )
    }

    fun clearText() {
        vibrate()
        _uiState.value = _uiState.value.copy(
            currentText = "",
            suggestedCorrection = null
        )
    }

    // Speech Controls
    fun speakText(text: String = _uiState.value.currentText) {
        vibrate()
        if (text.isNotBlank()) {
            ttsManager.speak(text)
        }
    }

    fun stopSpeech() {
        vibrate()
        ttsManager.stop()
    }

    fun repeatSpeech() {
        vibrate()
        ttsManager.repeat()
    }

    fun setSpeechSpeed(speed: Float) {
        _uiState.value = _uiState.value.copy(speechSpeed = speed)
        ttsManager.setSpeechRate(speed)
    }

    fun setLanguage(language: String) {
        vibrate()
        _uiState.value = _uiState.value.copy(selectedLanguage = language)
        _currentLanguageFlow.value = language
        ttsManager.setLanguage(language)
        viewModelScope.launch {
            repository.checkAndSeedDefaults(language)
        }
    }

    fun toggleAutoSpeak() {
        vibrate()
        val next = !_uiState.value.autoSpeakEnabled
        _uiState.value = _uiState.value.copy(autoSpeakEnabled = next)
    }

    fun toggleDarkMode() {
        vibrate()
        val next = !_uiState.value.isDarkMode
        _uiState.value = _uiState.value.copy(isDarkMode = next)
    }

    fun toggleVibration() {
        val next = !_uiState.value.vibrationEnabled
        _uiState.value = _uiState.value.copy(vibrationEnabled = next)
        if (next) vibrate()
    }

    fun setFontScale(scale: Float) {
        vibrate()
        _uiState.value = _uiState.value.copy(fontScale = scale)
    }

    // Quick phrase interactions
    fun onPhraseTapped(phrase: Phrase) {
        vibrate()
        _uiState.value = _uiState.value.copy(
            currentText = phrase.text,
            suggestedCorrection = null
        )
        ttsManager.speak(phrase.text)
    }

    fun openAddPhraseDialog(editing: Phrase? = null) {
        vibrate()
        _uiState.value = _uiState.value.copy(
            isAddPhraseDialogOpen = true,
            editingPhrase = editing
        )
    }

    fun closeAddPhraseDialog() {
        _uiState.value = _uiState.value.copy(
            isAddPhraseDialogOpen = false,
            editingPhrase = null
        )
    }

    fun savePhrase(text: String, category: String = "general") {
        vibrate()
        viewModelScope.launch {
            val editing = _uiState.value.editingPhrase
            if (editing != null) {
                repository.updatePhrase(editing.copy(text = text.trim(), category = category))
            } else {
                repository.addPhrase(text, _uiState.value.selectedLanguage, category)
            }
            closeAddPhraseDialog()
        }
    }

    fun deletePhrase(phrase: Phrase) {
        vibrate()
        viewModelScope.launch {
            repository.deletePhrase(phrase)
        }
    }

    fun movePhrase(phrase: Phrase, moveUp: Boolean) {
        vibrate()
        val phrases = quickPhrases.value
        val index = phrases.indexOf(phrase)
        if (index == -1) return
        val targetIndex = if (moveUp) index - 1 else index + 1
        if (targetIndex in phrases.indices) {
            val targetPhrase = phrases[targetIndex]
            viewModelScope.launch {
                repository.swapOrder(phrase, targetPhrase)
            }
        }
    }

    // Emergency Mode
    fun requestEmergencyMode() {
        vibrate()
        _uiState.value = _uiState.value.copy(isConfirmingEmergency = true)
    }

    fun confirmEmergencyMode() {
        vibrate()
        _uiState.value = _uiState.value.copy(
            isConfirmingEmergency = false,
            isEmergencyModeActive = true
        )
        // Speak initial alert message
        val alert = when (_uiState.value.selectedLanguage) {
            "ar" -> "أحتاج إلى مساعدة عاجلة!"
            "zh" -> "我需要紧急帮助！"
            else -> "I need urgent help!"
        }
        ttsManager.speak(alert)
    }

    fun cancelEmergencyMode() {
        vibrate()
        _uiState.value = _uiState.value.copy(
            isConfirmingEmergency = false,
            isEmergencyModeActive = false
        )
        ttsManager.stop()
    }

    fun toggleSettings() {
        vibrate()
        _uiState.value = _uiState.value.copy(
            isSettingsOpen = !_uiState.value.isSettingsOpen
        )
    }

    override fun onCleared() {
        super.onCleared()
        ttsManager.shutdown()
    }
}
