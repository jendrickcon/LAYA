# LAYA: The Desk Breakout

LAYA is an educational Android AR game. Play a tiny garden inside a floating screen, then pull
the same sampaguita-inspired fairy out of it with a finger on the phone screen. A miniature
board appears during her descent, leading into Balance Bridge: weight and distance from a pivot.

Intended entry for the ITSO 2026 Application Development Using Android Programming, Game category.

## Download the APK

[Get the APK from the M4 release page](https://github.com/jendrickcon/LAYA/releases/tag/m4-playable-screen)
under **Assets**, or [download LAYA 0.4.0-m4 directly](https://github.com/jendrickcon/LAYA/releases/download/m4-playable-screen/LAYA-0.4.0-m4-debug.apk).
Sign in to a GitHub account with access to this private repository to download it.
This development prerelease targets Android 7.0+ (API 24), requires an ARCore-supported device
for distribution, and has not been runtime-verified on the target phone.

The uploaded `LAYA-0.4.0-m4-debug.apk` is 45,555,928 bytes. GitHub's asset checksum matches
SHA-256: `5AB93D7C2D2E231613DCDC160C84EB949175E82DD6DC68EBFD51EECC78EED32F`.

## Status
Milestone 4 adds a playable garden, a continuous screen-to-board reveal, and an original geometric
fairy in the shared preview/AR experience. Version **0.4.0-m4 (versionCode 5)** assembles successfully.
No emulator, automated tests, lint, or phone run was performed for M4, as requested.
The user-reported phone success and 20 passing tests belong to the previous M2 build.
See `docs/STATUS.md` and `docs/VERIFICATION.md` for the actual checks.

`docs/ASTRA-M4-PLAYABLE-SCREEN.md` records this milestone's scope. Camera-based real-hand tracking stays
later; all current dragging uses a finger touching the phone screen.

## Play the garden, breakout, and Balance Bridge
1. Choose **3D preview (no camera)**, then **Place experience** (or tap the scene).
2. Use **Left**, **Right**, and **Jump** to clear the branch, collect the glowing bud, and reach
   the flower gate. A short route is **Right ×3 → Jump → Right ×3**, letting each move finish.
   Jump travels two tiles in the last chosen direction. **Skip intro** takes the fairy to the exit.
3. At the gate, touch the fairy and drag her down until her feet reach the marker line.
   When the prompt says **Release now**, let go. An early release returns her to the exit and
   keeps the collected bud. **Free her** is the accessible alternative. The board appears during
   her continuous outward/downward escape, and the puzzle unlocks after landing.
4. Drag the single teal weight along the right side of the lever. It highlights while held and
   snaps to the nearest 1 / 2 / 3 slot on release. The **1 step**, **2 steps**, and **3 steps**
   buttons remain available. Cancelling a drag restores the last committed position.
5. At **2 steps**, both sides balance. Tap **Cross the bridge**, then read the explanation.
   Scroll the control card if its lower controls are outside view.
6. **Play again** resets the garden, bud, fairy, and puzzle at the same placement.
   **Reset placement** removes the experience. **Back to AR** returns to camera placement.

In preview, drag empty space to orbit and pinch to zoom; grabbing an object consumes that touch
sequence so it cannot also orbit the camera. Very shallow/edge-on views cancel object dragging
safely; change the viewing angle or use the buttons.

In AR, tap a detected surface or opt into Quick placement to place the same experience. The board
faces the player at placement and then stays anchored. Tracking loss/backgrounding pauses the
garden movement, escape, and crossing and cancels active drags. Mode switches discard placement
and game progress. The first fairy art pass uses cream petals, an olive bodice, warm brown skin,
dark hair, pale leaf-shaped wings, and a flower headpiece.

The floating game is a shallow, bounded 2.5D scene against an unlit backdrop. It does not use a
render-to-texture viewport or portal clipping; side views can reveal its shallow depth. Its
appearance, touch feel, and performance still need feedback from the target phone.

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
The source checkpoints are tagged `m3-touch-interaction` and `m4-playable-screen`. APKs, build caches,
local SDK paths, and signing files are excluded from Git.

## Run on a device
Target phone: Redmi Note 9 Pro, Android 10. Enable USB debugging, connect, press Run in Android
Studio. Google Play Services for AR must be installed (the Play Store installs it automatically
for most devices). For real AR, use a flat surface with visible detail in good light; a floor or
book can work too. A desk is not required by the placement code.

## Test without a desk
- **3D preview (no camera):** available on both the permission screen and the AR screen.
  It opens the virtual experience with no AR session or camera permission requirement. Place it,
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
messages, optional approximate placement, virtual preview, a playable garden, a touch-drag escape,
and one educational balance puzzle.
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
The floating garden, fairy, board, and weights use original arrangements of SceneView primitives.
The supplied video and fairy pictures informed the concept, palette, and miniature scale; their
images, characters, and media are not embedded in the APK. Lighting uses SceneView's bundled
environment. No external character models or remote assets.
Record every future asset here with author, license, and source. No Nintendo or copied game assets.
