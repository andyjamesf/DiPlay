package com.shilapi.xcertplay.media

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PlaybackDiagnosticLineTest {
    @Test fun aPlayingTrackReportsItsStateWithoutItsName() {
        val line = CarPlayNowPlaying(
            title = "Paranoid Android",
            album = "OK Computer",
            artist = "Radiohead",
            sourceApp = "com.apple.Music",
            durationMillis = 383_000,
            elapsedMillis = 12_000,
            playing = true,
        ).diagnosticLine()

        assertEquals(
            "Playback: playing=true sourceApp=com.apple.Music titlePresent=true" +
                " durationMs=383000 elapsedMs=12000",
            line,
        )
        // The point of the line is the state, not the listening history.
        listOf("Paranoid Android", "OK Computer", "Radiohead").forEach {
            assertFalse(it, line.contains(it))
        }
    }

    @Test fun anEmptyStateSaysSoRatherThanPrintingNulls() {
        val line = CarPlayNowPlaying().diagnosticLine()
        assertEquals(
            "Playback: playing=false sourceApp=unknown titlePresent=false durationMs=-1 elapsedMs=-1",
            line,
        )
    }

    @Test fun anEmptySourceAppReadsAsUnknown() {
        val line = CarPlayNowPlaying(sourceApp = "", playing = true).diagnosticLine()
        assertTrue(line, line.contains("sourceApp=unknown"))
    }

    @Test fun anEmptyTitleCountsAsAbsent() {
        assertTrue(CarPlayNowPlaying(title = "").diagnosticLine().contains("titlePresent=false"))
        assertTrue(CarPlayNowPlaying(title = "x").diagnosticLine().contains("titlePresent=true"))
    }
}
