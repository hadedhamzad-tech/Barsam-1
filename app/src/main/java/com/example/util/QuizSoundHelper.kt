package com.example.util

import android.content.Context
import android.media.AudioManager
import android.media.ToneGenerator
import android.os.Build
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.util.Log

object QuizSoundHelper {
    private var toneGenerator: ToneGenerator? = null
    private const val PREFS_NAME = "physics_sound_prefs"
    private const val KEY_SOUND_ENABLED = "quiz_sound_effects_enabled"

    init {
        try {
            toneGenerator = ToneGenerator(AudioManager.STREAM_MUSIC, 85)
        } catch (e: Exception) {
            Log.e("QuizSoundHelper", "Could not initialize ToneGenerator", e)
        }
    }

    fun isSoundEnabled(context: Context): Boolean {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return sp.getBoolean(KEY_SOUND_ENABLED, true)
    }

    fun setSoundEnabled(context: Context, enabled: Boolean) {
        val sp = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        sp.edit().putBoolean(KEY_SOUND_ENABLED, enabled).apply()
    }

    /**
     * Checks if the device is currently in silent or vibrate-only ringer mode.
     */
    private fun isDeviceSilent(context: Context): Boolean {
        return try {
            val audioManager = context.getSystemService(Context.AUDIO_SERVICE) as? AudioManager
            val ringerMode = audioManager?.ringerMode ?: AudioManager.RINGER_MODE_NORMAL
            ringerMode != AudioManager.RINGER_MODE_NORMAL
        } catch (_: Exception) {
            false
        }
    }

    /**
     * پخش افکت صوتی دلنشین و لرزش ملایم برای پاسخ صحیح
     * احترام به سوییچ کاربر و حالت سکوت (Silent Mode) گوشی
     */
    fun playCorrectSound(context: Context? = null) {
        val ctx = context
        if (ctx != null && !isSoundEnabled(ctx)) {
            return
        }

        val deviceSilent = ctx?.let { isDeviceSilent(it) } ?: false

        if (!deviceSilent) {
            try {
                // TONE_PROP_BEEP2: صدای زنگ شاداب و تاییدکننده
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_BEEP2, 180)
            } catch (e: Exception) {
                Log.e("QuizSoundHelper", "Error playing correct sound", e)
            }
        }

        ctx?.let { c ->
            vibrate(c, longArrayOf(0, 40, 30, 60))
        }
    }

    /**
     * پخش افکت صوتی بم و خطادار برای پاسخ نادرست
     * احترام به سوییچ کاربر و حالت سکوت (Silent Mode) گوشی
     */
    fun playIncorrectSound(context: Context? = null) {
        val ctx = context
        if (ctx != null && !isSoundEnabled(ctx)) {
            return
        }

        val deviceSilent = ctx?.let { isDeviceSilent(it) } ?: false

        if (!deviceSilent) {
            try {
                // TONE_PROP_NACK: صدای خطای ملایم
                toneGenerator?.startTone(ToneGenerator.TONE_PROP_NACK, 220)
            } catch (e: Exception) {
                Log.e("QuizSoundHelper", "Error playing incorrect sound", e)
            }
        }

        ctx?.let { c ->
            vibrate(c, longArrayOf(0, 100))
        }
    }

    private fun vibrate(context: Context, timings: LongArray) {
        try {
            val vibrator = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                val vibratorManager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as? VibratorManager
                vibratorManager?.defaultVibrator
            } else {
                @Suppress("DEPRECATION")
                context.getSystemService(Context.VIBRATOR_SERVICE) as? Vibrator
            }

            if (vibrator != null && vibrator.hasVibrator()) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    val effect = VibrationEffect.createWaveform(timings, -1)
                    vibrator.vibrate(effect)
                } else {
                    @Suppress("DEPRECATION")
                    vibrator.vibrate(timings, -1)
                }
            }
        } catch (_: Exception) {
            // Silently ignore if vibration permission not granted or device muted
        }
    }
}
