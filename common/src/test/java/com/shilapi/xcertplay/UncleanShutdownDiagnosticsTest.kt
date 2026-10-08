package com.shilapi.xcertplay

import org.junit.After
import org.junit.Assert.assertTrue
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [27], manifest = Config.NONE)
class UncleanShutdownDiagnosticsTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val minute = 60_000L

    @After fun tearDown() {
        context.getSharedPreferences("diplay_shutdown", 0).edit().clear().commit()
    }

    @Test fun aCleanStopLeavesNothingToReport() {
        UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = 10 * minute)
        UncleanShutdownDiagnostics.sessionStoppedCleanly(context)
        val report = UncleanShutdownDiagnostics.report(context, nowMillis = 20 * minute)
        assertTrue(report, report.contains("uncleanStops=0"))
        assertTrue(report, report.contains("cleanStops=1"))
        assertTrue(report, report.contains("noRunMarked"))
    }

    @Test fun aRunThatNeverStoppedIsNoticedByTheNextOne() {
        // The ignition goes off here: no onDestroy, so the mark stays.
        UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = 10 * minute)
        UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = 70 * minute)

        val report = UncleanShutdownDiagnostics.report(context, nowMillis = 75 * minute)
        assertTrue(report, report.contains("uncleanStops=1"))
        assertTrue(report, report.contains("cleanStops=0"))
        assertTrue(report, report.contains("lastUncleanStartedMinutesAgo=65"))
        assertTrue(report, report.contains("thisRunStartedMinutesAgo=5"))
    }

    @Test fun repeatedCutsAccumulate() {
        repeat(4) { index ->
            UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = (index + 1) * minute)
        }
        val report = UncleanShutdownDiagnostics.report(context, nowMillis = 10 * minute)
        assertTrue(report, report.contains("uncleanStops=3"))
    }

    @Test fun aCleanStopAfterACutKeepsTheEarlierCountAndClearsTheMark() {
        UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = 1 * minute)
        UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = 2 * minute)
        UncleanShutdownDiagnostics.sessionStoppedCleanly(context)

        val report = UncleanShutdownDiagnostics.report(context, nowMillis = 3 * minute)
        assertTrue(report, report.contains("uncleanStops=1"))
        assertTrue(report, report.contains("cleanStops=1"))
        assertTrue(report, report.contains("noRunMarked"))
    }

    @Test fun aSecondCleanStopWithNoRunInProgressDoesNotCountTwice() {
        UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = 1 * minute)
        UncleanShutdownDiagnostics.sessionStoppedCleanly(context)
        UncleanShutdownDiagnostics.sessionStoppedCleanly(context)
        val report = UncleanShutdownDiagnostics.report(context, nowMillis = 2 * minute)
        assertTrue(report, report.contains("cleanStops=1"))
    }

    @Test fun theReportSurvivesTheRedactor() {
        UncleanShutdownDiagnostics.sessionStarted(context, nowMillis = 1 * minute)
        val line = "Own shutdown record (works below Android 11): " +
            UncleanShutdownDiagnostics.report(context, nowMillis = 2 * minute)
        assertTrue(line, DiagnosticRedactor.redact(line) != null)
    }
}
