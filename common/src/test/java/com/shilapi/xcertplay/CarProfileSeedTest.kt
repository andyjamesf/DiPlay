package com.shilapi.xcertplay

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27], manifest = Config.NONE)
class CarProfileSeedTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @After fun tearDown() {
        context.getSharedPreferences("xcertplay_airplay", 0).edit().clear().commit()
    }

    @Test fun itDoesNothingWhereTheProfileIsOff() {
        CarProfileSeed.enforce(context, enabled = false)
        assertFalse(AirPlayPersistence.loadMainBufferedAudio(context))
        assertEquals(300, AirPlayPersistence.loadMediaBufferMillis(context))
    }

    @Test fun theAbsentSettingsAreFilledIn() {
        CarProfileSeed.enforce(context, enabled = true)
        assertTrue(AirPlayPersistence.loadMainBufferedAudio(context))
        assertEquals(1000, AirPlayPersistence.loadMediaBufferMillis(context))
        assertTrue(AirPlayPersistence.loadHevcEnabled(context))
        assertTrue(AirPlayPersistence.loadLocationReportingEnabled(context))
    }

    @Test fun aChoiceAlreadyMadeIsLeftAlone() {
        AirPlayPersistence.saveMainBufferedAudio(context, false)
        AirPlayPersistence.saveMediaBufferMillis(context, 500)
        AirPlayPersistence.saveHevcEnabled(context, false)

        CarProfileSeed.enforce(context, enabled = true)

        assertFalse(AirPlayPersistence.loadMainBufferedAudio(context))
        assertEquals(500, AirPlayPersistence.loadMediaBufferMillis(context))
        assertFalse(AirPlayPersistence.loadHevcEnabled(context))
    }

    @Test fun audioFocusIsNeverSeededOn() {
        CarProfileSeed.enforce(context, enabled = true)
        assertFalse(AirPlayPersistence.loadAudioFocusEnabled(context))
    }

    @Test fun audioFocusIsForcedOffEvenWhenItWasTurnedOn() {
        // This is the regression: an earlier version seeded it on, and this head unit then
        // silenced media completely. It is a known-bad value here, not a preference.
        AirPlayPersistence.saveAudioFocusEnabled(context, true)
        CarProfileSeed.enforce(context, enabled = true)
        assertFalse(AirPlayPersistence.loadAudioFocusEnabled(context))
    }

    @Test fun audioFocusStaysOffAcrossRepeatedRuns() {
        repeat(3) {
            AirPlayPersistence.saveAudioFocusEnabled(context, true)
            CarProfileSeed.enforce(context, enabled = true)
            assertFalse(AirPlayPersistence.loadAudioFocusEnabled(context))
        }
    }

    @Test fun theNarrowbandVoiceChannelsAreClearedFromTheMediaStream() {
        listOf(6, 8).forEach { unsafe ->
            AirPlayPersistence.saveMediaAudioChannel(context, unsafe)
            CarProfileSeed.enforce(context, enabled = true)
            assertEquals("channel $unsafe", 0, AirPlayPersistence.loadMediaAudioChannel(context))
        }
    }

    @Test fun aUsableMediaStreamChoiceSurvives() {
        AirPlayPersistence.saveMediaAudioChannel(context, 3)
        CarProfileSeed.enforce(context, enabled = true)
        assertEquals(3, AirPlayPersistence.loadMediaAudioChannel(context))
    }
}
