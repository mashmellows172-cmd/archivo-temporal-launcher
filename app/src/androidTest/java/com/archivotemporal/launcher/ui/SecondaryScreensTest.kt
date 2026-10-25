package com.archivotemporal.launcher.ui

import androidx.activity.compose.setContent
import androidx.compose.ui.input.key.Key
import androidx.compose.ui.test.junit4.createAndroidComposeRule
import androidx.compose.ui.test.assertIsDisplayed
import androidx.compose.ui.test.assertIsFocused
import androidx.compose.ui.test.assertIsSelected
import androidx.compose.ui.test.onNodeWithTag
import androidx.compose.ui.test.onNodeWithText
import androidx.compose.ui.test.performSemanticsAction
import androidx.compose.ui.test.performKeyInput
import androidx.compose.ui.test.performTextInput
import androidx.compose.ui.test.pressKey
import androidx.compose.ui.semantics.SemanticsActions
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.archivotemporal.launcher.MainActivity
import com.archivotemporal.launcher.apps.InstalledApp
import com.archivotemporal.launcher.domain.AppDestination
import com.archivotemporal.launcher.domain.HomeSlot
import com.archivotemporal.launcher.domain.SlotId
import com.archivotemporal.launcher.preferences.LauncherSettings
import com.archivotemporal.launcher.preferences.MotionMode
import com.archivotemporal.launcher.preferences.TextureLevel
import com.archivotemporal.launcher.ui.library.LibraryScreen
import com.archivotemporal.launcher.ui.apps.AppsScreen
import com.archivotemporal.launcher.ui.search.SearchScreen
import com.archivotemporal.launcher.ui.settings.SettingsScreen
import com.archivotemporal.launcher.ui.theme.ArchiveTheme
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SecondaryScreensTest {
    @get:Rule val rule = createAndroidComposeRule<MainActivity>()

    @Test fun searchFiltersInstalledLabelsCaseInsensitively() {
        rule.activity.setContent {
            ArchiveTheme {
                SearchScreen(
                    apps = listOf(app("Netflix", "com.netflix"), app("Spotify", "com.spotify")),
                    onLaunch = {},
                    onBack = {},
                )
            }
        }

        rule.onNodeWithTag("search-input").assertIsFocused().performTextInput("nEt")

        rule.onNodeWithText("Netflix").assertExists()
        rule.onNodeWithText("Spotify").assertDoesNotExist()
    }

    @Test fun appsGridScrollsSelectedItemIntoView() {
        rule.activity.setContent {
            ArchiveTheme {
                AppsScreen(
                    apps = (0..14).map { app("Aplicación $it", "com.app.$it") },
                    onAssign = {},
                    onBack = {},
                )
            }
        }

        rule.onNodeWithTag("apps-screen").performKeyInput {
            repeat(4) { pressKey(Key.DirectionDown) }
        }

        rule.onNodeWithTag("apps-item-com.app.12").assertIsDisplayed()
    }

    @Test fun searchResultsScrollWithDpadSelection() {
        rule.activity.setContent {
            ArchiveTheme {
                SearchScreen(
                    apps = (0..14).map { app("Aplicación $it", "com.search.$it") },
                    onLaunch = {},
                    onBack = {},
                )
            }
        }

        rule.onNodeWithTag("search-screen").performKeyInput {
            repeat(12) { pressKey(Key.DirectionDown) }
        }

        rule.onNodeWithTag("search-item-com.search.12").assertIsDisplayed()
    }

    @Test fun libraryAppsScrollWithDpadSelection() {
        rule.activity.setContent {
            ArchiveTheme {
                LibraryScreen(
                    slots = emptySlots(),
                    apps = (0..14).map { app("Aplicación $it", "com.library.$it") },
                    candidatePackage = null,
                    onAssign = { _, _ -> },
                    onBack = {},
                )
            }
        }
        rule.onNodeWithTag("library-slot-movies").performSemanticsAction(SemanticsActions.OnClick)

        rule.onNodeWithTag("library-screen").performKeyInput {
            repeat(12) { pressKey(Key.DirectionDown) }
        }

        rule.onNodeWithTag("library-app-com.library.12").assertIsDisplayed()
    }

    @Test fun libraryRequiresSlotThenAssignsAndClearsIt() {
        val writes = mutableListOf<Pair<SlotId, String?>>()
        rule.activity.setContent {
            ArchiveTheme {
                LibraryScreen(
                    slots = emptySlots(),
                    apps = listOf(app("Cinema", "com.cinema")),
                    candidatePackage = null,
                    onAssign = { slot, packageName -> writes += slot to packageName },
                    onBack = {},
                )
            }
        }

        rule.onNodeWithTag("library-slot-movies").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithTag("library-app-com.cinema").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithTag("library-clear").performSemanticsAction(SemanticsActions.OnClick)

        assertEquals(
            listOf(SlotId.MOVIES to "com.cinema", SlotId.MOVIES to null),
            writes,
        )
    }

    @Test fun libraryClearIsReachableAndActivatedWithDpadOnly() {
        val writes = mutableListOf<Pair<SlotId, String?>>()
        rule.activity.setContent {
            ArchiveTheme {
                LibraryScreen(
                    slots = emptySlots(),
                    apps = listOf(app("Cinema", "com.cinema")),
                    candidatePackage = null,
                    onAssign = { slot, packageName -> writes += slot to packageName },
                    onBack = {},
                )
            }
        }

        rule.onNodeWithTag("library-screen").performKeyInput {
            pressKey(Key.DirectionRight)
            pressKey(Key.DirectionCenter)
            pressKey(Key.DirectionUp)
        }
        rule.onNodeWithTag("library-clear").assertIsSelected()
        rule.onNodeWithTag("library-screen").performKeyInput { pressKey(Key.DirectionCenter) }

        assertEquals(listOf(SlotId.MOVIES to null), writes)
    }

    @Test fun settingsChangesMotionTextureAndHints() {
        var motion: MotionMode? = null
        var texture: TextureLevel? = null
        var hints: Boolean? = null
        rule.activity.setContent {
            ArchiveTheme {
                SettingsScreen(
                    settings = settings(),
                    onMotionMode = { motion = it },
                    onTextureLevel = { texture = it },
                    onHintsVisible = { hints = it },
                    onBack = {},
                )
            }
        }

        rule.onNodeWithTag("setting-motion").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithTag("setting-texture").performSemanticsAction(SemanticsActions.OnClick)
        rule.onNodeWithTag("setting-hints").performSemanticsAction(SemanticsActions.OnClick)

        assertEquals(MotionMode.REDUCED, motion)
        assertEquals(TextureLevel.MEDIUM, texture)
        assertEquals(false, hints)
    }

    @Test fun backReturnsHomeWithPreviousFocusRestored() {
        rule.onNodeWithTag("home").performKeyInput {
            pressKey(Key.DirectionUp)
            pressKey(Key.DirectionUp)
            pressKey(Key.DirectionCenter)
        }
        rule.onNodeWithTag("library-screen").assertExists()

        rule.onNodeWithTag("library-screen").performKeyInput { pressKey(Key.Back) }

        rule.onNodeWithTag("home").assertExists()
        rule.onNodeWithTag("utility-library").assertExists()
        rule.onNodeWithTag("utility-library").fetchSemanticsNode().also {
            assertEquals(true, it.config[androidx.compose.ui.semantics.SemanticsProperties.Selected])
        }
    }

    private fun app(label: String, packageName: String) =
        InstalledApp(AppDestination(packageName, label, null))

    private fun emptySlots() = SlotId.entries.map { HomeSlot(it, it.name, null) }

    private fun settings() = LauncherSettings(
        slots = emptySlots(),
        lastLaunched = null,
        motionMode = MotionMode.FULL,
        textureLevel = TextureLevel.LOW,
        hintsVisible = true,
    )
}
