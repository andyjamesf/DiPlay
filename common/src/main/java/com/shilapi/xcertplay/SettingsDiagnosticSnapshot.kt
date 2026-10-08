package com.shilapi.xcertplay

import android.content.Context

/**
 * The settings a report needs to be read without guessing.
 *
 * Without this, a setting's value has to be inferred from its effects: the music buffer from
 * `trackBufferFrames`, buffered music from a stream being accepted, location reporting from a
 * flag on the session-start line. Settings that leave no trace at all, such as audio focus,
 * cannot be inferred and have been mistaken for defaults.
 *
 * Secrets never appear. The hotspot name and passphrase are reported as configured or not, and
 * nothing here is a credential, because this block is built straight into the report and does
 * not pass through [DiagnosticRedactor], which only filters the session log files.
 */
internal object SettingsDiagnosticSnapshot {
    fun report(context: Context): String = buildString {
        appendLine("Audio: " +
            "mediaBufferMs=${AirPlayPersistence.loadMediaBufferMillis(context)}" +
            " bufferedMusic=${AirPlayPersistence.loadMainBufferedAudio(context)}" +
            " focus=${AirPlayPersistence.loadAudioFocusEnabled(context)}" +
            " focusAutoYield=${AirPlayPersistence.loadAudioFocusAutoYield(context)}" +
            " carBluetoothAudio=${AirPlayPersistence.loadCarBluetoothAudio(context)}")
        appendLine("Audio routing: " +
            "mediaChannel=${AirPlayPersistence.loadMediaAudioChannel(context)}" +
            " navigationChannel=${AirPlayPersistence.loadNavigationAudioChannel(context)}" +
            " navigationStream=${AirPlayPersistence.loadNavigationStreamType(context)}" +
            " usageRouting=${AirPlayPersistence.loadAdvancedAudioChannelMapping(context)}")
        appendLine("Call processing: " +
            "echoCancellation=${AirPlayPersistence.loadCallEchoCancellation(context)}" +
            " voiceFilter=${AirPlayPersistence.loadCallVoiceFilter(context)}")
        appendLine("Video: " +
            "fps=${AirPlayPersistence.loadFps(context)}" +
            " resolutionPercent=${AirPlayPersistence.loadDisplayScalePercent(context)}" +
            " hevc=${AirPlayPersistence.loadHevcEnabled(context)}" +
            " hevcSoftwareDecoder=${AirPlayPersistence.loadHevcSoftwareDecoderEnabled(context)}" +
            " smoothVideo=${AirPlayPersistence.loadSmoothVideo(context)}" +
            " adaptPipResolution=${AirPlayPersistence.loadAdaptPipResolution(context)}")
        appendLine("Connection: " +
            "wireless=${AirPlayPersistence.loadWirelessEnabled(context)}" +
            " hotspotMode=${AirPlayPersistence.loadWirelessHotspotMode(context)}" +
            " p2pPreferredChannel=${AirPlayPersistence.loadWifiP2pPreferredChannel(context)}" +
            " mfi=${AirPlayPersistence.loadMfiTarget(context)}" +
            " manualHotspotConfigured=${
                AirPlayPersistence.loadManualHotspotSsid(context).isNotEmpty() &&
                    AirPlayPersistence.loadManualHotspotPassphrase(context).isNotEmpty()
            }" +
            " manualHotspotBand=${AirPlayPersistence.loadManualHotspotBand(context)}" +
            " manualHotspotChannel=${AirPlayPersistence.loadManualHotspotChannel(context)}" +
            " savedNetworkConfigured=${AirPlayPersistence.loadExistingWifiSsid(context).isNotEmpty()}")
        appendLine("Navigation: " +
            "locationToIphone=${AirPlayPersistence.loadLocationReportingEnabled(context)}")
        append("Other: " +
            "gestureFingers=${AirPlayPersistence.loadSettingsGestureFingers(context)}" +
            " rightHandDrive=${AirPlayPersistence.loadRightHandDrive(context)}" +
            " hideTopBar=${AirPlayPersistence.loadHideTopBar(context)}" +
            " hideBottomBar=${AirPlayPersistence.loadHideBottomBar(context)}" +
            " openAfterBoot=${AirPlayPersistence.loadAutoStartOnBoot(context)}" +
            " debugLogs=${AirPlayPersistence.loadDebugLogsEnabled(context)}")
    }
}
