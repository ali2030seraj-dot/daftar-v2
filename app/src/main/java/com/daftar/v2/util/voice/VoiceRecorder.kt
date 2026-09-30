package com.daftar.v2.util.voice

import android.content.Context
import android.media.MediaRecorder
import android.os.Build
import java.io.File

/**
 * مسجّل صوتي بسيط — صيغة AAC (.m4a).
 */
class VoiceRecorder(private val context: Context) {

    private var recorder: MediaRecorder? = null
    private var currentFile: File? = null
    private var startTime: Long = 0L

    fun start(): File {
        stop()

        val dir = File(context.filesDir, "voice_notes").apply { mkdirs() }
        val file = File(dir, "voice_${System.currentTimeMillis()}.m4a")

        val mr = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            MediaRecorder(context)
        } else {
            @Suppress("DEPRECATION")
            MediaRecorder()
        }

        mr.apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setAudioEncodingBitRate(64000)
            setAudioSamplingRate(22050)
            setOutputFile(file.absolutePath)
            prepare()
            start()
        }

        recorder = mr
        currentFile = file
        startTime = System.currentTimeMillis()

        return file
    }

    fun stop(): File? {
        val mr = recorder ?: return null
        val file = currentFile

        return try {
            mr.stop()
            mr.release()
            recorder = null
            currentFile = null
            if (file != null && file.exists() && file.length() > 0) file else null
        } catch (e: Exception) {
            e.printStackTrace()
            try { mr.release() } catch (_: Exception) {}
            recorder = null
            currentFile = null
            file?.delete()
            null
        }
    }

    fun cancel() {
        val mr = recorder ?: return
        try {
            mr.stop()
            mr.release()
        } catch (_: Exception) {}
        recorder = null
        currentFile?.delete()
        currentFile = null
    }

    fun elapsedSeconds(): Int {
        if (startTime == 0L) return 0
        return ((System.currentTimeMillis() - startTime) / 1000).toInt()
    }

    fun isRecording(): Boolean = recorder != null
}