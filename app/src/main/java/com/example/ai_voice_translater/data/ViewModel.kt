// MainViewModel.kt
package com.example.ai_voice_translater.data

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.ai_voice_translater.audio.AudioRecorder
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
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
    private val auth = Firebase.auth

    private val _uiState = MutableStateFlow<UiState>(UiState.Idle)
    val uiState = _uiState.asStateFlow()

    private val _translationHistory = MutableStateFlow<List<Translation>>(emptyList())
    val translationHistory = _translationHistory.asStateFlow()

    private val _selectedLanguage = MutableStateFlow("hi-IN") // Default to Hindi
    val selectedLanguage = _selectedLanguage.asStateFlow()

    private val apiKey = "sk_ugfock7d_FAuJV0IgUrPAeSyfAir4jTaW"

    init {
        if (auth.currentUser != null) {
            loadHistory()
        }
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
                        repository.saveTranslation(originalText, translatedText)
                        loadHistory()
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

    fun loadHistory() {
        if (auth.currentUser == null) return
        viewModelScope.launch {
            _translationHistory.value = repository.getTranslationHistory()
        }
    }

    fun resetState() {
        _uiState.value = UiState.Idle
    }
}
