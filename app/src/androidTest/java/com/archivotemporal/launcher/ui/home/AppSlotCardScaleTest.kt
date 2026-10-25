package com.archivotemporal.launcher.ui.home

import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.semantics.SemanticsActions
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.unit.Density
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.archivotemporal.launcher.home.HomeState
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppSlotCardScaleTest {
    @get:Rule val rule = createComposeRule()

    @Test fun enlargedAplicacionesLabelIsFullyLaidOutInsideFocusedCard() {
        rule.setContent {
            val density = LocalDensity.current
            CompositionLocalProvider(LocalDensity provides Density(density.density, 1.6f)) {
                HomeScreen(state = HomeState(), onAction = {})
            }
        }
        val card = rule.onNodeWithTag("slot-apps")
        card.assertIsDisplayed()
        card.assertIsSelected()
        val label = rule.onNodeWithText("Aplicaciones", useUnmergedTree = true)
        label.assertIsDisplayed()
        val results = mutableListOf<androidx.compose.ui.text.TextLayoutResult>()
        label.fetchSemanticsNode().config[SemanticsActions.GetTextLayoutResult].action?.invoke(results)
        val layout = results.single()
        assertTrue("Slot typography must stay at least 24sp", layout.layoutInput.style.fontSize.value >= 24f)
        assertEquals("Full label must be laid out", "Aplicaciones".length,
            layout.getLineEnd(layout.lineCount - 1, visibleEnd = true))
        assertFalse("Text must not overflow its box", layout.hasVisualOverflow)
        val cardBounds = card.fetchSemanticsNode().boundsInRoot
        val labelBounds = label.fetchSemanticsNode().boundsInRoot
        assertFalse("Label must stay inside the focus border", labelBounds.left < cardBounds.left ||
            labelBounds.top < cardBounds.top || labelBounds.right > cardBounds.right ||
            labelBounds.bottom > cardBounds.bottom)
    }
}
