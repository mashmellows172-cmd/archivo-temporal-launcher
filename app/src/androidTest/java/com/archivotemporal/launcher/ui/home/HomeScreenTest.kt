package com.archivotemporal.launcher.ui.home

import androidx.compose.ui.input.key.Key
import androidx.compose.ui.graphics.toPixelMap
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.captureToImage
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.onAllNodesWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.pressKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.archivotemporal.launcher.MainActivity
import org.junit.Assert.assertFalse
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeScreenTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun homeHasFiveVisibleDestinationsAndGuide() {
        rule.onNodeWithTag("home").assertExists()
        listOf("continue", "movies", "tv", "music", "apps").forEach {
            rule.onNodeWithTag("slot-$it").assertExists()
        }
        rule.onNodeWithTag("miss-minutes").assertExists()
    }

    @Test fun rightAndLeftMoveAcrossRowAndStopAtEnds() {
        rule.onNodeWithTag("slot-apps").assertIsSelected()
        repeat(6) { rule.press(Key.DirectionLeft) }
        rule.onNodeWithTag("slot-continue").assertIsSelected()
        repeat(4) { rule.press(Key.DirectionRight) }
        rule.onNodeWithTag("slot-apps").assertIsSelected()
        rule.press(Key.DirectionRight)
        rule.onNodeWithTag("slot-apps").assertIsSelected()
    }

    @Test fun upEntersUtilityAndDownReturnsToRememberedSlot() {
        rule.press(Key.DirectionLeft)
        rule.onNodeWithTag("slot-music").assertIsSelected()
        rule.press(Key.DirectionUp)
        rule.onNodeWithTag("utility-settings").assertIsSelected()
        rule.press(Key.DirectionDown)
        rule.onNodeWithTag("slot-music").assertIsSelected()
    }

    @Test fun guideIsDecorativeAndCannotBeSelected() {
        rule.onNodeWithTag("miss-minutes").assertExists()
        assertFalse(rule.onNodeWithTag("miss-minutes").fetchSemanticsNode().config.contains(
            androidx.compose.ui.semantics.SemanticsActions.OnClick
        ))
        assertFalse(rule.onNodeWithTag("miss-minutes").fetchSemanticsNode().config.contains(
            androidx.compose.ui.semantics.SemanticsActions.RequestFocus
        ))
    }

    @Test fun recreationKeepsPreviousDestinationFocused() {
        rule.press(Key.DirectionLeft)
        rule.press(Key.DirectionLeft)
        rule.onNodeWithTag("slot-tv").assertIsSelected()
        rule.activityRule.scenario.recreate()
        rule.onNodeWithTag("slot-tv").assertIsSelected()
    }

    @Test fun guideRestsAboveTheFocusedSlot() {
        rule.onNodeWithTag("slot-apps").assertIsSelected()
        rule.waitUntil(4_000) {
            val pixels = rule.onNodeWithTag("miss-minutes").captureToImage().toPixelMap()
            var orangeSum = 0L
            var count = 0L
            for (x in 0 until pixels.width step 3) for (y in 0 until pixels.height step 3) {
                val pixel = pixels[x, y]
                if (pixel.red > 0.55f && pixel.green in 0.18f..0.55f && pixel.blue < 0.30f) {
                    orangeSum += x
                    count++
                }
            }
            count > 4 && orangeSum.toFloat() / count > pixels.width * 0.70f
        }
    }

    @Test fun selectingAnUnfocusedCardOpensItsOwnDestination() {
        rule.onNodeWithTag("slot-movies").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithText("LIBRARY").assertExists()
    }

    @Test fun selectingAnUnfocusedUtilityOpensItsOwnDestination() {
        rule.onNodeWithTag("utility-search").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithText("SEARCH").assertExists()
    }

    @Test fun localNoticeExpiresAfterGuideNotification() {
        repeat(4) { rule.press(Key.DirectionLeft) }
        rule.press(Key.DirectionCenter)
        rule.onNodeWithText("Aún no hay una aplicación reciente").assertExists()
        rule.waitUntil(5_000) {
            rule.onAllNodesWithText("Aún no hay una aplicación reciente").fetchSemanticsNodes().isEmpty()
        }
    }


    private fun androidx.compose.ui.test.junit4.AndroidComposeTestRule<*, *>.press(key: Key) {
        onNodeWithTag("home").performKeyInput { pressKey(key) }
    }
}
