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
This experimental branch, **portal-rnd**, adds **M5 / 0.5.0-portal-poc (versionCode 6)**.
The debug APK builds successfully. M4's garden, touch escape, and Balance Bridge remain the default
experience; a separate **Portal lab (experimental)** entry opens the new rendering study.
The download links above still point to the published **M4 baseline**. M5 has not been published.
Build M5 locally to get `app/build/outputs/apk/debug/app-debug.apk`.

M5 implements a contained 3D world rendered into a floating window, with an asymmetric camera
projection driven by viewer position. It has a static fairy, near/far markers, and preview viewpoint
sliders. It has **not been observed in preview or AR on the phone**. Emulator, automated tests,
and lint were skipped as requested. See [portal findings](docs/PORTAL-RESEARCH.md),
[status](docs/STATUS.md), and [verification / phone checklist](docs/VERIFICATION.md).

M4 phone screenshots supplied by the user show the garden and Balance Bridge; they do not verify
every transition. Earlier general phone-success reports and 20 passing tests belong to M2.
Camera-based hand tracking stays later; current M4 dragging uses a finger touching the phone screen.

## Try the experimental portal

1. Open **3D preview**, choose **Portal lab (experimental)**, then **Place portal**.
   Use the left/right and below/above sliders to change the viewing position.
2. **Back to AR** keeps the lab selected. Place on a detected surface or use labeled
   approximate Quick placement; start around 40–80 cm from the window.
3. Move the phone sideways and moderately above/below. The gold near post and blue farther arch
   should show different parallax and changing overlap, always inside the rectangle.
4. **Reset placement** removes the experiment. **Return to LAYA** restores the normal M4 route.
   Switching modes discards placement/progress.

The portal is a front-facing rendering prototype with a static world. It does not yet add gameplay,
hand interaction, or a new breakout. Back/very close/steep views hide the interior and show a hint.
Frame rate, target orientation, lifecycle recovery, and the AR illusion need phone verification.

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

The normal M4 floating game is a shallow, bounded 2.5D scene against an unlit backdrop. It does not use a
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
Private repository: [jendrickcon/LAYA](https://github.com/jendrickcon/LAYA); M5 work is isolated on
`portal-rnd`, with `main` preserved. The M5 local checkpoint is `m5-portal-poc`; previous checkpoints
are `m3-touch-interaction` and `m4-playable-screen`. APKs, build caches,
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
| Resolved Filament / ARCore (transitive, unchanged) | 1.72.1 / 1.54.0 |
| compileSdk / targetSdk / minSdk | 36 / 36 / 24 |

## Assets and attribution
The floating garden, fairy, board, and weights use original arrangements of SceneView primitives.
The supplied video and fairy pictures informed the concept, palette, and miniature scale; their
images, characters, and media are not embedded in the APK. Lighting uses SceneView's bundled
environment. No external character models or remote assets.
Record every future asset here with author, license, and source. No Nintendo or copied game assets.
