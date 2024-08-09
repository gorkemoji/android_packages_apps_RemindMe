package com.gorkemoji.remindme.sound

import android.content.Context
import android.media.SoundPool
import com.gorkemoji.remindme.R

class SoundManager(context: Context) {
    private val soundPool: SoundPool = SoundPool.Builder().setMaxStreams(1).build()
    private val clickSoundId: Int = soundPool.load(context, R.raw.pencil_done, 1)

    fun playClickSound() {
        soundPool.play(clickSoundId, 1f, 1f, 1, 0, 1f)
    }
}
