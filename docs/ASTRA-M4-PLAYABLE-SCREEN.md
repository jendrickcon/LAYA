# Astra task — M4: playable floating screen, fairy, and breakout reveal

Implementation record: delivered as 0.4.0-m4 / versionCode 5 on 2026-10-11. APK assembly passed;
emulator, automated tests, lint, and device runtime checks were not run. See VERIFICATION.md.
The scope below is retained as the original milestone brief.

Implement this in C:\Users\DOT36\AndroidStudioProjects\LAYA.
Private repo: https://github.com/jendrickcon/LAYA
Starting checkpoint: main, commit 7d456b2, tag m3-touch-interaction.
This is an implementation request: deliver code and the updated debug APK.

## Budget and current context

I have about 40% of my allowance remaining. Keep this to one small, complete feature pass.
Read AGENTS.md and the relevant current files, then implement. Keep research, output, and
refactoring focused. Do not build a general platformer engine or spend the allowance on art detail.
Prioritize: playable screen -> continuous escape reveal -> recognizable fairy -> assembled APK.
Finish a complete simple version before adding optional decoration.

M3 is 0.3.0-m3 / versionCode 4. It already has preview, AR placement, Quick placement,
a floating frame, touch-drag escape with return/descent, Balance Bridge, direct weight dragging,
distance buttons, explanation, replay, and reset. APK assembly passed; M3 was not run on the phone.
User-reported phone success applies to M2. Preserve the existing working structure.

## The intended audience experience

The current frame looks like an avatar pinned to a display. Change it into a tiny playable game.
At first the audience should think: "This is a game on a floating screen."
The surprise is that the SAME fairy can then be dragged out of that screen into the anchored
tabletop world. The game continues with the existing educational Balance Bridge.

Flow:
Place experience -> play a short garden screen -> fairy reaches the exit ->
drag her through the screen boundary -> she lands on the revealed board ->
move the weight -> cross -> explanation -> replay.

The attached X.mp4 illustrates the screen-to-room reveal. It is visual inspiration; it does
not establish what hardware, hand tracking, or rendering technique produced the original.
Our controls remain touches on the phone screen.

## 1. Make the screen genuinely playable

Build ONE tiny garden level, roughly 10–20 seconds:
- A readable framed display with an original sky/garden backdrop, a ground strip, one low
  obstacle, one collectible glowing bud, and an exit position.
- The fairy responds to Left, Right, and Jump controls. Discrete steps or a short controlled hop
  are acceptable; successful input must actually move her and collect the bud.
- The obstacle requires a jump to pass. Block grounded movement through it rather than allowing
  a button tap to bypass the entire level. Keep jump/movement rules small and deterministic.
- Keep her inside the display during this phase. No camera orbit should move the playfield
  relative to its frame. Preview orbit remains available through empty-space gestures.
- Once the bud is collected and she reaches the exit, lock walking/jumping and make her ready
  to grab. Avoid enemy AI, scrolling levels, scores, inventories, or additional puzzles.

Use a shallow 2.5D playfield in panel-local coordinates inside the existing SceneView.
A flat backdrop and simple generated geometry are enough. Bound the entire fairy silhouette,
including jump height, inside the display. Keep the garden background inside after she escapes.
Use the existing renderer; no second SceneView, embedded Android game, WebView, video, new
graphics engine, or render-to-texture framework for this pass.

Initially hide the tabletop board, weights, and math controls. Show the screen-game controls.
Do not lead with "Drag her out" before the audience has seen it behave like a game.
After reaching the exit, show a brief grab/pull hint and the accessible Free her button.
A discreet Skip intro option may reach the same exit-ready state without trapping users.

## 2. Make leaving the screen a continuous reveal

Reuse M3's pointer capture, early-release return, release threshold, descent, and tracking pause.
Start the drag from her ACTUAL exit position in the screen, not the old fixed lantern position.
She visibly crosses the frame's boundary and moves outward toward the viewer before descending.
Reveal the existing board during descent and land her on its starting platform.

Use one fairy instance/design throughout screen play, dragging, descent, and bridge crossing.
No visible teleport, duplicate fairy, or replacement by the old lantern after landing.
If a scene parent changes, preserve the world pose at that moment; alternatively keep one
shared board-local origin and calculate the whole path there.
Keep the frame floating above the same anchor after she leaves.

Before the exit-ready state, touching her must not trigger breakout.
An early release/cancel returns her to the exit-ready pose without losing the collected bud.
Pause scripted motion on backgrounding/tracking loss, cancel active pointers safely, and resume
with a fresh frame clock. Guard against duplicate escape starts.

## 3. Replace the lantern with an original sampaguita-inspired fairy

Reference images:
- C:\Users\DOT36\Downloads\92c7cc231e4adf6511e983fd128fc575.jpg
- C:\Users\DOT36\Downloads\images.jpg

Use the first image for flower/petal shapes and the cream/olive palette.
Use the second for delicate wings, flowing nature clothing, and her tiny scale beside a hand.
These are inspiration for an ORIGINAL stylized character, not textures to paste into the app.

Minimum recognizable silhouette:
- Warm brown skin, dark hair, a small flower/petal headpiece.
- Olive-green bodice, cream petal/bell skirt, two or four pale leaf-shaped wings.
- Simple face, arms, and feet; keep her readable at miniature scale.
- A subtle hop/wing motion is sufficient. No detailed rig, cloth simulation, realistic portrait,
  expensive transparency, asset hunting, or external model generator is required.

Extend/extract the existing reusable character composable using lightweight generated geometry.
Use opaque pale wing materials if transparency complicates rendering. Every fairy part should
remain under the named avatar node so the existing grab hit-testing can recognize her.
Fit her proportions to the screen and landing platform; picking should match the visible body.
This is the first fairy art pass, not a final production character.

## Architecture and boundaries

Relevant files under app/src/main/kotlin/com/laya/desk:
- game/EscapeState.kt; ui/BalanceBridgeGame.kt: extend the flow with screen play and exit-ready.
- ui/BalanceBridgeScene.kt: current FloatingScreen and LanternSpirit helpers.
- ui/ExperienceTouchController.kt: named node picking and local camera-ray projection.
- ui/BalanceBridgePanel.kt and both preview/AR host screens.

Keep screen-game rules in pure Kotlin with explicit state. Preserve BalanceBridgeState's integer
weight × distance model and existing release-to-commit weight dragging.
Use viewport-local touch events and camera rays transformed into the appropriate local plane.
Update the avatar plane/path constants to match her new exit pose; raw screen pixels are not
AR world coordinates. Object drags must continue consuming their entire pointer sequence.

Replay resets screen play, collectible, fairy, breakout, and puzzle at the same placement.
Reset placement/mode switching clears everything. Preserve permission denial, camera-free
preview, Quick placement labeling, accessible buttons, and anchor/material ownership.
Read matching SceneView 4.23.0 source only when needed; extracted sources are in
C:\Users\DOT36\jendrick\PROJECTS\tmp\laya-review\scene and the neighboring ar directory.

Keep SceneView 4.23.0, Kotlin/Compose 2.4.0, AGP 9.4.1, Gradle 9.6.0, JVM 17, and SDK pins.
M3 uses scalar math to avoid transitive JVM-21 inline-vector operators; preserve that approach.
No MediaPipe, camera-based hand tracking, new permissions, network paths, or other app edits.
Competition paperwork, extra levels, and broad UI polish are outside this pass.

## Build and handoff

Increment to 0.4.0-m4 / versionCode 5.
Build only; skip emulator, automated tests, and lint as the user requested:

```powershell
$env:JAVA_HOME = 'C:\Users\DOT36\jendrick\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug --console=plain --max-workers=1 --no-parallel '-Pkotlin.compiler.execution.strategy=in-process'
```

Repair build errors. Update README.md, docs/STATUS.md, and docs/VERIFICATION.md with the controls,
actual assembly result, and runtime checks not performed. Keep this M4 scope document as a record.
If allowance gets tight, finish the simple playable/reveal/fairy version and assemble it before
adding detail; report any remaining work accurately.

Deliver the APK path and a short change summary. A finished source flow must cover:
play screen -> collect/reach exit -> grab/pull/release -> same fairy lands -> balance/cross ->
explanation -> replay into a fresh screen level.
Compilation does not verify phone behavior or the full competition MVP.
