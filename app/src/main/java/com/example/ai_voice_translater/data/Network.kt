// Network.kt
package com.example.ai_voice_translater.data

import okhttp3.MultipartBody
import okhttp3.OkHttpClient
import okhttp3.RequestBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.http.Body
import retrofit2.http.Header
import retrofit2.http.Multipart
import retrofit2.http.POST
import retrofit2.http.Part
import java.util.concurrent.TimeUnit

// Base URL for the Sarvam AI API
private const val BASE_URL = "https://api.sarvam.ai/"

/**
 * Retrofit service interface for the Sarvam AI API.
 */
interface SarvamApiService {

    // Endpoint for Speech-to-Text
    @Multipart
    @POST("speech-to-text")
    suspend fun transcribeAudio(
        @Header("api-subscription-key") apiKey: String,
        @Part("language_code") languageCode: RequestBody, // Changed from String to RequestBody
        @Part file: MultipartBody.Part
    ): Response<SpeechToTextResponse>

    // Endpoint for Text-to-Text Translation
    @POST("translate")
    suspend fun translateText(
        @Header("api-subscription-key") apiKey: String,
        @Body request: TextTranslationRequest
    ): Response<TextTranslationResponse>
}

/**
 * Singleton object to provide a Retrofit instance.
 */
object RetrofitClient {
    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor(loggingInterceptor) // Add logging for debugging
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .build()

    val instance: SarvamApiService by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(SarvamApiService::class.java)
    }
}
