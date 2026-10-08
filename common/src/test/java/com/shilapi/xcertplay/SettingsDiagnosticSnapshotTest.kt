package com.shilapi.xcertplay

import com.shilapi.xcertplay.orchestration.ManualHotspotBand
import com.shilapi.xcertplay.orchestration.WirelessHotspotMode
import org.junit.After
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27], manifest = Config.NONE)
class SettingsDiagnosticSnapshotTest {
    private val context get() = RuntimeEnvironment.getApplication()

    @After fun tearDown() {
        context.getSharedPreferences("xcertplay_airplay", 0).edit().clear().commit()
    }

    @Test fun theHotspotNameAndPassphraseNeverReachTheReport() {
        AirPlayPersistence.saveManualHotspotSsid(context, "AndysCarHotspot")
        AirPlayPersistence.saveManualHotspotPassphrase(context, "correct-horse-battery")
        AirPlayPersistence.saveExistingWifiCredentials(context, "HomeNetwork", "another-secret")

        val report = SettingsDiagnosticSnapshot.report(context)

        assertFalse(report.contains("AndysCarHotspot"))
        assertFalse(report.contains("correct-horse-battery"))
        assertFalse(report.contains("HomeNetwork"))
        assertFalse(report.contains("another-secret"))
        // What a reader actually needs: whether it is set up, not what it says.
        assertTrue(report.contains("manualHotspotConfigured=true"))
        assertTrue(report.contains("savedNetworkConfigured=true"))
    }

    @Test fun anUnconfiguredHotspotReadsAsUnconfigured() {
        val report = SettingsDiagnosticSnapshot.report(context)
        assertTrue(report.contains("manualHotspotConfigured=false"))
        assertTrue(report.contains("savedNetworkConfigured=false"))
    }

    @Test fun thePassphraseAloneIsNotEnoughToCountAsConfigured() {
        AirPlayPersistence.saveManualHotspotPassphrase(context, "orphan-secret")
        val report = SettingsDiagnosticSnapshot.report(context)
        assertFalse(report.contains("orphan-secret"))
        assertTrue(report.contains("manualHotspotConfigured=false"))
    }

    @Test fun theSettingsThatCannotBeInferredFromEffectsAreAllReported() {
        AirPlayPersistence.saveAudioFocusEnabled(context, true)
        AirPlayPersistence.saveMainBufferedAudio(context, true)
        AirPlayPersistence.saveMediaBufferMillis(context, 1000)
        AirPlayPersistence.saveHevcEnabled(context, true)
        AirPlayPersistence.saveLocationReportingEnabled(context, true)
        AirPlayPersistence.saveCarBluetoothAudio(context, true)
        AirPlayPersistence.saveWirelessHotspotMode(context, WirelessHotspotMode.MANUAL)
        AirPlayPersistence.saveManualHotspotBand(context, ManualHotspotBand.GHZ_2_4)

        val report = SettingsDiagnosticSnapshot.report(context)

        listOf(
            "focus=true",
            "focusAutoYield=true",
            "bufferedMusic=true",
            "mediaBufferMs=1000",
            "hevc=true",
            "locationToIphone=true",
            "carBluetoothAudio=true",
            "hotspotMode=MANUAL",
            "manualHotspotBand=GHZ_2_4",
        ).forEach { assertTrue(it, report.contains(it)) }
    }

    @Test fun everyLineSurvivesTheRedactorThatFiltersTheSessionLogs() {
        AirPlayPersistence.saveManualHotspotSsid(context, "AndysCarHotspot")
        AirPlayPersistence.saveManualHotspotPassphrase(context, "correct-horse-battery")
        // The snapshot is written straight into the report, so the redactor never sees it. Hold it
        // to that standard anyway: a line it would drop is a line carrying something it mistrusts.
        SettingsDiagnosticSnapshot.report(context).lines().forEach { line ->
            assertTrue(line, DiagnosticRedactor.redact(line) != null)
        }
    }
}
