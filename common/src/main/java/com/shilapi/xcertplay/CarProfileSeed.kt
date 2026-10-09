package com.shilapi.xcertplay

import android.content.Context
import com.shilapi.xcertplay.host.R

/**
 * Settles this car's settings, so the app arrives configured for an `alps FF-5000`.
 *
 * This exists instead of changing the defaults in [AirPlayPersistence]. Changing them there broke
 * eight tests that assert what the defaults are, which costs the test suite as a safety net on the
 * very branch that carries the most experimental work.
 *
 * Only keys that are absent are written, so a choice the driver made deliberately is never
 * overridden — including turning one of these back off.
 *
 * Audio focus is deliberately NOT seeded. It was, and it was wrong: this head unit grants the
 * focus request and takes it back about two milliseconds later with AUDIOFOCUS_LOSS, every time,
 * after which it keeps the focus and silences our output. Media stops playing entirely. The
 * setting's own description already warned that some head units handle focus badly.
 *
 * Each value is here because this unit's diagnostic reports justify it:
 *  - buffered music: measured queueing 108-117 s ahead over TCP here.
 *  - 1000 ms media buffer: AudioTrack underruns 0.207 -> 0.156 per sample.
 *  - HEVC: `video=HEVC decoder=hardware` works, and fewer bits matter on a 2.4 GHz link with no
 *    5 GHz available.
 *  - location reporting: otherwise Apple Maps falls back to the iPhone's own GPS.
 */
internal object CarProfileSeed {
    private const val PREFS = "xcertplay_airplay" // the same store AirPlayPersistence writes
    private const val KEY_SEEDED = "car_profile_seeded"

    /** 0 is usage routing; 6 is STREAM_BLUETOOTH_SCO and 8 is STREAM_DTMF. */
    private val MEDIA_UNSAFE_CHANNELS = setOf(6, 8)

    /**
     * [enabled] is off in the library and on in the app module, the same way
     * config_advanced_audio_channel_mapping is done, so the library's own unit tests keep asserting
     * the real defaults. It is a parameter so those tests can still exercise this directly.
     */
    fun enforce(
        context: Context,
        enabled: Boolean = context.resources.getBoolean(R.bool.config_car_profile_seed),
    ) {
        if (!enabled) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        val edit = prefs.edit()
        // Forced, not seeded, and applied on every run rather than once: this head unit grants the
        // focus request and takes it back about two milliseconds later with AUDIOFOCUS_LOSS, every
        // time, after which it keeps the focus and silences media completely. An earlier version of
        // this file seeded it ON, which stopped music playing on a car that had always worked. It is
        // not a preference here, it is a known-bad value for this hardware.
        if (prefs.getBoolean("audio_focus_enabled", false)) edit.putBoolean("audio_focus_enabled", false)
        // Likewise 6 is STREAM_BLUETOOTH_SCO, a narrowband voice channel that produced painful
        // noise on this unit. AndroidMediaSink refuses it at runtime; clear it so the screen does
        // not keep showing a value that cannot be honoured.
        if (prefs.getInt("media_audio_channel", 0) in MEDIA_UNSAFE_CHANNELS) {
            edit.putInt("media_audio_channel", 0)
        }
        if (!prefs.contains("main_buffered_audio")) edit.putBoolean("main_buffered_audio", true)
        if (!prefs.contains("media_buffer_ms")) edit.putInt("media_buffer_ms", 1000)
        if (!prefs.contains("hevc_enabled")) edit.putBoolean("hevc_enabled", true)
        if (!prefs.contains("location_reporting_enabled")) {
            edit.putBoolean("location_reporting_enabled", true)
        }
        // commit, not apply: a head unit can lose power before an async write reaches disk.
        if (!prefs.getBoolean(KEY_SEEDED, false)) edit.putBoolean(KEY_SEEDED, true)
        edit.commit()
    }
}
