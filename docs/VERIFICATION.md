# VERIFICATION

Rule: only record what was actually run. Leave "not run" until it is.

## Milestone 4 — 2026-10-11

Delivered version: **0.4.0-m4 / versionCode 5**.

| Check | Result |
|---|---|
| Garden, continuous reveal, and fairy APK assembly | Passed in 9s; 36 actionable tasks, 12 executed, 24 up-to-date |
| Final APK assembly after heading-copy refinement | Passed in 2s; 36 actionable tasks, 4 executed, 32 up-to-date |
| APK metadata/permissions inspection (AAPT2) | com.laya.desk, versionCode 5, 0.4.0-m4; minSdk 24 / targetSdk 36; CAMERA plus AndroidX internal signature permission; no INTERNET or VIBRATE |
| Emulator, automated tests, lint | Not run, per the user's explicit instruction |
| Installation / phone interaction / AR behavior | Not run by the agent; previous user-reported phone success applies to M2 |

Only `:app:assembleDebug` was invoked, using the same one-worker, in-process Kotlin command
documented below for M3. Dependency pins and JVM 17 remain unchanged.

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Final APK size: 45,658,551 bytes. SHA-256:
`DEF129F3E53AFB6925823F2BB6CE2A64207DB23A46BA2C5E6089A35A5599989A`.

### APK publication — 2026-10-11

The existing output APK was inspected again for the requested GitHub upload. It had been replaced
since the assembly record above: 45,555,928 bytes, last modified 02:23:08 local time. No new build,
emulator, automated tests, or lint was run for publication. AAPT2 confirms the same package,
versionCode 5 / 0.4.0-m4, CAMERA and the AndroidX internal permission, and no INTERNET or VIBRATE.
The current source checkout was clean before the README/publication documentation changes.

Published artifact name: `LAYA-0.4.0-m4-debug.apk`, linked from README through the M4 prerelease.
SHA-256: `5AB93D7C2D2E231613DCDC160C84EB949175E82DD6DC68EBFD51EECC78EED32F`.
The earlier hash remains the record of the agent's assembled APK. Runtime acceptance is still pending.

Publication completed: the `m4-playable-screen` tag and branch were pushed, and the user merged
PR #1 into main. The merged PR's title and description were updated. GitHub's release API reports
the APK asset as `uploaded`, size 45,555,928 bytes, with digest
`sha256:5ab93d7c2d2e231613dcdc160c84eb949175e82dd6dc68ebfd51eecc78eed32f`.
The returned download URL matches README. Private-repository authentication remains required.

Implementation/source review coverage (not runtime tests):
- New pure Kotlin `game/GardenState.kt`: bounded left/right movement, branch jump, bud, exit,
  animated Skip intro, and capped frame deltas. Normal route: Right x3, Jump, Right x3.
- `game/EscapeState.kt` and `ui/BalanceBridgeGame.kt`: garden/exit phases, lifecycle/tracking
  gating, preserved exit progress after early release, and full replay reset.
- New `ui/GardenScreen.kt` and `ui/SampaguitaFairy.kt`: original generated garden and fairy.
  `BalanceBridgeScene.kt` keeps one fairy call/parent across all phases, delays board visibility,
  and moves the character outward before landing. The screen remains under the same anchor.
- Shared panel, touch controller, preview, and AR hosts updated for exit picking, garden buttons,
  framing, and staged instructions. Existing weight dragging and balance rules are retained.
- Matching SceneView 4.23.0 primitive and unlit-material signatures were read from local sources;
  the code compiles against the pinned artifact. No new dependencies, permissions, or media assets.
- README, STATUS, and the M4 scope record describe the delivered controls and limitations.
  Kotlin paths above are relative to `app/src/main/kotlin/com/laya/desk/`.

The screen uses bounded shallow 2.5D geometry, not render-to-texture or portal clipping. Side views
can reveal its depth; the fairy is a first primitive art pass with opaque wings. No phone appearance,
touch feel, frame rate, tracking recovery, replay stability, or full-demo acceptance is established
by compilation. The earlier 20 passing tests belong to M2 and do not verify these changes.

## Milestone 3 — 2026-10-11

Delivered version: **0.3.0-m3 / versionCode 4**.

| Check | Result |
|---|---|
| Stage A avatar-escape APK assembly | Passed in 31s after compiler repairs, before weight dragging was implemented |
| Stage B escape + weight-drag APK assembly | Passed in 15s |
| Final APK assembly after control copy/scroll refinement | Passed in 2s; 36 actionable tasks, 4 executed, 32 up-to-date |
| APK metadata/permissions inspection (AAPT2) | com.laya.desk, versionCode 4, 0.3.0-m3; CAMERA plus AndroidX internal signature permission; no INTERNET or VIBRATE |
| Emulator, automated tests, lint | Not run, per the user's explicit instruction |
| Installation / phone interaction / AR behavior | Not run by the agent; earlier user-reported phone success applies to M2 |

Only this Gradle task was requested during this milestone:

```powershell
$env:JAVA_HOME = 'C:\Users\DOT36\jendrick\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug --console=plain --max-workers=1 --no-parallel '-Pkotlin.compiler.execution.strategy=in-process'
```

APK: `app/build/outputs/apk/debug/app-debug.apk`.
Final APK size: 45,613,227 bytes. SHA-256:
`9216515631DF402169EF2092B1FE1DBDFE98A4A0CC42A36C132BB256FA12AEE0`.

Changed implementation files:
- New `game/EscapeState.kt` and `game/WeightDragState.kt`: pure flow/drag rules.
- New `ui/ExperienceTouchController.kt`: picking, pointer ownership, and local-plane projection.
- Updated `ui/BalanceBridgeGame.kt`, `BalanceBridgeScene.kt`, and `BalanceBridgePanel.kt`:
  shared flow, geometry, animation, accessible controls, and educational feedback.
- Updated `ui/PlacementPreviewScreen.kt` and `ui/ArPlacementScreen.kt`: shared touch wiring,
  preview framing, and the board's initial AR heading.
- Updated app version, README, STATUS, this verification record, and the milestone scope note.
  Kotlin paths above are relative to `app/src/main/kotlin/com/laya/desk/`.

Compiler repairs made during Stage A:
- Imported `rememberARView` from `io.github.sceneview`, as the matching ARSceneView source does.
- Replaced inline vector operators compiled for JVM 21 with scalar coordinate arithmetic,
  preserving this app's JVM 17 target and its existing dependencies.
- Renamed a holder method to `updateAvailability` to avoid clashing with a Kotlin property's JVM setter.

Source inspection checked the pinned SceneView 4.23.0 raw touch dispatch, SurfaceView-local
MotionEvents, hit results, node hierarchy/conversion helpers, and primitive/material APIs.
The AR board's one-time facing calculation uses
[ARCore Pose inverse/transformPoint](https://developers.google.com/ar/reference/java/com/google/ar/core/Pose).
Reading APIs and assembling the APK do not verify runtime input, appearance, or tracking behavior.

Implementation coverage (source/build only):
- Grab the spirit in the floating frame, pull to the marker, release to descend; early
  release/cancel returns smoothly. Free her uses the same descent. Landing unlocks the puzzle.
- Grab the teal weight, highlight it, constrain its movement to the lever, then release to snap
  to the nearest slot. Cancellation leaves the integer puzzle distance unchanged.
- Ray projection uses the actual scene viewport and board/lever-local planes; captured streams
  bypass preview orbit. Unusable projections and extra pointers cancel safely.
- Tracking/lifecycle loss cancels active drags and pauses the escape/crossing frame clock.
  Replay restores the entire experience at the same placement; reset/mode changes clear it.
- Preview, regular AR placement, Quick placement labeling, camera permission flows, and existing
  distance buttons remain in the code. No new permission, dependency, ML model, or network path.

Still unverified on the target phone: object pick sizes, preview orbit/drag transitions, AR facing
and dragging, cancellation and tracking recovery, background/resume, repeated replay/reset,
large-font card layout, frame rate, and completion of the full demo. No full-MVP acceptance claim.

## Checks run
| Date | Check | Result |
|---|---|---|
| 2026-10-10 | Required root/build/docs/main/test files | All 17 checked paths present in the expected locations |
| 2026-10-10 | :app:assembleDebug | Passed; app/build/outputs/apk/debug/app-debug.apk generated |
| 2026-10-10 | :app:testDebugUnitTest | Passed; PlacementPhaseTest: 4 tests, 0 failures, 0 errors |
| 2026-10-10 | :app:lintDebug | Passed after fixes; 0 errors, 21 warnings |
| 2026-10-10 | Merged debug manifest | com.laya.desk.MainActivity; ARCore required; CAMERA plus AndroidX internal signature permission; no INTERNET or VIBRATE |
| 2026-10-10 | Git initialization | Local repository initialized; no commit/tag/remote created |
| 2026-10-10 | Fallback build: assembleDebug + testDebugUnitTest + lintDebug | Passed in one run; 8 tests, 0 failures/errors; lint 0 errors, 21 warnings |
| 2026-10-10 | Fallback APK metadata/permissions | com.laya.desk, versionCode 2, versionName 0.1.1-m1; CAMERA plus internal receiver permission; no INTERNET or VIBRATE |
| 2026-10-10 | Claude Milestone 2 handoff packaging | Source TXT plus clean ZIP prepared; all 45 archive entries hash-checked against local files and CRC-checked; no caches, build outputs, local.properties, or signing files |
| 2026-10-10 | Milestone 2 assembly + unit tests + lint | Passed in 31s: 12 BalanceBridgeState tests + 8 placement tests, 0 failures/errors; lint 0 errors, 21 warnings |
| 2026-10-11 | M3 source checkpoint | Initial source commit on main, tag m3-touch-interaction; private origin jendrickcon/LAYA; generated files and local SDK/signing files excluded |

Build environment: Windows, Android Studio JBR 25.0.3, Gradle 9.6.0, AGP 9.4.1,
Kotlin/Compose compiler plugin 2.4.0, SceneView 4.23.0, Android SDK Platform 36.

Commands used:

```powershell
$env:JAVA_HOME = 'C:\Users\DOT36\jendrick\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug :app:testDebugUnitTest :app:lintDebug --console=plain --max-workers=1 --no-parallel '-Pkotlin.compiler.execution.strategy=in-process'
.\gradlew.bat :app:assembleDebug :app:lintDebug --console=plain --max-workers=1 --no-parallel '-Pkotlin.compiler.execution.strategy=in-process'
```

The second command verified the final manifest-only correction. Unit tests were not repeated
after that correction because Kotlin source and test logic were unchanged.

Failures found and repaired:
- Wrong archive extraction location: root configuration remained the wizard's, and imported
  test files were nested below the production source directory.
- ARCore required/optional metadata conflict with the library manifest.
- Unqualified adaptive icons incompatible with the imported minSdk 24.
- Lint error for an implicit camera hardware requirement.
- SceneView-added INTERNET and VIBRATE permissions contradicted the intended milestone scope.

The original unrestricted build was cancelled after a manifest failure and low-memory slowdown.
The next build flag attempt failed because PowerShell split an unquoted -P property; quoted
arguments worked. Subsequent runs used one worker and in-process compilation.

The fallback change used the first command above and completed successfully in 2m 6s.
The XML test report confirms all 8 placement-state tests passed, including Quick placement
without a plane, tracking loss overriding Quick placement, and existing placement taking
priority. The lint XML still reports 21 warnings and no errors. AAPT2 checked the new APK's
version, launch activity, and permissions. These checks do not prove either scene works on device.

The next handoff changes documentation only; build checks were not repeated. Generated handoff
files are outside the repository in the task workspace's output/laya-handoff-20261010 directory.
The ZIP is a source snapshot/backup; the text bundle is the supported-format chat input.

## Reference-material checks (documentation only, no code run)
- 2026-10-10: Competition PDF read; guidelines summarized in STATUS.md match the team's earlier summary.
- 2026-10-10: SceneView 4.23.0 Kotlin/SDK requirements taken from the project's llms.txt snapshot.
- 2026-10-10: Repair inspected the tagged 4.23.0 version catalog, its published Maven POM, and
  sceneview/arsceneview 4.23.0 source JARs. Gesture callbacks, composable node APIs, material
  creation, and AnchorNode detachment were checked against that release.
- 2026-10-10: Fallback work checked SceneView 4.23.0's non-AR scene/camera/gesture APIs and AR
  instant-placement configuration against the published sources. The official ARCore Frame
  reference confirms Instant Placement still requires TRACKING and sufficient feature points;
  its developer guide explains approximate depth and subsequent pose/scale changes.

## Device testing (Redmi Note 9 Pro, Android 10)
User-reported initial run (2026-10-10): the app displays insufficient-detail / tracking-loss hints
at home, where a suitable desk/table is unavailable. This is a user observation, not an
agent-controlled device check; successful placement was not reported.

Follow-up user report (2026-10-10), after receiving the fallback build: "it is working."
The tested mode and individual actions were not specified. Record this as general user-reported
success; do not mark the remaining AR, permission, reset, and lifecycle checks as passed.

M2 user report (2026-10-11): "i tried it in my phone and it's working."
The user asked for no further testing and for the next Astra implementation instructions.
Record this as user-reported phone success, not as a pass of every individual checklist item.

Not verified: permission grant/denial; plane detection; tap to place; walk around anchored
cube; reset and re-place; background/return; tracking interruption (cover camera); airplane-mode
offline run; Google Play Services for AR absent behavior.

The earlier repair saw only an unauthorized emulator. ADB listed no devices during the fallback
change. No APK was installed or launched by the agent during those earlier M1 checks.
Also check that dragging does not place a
cube, and repeated reset/re-place cycles remain stable.

## Fallback phone checks (not run)
- Launch without camera permission; choose 3D preview. A virtual board should appear without a
  permission prompt or AR session. Tap to place; orbit/zoom; Reset; repeat at least 10 times.
- Android Back and Back to AR should return to the permission screen while permission is denied.
- With camera permission granted, switch AR → preview → AR repeatedly, both before and after
  placing. The old cube should be gone and the resumed AR session should scan again.
- In AR, choose Quick placement before a plane is found. While the camera is tracking, tap to
  try estimated placement. Check that it is clearly labeled and never promises a real surface.
- When a horizontal plane is available, Quick placement should prefer that plane.
- Interrupt tracking: Quick placement should not place a cube. 3D preview should stay accessible.
- Use surface detection should restore the original plane-only path. Reset should detach the
  old anchor; switching modes should not retain it.
- Background/resume both modes; check visibility, camera release in preview, and no crashes.
- Repeat offline after Google Play Services for AR is provisioned. The preview uses bundled
  generated geometry and SceneView's bundled lighting; no remote assets.

## Five-run demonstration (required before declaring MVP ready)
Not run. Record per run: date, duration, lighting/surface, outcome, failures.

## Milestone 2 phone acceptance (general success reported; individual results not enumerated)
- Enter preview without camera permission; place the board by button and by scene tap.
- At distance 1 the fixed stack tips down and crossing is disabled. At 3 the movable side tips
  down. At 2 the bridge levels and crossing becomes available.
- Trigger crossing once; controls lock; the spirit reaches the opposite platform and an
  explanation appears. Play again restores distance 1 and the starting platform.
- Repeat twice, then reset during crossing and re-place: no old progress or delayed completion.
- Background mid-crossing for several seconds; return and confirm progress resumes smoothly.
- Check orbit/zoom, readable controls, and larger font settings on the target phone.
- Place through surface detection and Quick placement; play the same puzzle in AR.
- Lose tracking before/during crossing: start is blocked or progress pauses. Recover tracking
  and continue. Reset remains available if the anchor is permanently lost.
- Switch preview/AR repeatedly, with and without a placed puzzle; check camera release and no
  stale state. Repeat offline after AR services are provisioned.

## M2 emulator attempt and stop
- The agent installed 0.2.0-m2 on a temporary read-only API 37 emulator. Launch and the camera
  rationale/preview entry rendered. On entering preview, the SwiftShader graphics backend
  aborted in Filament: "OpenGL program fxaa failed to link or compile." No puzzle interaction
  was verified in that attempt.
- A read-only hardware-graphics retry was started, then stopped when the user reported phone
  success and asked for no further testing. No passing result is claimed for that retry.
- The agent-created emulator and its dedicated ADB server on port 5038 were stopped.
- No more test/emulator runs were performed after the user's request. The next task requests
  APK assembly only; new implementation checks must be reported separately from prior M2 results.

## Known limitations
The phone build is user-reported working; detailed AR placement/recovery and performance results
were not enumerated. The user's previous Android projects already establish
the phone's basic build/install workflow; LAYA's AR-specific behavior still requires an actual run.
Lint's 21 warnings remain documented rather than suppressed. They include update notices,
portrait-lock/large-screen behavior, backup configuration, and unused default resources.
