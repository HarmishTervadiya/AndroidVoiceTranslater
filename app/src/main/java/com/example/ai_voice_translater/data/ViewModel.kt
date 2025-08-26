// MainViewModel.kt
package com.example.voicetranslator.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_voice_translater.audio.AudioRecorder
import com.example.ai_voice_translater.data.Translation
import com.example.ai_voice_translater.data.TranslationRepository
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

sealed class UiState {
    object Idle : UiState()
    object Recording : UiState()
    object Loading : UiState()
    data class Success(val translation: String) : UiState()
    data class Error(val message: String) : UiState()
}

class MainViewModel(application: Application) : AndroidViewModel(application) {

    private val repository = TranslationRepository()
    private val audioRecorder = AudioRecorder(application)
    private val auth = FirebaseAuth.getInstance()

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _translationHistory = MutableStateFlow<List<Translation>>(emptyList())
    val translationHistory = _translationHistory.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("hi-IN")
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val apiKey = ""

    init {
        // Start listening for history updates as soon as the ViewModel is created
        listenForHistoryUpdates()
    }

    private fun listenForHistoryUpdates() {
        repository.getTranslationHistory()
            .onEach { translations ->
                _translationHistory.value = translations
            }
            .launchIn(viewModelScope)
    }

    fun setLanguage(languageCode: String) {
        _selectedLanguage.value = languageCode
    }

    fun toggleRecording() {
        if (auth.currentUser == null) {
            _uiState.value = UiState.Error("Please sign in to use the translator.")
            return
        }
        when (_uiState.value) {
            is UiState.Recording -> stopRecordingAndTranslate()
            else -> startRecording()
        }
    }

    private fun startRecording() {
        _uiState.value = UiState.Recording
        audioRecorder.start()
    }

    private fun stopRecordingAndTranslate() {
        _uiState.value = UiState.Loading
        val audioFile = audioRecorder.stop()

        if (audioFile != null) {
            viewModelScope.launch {
                val result = repository.getFullTranslation(audioFile, apiKey, _selectedLanguage.value)
                result.fold(
                    onSuccess = { (originalText, translatedText) ->
                        _uiState.value = UiState.Success(translatedText)
                        // Save the translation. The listener will automatically update the history.
                        repository.saveTranslation(originalText, translatedText)
                    },
                    onFailure = { error ->
                        _uiState.value = UiState.Error(error.message ?: "Unknown error")
                    }
                )
            }
        } else {
            _uiState.value = UiState.Error("Failed to record audio.")
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }

    fun deleteTranslation(translationId: String) {
        viewModelScope.launch {
            repository.deleteTranslation(translationId)
        }
    }
}
