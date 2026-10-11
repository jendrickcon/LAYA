# Astra task — M5: prove the floating portal illusion

You are the technical architect and implementation agent for LAYA. Investigate the rendering
approach, choose the smallest viable solution, and produce an isolated, buildable portal
proof-of-concept. Keep the scope narrow and the reporting factual.

## 1. The question to answer

Can LAYA make a floating rectangular screen feel like a window into a three-dimensional game
world on the user's Redmi Note 9 Pro, Android 10?

This milestone proves containment, depth, and view-dependent perspective. The future vision is:

1. The audience discovers a playable world inside a floating screen.
2. Moving the phone reveals different views into that world.
3. A real hand is represented inside it and the fairy reacts.
4. The fairy steps onto the hand and crosses into the anchored tabletop world.

Only the first two rendering foundations belong to this task. A static fairy is enough.
Third-person movement, hand tracking, palm interaction, and a new breakout implementation follow
after the portal rendering works. Preserve the existing game while researching the new approach.

## 2. Repository and actual history

Project: `C:\Users\DOT36\AndroidStudioProjects\LAYA`.
Private repository: https://github.com/jendrickcon/LAYA
Starting main checkpoint when this brief was written: `aa03206`.
M4 implementation checkpoint: `239f40a`, tag `m4-playable-screen`.
M4 was merged through PR #1; the APK and README download link were subsequently published.

Read AGENTS.md, README.md, docs/STATUS.md, docs/VERIFICATION.md, and the relevant source files.
Inspect the current Git state first: later work may exist. Preserve unrelated changes and use
the latest appropriate M4 main baseline instead of resetting to an old commit.

M4 is version 0.4.0-m4 / versionCode 5. It contains:
- Camera permission handling, regular AR placement, labeled Quick placement, and camera-free preview.
- Shared preview/AR rendering, an original generated fairy, and a playable shallow garden.
- Left/right movement, a branch jump, one collectible bud, and an exit.
- Touch-triggered extraction with early-return behavior and continuous descent/board reveal.
- Balance Bridge, direct touch weight dragging, distance buttons, explanation, replay, and reset.
- Lifecycle/tracking gates and touch ownership that prevent object dragging from also orbiting preview.

M4 assembly passed. The user supplied M4 phone screenshots showing the garden and Balance Bridge
states. These are useful appearance evidence; they do not establish every transition, performance,
or recovery behavior. Earlier full phone-success reports applied to M2. Do not call breakout
unimplemented, and do not turn screenshot evidence into a complete device-acceptance claim.

## 3. Why the current appearance falls short

M4 deliberately used shallow 2.5D geometry in front of a flat backdrop. At side angles, the fairy,
flowers, and platform visibly protrude from the frame. That communicates a diorama.

The desired screen contains its world. Before any deliberate breakout, interior scenery cannot
spill outside its aperture. Objects at different depths must show different apparent movement
when the viewer changes position. Near objects should reveal or obscure farther objects naturally.

A flat texture with a fixed camera gives a floating game display, but it does not meet the
window-into-a-world criterion. Panning texture UVs or rotating the entire garden also does not
prove perspective correctness. Do not prescribe "viewer left means world left": the behavior
must follow geometry, with depth-dependent parallax relative to the window.

## 4. Isolation and scope

Work on a local experimental branch named `portal-rnd`, starting from the appropriate main
baseline. Reuse it if it already contains relevant work; inspect before changing it. Keep main
and the M4 checkpoint intact. Avoid running two development efforts or refactoring the game.

Add a clearly labeled experimental Portal lab entry. The normal M4 experience remains available
and is the default. Prefer one focused feature entry with shared placement plumbing rather than
a rewrite of existing gameplay or a second Activity.

The PoC contains only:
- One thin rectangular portal, approximately 30 cm wide, at a virtual origin or AR anchor.
- A small floor extending into depth, two or three objects at clearly different depths, and a
  static original fairy or simple marker. Use inexpensive generated geometry.
- A deliberately composed initial view from slightly above and behind the fairy.
- Correct containment and perspective changes as the preview camera or physical phone moves.
- Reset, return to the normal game, and a small status/debug label when useful.

No movement controls, new objectives, extra levels, lore, production art, educational redesign,
hand tracking, physics engine, recursion, walking through portals, or second camera capture.
Keep the scene's depth obvious; a near marker and a far marker are more valuable than decoration.

## 5. Verify capabilities before selecting an approach

Keep the existing pins:
- SceneView / arsceneview 4.23.0.
- Kotlin and Compose compiler plugin 2.4.0; Compose BOM 2026.02.01.
- AGP 9.4.1; Gradle 9.6.0; JVM 17.
- compileSdk / targetSdk / minSdk 36 / 36 / 24.

Determine the actual transitive Filament and ARCore versions from the resolved artifacts or POMs;
they are not separately specified here. Consult matching sources. Current online examples are
not proof that the pinned versions expose the same APIs.

Extracted SceneView and AR sources are available under:
`C:\Users\DOT36\jendrick\PROJECTS\tmp\laya-review\scene` and the neighboring `ar` directory.

Investigate only the critical rendering path:
1. Can a separate Filament scene/view/camera render into a texture using the existing engine?
2. Can that texture be sampled by an appropriate material on the anchored rectangle?
3. Where can the offscreen pass run safely relative to SceneView's owned rendering frame?
4. Can the secondary camera use the required asymmetric/custom projection?
5. What clipping or visibility controls exist in this exact version?
6. Are a compatible texture material and any necessary material compiler actually available?

Use local matching source first, then official Filament, SceneView, and ARCore documentation.
Record exact API symbols and relevant file/version references. Do not invent convenience APIs,
assume stencil control exists from an enum name, or put uncompiled shader text into an APK.

Useful starting references, which still need version matching:
- https://github.com/google/filament/blob/main/android/filament-android/src/main/java/com/google/android/filament/RenderTarget.java
- https://github.com/google/filament/blob/main/android/filament-android/src/main/java/com/google/android/filament/View.java
- https://developers.google.com/ar/reference/java/com/google/ar/core/Camera
- https://developers.google.com/ar/reference/java/com/google/ar/core/Pose

## 6. Compare and rank practical approaches

Provide a compact easiest-to-hardest ranking based on this project, not a generic tutorial.
For each approach, explain required APIs, integration effort, visual fidelity, likely GPU cost,
and whether it actually meets the success criterion. Consider:

**Fixed-camera render texture:** useful as a minimal plumbing check. It proves texture output
and sampling, but not a view-dependent portal. Label it an intermediate stage if used.

**A clipped interior in the primary scene:** potentially avoids a second rendering pass.
Investigate the real aperture and near-plane clipping controls. Describe where scenery might
leak, where masking could incorrectly cover the camera background, and any material limitations.

**View-dependent render texture with a secondary camera:** the leading candidate. An offscreen
world is rendered onto the floating rectangle; its projection is derived from the viewer and
portal geometry. Check frame ownership, material sampling, and the extra pass before committing.

Advanced arbitrary or recursive portals are outside scope. Choose the simplest verified approach
that genuinely meets containment and parallax. If the leading candidate is blocked, evaluate one
credible fallback. Avoid repeatedly changing stacks or upgrading dependencies to chase examples.

## 7. Perspective and rendering requirements

Define the coordinate conventions explicitly: portal origin, width/height, front normal, camera
side, interior depth direction, units, and the mapping into the miniature virtual world.

For a render-texture approach, transform the viewer's position into portal-local coordinates.
Use the aperture corners and viewer position to derive the virtual camera and off-axis frustum.
Keep the physical rectangle's own perspective and the texture's internal perspective consistent;
avoid applying the viewer transform twice. Handle the plane orientation and near clipping correctly.

The initial world composition may resemble a third-person game. This task uses a stationary
subject and no following camera: physical viewing movement must have one unambiguous effect.
Do not mix orbit controls, character-follow behavior, and portal projection prematurely.

Start with a useful front viewing range, such as roughly +/-30 degrees horizontally and
modest movement above/below. Treat this as a design target, not an already verified capability.
Handle close, edge-on, and behind-screen views deliberately with safe limits or a simple back
surface. Avoid division by zero, mirrored views, NaNs, extreme projections, and visual explosions.

The window remains anchored while perspective changes. Interior objects stay inside its boundary
at all supported viewpoints. A render-texture world must not also appear in the main AR scene.
No physical room occlusion, Depth API, or table-edge detection is required to prove this illusion.

## 8. Architecture and resource ownership

Relevant current files, relative to app/src/main/kotlin/com/laya/desk:
- ui/ArPlacementScreen.kt: AR session, placement, anchor, tracking, and board heading.
- ui/PlacementPreviewScreen.kt: camera-free preview and camera controls.
- ui/BalanceBridgeScene.kt: shared M4 scene and the existing breakout.
- ui/GardenScreen.kt and ui/SampaguitaFairy.kt: reusable primitive visuals.
- ui/BalanceBridgeGame.kt and ui/ExperienceTouchController.kt: existing state/input boundaries.

Prefer a small portal renderer/resource owner, reusable portal geometry, and pure coordinate/
projection math when practical. Choose file names after inspecting the code; do not add a
framework, DI system, extra Gradle module, or speculative abstraction layer.

Respect SceneView's rendering and lifecycle ownership. Verify exactly where beginFrame/render/
endFrame happen. Do not nest competing frame loops on one renderer or mutate Filament resources
from arbitrary background threads. Reuse the host engine where supported and verify cleanup order.

Remember persistent resources. Avoid creating scenes, cameras, materials, targets, or textures
each frame. Resize only when needed. Destroy only owned resources and account for attachments,
material instances, entities, views, scenes, and cameras. Reset, navigation, tracking loss,
background/resume, and disposal must not leave a second render loop running.

Existing M3/M4 scalar math avoids transitive JVM-21 inline-vector operations; preserve JVM 17.
No new permissions, network path, saved camera frames, paid assets, backend, or other-project edits.

## 9. Execution sequence and budget discipline

Keep research bounded to the APIs necessary for one rectangle and one tiny world.

1. Inspect state, isolate portal-rnd, and write a short capability/approach note.
2. Confirm the critical rendering/material/projection path with matching sources.
3. If using a render texture, prove the fixed-view output first with unmistakable near/far markers.
4. Add portal-relative perspective and preview camera movement; keep every object contained.
5. Reuse placement to attach the same portal experiment to an AR anchor and drive the view
   from the actual AR camera pose. Preserve permission and tracking behavior.
6. Handle unsupported viewpoints, reset, and resource disposal; keep controls minimal and readable.
7. Assemble the debug APK and finish the findings, limitations, and manual phone checklist.

Do not stop at a long architecture essay if a practical implementation path is verified.
Do not exhaust the allowance on optional art or broad optimization. If the critical path is
blocked after the focused attempt and one credible fallback, preserve useful work and report the
specific blocker, evidence, and smallest next action. An honest limited result is better than
declaring a flat screen a successful portal.

Use a conservative texture size and simple lighting first; document the chosen resolution and
render cadence. A smooth 30 fps phone demo is a reasonable target, not a promise. Do not assume
the phone can sustain AR plus two rendering passes from its model name. Report measurements only
if they were actually taken; otherwise leave performance pending.

## 10. Build and verification policy

For an assembled PoC, use version 0.5.0-portal-poc / versionCode 6 on the experimental branch.
Retain the normal M4 route within the app. Build the APK; skip emulator, automated tests, and lint
as requested by the user. Do not turn repository test guidelines into an approval question.

```powershell
$env:JAVA_HOME = 'C:\Users\DOT36\jendrick\Android Studio\jbr'
.\gradlew.bat :app:assembleDebug --console=plain --max-workers=1 --no-parallel '-Pkotlin.compiler.execution.strategy=in-process'
```

Fix compilation failures. Inspect APK version and permissions, and report the resulting path.
Do not claim on-device success from compilation or camera-free preview. The user handles phone
use; supply a short manual checklist rather than starting an emulator or automated suite.

Manual acceptance checklist:
- Place one portal and keep the entire aperture visible from a normal viewing distance.
- Move the phone sideways without rotating it only: near/far markers must move differently.
- Move moderately above/below: interior perspective changes consistently with depth.
- Scenery stays contained, and no duplicate world appears outside the screen.
- The window itself remains anchored; camera rotation alone is not the only parallax evidence.
- Approach the supported limits, then reset/re-place and background/resume without broken rendering.
- Return to the normal M4 experience. Record subjective smoothness separately from measured fps.

## 11. Deliverables and verdict

Deliver on portal-rnd:
1. docs/PORTAL-RESEARCH.md: concise feasibility verdict, ranked approaches, exact API/version
   evidence, chosen architecture, tradeoffs, risks, and any blockers.
2. The smallest working source prototype if the verified path is practical.
3. The updated debug APK, its version/path, actual build result, and checks not performed.
4. Updates to STATUS.md and VERIFICATION.md on the experimental branch; preserve historical evidence.
5. A local checkpoint named m5-portal-poc after a successful assembled prototype, plus a short
   phone checklist and next milestone recommendation. Keep the experiment separate from main.

Distinguish these outcomes:
- Supported by API/source evidence: a credible architecture exists.
- Prototype assembled: implementation compiles and packages; appearance remains unverified.
- Preview observed: perspective/containment demonstrated in preview, if actually inspected.
- Phone verified: the user or an authorized manual check demonstrates the AR illusion on the
  Redmi. Record who checked what; do not infer a full pass from one screenshot.

Success is a contained 3D world whose perspective changes correctly with physical viewing
position. Once the user verifies that illusion, recommend third-person garden movement next,
followed by the isolated hand/palm experiment. This milestone ends at proving the portal.
