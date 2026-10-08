package com.shilapi.xcertplay

import android.content.Context

/**
 * Whether the previous run ended without a clean stop.
 *
 * [ProcessExitDiagnostics] answers this from `ApplicationExitInfo`, which needs API 30. A head
 * unit on API 27 reports `available=false requiresApi=30` and nothing else, on exactly the device
 * class whose power is cut at ignition-off. This records it ourselves instead, so a crash, a
 * low-memory kill and a power cut can be told apart from an ordinary exit.
 *
 * Writes use `commit()` rather than `apply()` on purpose: the whole point is the mark surviving
 * a cut that gives the process no chance to flush.
 */
internal object UncleanShutdownDiagnostics {
    private const val PREFS = "diplay_shutdown"
    private const val KEY_RUNNING_SINCE = "running_since"
    private const val KEY_LAST_UNCLEAN = "last_unclean"
    private const val KEY_UNCLEAN_COUNT = "unclean_count"
    private const val KEY_CLEAN_COUNT = "clean_count"

    private fun prefs(context: Context) = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)

    /** Marks a run as in progress, and notices that the one before it never cleared its mark. */
    fun sessionStarted(context: Context, nowMillis: Long = System.currentTimeMillis()) {
        val prefs = prefs(context)
        val previous = prefs.getLong(KEY_RUNNING_SINCE, 0L)
        prefs.edit().apply {
            if (previous != 0L) {
                putLong(KEY_LAST_UNCLEAN, previous)
                putInt(KEY_UNCLEAN_COUNT, prefs.getInt(KEY_UNCLEAN_COUNT, 0) + 1)
            }
            putLong(KEY_RUNNING_SINCE, nowMillis)
        }.commit()
    }

    fun sessionStoppedCleanly(context: Context) {
        val prefs = prefs(context)
        if (prefs.getLong(KEY_RUNNING_SINCE, 0L) == 0L) return
        prefs.edit()
            .putLong(KEY_RUNNING_SINCE, 0L)
            .putInt(KEY_CLEAN_COUNT, prefs.getInt(KEY_CLEAN_COUNT, 0) + 1)
            .commit()
    }

    fun report(context: Context, nowMillis: Long = System.currentTimeMillis()): String {
        val prefs = prefs(context)
        val unclean = prefs.getInt(KEY_UNCLEAN_COUNT, 0)
        val clean = prefs.getInt(KEY_CLEAN_COUNT, 0)
        val last = prefs.getLong(KEY_LAST_UNCLEAN, 0L)
        val running = prefs.getLong(KEY_RUNNING_SINCE, 0L)
        return buildString {
            append("uncleanStops=$unclean cleanStops=$clean")
            if (last != 0L) {
                append(" lastUncleanStartedMinutesAgo=${(nowMillis - last).coerceAtLeast(0) / 60_000}")
            }
            append(if (running != 0L) {
                " thisRunStartedMinutesAgo=${(nowMillis - running).coerceAtLeast(0) / 60_000}"
            } else {
                " noRunMarked"
            })
        }
    }
}
