package com.madak.spices.designsystem.splash

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.media.MediaPlayer
import com.madak.spices.designsystem.R

/**
 * Plays the Madak intro sound design (whoosh → spice sprinkle → wordmark thump → leaf pluck →
 * shimmer → warm chord) timed to [MadakMotionSplash]. Silent when the phone is on silent/vibrate.
 */
class SplashSound(private val context: Context) {
    private var player: MediaPlayer? = null

    fun play() {
        val audio = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
        if (audio != null && audio.ringerMode != AudioManager.RINGER_MODE_NORMAL) return
        runCatching {
            val attrs = AudioAttributes.Builder()
                .setUsage(AudioAttributes.USAGE_MEDIA)
                .setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION)
                .build()
            val session = audio?.generateAudioSessionId() ?: AudioManager.AUDIO_SESSION_ID_GENERATE
            player = MediaPlayer.create(context, R.raw.madak_intro, attrs, session)?.apply {
                setVolume(0.9f, 0.9f)
                setOnCompletionListener { release(); if (player === it) player = null }
                start()
            }
        }
    }

    fun fadeOutAndRelease() {
        runCatching { player?.run { if (isPlaying) stop(); release() } }
        player = null
    }
}
