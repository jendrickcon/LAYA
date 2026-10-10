# Astra next task — Milestone 3: touch-drag breakout and object interaction

Implementation note, 2026-10-11: Stage A and Stage B are now implemented in 0.3.0-m3 /
versionCode 4. APK assembly passed; emulator, automated tests, lint, and device checks were
not run for this milestone. This document is retained as the delivered scope; see STATUS.md
and VERIFICATION.md for current evidence and remaining work.

Work directly in C:\Users\DOT36\AndroidStudioProjects\LAYA.
Read AGENTS.md, README.md, STATUS.md, VERIFICATION.md, and the current Kotlin source.
Implement the next milestone; deliver working code and a debug APK.

## Current checkpoint and latest user instructions

- The starting app is 0.2.0-m2 / versionCode 3.
- Balance Bridge already works in camera-free preview and the existing AR placement paths.
  It has three weight positions, tilt, spirit crossing, explanation, replay, and placement reset.
- The user reports on 2026-10-11: "i tried it in my phone and it's working."
  Use that working phone checkpoint as the base. The individual mode/checks were not specified.
- The user asked to stop testing and provide the next implementation instructions.
  Do not launch an emulator or run automated test/lint suites in this task unless the user later
  requests them. Assemble the debug APK for installation and repair compilation errors.
  Record any checks not run honestly; the latest user instruction overrides the routine
  verification commands in AGENTS.md for this pass.
- SceneView is pinned to 4.23.0, Kotlin/Compose compiler 2.4.0, AGP 9.4.1, Gradle 9.6.0;
  compile/target SDK 36, minimum SDK 24. Do not change working pins without a concrete need.
- Target phone: Redmi Note 9 Pro, Android 10.
- Earlier M2 local checks already passed: assembly, 20 unit tests, lint with 0 errors / 21 warnings.
  Do not describe those earlier results as verification of this new milestone.

## Stage A — the avatar escapes her floating screen

Complete this stage as a usable checkpoint before implementing Stage B.

After placing the experience:
1. Show the original lantern-spirit placeholder trapped inside a small floating framed screen
   above the board. The screen and board share one local origin / AR anchor.
2. Keep the puzzle interaction locked until the escape finishes. Tell the player:
   "Drag her down to set her free."
3. A touch that grabs the avatar starts a downward drag. Move the avatar visibly with the drag
   along a constrained path; picking her up must feel like grabbing the visible character.
4. If the player releases before the clearly communicated escape threshold, or the pointer
   is cancelled, smoothly return her to the screen.
5. Beyond the threshold, play one short scripted escape/descent and land her on the existing
   bridge's starting platform. Then enable the current Balance Bridge puzzle.
6. Add an accessible "Free her" button that completes the same transition, for users who cannot
   perform the drag. This remains touch interaction, never advertised as real-hand tracking.

Use simple generated geometry. Reuse/extract the current original spirit geometry so she looks
the same in the screen, while escaping, and while crossing. A framed flat panel with a small
original backdrop is sufficient; do not embed another game, WebView, video, or Nintendo assets.
The floating screen should have a convincing front-facing or slightly tilted orientation and
remain positioned relative to the placed board as the AR camera moves.

Keep the escaped character visibly grounded on the board. Use a scripted path, not physical
table-edge detection, collisions, or gravity simulation. Retain the floating frame after escape
if useful for the before/after effect, while keeping the puzzle readable.

## Stage B — drag the movable weight with a finger on the phone screen

Once Stage A is implemented, add direct touch dragging of the existing 1-unit weight.
This is the first interactive obstacle/object; do not add new levels or arbitrary obstacles.

- Touch the movable weight to select it. Give visible selection feedback.
- Drag it along its existing right-side lever axis, constrained to the puzzle's valid range.
- On release, snap to the nearest of the existing 1 / 2 / 3 distance slots and update the SAME
  BalanceBridgeState via its existing distance-selection path.
- Keep the 1-step / 2-step / 3-step buttons as an accessible fallback.
- Do not let the fixed 2-unit stack, pivot, bridge, or board move.
- Lock weight dragging while the spirit is escaping, crossing, or complete, and while AR
  tracking is unavailable. Cancel an active gesture cleanly on tracking loss.
- Cancelled drags return to the last committed slot; they must not silently solve the puzzle.
- Feedback and correctness continue to use the existing integer weight × distance model.

## Input and architecture requirements

- Share the experience state and scene content between preview and AR.
- Keep flow/drag rules independent of Android where practical. Use explicit states for the
  trapped avatar, dragging, escape animation, and puzzle play.
- Interpret pointer positions relative to the actual scene viewport. Preview's viewport is
  inside a Column; AR uses a full camera view. Do not assume both have identical screen offsets.
- Hit-test the intended object. If mapping a drag to the board, use the camera ray and a
  defined interaction plane in board-local coordinates; avoid directly treating screen X/Y as
  AR world coordinates. The interaction must still make sense after orbiting the preview.
- During an object drag, suspend competing preview orbit/pan handling. Restore normal camera
  gestures on release/cancel. Dragging empty space should still operate the preview camera.
- If the current view angle makes the drag projection unusable, cancel safely and leave the
  on-screen buttons available.
- Pause scripted animation during backgrounding and AR tracking loss. Resume without jumping
  or completing an animation twice.
- Replay at the same placement should restore the trapped avatar and initial puzzle.
  Placement reset and mode switching clear the entire experience and any in-progress pointer.
- Keep permissions, camera denial behavior, Quick placement labeling, and camera-free preview.
- Follow SceneView's existing remembered geometry/material and AnchorNode ownership patterns.
- Verify unfamiliar API signatures against the pinned 4.23.0 source rather than guessing.
  Published sources are already extracted under:
  C:\Users\DOT36\jendrick\PROJECTS\tmp\laya-review\scene\
  C:\Users\DOT36\jendrick\PROJECTS\tmp\laya-review\ar\

## Real-hand tracking is a later work package

Do not add MediaPipe or open a second camera pipeline in this milestone.
Camera-based hand tracking remains a later isolated prototype under the existing roadmap,
after the core game and competition package. Keep touch as the reliable fallback.

Future technical context, not instructions to implement now:
MediaPipe Hand Landmarker supports Android camera input and produces finger landmarks.
Its normalized image coordinates and hand-centered world landmarks are not already positions
in ARCore's anchored coordinate system. A future prototype must establish the screen/board
mapping, handle selection/pinch/release and tracking loss, and measure performance on the phone.
Start that prototype with one movable weight rather than the entire escape sequence.
Official reference:
https://developers.google.com/edge/mediapipe/solutions/vision/hand_landmarker/android

## Delivery

- Implement Stage A, then Stage B, keeping each checkpoint usable.
- Increment the app version to identify the new build.
- Assemble the debug APK using the established low-memory Windows command, with only
  :app:assembleDebug requested. Do not start another emulator/test/lint run.
- Update README.md, STATUS.md, and VERIFICATION.md with the final controls, actual build result,
  and the user's testing preference.
- Report a short changed-file summary, the APK path, and instructions for grabbing the avatar,
  dragging the weight, replaying, and resetting.
- Keep the full flow: place → trapped avatar → touch-drag escape → Balance Bridge → explanation
  → replay. Do not claim the full competition MVP or real-hand tracking is verified.
