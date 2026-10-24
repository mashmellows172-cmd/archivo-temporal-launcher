# Archivo Temporal Launcher Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Build a private, sideloadable Fire TV launcher with a minimalist TVA-inspired interface and a continuously animated, silent Miss Minutes focus guide.

**Architecture:** One Android application module uses Compose for TV for the 10-foot interface, a small unidirectional state store for focus and navigation, repository interfaces for installed apps and preferences, and a custom Compose Canvas animation driven by a pure state machine. Android-specific discovery and launch code stays behind interfaces so reducer, ordering, fallback, and animation behavior can be unit tested on the JVM.

**Tech Stack:** Kotlin 2.4.21, JDK 17, Android Gradle Plugin 9.4.1, Gradle 9.6, compile/target SDK 36, min SDK 30, Compose BOM 2026.09.00, Compose for TV `1.0.0`, Activity Compose `1.13.0`, Lifecycle `2.11.0`, DataStore Preferences `1.3.0`, JUnit 4, AndroidX Compose UI tests.

**Spec:** `docs/superpowers/specs/2026-10-24-archivo-temporal-launcher-design.md`

## Global Constraints

- Primary target is Fire TV Stick 4K 2nd Gen (`AFTKM`), Fire OS 8, Android API 30, 2 GB RAM, 32-bit ABI.
- Repository name is `archivo-temporal-launcher`; GitHub visibility is private.
- Package name is `com.archivotemporal.launcher`.
- The app must run without network access, advertising, analytics, login, or cloud services.
- Home contains one row of exactly five configurable destinations.
- `Continuar` launches the last app successfully started by Archivo Temporal; it does not deep-resume third-party media.
- Miss Minutes is always present on Home, silent, reactive, and must never obscure labels or focus.
- The app must remain useful when Fire OS refuses permanent Home replacement.
- All visual and character assets remain inside the private repository and must not be published separately.
- Prefer stable dependencies only; no alpha, beta, or release-candidate artifacts.

## Review Focus

- An installed favorite is removed: replace it with an empty configurable slot without crashing or losing the other favorites; Task 3 owns the test.
- A package exposes no leanback intent: fall back to its standard launch intent, then show a local error if neither exists; Task 3 owns the test.
- Rapid opposite D-pad presses arrive while Miss Minutes is moving: focus updates immediately and the guide retargets without queuing stale destinations; Task 6 owns the test.
- DataStore contains duplicate or malformed package names: sanitize to five unique valid slots and preserve order; Task 4 owns the test.
- The activity resumes after a launched app closes: restore the previous destination and stable animation state; Task 7 owns the test.

---

### Task 1: Android TV Project Foundation

**Files:**
- Create: `settings.gradle.kts`
- Create: `build.gradle.kts`
- Create: `gradle.properties`
- Create: `gradle/libs.versions.toml`
- Create: `gradle/wrapper/gradle-wrapper.properties`
- Create: `gradlew`
- Create: `gradlew.bat`
- Create: `app/build.gradle.kts`
- Create: `app/proguard-rules.pro`
- Create: `app/src/main/AndroidManifest.xml`
- Create: `app/src/main/java/com/archivotemporal/launcher/MainActivity.kt`
- Create: `app/src/main/res/values/strings.xml`
- Create: `app/src/main/res/values/themes.xml`
- Create: `app/src/main/res/drawable/tv_banner.xml`
- Create: `app/src/test/java/com/archivotemporal/launcher/ManifestContractTest.kt`
- Create: `.gitignore`

**Interfaces:**
- Consumes: none.
- Produces: launchable `MainActivity`; package `com.archivotemporal.launcher`; Gradle tasks `testDebugUnitTest`, `assembleDebug`, and `connectedDebugAndroidTest`.

- [ ] **Step 1: Write the failing manifest contract test**

Create `ManifestContractTest` that reads the merged-source manifest and asserts `android.software.leanback`, `android.hardware.touchscreen` with `required=false`, `LEANBACK_LAUNCHER`, and an application banner are declared.

- [ ] **Step 2: Run the test and verify it fails**

Run: `./gradlew testDebugUnitTest --tests '*ManifestContractTest'`

Expected: FAIL because the Gradle project and manifest do not exist.

- [ ] **Step 3: Create the pinned project foundation**

Use the versions in the plan header. Configure Java/Kotlin JVM target 17, `compileSdk=36`, `targetSdk=36`, `minSdk=30`, Compose enabled, no network permission, and TV-only launcher metadata. `MainActivity` initially renders `Text("Archivo Temporal")` inside the app theme.

- [ ] **Step 4: Generate the Gradle wrapper**

Run: `gradle wrapper --gradle-version 9.6`

Expected: wrapper scripts and `gradle-wrapper.jar` are created.

- [ ] **Step 5: Run foundation verification**

Run: `./gradlew testDebugUnitTest assembleDebug`

Expected: PASS and `app/build/outputs/apk/debug/app-debug.apk` exists.

- [ ] **Step 6: Commit**

```bash
git add settings.gradle.kts build.gradle.kts gradle.properties gradle gradlew gradlew.bat app .gitignore
git commit -m "build: scaffold Fire TV launcher"
```

### Task 2: Domain Models and Deterministic Home Slots

**Files:**
- Create: `app/src/main/java/com/archivotemporal/launcher/domain/AppDestination.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/domain/HomeSlot.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/domain/HomeSlots.kt`
- Create: `app/src/test/java/com/archivotemporal/launcher/domain/HomeSlotsTest.kt`

**Interfaces:**
- Consumes: Kotlin/JVM test runtime from Task 1.
- Produces: `AppDestination(packageName: String, label: String, iconKey: String?)`; `HomeSlot(id: SlotId, suggestedLabel: String, assignment: AppDestination?)`; `HomeSlots.sanitize(assignments: List<AppDestination>, installedPackages: Set<String>): List<HomeSlot>`.

- [ ] **Step 1: Write failing slot tests**

Test names and assertions:

```kotlin
fun sanitize_returns_exactly_five_slots_in_spec_order()
fun sanitize_removes_duplicate_packages_preserving_first_assignment()
fun sanitize_clears_uninstalled_assignments_without_reordering_slots()
fun sanitize_rejects_blank_package_names()
```

- [ ] **Step 2: Run tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests '*HomeSlotsTest'`

Expected: FAIL with unresolved domain types.

- [ ] **Step 3: Implement models and sanitizer**

Define `SlotId` values `CONTINUE`, `MOVIES`, `TV`, `MUSIC`, `APPS`. Always return those five slots in that order. Use immutable data classes and no Android types.

- [ ] **Step 4: Run tests**

Run: `./gradlew testDebugUnitTest --tests '*HomeSlotsTest'`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/archivotemporal/launcher/domain app/src/test/java/com/archivotemporal/launcher/domain
git commit -m "feat: define configurable launcher slots"
```

### Task 3: Installed Application Discovery and Launching

**Files:**
- Create: `app/src/main/java/com/archivotemporal/launcher/apps/InstalledApp.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/apps/AppCatalog.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/apps/AndroidAppCatalog.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/apps/AppLauncher.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/apps/AndroidAppLauncher.kt`
- Create: `app/src/test/java/com/archivotemporal/launcher/apps/AppLaunchPolicyTest.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/apps/AppLaunchPolicy.kt`

**Interfaces:**
- Consumes: `AppDestination` from Task 2.
- Produces: `AppCatalog.observeInstalledApps(): Flow<List<InstalledApp>>`; `AppLauncher.launch(packageName: String): LaunchResult`; `AppLaunchPolicy.resolve(leanbackIntent: Intent?, standardIntent: Intent?): Intent?`.

- [ ] **Step 1: Write failing launch policy tests**

```kotlin
fun resolve_prefers_leanback_intent()
fun resolve_falls_back_to_standard_launch_intent()
fun resolve_returns_null_when_package_is_not_launchable()
```

- [ ] **Step 2: Run tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests '*AppLaunchPolicyTest'`

Expected: FAIL with unresolved `AppLaunchPolicy`.

- [ ] **Step 3: Implement catalog and launch policy**

Query launcher activities through `PackageManager`, exclude `com.archivotemporal.launcher`, sort with `Collator` by label, and map icons to `Drawable` only in the Android adapter. Prefer leanback launch intent, then standard launch intent. Return sealed `LaunchResult.Started`, `LaunchResult.NotFound`, or `LaunchResult.Failed`.

- [ ] **Step 4: Run unit tests and compile**

Run: `./gradlew testDebugUnitTest assembleDebug`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/archivotemporal/launcher/apps app/src/test/java/com/archivotemporal/launcher/apps
git commit -m "feat: discover and launch installed TV apps"
```

### Task 4: Local Preferences, Favorites, and Continue State

**Files:**
- Create: `app/src/main/java/com/archivotemporal/launcher/preferences/LauncherPreferences.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/preferences/DataStoreLauncherPreferences.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/preferences/PreferenceSanitizer.kt`
- Create: `app/src/test/java/com/archivotemporal/launcher/preferences/PreferenceSanitizerTest.kt`

**Interfaces:**
- Consumes: `HomeSlot`, `SlotId`, `AppDestination` from Task 2.
- Produces: `LauncherPreferences.state: Flow<LauncherSettings>`; `assign(slotId: SlotId, packageName: String?)`; `recordLaunch(packageName: String)`; `setMotionMode(mode: MotionMode)`; `setTextureLevel(level: TextureLevel)`; `setHintsVisible(visible: Boolean)`.

- [ ] **Step 1: Write failing preference sanitizer tests**

Assert duplicate packages, blank values, more than five stored entries, unknown slot IDs, and a removed last-launched package are repaired deterministically.

- [ ] **Step 2: Run tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests '*PreferenceSanitizerTest'`

Expected: FAIL with unresolved sanitizer.

- [ ] **Step 3: Implement DataStore preferences**

Persist package names only. Store five slot keys, last launched package, motion mode, texture level, and hint visibility. Expose immutable `LauncherSettings`. Apply `PreferenceSanitizer` on every read against current installed packages.

- [ ] **Step 4: Run tests**

Run: `./gradlew testDebugUnitTest --tests '*PreferenceSanitizerTest'`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/archivotemporal/launcher/preferences app/src/test/java/com/archivotemporal/launcher/preferences
git commit -m "feat: persist launcher favorites and settings"
```

### Task 5: Launcher State Store and Navigation Reducer

**Files:**
- Create: `app/src/main/java/com/archivotemporal/launcher/home/HomeState.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/home/HomeAction.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/home/HomeEffect.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/home/HomeReducer.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/home/HomeViewModel.kt`
- Create: `app/src/test/java/com/archivotemporal/launcher/home/HomeReducerTest.kt`

**Interfaces:**
- Consumes: repositories from Tasks 3–4.
- Produces: `HomeReducer.reduce(state: HomeState, action: HomeAction): ReduceResult<HomeState, HomeEffect>`; `HomeViewModel.state: StateFlow<HomeState>`; one-shot `effects: Flow<HomeEffect>`.

- [ ] **Step 1: Write failing reducer tests**

Cover left/right bounds, up/down between app row and utility rail, Select on assigned and empty slots, Continue with and without history, Back from secondary screen, launch failure notice, and immediate focus replacement during rapid input.

- [ ] **Step 2: Run tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests '*HomeReducerTest'`

Expected: FAIL with unresolved reducer.

- [ ] **Step 3: Implement reducer and ViewModel**

Use `HomeEffect.LaunchPackage`, `OpenScreen`, `ShowNotice`, and `None`. Keep navigation events out of persistent `StateFlow`; emit them through a `Channel` exposed as `receiveAsFlow()`.

- [ ] **Step 4: Run tests**

Run: `./gradlew testDebugUnitTest --tests '*HomeReducerTest'`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/archivotemporal/launcher/home app/src/test/java/com/archivotemporal/launcher/home
git commit -m "feat: add deterministic launcher state store"
```

### Task 6: Miss Minutes Animation State Machine

**Files:**
- Create: `app/src/main/java/com/archivotemporal/launcher/character/MissMinutesState.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/character/MissMinutesController.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/character/MissMinutesCanvas.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/character/MissMinutesMotion.kt`
- Create: `app/src/test/java/com/archivotemporal/launcher/character/MissMinutesControllerTest.kt`

**Interfaces:**
- Consumes: focused `SlotId`, launch/notice events, motion preference.
- Produces: `MissMinutesController.onFocusChanged(slotId: SlotId, targetX: Float)`; `onLaunchStarted()`; `onNotice(notice: Notice)`; `state: StateFlow<MissMinutesState>`; `MissMinutesCanvas(state, modifier)`.

- [ ] **Step 1: Write failing controller tests**

```kotlin
fun focus_change_moves_then_points_at_latest_target()
fun rapid_focus_changes_cancel_stale_target()
fun confirm_returns_to_idle_after_timeout()
fun system_notice_preempts_idle_and_focus_motion()
fun reduced_motion_skips_walk_cycle_but_updates_position()
```

Use a test coroutine scheduler; assert state sequence and final target.

- [ ] **Step 2: Run tests and verify failure**

Run: `./gradlew testDebugUnitTest --tests '*MissMinutesControllerTest'`

Expected: FAIL with unresolved controller.

- [ ] **Step 3: Implement the pure state machine**

States are `Idle`, `Moving`, `Pointing`, `Confirming`, `Notifying`, and `Sleeping`. New focus cancels the previous movement job. Durations stay inside the spec’s 220–360 ms movement and 160–220 ms pointing ranges.

- [ ] **Step 4: Implement the custom Compose Canvas character**

Draw the clock body, ticks, eyes, mouth, arms, gloves, legs, and shoes with Compose paths and primitives. Animate only `Animatable` transforms, eye direction, limb rotations, and alpha. Keep semantics decorative; the character never receives focus.

- [ ] **Step 5: Run controller tests and assemble**

Run: `./gradlew testDebugUnitTest --tests '*MissMinutesControllerTest' assembleDebug`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/archivotemporal/launcher/character app/src/test/java/com/archivotemporal/launcher/character
git commit -m "feat: animate Miss Minutes focus guide"
```

### Task 7: TVA-Inspired Home Screen and D-pad Focus

**Files:**
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/theme/Color.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/theme/Type.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/theme/Theme.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/components/TemporalFrame.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/components/ArchiveHeader.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/components/AppSlotCard.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/components/UtilityRail.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/home/HomeScreen.kt`
- Create: `app/src/androidTest/java/com/archivotemporal/launcher/ui/home/HomeScreenTest.kt`
- Modify: `app/src/main/java/com/archivotemporal/launcher/MainActivity.kt`

**Interfaces:**
- Consumes: `HomeState`, `HomeAction`, and `MissMinutesCanvas`.
- Produces: `HomeScreen(state: HomeState, onAction: (HomeAction) -> Unit)`; stable test tags `home`, `slot-{id}`, `utility-{id}`, `miss-minutes`, and `notice`.

- [ ] **Step 1: Write failing Compose UI tests**

Assert five visible slot nodes, focus order across the row, Up enters utility rail, Down returns to the prior slot, the focused slot exposes selected semantics, Miss Minutes has no focus action, and recreation restores the previously focused slot.

- [ ] **Step 2: Run connected tests and verify failure**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.archivotemporal.launcher.ui.home.HomeScreenTest`

Expected: FAIL because Home components do not exist.

- [ ] **Step 3: Implement the design tokens and components**

Use soot black, burnt orange, cream, and muted technical-grid tokens. Use square corners, 5% safe-area inset, 32sp+ header/time, 24sp+ card labels, and a two-layer 3:1+ focus perimeter. Add low-cost scanline and print-noise drawing without bitmap backgrounds.

- [ ] **Step 4: Implement Home and effect collection**

Place header at top, utility rail at left, at least 60% negative space, Miss Minutes above the app row, and exactly five cards at bottom. Collect one-shot effects on `Dispatchers.Main.immediate`. Restore focus in `onResume` without restarting a stale movement.

- [ ] **Step 5: Run UI and unit tests**

Run: `./gradlew testDebugUnitTest connectedDebugAndroidTest`

Expected: PASS.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/java/com/archivotemporal/launcher/ui app/src/main/java/com/archivotemporal/launcher/MainActivity.kt app/src/androidTest
git commit -m "feat: build Archivo Temporal home interface"
```

### Task 8: Applications, Search, Library, and Settings Screens

**Files:**
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/apps/AppsScreen.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/search/SearchScreen.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/library/LibraryScreen.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/ui/settings/SettingsScreen.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/navigation/Screen.kt`
- Create: `app/src/main/java/com/archivotemporal/launcher/navigation/LauncherNavigator.kt`
- Create: `app/src/androidTest/java/com/archivotemporal/launcher/ui/SecondaryScreensTest.kt`

**Interfaces:**
- Consumes: app catalog, launcher preferences, `HomeEffect.OpenScreen`.
- Produces: sealed `Screen` routes; `LauncherNavigator`; assignment flow from Apps/Library into one of five slots.

- [ ] **Step 1: Write failing secondary-screen tests**

Assert Search filters app labels case-insensitively, Library can assign and clear a slot, Settings changes motion/texture/hints, and Back returns Home with focus restored.

- [ ] **Step 2: Run connected tests and verify failure**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.archivotemporal.launcher.ui.SecondaryScreensTest`

Expected: FAIL because screens and routes do not exist.

- [ ] **Step 3: Implement typed local navigation and screens**

Use sealed `Screen` values, no string routes. Keep all lists D-pad navigable. Search uses an on-screen TV text field only when opened. Library explicitly chooses one of five slots before assigning an app.

- [ ] **Step 4: Run all tests**

Run: `./gradlew testDebugUnitTest connectedDebugAndroidTest`

Expected: PASS.

- [ ] **Step 5: Commit**

```bash
git add app/src/main/java/com/archivotemporal/launcher/ui app/src/main/java/com/archivotemporal/launcher/navigation app/src/androidTest
git commit -m "feat: add launcher management screens"
```

### Task 9: Performance, Accessibility, and Fire TV Validation Tooling

**Files:**
- Create: `app/src/main/baseline-prof.txt`
- Create: `scripts/check-adb-device.ps1`
- Create: `scripts/install-debug.ps1`
- Create: `docs/testing-fire-tv.md`
- Create: `app/src/androidTest/java/com/archivotemporal/launcher/PerformanceSmokeTest.kt`
- Modify: `app/build.gradle.kts`

**Interfaces:**
- Consumes: completed application.
- Produces: repeatable emulator and AFTKM install/launch/metrics workflow.

- [ ] **Step 1: Write failing performance smoke test**

Launch Home, issue 30 alternating left/right key events, and assert final focus equals the last event target and no app-not-responding or uncaught exception appears. Record frame timing for manual inspection rather than making emulator FPS a hard gate.

- [ ] **Step 2: Run the smoke test and verify current failure**

Run: `./gradlew connectedDebugAndroidTest -Pandroid.testInstrumentationRunnerArguments.class=com.archivotemporal.launcher.PerformanceSmokeTest`

Expected: FAIL until test tags and timing hooks are available.

- [ ] **Step 3: Add baseline profile and device scripts**

`check-adb-device.ps1` must reject non-`AFTKM` devices unless `-AllowOtherDevice` is passed. `install-debug.ps1` builds, installs with `adb install -r`, launches `MainActivity`, and prints commands for Amazon System X-Ray and frame stats. Scripts must not alter Home defaults.

- [ ] **Step 4: Run automated verification**

Run: `./gradlew clean testDebugUnitTest assembleDebug connectedDebugAndroidTest lintDebug`

Expected: BUILD SUCCESSFUL; all tests pass; lint has zero errors.

- [ ] **Step 5: Run physical AFTKM verification when available**

Run: `powershell -ExecutionPolicy Bypass -File scripts/check-adb-device.ps1`

Run: `powershell -ExecutionPolicy Bypass -File scripts/install-debug.ps1`

Expected: model reports `AFTKM`; APK installs; Home responds to remote; System X-Ray shows no sustained sub-30-fps interaction; no labels clip at 4K output; Back and resume restore focus.

- [ ] **Step 6: Commit**

```bash
git add app/src/main/baseline-prof.txt app/src/androidTest scripts docs/testing-fire-tv.md app/build.gradle.kts
git commit -m "test: add Fire TV performance validation"
```

### Task 10: Documentation, CI, Release APK, and Private GitHub Repository

**Files:**
- Create: `README.md`
- Create: `LICENSE-NOTICE.md`
- Create: `.github/workflows/android.yml`
- Create: `docs/architecture.md`
- Create: `docs/design-system.md`
- Create: `docs/screenshots/.gitkeep`

**Interfaces:**
- Consumes: all build and test tasks.
- Produces: private GitHub repository, CI verification, debug APK artifact, complete setup instructions for the home computer.

- [ ] **Step 1: Write documentation acceptance checklist**

README must cover prerequisites, clone, JDK 17, Android SDK 36, build, emulator, ADB connection, sideload, configuration, IP restrictions, and the limitation that Fire OS may retain its default Home.

- [ ] **Step 2: Add CI**

Configure GitHub Actions to use JDK 17 and run `./gradlew testDebugUnitTest assembleDebug lintDebug`. Upload `app-debug.apk` as a private workflow artifact. Do not upload character assets as a separate artifact.

- [ ] **Step 3: Run final local verification**

Run: `./gradlew clean testDebugUnitTest assembleDebug lintDebug`

Expected: BUILD SUCCESSFUL; all unit tests pass; lint has zero errors; debug APK exists.

- [ ] **Step 4: Initialize and inspect Git history**

Run: `git status --short`

Expected: only planned files are present.

Run: `git log --oneline --decorate -10`

Expected: one focused commit per completed task.

- [ ] **Step 5: Create the private GitHub repository and push**

Run: `gh repo create archivo-temporal-launcher --private --source . --remote origin --push`

Expected: GitHub reports a private repository URL and `origin` points to it.

- [ ] **Step 6: Verify remote and workflow**

Run: `gh repo view --json name,visibility,url,defaultBranchRef`

Expected: `name=archivo-temporal-launcher`, `visibility=PRIVATE`, default branch exists.

Run: `gh run list --limit 1`

Expected: Android workflow is queued, in progress, or completed successfully.

- [ ] **Step 7: Commit any final documentation correction and push**

```bash
git add README.md LICENSE-NOTICE.md .github docs
git commit -m "docs: complete private launcher handoff"
git push origin HEAD
```

## Self-review

- Spec coverage: Home, five configurable slots, Continue behavior, utility rail, secondary screens, Miss Minutes states, settings, offline operation, Fire OS limitation, performance, ADB, private distribution, and GitHub handoff are each owned by a task.
- Step scan: each action produces one verifiable change; implementation bodies are omitted except where contracts require exact signatures or values.
- Type consistency: `SlotId`, `AppDestination`, `HomeSlot`, `LauncherSettings`, `HomeState`, `HomeAction`, `HomeEffect`, and `MissMinutesState` have one owner and stable downstream names.
- Review Focus: all five listed failure modes have explicit tests in their owning tasks.
- Proportion: the plan describes interfaces, acceptance tests, commands, and decisions without transcribing production bodies.

