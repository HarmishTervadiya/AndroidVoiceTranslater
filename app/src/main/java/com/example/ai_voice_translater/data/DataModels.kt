// DataModels.kt
package com.example.ai_voice_translater.data

import com.google.firebase.firestore.ServerTimestamp
import com.google.gson.annotations.SerializedName
import java.util.Date

/**
 * Represents the response from the Sarvam AI Speech-to-Text API.
 */
data class SpeechToTextResponse(
    val transcript: String?,
    @SerializedName("language_code")
    val language_code: String?, // The auto-detected language of the audio
    val error: String?
)

/**
 * Represents the request body for the Sarvam AI Text Translation API.
 */
data class TextTranslationRequest(
    @SerializedName("source_language_code")
    val sourceLanguage: String,
    @SerializedName("target_language_code")
    val targetLanguage: String,
    val input: String
)
/**
 * Represents the response from the Sarvam AI Text Translation API.
 */
data class TextTranslationResponse(
    @SerializedName("translated_text")
    val translatedText: String?,
    val error: String?
)


/**
 * Represents a single translation record to be stored in Firestore.
 * It now clearly separates the original transcription from the final translation.
 */
data class Translation(
    val userId: String = "",
    val originalText: String = "", // e.g., "Aap kaise hain?"
    val translatedText: String = "", // e.g., "How are you?"
    @ServerTimestamp
    val timestamp: Date? = null
)

/**
 * A simple data class to hold user information from Google Sign-In.
 */
data class UserData(
    val userId: String,
    val username: String?,
    val profilePictureUrl: String?
)
