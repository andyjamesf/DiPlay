package com.shilapi.xcertplay

import android.content.Context
import com.shilapi.xcertplay.host.R

/**
 * Writes this car's settings once, so the app arrives configured for an `alps FF-5000`.
 *
 * This exists instead of changing the defaults in [AirPlayPersistence]. Changing them there broke
 * eight tests that assert what the defaults are, which costs the test suite as a safety net on the
 * very branch that carries the most experimental work.
 *
 * Only keys that are absent are written, so a choice the driver made deliberately is never
 * overridden — including turning one of these back off.
 *
 * Each value is here because this unit's diagnostic reports justify it:
 *  - audio focus: after a call the media track reports `playState=3` with nothing audible, and
 *    every line carries `focus=false`, so nothing tells the mixer the call ended.
 *  - buffered music: measured queueing 108-117 s ahead over TCP here.
 *  - 1000 ms media buffer: AudioTrack underruns 0.207 -> 0.156 per sample.
 *  - HEVC: `video=HEVC decoder=hardware` works, and fewer bits matter on a 2.4 GHz link with no
 *    5 GHz available.
 *  - location reporting: otherwise Apple Maps falls back to the iPhone's own GPS.
 */
internal object CarProfileSeed {
    private const val PREFS = "xcertplay_airplay" // the same store AirPlayPersistence writes
    private const val KEY_SEEDED = "car_profile_seeded"

    fun applyOnce(context: Context) {
        // Off in the library, on in the app module, the same way config_advanced_audio_channel_mapping
        // is done. That keeps it out of the library's own unit tests, which assert the real defaults.
        if (!context.resources.getBoolean(R.bool.config_car_profile_seed)) return
        val prefs = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
        if (prefs.getBoolean(KEY_SEEDED, false)) return
        val edit = prefs.edit()
        if (!prefs.contains("audio_focus_enabled")) edit.putBoolean("audio_focus_enabled", true)
        if (!prefs.contains("main_buffered_audio")) edit.putBoolean("main_buffered_audio", true)
        if (!prefs.contains("media_buffer_ms")) edit.putInt("media_buffer_ms", 1000)
        if (!prefs.contains("hevc_enabled")) edit.putBoolean("hevc_enabled", true)
        if (!prefs.contains("location_reporting_enabled")) {
            edit.putBoolean("location_reporting_enabled", true)
        }
        // commit, not apply: a head unit can lose power before an async write reaches disk, and
        // seeding twice would overwrite a choice made between the two runs.
        edit.putBoolean(KEY_SEEDED, true).commit()
    }
}
