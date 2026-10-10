# LAYA: The Desk Breakout

LAYA is an educational Android AR game. A small original character is trapped inside a floating
screen. The player drags her down with a finger on the phone screen, she lands on the placed board, and it becomes a
miniature play area with a physics puzzle (Balance Bridge: weight and distance from a pivot).

Intended entry for the ITSO 2026 Application Development Using Android Programming, Game category.

## Status
Milestone 3 implements the floating-screen escape first, then direct touch dragging of the
movable weight, in the shared preview/AR experience. Version **0.3.0-m3 (versionCode 4)**
assembles successfully. No emulator, automated tests, lint, or phone run was performed for M3,
as requested. The user-reported phone success and 20 passing tests belong to the previous M2 build.
See `docs/STATUS.md` and `docs/VERIFICATION.md` for the actual checks.

`docs/ASTRA-NEXT-TASK.md` records this milestone's scope. Camera-based real-hand tracking stays
later; all current dragging uses a finger touching the phone screen.

## Play the breakout and Balance Bridge
1. Choose **3D preview (no camera)**, then **Place board** (or tap the scene).
2. Touch the spirit inside the floating frame and drag her down until her feet reach the marker
   line. When the prompt says **Release now**, let go. An early release returns her to the frame.
   **Free her** is the accessible button alternative. The puzzle unlocks after she lands.
3. Drag the single teal weight along the right side of the lever. It highlights while held and
   snaps to the nearest 1 / 2 / 3 slot on release. The **1 step**, **2 steps**, and **3 steps**
   buttons remain available. Cancelling a drag restores the last committed position.
4. At **2 steps**, both sides balance. Tap **Cross the bridge**, then read the explanation.
   Scroll the control card if its lower controls are outside view.
5. **Play again** restores the trapped spirit and initial puzzle at the same placement.
   **Reset placement** removes the experience. **Back to AR** returns to camera placement.

In preview, drag empty space to orbit and pinch to zoom; grabbing an object consumes that touch
sequence so it cannot also orbit the camera. Very shallow/edge-on views cancel object dragging
safely; change the viewing angle or use the buttons.

In AR, tap a detected surface or opt into Quick placement to place the same experience. The board
faces the player at placement and then stays anchored. Tracking loss/backgrounding pauses the
escape and crossing and cancels active drags. Mode switches discard placement and game progress.
The character is an original geometric lantern-spirit placeholder.

## Open and build
1. Android Studio, open this folder.
2. Gradle sync. Wrapper is Gradle 9.6.0; AGP 9.4.1; Kotlin 2.4.0 (see STATUS decision D1).
3. `./gradlew :app:assembleDebug` (Windows: `gradlew.bat :app:assembleDebug`)

For this milestone the user requested APK assembly only. Automated tests, lint, and emulator
runs are deferred. Earlier verification commands remain documented in VERIFICATION.md.

For a low-memory Windows build, use:

```powershell
$env:JAVA_HOME = 'C:\Users\DOT36\jendrick\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug --console=plain --max-workers=1 --no-parallel '-Pkotlin.compiler.execution.strategy=in-process'
```

The verified build used Android Studio's JBR 25 and installed Android SDK Platform 36.
The generated debug APK is `app/build/outputs/apk/debug/app-debug.apk`.
Private repository: [jendrickcon/LAYA](https://github.com/jendrickcon/LAYA), branch `main`.
The Milestone 3 source checkpoint is tagged `m3-touch-interaction`. APKs, build caches,
local SDK paths, and signing files are excluded from Git.

## Run on a device
Target phone: Redmi Note 9 Pro, Android 10. Enable USB debugging, connect, press Run in Android
Studio. Google Play Services for AR must be installed (the Play Store installs it automatically
for most devices). For real AR, use a flat surface with visible detail in good light; a floor or
book can work too. A desk is not required by the placement code.

## Test without a desk
- **3D preview (no camera):** available on both the permission screen and the AR screen.
  It opens a virtual board with no AR session or camera permission requirement. Place the experience,
  drag empty space to orbit, pinch to zoom, and use Reset placement to place again.
  Back to AR (or Android Back) returns to camera placement. Switching modes discards the old placement.
- **Quick placement:** on the AR screen, choose this before placing, then tap in the camera view.
  A detected horizontal plane is preferred. Otherwise ARCore Instant Placement estimates the
  position about 50 cm away; it may shift or change apparent size as tracking improves.
  Use surface detection restores the normal placement mode before another placement.
  Quick placement still needs camera tracking and sufficient feature points. It cannot resolve
  tracking loss; use 3D preview when the room is unsuitable.

These development shortcuts support the playable puzzle. Preview is not real-world AR, and
neither shortcut proves the competition AR acceptance checks.

## Baseline behavior (target)
Camera permission with rationale, horizontal surface scan, tap to place, reset, tracking-loss
messages, optional approximate placement, virtual preview, a touch-drag escape, and one educational balance puzzle.
Touch controls only.
Stable real-world anchoring still needs device verification. The merged debug manifest has CAMERA as its
only Android platform permission; dependency-added INTERNET and VIBRATE permissions are explicitly
removed. AndroidX also declares an internal signature permission for protected dynamic receivers.
No remote assets are loaded. Offline AR behavior still needs an on-device check.

## Dependencies (pinned)
| Dependency | Version |
|---|---|
| Android Gradle Plugin | 9.4.1 |
| Gradle wrapper | 9.6.0 |
| Kotlin / Compose compiler plugin | 2.4.0 |
| Compose BOM | 2026.02.01 |
| SceneView `arsceneview` (brings ARCore) | 4.23.0 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 |

## Assets and attribution
The floating frame/backdrop, board, weights, and lantern-spirit placeholder use original arrangements of SceneView
primitives. Lighting uses SceneView's bundled environment. No external models or remote assets.
Record every future asset here with author, license, and source. No Nintendo or copied game assets.
