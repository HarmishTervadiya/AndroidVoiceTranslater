// TranslationRepository.kt
package com.example.ai_voice_translater.data

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.auth.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.File

/**
 * Repository for handling the complete two-step translation process.
 */
class TranslationRepository {

    private val apiService = RetrofitClient.instance
    private val firestore = Firebase.firestore
    private val auth = Firebase.auth

    /**
     * Performs the full translation flow:
     * 1. Transcribes audio with auto-detection.
     * 2. Translates the resulting text to the target language.
     * @return A Result wrapper containing the final translated text and the original transcription.
     */
    suspend fun getFullTranslation(
        audioFile: File,
        apiKey: String,
        targetLanguageCode: String
    ): Result<Pair<String, String>> { // Returns (Original, Translated)
        try {
            // STEP 1: Transcribe the audio
            val transcriptionResult = transcribeAudio(audioFile, apiKey)
            val originalText = transcriptionResult.getOrThrow().transcript
            val detectedLanguage = transcriptionResult.getOrThrow().language_code

            Log.d("Results", originalText + detectedLanguage);
            if (originalText.isNullOrEmpty() || detectedLanguage.isNullOrEmpty()) {
                return Result.failure(Exception("Could not transcribe audio."))
            }

            Log.d("TranslationRepository", "Original text: $originalText, Detected language: $detectedLanguage")

            // STEP 2: Translate the transcribed text
            val translationResult = translateText(originalText, detectedLanguage, targetLanguageCode, apiKey)
            val translatedText = translationResult.getOrThrow().translatedText

            if (translatedText.isNullOrEmpty()) {
                return Result.failure(Exception("Could not translate text."))
            }

            // Return both the original and translated text
            return Result.success(Pair(originalText, translatedText))

        } catch (e: Exception) {
            Log.e("TranslationRepository", "Full translation error", e)
            return Result.failure(e)
        }
    }

    private suspend fun transcribeAudio(audioFile: File, apiKey: String): Result<SpeechToTextResponse> {
        return try {
            // Log file details for debugging
            Log.d("TranslationRepository", "Audio file: ${audioFile.name}, Size: ${audioFile.length()} bytes, Exists: ${audioFile.exists()}")
            Log.d("TranslationRepository", "File path: ${audioFile.absolutePath}")

            // Determine media type based on file extension
            val mediaType = when (audioFile.extension.lowercase()) {
                "mp3" -> "audio/mp3"
                "mp4" -> "audio/mp4"
                "m4a" -> "audio/mp4"
                "wav" -> "audio/wav"
                "aac" -> "audio/aac"
                "webm" -> "audio/webm"
                "ogg" -> "audio/ogg"
                else -> "audio/mp4" // default fallback
            }

            val requestFile = audioFile.asRequestBody(mediaType.toMediaTypeOrNull())
            val filePart = MultipartBody.Part.createFormData("file", audioFile.name, requestFile)

            // Create language_code as RequestBody (not String)
            val languageCodePart = "unknown".toRequestBody("text/plain".toMediaTypeOrNull())

            Log.d("TranslationRepository", "Making API call with key: ${apiKey.take(8)}...")

            val response = apiService.transcribeAudio(apiKey, languageCodePart, filePart)

            Log.d("TranslationRepository", "API Response - Code: ${response.code()}, Success: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                Log.d("TranslationRepository", "Transcription successful: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TranslationRepository", "API Error (Transcription) - Code: ${response.code()}, Message: ${response.message()}, Body: $errorBody")
                Result.failure(Exception("API Error (Transcription): ${response.code()} - ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("TranslationRepository", "Exception during transcription", e)
            Result.failure(e)
        }
    }

    private suspend fun translateText(
        text: String,
        sourceLang: String,
        targetLang: String,
        apiKey: String
    ): Result<TextTranslationResponse> {
        return try {
            val request = TextTranslationRequest(
                sourceLanguage = sourceLang,
                targetLanguage = targetLang,
                input = text
            )

            Log.d("TranslationRepository", "Translation request: $request")

            val response = apiService.translateText(apiKey, request)

            Log.d("TranslationRepository", "Translation Response - Code: ${response.code()}, Success: ${response.isSuccessful}")

            if (response.isSuccessful && response.body() != null) {
                Log.d("TranslationRepository", "Translation successful: ${response.body()}")
                Result.success(response.body()!!)
            } else {
                val errorBody = response.errorBody()?.string()
                Log.e("TranslationRepository", "API Error (Translation) - Code: ${response.code()}, Message: ${response.message()}, Body: $errorBody")
                Result.failure(Exception("API Error (Translation): ${response.code()} - ${response.message()} - $errorBody"))
            }
        } catch (e: Exception) {
            Log.e("TranslationRepository", "Exception during translation", e)
            Result.failure(e)
        }
    }

    suspend fun saveTranslation(originalText: String, translatedText: String) {
        try {
            val userId = auth.currentUser?.uid ?: return
            val translation = Translation(
                userId = userId,
                originalText = originalText,
                translatedText = translatedText
            )
            firestore.collection("translations").add(translation).await()
            Log.d("TranslationRepository", "Translation saved successfully")
        } catch (e: Exception) {
            Log.e("TranslationRepository", "Error saving translation", e)
        }
    }

    fun getTranslationHistory(): Flow<List<Translation>> = callbackFlow {
        val userId = auth.currentUser?.uid
        if (userId == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        Log.d("UserId", userId)
        val listener = firestore
            .collection("translations")
            .whereEqualTo("userId", userId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                if (snapshot != null) {
                    val translations = snapshot.toObjects(Translation::class.java)
                    trySend(translations)
                }
            }

        awaitClose { listener.remove() }
    }

    suspend fun deleteTranslation(translationId: String) {
        try {
            firestore
                .collection("translations").document(translationId)
                .delete()
                .await()
        } catch (e: Exception) {
            Log.e("TranslationRepo", "Error deleting translation", e)
        }
    }
}