# STATUS

Last updated: 2026-10-11

## Implemented (M4 compiled; M2 phone success reported previously)
- Milestone 4 / 0.4.0-m4 (versionCode 5): a short floating garden with left/right steps,
  a two-tile jump over a branch, one collectible bud, and a flower-gate exit. Pure Kotlin
  GardenState owns movement and completion; Skip intro offers an accessible route to the exit.
- Before the exit, the board is hidden and only garden controls are shown. Exit completion
  unlocks touch extraction and Free her. An early release returns to the exit without losing
  the bud. One fairy node follows a continuous outward/downward path as the board appears.
- Original primitive fairy: warm brown skin, dark hair, cream petal skirt, olive bodice,
  pale leaf wings, flower headpiece, and small movement gestures. Reference media are not shipped.
- The garden and fairy render through the same scene in preview and AR; existing weight dragging,
  integer balance rules, accessible distance controls, explanation, placement, and tracking gates
  remain. Replay resets the entire garden/escape/puzzle sequence at the same placement.
- M4 assembly passed in 9s; final heading-copy refinement assembled in 2s. APK metadata confirms
  versionCode 5 / 0.4.0-m4 and unchanged permissions. No emulator, tests, lint, install, or device
  runtime check was run for M4. The local milestone checkpoint is tagged m4-playable-screen.

### Previous milestones
- Milestone 3 / 0.3.0-m3 (versionCode 4): original floating frame and spirit above the board,
  touch grab/pull with a visible release threshold, smooth return on early release/cancel,
  one scripted descent to the starting platform, and a Free her button. Puzzle controls unlock
  only after landing. The same spirit geometry is used for escape and crossing.
- Stage A was assembled successfully before Stage B began. Stage B adds dragging of the single
  movable weight, visible selection feedback, constrained movement, and nearest-slot snapping.
  Cancellation restores the committed slot; balance math updates only on release through the
  existing distance-selection path. Distance buttons remain as the accessible fallback.
- Preview and AR share the escape state, puzzle holder, scene, controls, and touch controller.
  Grabs use actual node hit tests and camera rays transformed into board/lever interaction planes.
  Whole grabbed pointer streams are consumed so preview orbit cannot compete. Degenerate view
  angles, multi-touch, cancellation, tracking loss, and backgrounding cancel uncommitted drags.
- Replay restores the whole trapped-avatar/puzzle flow at the current placement. Reset and mode
  changes clear the experience. Escape return/descent and crossing use a lifecycle/tracking-gated
  frame clock, with fresh timing after resuming.
- In AR the board faces the player once at placement; its screen then stays under the same anchor.
  Preview frames both board and screen and uses a stable-height scrollable control card.
- M3 assembly passes: Stage A in 31s; Stage B in 15s; final control-copy/scroll refinement in 2s.
  APK metadata confirms versionCode 4 / 0.3.0-m3, CAMERA plus the internal AndroidX permission,
  and no INTERNET or VIBRATE. No emulator, tests, lint, install, or runtime check was run for M3.
- Previous Milestone 2 / 0.2.0-m2 (versionCode 3): one shared Balance Bridge scene in preview and AR,
  three weight positions, animated tilt, original geometric spirit crossing, explanation,
  replay at the same placement, and placement reset.
- Pure Kotlin puzzle rules use signed integer mass × distance, validate state, bound inputs,
  lock crossing controls, prevent duplicate starts, and cap delayed frame deltas.
- A shared Compose game holder runs crossing only during active play and a resumed lifecycle.
  AR additionally requires both camera and anchor tracking. Resuming starts a fresh frame clock.
- Previous M2 checks: 12 puzzle tests and 8 placement tests passed; assembly/lint passed
  (0 errors, 21 warnings). These results do not verify M3.
- The user reports on 2026-10-11 that the M2 build works on the phone and requests no further
  testing at this point. Specific modes/checks were not enumerated.
- Milestone 1 source: camera permission gate with rationale and denial path, AR scene,
  horizontal plane scan, anchored placement, reset, tracking-loss hints,
  session-failure message, pure-Kotlin placement phase logic with 8 passing unit tests.
- Camera-free 3D preview accessible before camera permission and from the AR screen: virtual
  board, shared puzzle, single-tap/button placement, orbit/zoom, reset, and Back to AR.
- Explicit Quick placement toggle: prefer horizontal plane hits, then ARCore Instant Placement
  with an approximate 0.5 m distance. Placement remains gated on camera tracking.
- The old shared cube has been replaced by shared puzzle content. Mode switches discard placement.
- Version 0.1.1-m1 (versionCode 2) was the previous fallback checkpoint.
- Imported files now live at the project root and normal main/test source locations.
- ARCore-required metadata explicitly overrides SceneView's optional declaration.
- Confirmed single-tap gestures replace raw ACTION_UP handling, so drags are not intentionally
  treated as placement taps. A shared explicit material colors the 10 cm cube.
- SceneView's AnchorNode lifecycle handles detachment; the duplicate parent detach effect is removed.
- Adaptive launcher icons are qualified with mipmap-anydpi-v26, preserving minSdk 24.
- Dependency-added INTERNET and VIBRATE permissions are removed from the merged manifest.
- Private GitHub origin configured at https://github.com/jendrickcon/LAYA.git on branch main.
  M3 is the initial source commit, checkpoint tag m3-touch-interaction. Generated caches, APKs,
  local.properties, and signing files are ignored.

## Incomplete
- M4 runtime/device acceptance, final character/art, polish, and the competition package.
  The full competition MVP is not verified.
- App icon/theme resources still use the Android Studio wizard defaults.
- Detailed phone acceptance checks remain pending; M4 assembly is the current build evidence.

## Blockers
- No build blocker: 0.4.0-m4 assembles successfully. Further testing is deferred by user request.
- The user reports insufficient visual detail / tracking loss on the first phone run at home.
  The user later reports "it is working," without specifying the mode or individual checks.
  The fallback is usable according to that report. The M2 phone build is also user-reported working.
  This establishes a usable development checkpoint; complete AR acceptance is not enumerated.
- No device was connected to ADB during the fallback change.

## Known defects / unknowns
- SceneView APIs now compile against the published 4.23.0 artifact. Cubes/anchors use the
  composable scope functions, not imported imperative node constructors.
- M4 garden controls, fairy appearance, object picking, breakout continuity, drag feel after orbiting,
  AR heading, lifecycle/tracking recovery, repeated
  replay/reset, and control-card layout have not been run on the target phone.
- The garden is bounded shallow 2.5D geometry, not a render-to-texture screen or clipped portal.
  Side views can reveal the depth. The opaque-wing fairy is a first art pass; no detailed rig or
  physical screen interaction is claimed. The small board scales into view during descent.
- Very shallow/edge-on camera angles cannot project a stable drag; the interaction cancels with
  a hint and the accessible controls stay available. Small objects may be easier to grab after zooming.
- The bridge is a teaching model, with scripted tilt/crossing rather than a physics simulation.
  The character does not add mass to the lever; the compared loads are the three weight blocks.
- Instant Placement does not remove the camera-tracking requirement and may change pose/apparent
  scale as depth is refined. Preview exercises rendering and interaction, not AR tracking.
- The previous M2 lint run reported 21 warnings: pinned dependency/SDK update notices, portrait-orientation notices,
  backup configuration, and unused wizard resources. No lint baseline or error suppression was added.
- An API 37 emulator using SwiftShader aborted on Filament's FXAA shader before board rendering.
  A hardware-backend retry was started but cancelled at the user's request; it was not verified.
  The test emulator and dedicated ADB server have been stopped.

## Decisions
- D1 Kotlin/Compose compiler plugin 2.4.0 (Audiary declares 2.2.10). The tagged SceneView 4.23.0
  version catalog and published Maven POM identify Kotlin 2.4.0. The LAYA configuration with
  AGP 9.4.1 and Gradle 9.6.0 now builds. Applies to LAYA only.
- D2 SceneView pinned 4.23.0, not the newest (4.53.0 per README on 2026-10-10). Reason: 4.23.0 is
  the release initially selected from its docs. The repair also inspected the published POM and
  source JARs for 4.23.0. Upgrade deliberately rather than changing a working pin to remove a warning.
- D3 minSdk 24 (SceneView minimum) for compatibility across Android versions; judged criterion.
  Manifest requires ARCore, so Play Store listing is limited to ARCore-capable devices.
- D4 Portrait lock for MVP to avoid Activity recreation during AR sessions.
- D5 Dependencies kept from Audiary: core-ktx, lifecycle, activity-compose, Compose BOM, material3.
  Dropped: Room, KSP, Coil, OkHttp, Navigation, coroutines-test, icons-extended, Espresso.
- D6 Touch-triggered breakout is the baseline. Hand extraction is a later isolated prototype.
- D7 Keep compileSdk/targetSdk 36 and minSdk 24 for this checkpoint. Older-compatible launcher
  fallback images remain; adaptive icon XML applies only on API 26 and later.
- D8 Camera/ARCore hardware declarations are explicit, and unused networking/vibration permissions
  from SceneView are removed. Future haptics need an intentional permission decision.
- D9 Low-memory verification uses one Gradle worker and in-process Kotlin compilation. This is
  a build setting, not an AR performance measurement.
- D10 Keep the real AR baseline, but add clearly labeled development fallbacks because the user's
  available room/surfaces make tracking difficult. Preview creates a normal SceneView, not an
  ARSceneView, and bypasses the camera-permission gate. ARCore-required manifest metadata remains
  unchanged; preview is not a promise of distribution to devices without ARCore.
- D11 Quick placement uses Config.InstantPlacementMode.LOCAL_Y_UP and
  Frame.hitTestInstantPlacement(x, y, 0.5f), only after an explicit user choice. It prefers a valid
  upward-facing plane hit and handles tracking/anchor-resource failures. Depth API stays disabled.
  API signatures were checked against SceneView 4.23.0 sources and the official ARCore
  [Frame reference](https://developers.google.com/ar/reference/java/com/google/ar/core/Frame#hitTestInstantPlacement(float,%20float,%20float))
  and [Instant Placement guide](https://developers.google.com/ar/develop/java/instant-placement/developer-guide).
- D12 One small puzzle uses 2 units at -1 distance step against 1 unit at +1/+2/+3. UI distances
  map to scene metres; correctness depends only on integer model values.
- D13 Share BalanceBridgeScene and BalanceBridgePanel between both modes. Preview has a separate
  scene viewport and on-screen placement button; AR keeps its camera view and overlay controls.
- D14 Preview/AR scripted escape and crossing pause with lifecycle/tracking and resume from saved
  progress. M3 replay restores both the trapped avatar and puzzle; placement reset and mode
  switches clear the whole placed game.
- D15 M3 uses finger-on-screen dragging: first the avatar's floating-screen escape,
  then direct manipulation of the existing movable weight. Camera-based hand tracking remains
  a later isolated prototype with touch fallback.
- D16 User asks to stop further testing after reporting phone success. The next-task prompt
  requests APK assembly only; automated/emulator checks are deferred unless requested later.
- D17 Pure Kotlin EscapeState models trapped, dragging, returning, escaping, and playing.
  WeightDragState holds an uncommitted continuous distance; only release selects a puzzle slot.
- D18 SceneView 4.23.0's raw touch callback runs before its node/camera gesture detectors.
  Consume the full grabbed sequence and use viewport-local event coordinates, a camera ray,
  and live node world-to-local transforms. Avatar plane is vertical; weight plane follows the lever.
- D19 Keep JVM 17 and all dependency pins. Avoid kotlin-math's JVM-21 inline vector operators
  at app call sites by using scalar coordinate arithmetic. Correct rememberARView's package
  and avoid a Kotlin property-setter/method JVM signature clash found during Stage A assembly.
- D20 Board heading is calculated from the camera in anchor-local space only at placement,
  using ARCore Pose inverse/transformPoint. The board and floating screen keep that local heading.
  API reference: https://developers.google.com/ar/reference/java/com/google/ar/core/Pose
- D21 M4 adds SCREEN_PLAY and EXIT_READY before existing escape phases. One small lane with a
  blocked tile, fixed-duration hops, a bud, and an exit keeps the feature bounded and offline.
- D22 Keep the same named fairy under the same board-local parent throughout garden, extraction,
  landing, and crossing. Reveal the board only during descent; the garden remains anchored above it.
- D23 Generate the original fairy and garden from lightweight geometry. Opaque pale wings avoid
  transparency sorting; an unlit backdrop/bud communicates a screen without a second renderer.
- D24 The user explicitly requested assembly only for M4: skip emulator, automated tests, and lint.
  Compilation and source inspection establish implementation/build evidence, not runtime acceptance.

## Repository repair and backups
- The import originally sat inside app/src/main/java/com/example/laya, leaving the wizard's
  Gradle configuration active. Fourteen imported files were copied into their intended locations
  and hash-checked. The original imported folder was moved outside the project for preservation.
- Original source snapshot: ../LAYA-before-layout-fix-20261010-03d872a5.zip
- Original imported files and archived wizard sample tests:
  ../LAYA-imported-archive-backup-20261010/

## Competition facts (from the supplied PDF, 2026-10-10)
- Categories: Game, Productivity, IoT. Productivity explicitly covers learning and teaching
  techniques. Register LAYA as Game and make play the primary experience.
- Team: max 3 participants plus a faculty coach. All must attend. Incomplete submission is a
  ground for disqualification.
- Submission (Google form): school, names, app name, abstract 100–150 words, hardware/software
  requirements, product features, screenshots with brief descriptions, as PDF.
- Presentation 5–10 minutes: problem solved, how it works, unique features; then Q&A.
- Judging, 20% each: Innovation, Effective Use of Android Platform (hardware, compatibility across
  versions/devices), Security (encryption, authentication, vulnerabilities, data privacy),
  Functionality, UX. Three judges, IT professionals.
- Security story must be honest: no accounts, no network, minimal permission, no stored images.
  Do not claim encryption.

## Next recommended task
The implementation in docs/ASTRA-M4-PLAYABLE-SCREEN.md is delivered as M4. The user handles phone use;
no more testing is requested for this pass. Use subsequent phone feedback to guide garden/reveal/touch/layout
refinements, then finish presentation polish and the competition package before the isolated
real-hand-tracking prototype. The full competition MVP is not yet verified.

Local source checkpoints (outside the repository) preserve M2 before editing and Stage A after
its successful assembly under C:\Users\DOT36\jendrick\PROJECTS\tmp\laya-review\m3-checkpoints.
The local Git author identity reuses the existing Audiary identity, configured for LAYA only.
The m3-touch-interaction tag preserves M3; m4-playable-screen preserves the M4 implementation.
The user merged PR #1 into main. Its title and description are completed, and the M4 prerelease
now contains LAYA-0.4.0-m4-debug.apk. GitHub reports the uploaded asset's expected size and SHA-256.
README links to the release and direct download; access requires this private repository's permissions.
No new app build or runtime checks were performed for the publication patch.
