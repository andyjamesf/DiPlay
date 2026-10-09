package com.shilapi.xcertplay.media

import android.media.AudioManager
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/**
 * The media audio stream setting takes a number from 1 to 20 and hands it to
 * `setLegacyStreamType`. Most of those numbers are wrong for music and some are dangerous: a
 * driver set 6 and the head unit produced noise loud enough to hurt, because stream 6 is the
 * narrowband Bluetooth hands-free voice channel.
 */
class MediaStreamSafetyTest {
    @Test fun theNarrowbandVoiceStreamsAreRefusedForMedia() {
        // 6 is STREAM_BLUETOOTH_SCO, hidden in the public SDK, hence the literals.
        listOf(0, 6, 8).forEach { assertTrue("stream $it", it in MEDIA_UNSAFE_STREAMS) }
    }

    @Test fun theStreamsPeopleActuallyWantForMusicStayAvailable() {
        listOf(
            AudioManager.STREAM_MUSIC,
            AudioManager.STREAM_SYSTEM,
            AudioManager.STREAM_RING,
            AudioManager.STREAM_ALARM,
            AudioManager.STREAM_NOTIFICATION,
            AudioManager.STREAM_ACCESSIBILITY,
        ).forEach { assertFalse("stream $it", it in MEDIA_UNSAFE_STREAMS) }
    }

    @Test fun theSetMatchesTheAndroidConstantsItStandsFor() {
        assertTrue(AudioManager.STREAM_VOICE_CALL in MEDIA_UNSAFE_STREAMS)
        assertTrue(AudioManager.STREAM_DTMF in MEDIA_UNSAFE_STREAMS)
        // Nothing beyond the three: the setting exists to reach odd vehicle buses.
        assertTrue(MEDIA_UNSAFE_STREAMS.size == 3)
    }
}
