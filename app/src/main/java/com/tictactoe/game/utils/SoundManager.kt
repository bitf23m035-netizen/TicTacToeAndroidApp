package com.tictactoe.game.utils

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool

/**
 * Manages sound effects using [SoundPool].
 *
 * Uses resource-name lookup instead of direct R.raw.* references so the app
 * builds and runs correctly even when the optional audio files are absent.
 * To add sounds, place click.mp3 / win.mp3 / draw.mp3 in res/raw/.
 */
class SoundManager(private val context: Context) {

    private val soundPool: SoundPool = SoundPool.Builder()
        .setMaxStreams(3)
        .setAudioAttributes(
            AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_GAME)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
        )
        .build()

    // 0 = not loaded / file absent
    private val soundClick = loadByName("click")
    private val soundWin   = loadByName("win")
    private val soundDraw  = loadByName("draw")

    /** Load a raw resource by name — returns 0 silently if the file doesn't exist. */
    private fun loadByName(name: String): Int {
        val resId = context.resources.getIdentifier(name, "raw", context.packageName)
        if (resId == 0) return 0
        return runCatching { soundPool.load(context, resId, 1) }.getOrDefault(0)
    }

    fun playClick() = play(soundClick)
    fun playWin()   = play(soundWin)
    fun playDraw()  = play(soundDraw)

    private fun play(soundId: Int) {
        if (soundId != 0) soundPool.play(soundId, 1f, 1f, 1, 0, 1f)
    }

    fun release() = soundPool.release()
}
