# M5 portal research and implementation

Date: 2026-10-11. Branch: `portal-rnd`, based on M4 main checkpoint `aa03206`.

## Verdict and evidence level

**The pinned APIs support a credible portal path, and the prototype assembles.**
The selected implementation renders a separate world into a texture using a camera whose
asymmetric projection follows the viewer relative to the aperture. Actual appearance, texture
orientation, parallax, recovery, and performance on the Redmi Note 9 Pro remain unverified.
No preview was observed, no phone was installed/run, and no emulator, automated tests, or lint ran.
This is an assembled experiment, not a device acceptance claim.

## Practical approaches, easiest to hardest in this repository

| Rank | Approach | Required path / effort | Fidelity and likely cost | Decision |
|---|---|---|---|---|
| 1 | Fixed-camera texture | Private scene, camera, view, render target, and image material | Contains a 3D scene but viewpoint stays fixed; an extra pass when refreshed | Useful plumbing stage only; fails the moving-window criterion |
| 2 | View-dependent texture | Same resources plus portal-local eye conversion and an asymmetric frustum | Contains the world and produces geometric parallax; one extra small pass per active host frame | **Implemented**; simplest complete path verified in this pin |
| 3 | Interior in the main scene with aperture masking | Main-camera geometry plus a verified stencil, depth-mask, or per-material clipping path | Could avoid an offscreen pass, but masking adds draw/overdraw cost; leaks or camera-background occlusion if ordering is wrong | Higher integration risk; no complete aperture solution established |

SceneView has `MaterialLoader.createOcclusionInstance`, but an occlusion material by itself is not
an aperture clip. Correct masking of every interior object and preservation of the AR camera
background would still need proving. No usable SceneView stencil convenience API was assumed.
Recursive portals, custom native rendering, dependency upgrades, and new shader compilation were
unnecessary for the selected route. A separate fixed-camera runtime trial was not observed;
the delivered source proceeds directly to the view-dependent implementation.

## Exact versions and API evidence

Pins remain SceneView/arsceneview **4.23.0**, Kotlin/Compose plugin **2.4.0**, AGP **9.4.1**,
Gradle **9.6.0**, Compose BOM **2026.02.01**, JVM **17**, SDK **36 / 36 / 24**.
The cached matching POMs resolve **Filament 1.72.1** and **ARCore 1.54.0**.
Published matching source artifacts were read, not just current examples.

| Capability | Verified symbol/source | Consequence |
|---|---|---|
| Texture output | Filament 1.72.1 `Texture.Builder`, `RenderTarget.Builder.texture`, `View.setRenderTarget` | RGBA8 color attachment sampled by the host; separate DEPTH24 attachment |
| Secondary scene/camera | `Engine.createScene/createView/createCamera` | Shares the host engine; owns a private view and camera |
| Off-axis projection | `Camera.setProjection(Projection, left, right, bottom, top, near, far)` and `lookAt` | No custom matrix API or JVM-21 inline vector operation required |
| Safe offscreen submission | `Renderer.renderStandaloneView(View)` | Must be outside beginFrame/endFrame, on the engine thread, with a render target |
| Integration point | SceneView 4.23.0 `SceneRenderer.renderFrame` calls onBeforeRender before beginFrame | Preview onFrame and AR onSessionUpdated can submit the offscreen pass before the host frame |
| Current AR pose | ARSceneView updates its camera and nodes before onSessionUpdated | Uses frame.camera.pose.translation and the live anchor-relative transform |
| Texture sampling | `MaterialLoader.createImageInstance(Texture, TextureSampler)` | Packaged unlit image material; no matc install or runtime material compile |
| Camera conversion | `Node.getLocalPosition` uses fresh world-to-local data | Accounts for anchor motion and the fixed placement heading |
| Clearing | `View.setRenderTarget` documentation excludes Renderer.ClearOptions | An opaque private skybox fills the target background |
| Teardown | Node/GeometryNode/RenderableNode.destroy, MaterialLoader.destroyMaterialInstance, EngineDestroyQueue | Release renderables/materials before deferred texture destruction |

Primary matching sources:

- [Filament 1.72.1 Renderer](https://github.com/google/filament/blob/v1.72.1/android/filament-android/src/main/java/com/google/android/filament/Renderer.java),
  [Camera](https://github.com/google/filament/blob/v1.72.1/android/filament-android/src/main/java/com/google/android/filament/Camera.java),
  [View](https://github.com/google/filament/blob/v1.72.1/android/filament-android/src/main/java/com/google/android/filament/View.java),
  [Engine](https://github.com/google/filament/blob/v1.72.1/android/filament-android/src/main/java/com/google/android/filament/Engine.java).
- [SceneView 4.23.0 SceneRenderer](https://github.com/sceneview/sceneview/blob/v4.23.0/sceneview/src/main/java/io/github/sceneview/SceneRenderer.kt),
  [MaterialLoader](https://github.com/sceneview/sceneview/blob/v4.23.0/sceneview/src/main/java/io/github/sceneview/loaders/MaterialLoader.kt),
  [image material](https://github.com/sceneview/sceneview/blob/v4.23.0/sceneview/src/main/materials/image_texture.mat).
- Local extracted SceneView/AR source: `C:\Users\DOT36\jendrick\PROJECTS\tmp\laya-review\scene`
  and `ar`. Filament source: neighboring `filament-1.72.1`, extracted from the cached source JAR.
  Relevant SceneRenderer lines: 230–248; SceneView preview loop: 630–677;
  ARSceneView camera update/callback: approximately 1535 / 1647.
- SceneView's AAR contains `assets/materials/image_texture.filamat`. Its matching shader is
  unlit, samples UV0, uses premultiplied transparent blending, and disables culling.
  The interior skybox has opaque alpha. Plane UVs map bottom-left to (0,0); runtime orientation
  and color remain phone checklist items.

## Geometry and projection

Units are metres. The aperture is 0.30 wide by 0.20 high, centered 0.25 above its placement.
In portal-local coordinates, +X is right, +Y up, +Z the front; the interior occupies negative Z.
An AR placement faces the user once and keeps that heading under its anchor.
The interior exists only in the private scene. The host receives the textured rectangle,
thin border, and opaque back, never a duplicate of the interior objects.

Let the viewer in portal coordinates be E = (ex, ey, ez), with ez positive.
The secondary camera is at E and looks parallel to -Z with +Y up. Its near plane is the
aperture itself, so:

```text
near   = ez                 far = ez + 2
left   = -width/2 - ex      right = width/2 - ex
bottom = -height/2 - ey     top   = height/2 - ey
```

These are bounds at the near plane, not a symmetric FOV aimed at the window center.
The physical camera projects the quad normally; its matching portal-local eye determines the
quad's image. The secondary camera does not inherit a second orbit or character-follow rotation.
For an interior point P at depth D = -Pz, its horizontal aperture intersection is
`(ez * Px + D * ex) / (ez + D)`. Its response therefore depends on depth; this is not a UV pan.

The preview moves the eye with two sliders and aims the outer camera at the window so it stays
in view. AR uses the phone position directly. Normal viewing starts roughly 65 cm in front and
9 cm above the window center in preview; begin phone checks around 40–80 cm with modest angles.
Guards reject non-finite positions, front distance outside 0.08–2.5 m, horizontal offset
greater than 1.5 times front distance, or vertical offset greater than 1.2 times front distance.
Rejected views hide the texture and show a hint; the border/back remain. These numerical guards
are implemented bounds, not a phone-verified viewing envelope.

## Implementation and ownership

- `portal/PortalProjection.kt`: pure scalar coordinate/frustum math.
- `portal/PortalNode.kt`: private scene, view, camera, skybox, target, attachments, generated
  geometry, materials, and the host rectangle. A gold near post, blue farther arch, floor grid,
  distant block, and small stationary fairy-shaped marker make depth and orientation visible.
- `portal/PortalContent.kt`: Compose attachment and status controller. Native resources allocate
  in DisposableEffect, so an abandoned composition does not allocate the private render tree.
- The existing preview and AR hosts offer **Portal lab (experimental)** and **Return to LAYA**.
  M4 remains the default. Switching experience or AR/preview disposes the old host and placement.
  The permission screen can open the lab's camera-free preview.

The fixed **640 × 400** target uses one RGBA8 color texture and one DEPTH24 depth texture.
These attachments are roughly 2 MiB together depending on depth allocation/alignment; this is
an estimate, not measured process/GPU memory. No MSAA, shadows, lighting, postprocessing, or
antialiasing are requested for the private view. The host retains its existing rendering settings.
The slightly different pixel and physical aspect ratios are resolved by the explicit frustum
and full-quad sampling; texels need not represent square physical distances.

There is one offscreen submission per valid active host callback, followed by the host render.
No separate graphics loop, competing swap chain, camera capture, or engine is created.
The host's resumed/surface gates control submissions. AR camera/anchor tracking loss hides the
image and stops the private pass. A recoverable Java rendering failure shows a status until reset.
FPS, CPU/GPU load, thermal behavior, and memory across resets were not measured; 30 fps is a target.

Cleanup is idempotent. It detaches the portal, destroys host and private geometry/renderables,
releases owned entity IDs, destroys the private view, skybox, scene, camera components/entity,
and render target, then releases material instances. Textures go through SceneView's three-frame
EngineDestroyQueue after their bindings are released; the host drains that queue on teardown.
Partial construction retains handles for cleanup. If the host engine is already invalid,
teardown avoids stale native calls and clears retained references/entity IDs. Borrowed renderer,
engine, parent anchor, material definitions, and loader are not destroyed by PortalNode.
No new dependencies, permissions, remote assets, or saved camera frames.

## Limits, phone checklist, and next decision

Front viewing only; no walking through, recursive portals, real-room occlusion, physical collision,
hand tracking, playable third-person controls, or portal-world extraction. Existing M4 touch
breakout is still available through Return to LAYA. The experiment is a static rendering study.
No guarantee is made about a native GPU/driver failure; Kotlin exception handling is not a GPU
compatibility check. UV orientation, target clearing, depth behavior, frame pacing, and disposal
need the device checks below.

1. In **3D preview**, open **Portal lab**, then **Place portal**. Confirm floor below sky,
   gold post initially left, blue arch farther right, and a small fairy. Move both sliders.
2. Choose **Back to AR**, place on a detailed surface (a book/floor works), or use explicitly
   approximate **Quick placement**. Keep the whole window visible around 40–80 cm away.
3. Translate the phone sideways 10–20 cm, then moderately up/down. Near/far objects must change
   overlap and position differently while remaining inside the anchored aperture. Rotation alone
   is insufficient evidence of positional parallax. Check that no duplicate world is outside.
4. View the back/edge and approach closely, then return to the front. Confirm the hint/back surface
   and recovery. Briefly lose tracking, restore it, background/resume, then reset/re-place several
   times; look for crashes, stale images, increasing stutter, or black output.
5. **Return to LAYA** and check the normal garden, touch escape, and Balance Bridge in preview/AR.
   Record these separately from the portal. Describe smoothness subjectively unless fps is measured.

If containment and parallax pass on the phone, the next experiment is a small third-person garden
inside this renderer, then a separate hand/palm prototype after that foundation is reliable.
If the image is blank, flipped, distorted, or unstable, fix that rendering symptom before adding play.
Build results, APK metadata/hash, and verification boundaries are in [VERIFICATION.md](VERIFICATION.md).
