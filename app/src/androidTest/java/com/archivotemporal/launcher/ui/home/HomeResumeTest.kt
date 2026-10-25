package com.archivotemporal.launcher.ui.home

import androidx.compose.foundation.focusable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsNotFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.unit.dp
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.LifecycleRegistry
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.archivotemporal.launcher.character.MissMinutesState
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.home.HomeAction
import com.archivotemporal.launcher.home.HomeFocus
import com.archivotemporal.launcher.home.HomeState
import com.archivotemporal.launcher.preferences.MotionMode
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class HomeResumeTest {
    @get:Rule val rule = createComposeRule()

    private class TestOwner : LifecycleOwner {
        override val lifecycle = LifecycleRegistry(this)
    }

    @Test fun resumeRestoresRememberedSlotFocusWithoutSendingAStaleMove() {
        val owner = TestOwner()
        val competingFocus = FocusRequester()
        val actions = mutableListOf<HomeAction>()
        rule.runOnUiThread { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        rule.setContent {
            CompositionLocalProvider(LocalLifecycleOwner provides owner) {
                Box {
                    HomeScreen(
                        state = HomeState(
                            focus = HomeFocus.Slot(SlotId.MOVIES),
                            lastRowSlot = SlotId.MOVIES,
                            initialized = true,
                        ),
                        onAction = actions::add,
                        missMinutesState = MissMinutesState(
                            focusedSlot = SlotId.MOVIES,
                            targetX = 0.3f,
                            motionMode = MotionMode.REDUCED,
                        ),
                    )
                    Box(Modifier.size(1.dp).focusRequester(competingFocus).focusable())
                }
            }
        }
        rule.onNodeWithTag("home").assertIsFocused()
        rule.onNodeWithTag("slot-movies").assertIsSelected()
        rule.runOnIdle {
            owner.lifecycle.currentState = Lifecycle.State.STARTED
            competingFocus.requestFocus()
        }
        rule.onNodeWithTag("home").assertIsNotFocused()
        rule.runOnIdle { owner.lifecycle.currentState = Lifecycle.State.RESUMED }
        rule.onNodeWithTag("home").assertIsFocused()
        rule.onNodeWithTag("slot-movies").assertIsSelected()
        assertTrue("Resume must not replay a D-pad action into the guide", actions.isEmpty())
    }
}
