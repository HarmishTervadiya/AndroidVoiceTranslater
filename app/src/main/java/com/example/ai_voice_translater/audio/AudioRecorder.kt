package com.example.ai_voice_translater.audio

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

/**
 * A helper class to manage audio recording.
 * It handles starting and stopping the MediaRecorder.
 */
class AudioRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null

    // The output file where the recording will be saved.
    private val outputFile: File by lazy {
        File(context.cacheDir, "audiorecord.m4a")
    }

    /**
     * Starts the audio recording.
     * Initializes MediaRecorder and sets it up.
     * Throws IOException if prepare fails.
     */
    fun start() {
        // Create a new recorder for each recording session.
        recorder = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            // Use MPEG_4 container for AAC audio
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            // Use a modern, high-quality audio encoder
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            // Explicitly set the sampling rate to match the API's recommendation
            setAudioSamplingRate(16000)
            setOutputFile(FileOutputStream(outputFile).fd)
        }

        try {
            recorder?.prepare()
            recorder?.start()
        } catch (e: IOException) {
            Log.e("AudioRecorder", "prepare() failed", e)
            // Clean up the recorder if prepare fails
            releaseRecorder()
            // Rethrow the exception to inform the caller
            throw e
        } catch (e: IllegalStateException) {
            Log.e("AudioRecorder", "start() failed", e)
            releaseRecorder()
            throw e
        }
    }

    /**
     * Stops the audio recording.
     * @return The File object of the saved recording, or null if it failed.
     */
    fun stop(): File? {
        return try {
            recorder?.stop()
            releaseRecorder()
            outputFile
        } catch (e: Exception) {
            Log.e("AudioRecorder", "stop() failed", e)
            // Ensure the recorder is released even if stop fails
            releaseRecorder()
            null
        }
    }

    /**
     * Safely releases the MediaRecorder resources.
     */
    private fun releaseRecorder() {
        recorder?.apply {
            reset()
            release()
        }
        recorder = null
    }
}