package com.archivotemporal.launcher

import android.os.ParcelFileDescriptor
import android.util.Log
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import androidx.test.platform.app.InstrumentationRegistry
import java.io.FileInputStream
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class PerformanceSmokeTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Before
    fun resetRuntimeSignals() {
        shell("logcat -c")
        shell("dumpsys gfxinfo $PACKAGE_NAME reset")
    }

    @Test
    fun rapidDpadInputKeepsFinalFocusAndProducesNoRuntimeFailure() {
        rule.onNodeWithTag("slot-apps").assertIsSelected()

        repeat(30) { index ->
            val movingLeft = index % 2 == 0
            rule.onNodeWithTag("home").performKeyInput {
                pressKey(if (movingLeft) Key.DirectionLeft else Key.DirectionRight)
            }
            rule.onNodeWithTag(if (movingLeft) "slot-music" else "slot-apps").assertIsSelected()
        }

        rule.onNodeWithTag("slot-apps").assertIsSelected()

        val frameStats = shell("dumpsys gfxinfo $PACKAGE_NAME framestats")
        Log.i(FRAME_LOG_TAG, frameStats)
        assertTrue("Frame timing output should be available for manual inspection", frameStats.contains("PROFILEDATA"))

        val errors = shell("logcat -d -v brief *:E")
        assertFalse(errors.contains("ANR in $PACKAGE_NAME"))
        assertFalse(errors.contains("FATAL EXCEPTION") && errors.contains(PACKAGE_NAME))

    }

    private fun shell(command: String): String {
        val descriptor: ParcelFileDescriptor = InstrumentationRegistry.getInstrumentation()
            .uiAutomation
            .executeShellCommand(command)
        return FileInputStream(descriptor.fileDescriptor).bufferedReader().use { it.readText() }
    }

    private companion object {
        const val PACKAGE_NAME = "com.archivotemporal.launcher"
        const val FRAME_LOG_TAG = "ArchivoTemporalFrames"
    }
}
