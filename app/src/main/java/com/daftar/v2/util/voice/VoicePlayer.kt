package com.daftar.v2.util.voice

import android.media.MediaPlayer
import java.io.File

/**
 * مشغّل صوتي بسيط.
 */
class VoicePlayer {

    private var player: MediaPlayer? = null
    private var currentPath: String? = null

    fun play(path: String, onComplete: () -> Unit = {}) {
        stop()

        try {
            val file = File(path)
            if (!file.exists()) {
                onComplete()
                return
            }

            val mp = MediaPlayer()
            mp.setDataSource(path)
            mp.setOnCompletionListener {
                stop()
                onComplete()
            }
            mp.setOnErrorListener { _, _, _ ->
                stop()
                onComplete()
                true
            }
            mp.prepare()
            mp.start()

            player = mp
            currentPath = path
        } catch (e: Exception) {
            e.printStackTrace()
            stop()
            onComplete()
        }
    }

    fun stop() {
        try {
            player?.stop()
            player?.release()
        } catch (_: Exception) {}
        player = null
        currentPath = null
    }

    fun isPlaying(path: String): Boolean =
        currentPath == path && player?.isPlaying == true
}