# Claude handoff: Milestone 2 — first playable Balance Bridge

Implementation note: this task has now been implemented locally as 0.2.0-m2 by Codex after
Claude reached its usage limit. This document is the original specification; consult STATUS.md
and VERIFICATION.md for current results and outstanding acceptance checks before doing more work.

Paste the text below into the existing LAYA Claude conversation. Attach the current source
bundle, or provide access to the matching repository files. The old initial ZIP is superseded.

---

Continue development of **LAYA: The Desk Breakout**, our native Android educational AR game
for the ITSO 2026 Application Development Using Android Programming competition, Game category.
Your task is to implement the first playable Balance Bridge puzzle on the current working base.
Deliver code, not just a plan.

## Context and history

- We are a student team with limited money and free Claude usage. Small, buildable milestones
  matter. The owner has already built Audiary and Waffle's Nine Lives; Android setup is established.
- The reference video inspired a tiny original character escaping a floating screen into a real
  space. It is visual inspiration, not proof of a particular implementation or permission to use
  Nintendo assets.
- The full intended MVP is: launch/instructions → place experience → see trapped character →
  touch-triggered escape → Balance Bridge → short educational explanation → replay/reset.
  The floating screen and escape animation are a later milestone, after the puzzle works.
- You previously generated the initial repository and Milestone 1. Codex repaired the actual
  Android project: files were first extracted under the wizard's old Java source folder, leaving
  the wrong build configuration active. The current source is correctly rooted and replaces that
  old archive.
- Codex verified SceneView 4.23.0 APIs against its published source JARs, repaired the manifest
  merge/adaptive icons/permission declarations, and built the app.
- The first phone run showed tracking loss / insufficient visual detail. The owner has no
  suitable desk at home, so Codex added explicit Quick placement and a camera-free 3D preview.
- The owner now reports "it is working." The precise mode and full acceptance checklist were
  not specified. Treat this as an encouraging user report; do not label all AR checks passed.

## Exact starting point

- App package/namespace: com.laya.desk.
- Target phone: Redmi Note 9 Pro, Android 10 / API 29.
- Current app: 0.1.1-m1, versionCode 2.
- AGP 9.4.1, Gradle wrapper 9.6.0, Kotlin/Compose compiler 2.4.0.
- SceneView arsceneview 4.23.0, pinned. compileSdk/targetSdk 36; minSdk 24.
- AGP uses built-in Kotlin. Do not add the kotlin-android plugin.
- All 8 placement-phase unit tests passed. Debug assembly passed. Lint: 0 errors, 21 existing
  warnings. These are actual local checks by Codex, not evidence of your environment's capabilities.
- The current source files and docs in the attachment/repository are authoritative for
  implementation. Read AGENTS.md, README.md, docs/STATUS.md, docs/VERIFICATION.md, the version
  catalog, and the existing Kotlin sources first.
- There is a local Git repository with no commit or remote yet at handoff preparation time.

Existing behavior:
1. Camera permission rationale, denial/settings path; preview accessible without permission.
2. AR horizontal plane detection, confirmed single-tap placement, anchored 10 cm cube, Reset.
3. Quick placement is explicitly selected: prefer a valid horizontal-plane hit; otherwise use
   hitTestInstantPlacement with an estimated distance of 0.5 m. It still requires camera tracking.
4. 3D preview uses normal SceneView without an AR session. A virtual board is visible; single tap
   places the same cube. Drag orbits, pinch zooms, Reset removes it, Back to AR returns.
5. Tracking/session errors have hints. Mode switches discard the old placement.
6. PlacementCube is already shared via a composable SceneScope extension.

## Your task

Build ONE short Balance Bridge level. Make it playable in 3D preview first, then reuse the same
board/game content under the existing AR anchor. Preserve both placement options and the
camera-permission bypass for preview.

The loop should be:
place board → adjust a weight → see bridge tilt/balance → let an original placeholder character
cross → see a short explanation → play again.

Use this deliberately small puzzle:
- A bridge/lever rests on a central pivot.
- A fixed weight of 2 units sits 1 distance step to the left of the pivot.
- A movable weight of 1 unit sits on the right, initially 1 step away.
- The player chooses right-side distances of 1, 2, or 3 steps using clear on-screen controls.
- At distance 2 the turning effects match: 2 × 1 = 1 × 2.
- Show heavier-side tilt when unbalanced and a level bridge when balanced.
- Enable "Cross the bridge" only when balanced. Lock puzzle controls while crossing.
- Animate a small original geometric character across the bridge, then show completion.
  This is a scripted animation; do not add a physics engine or claim real-object collision.
- Explain in one or two plain sentences that a lighter weight can balance a heavier weight
  when it is farther from the pivot, because turning effect depends on weight and distance.
- "Play again" resets the puzzle at the existing placement. Placement Reset removes the game,
  clears puzzle/animation state, and allows placing again. Preserve Back to AR.

Use generated primitive geometry and simple materials for now. Keep the board roughly tabletop
scale and the current preview framing usable. Small labels and visible feedback should explain
the player's goal. Avoid relying on color alone. No downloaded character models are needed.

## Architecture and behavior

- Pure Kotlin puzzle math/state in com.laya.desk.puzzle (and game for shared flow if useful).
  No Android, Compose, SceneView, or ARCore types in that model.
- Use consistent signed distance steps for turning-effect calculations. Gravity is common to
  both sides in this simplified model; the comparison can use integer mass × distance units.
  Do not compare floating-point render coordinates for correctness.
- Explicit phases such as BALANCING, CROSSING, COMPLETE; repeated taps cannot start overlapping
  crossing animations. Validate/clamp input to the defined slots.
- Share the puzzle model and scene content across preview and AR. Do not maintain two puzzle
  rule implementations. Use the existing SceneScope/AnchorNode DSL patterns.
- Keep surface placement, Quick placement labeling, camera denial, session hints, reset, and
  mode switching intact. Preview must still work without camera permission or camera tracking.
- In AR, prevent starting a crossing while tracking is lost; keep recovery hints visible.
  Pause crossing progress during tracking loss and resume when tracking recovers.
- Keep geometry/materials remembered appropriately and let SceneView own resource cleanup.
  Follow the current anchor lifecycle pattern; avoid duplicate anchor detachment.
- One Activity, no extra Gradle modules, no DI framework or database.

## Scope boundaries

Do not implement the floating screen/escape sequence, hand tracking/MediaPipe, Depth API,
table-edge detection, occlusion, accounts, cloud services, analytics, network permission,
audio/haptics, or additional levels in this task.
Keep dependency pins and SDK versions unless a concrete build error requires a documented fix.
Reliability and a complete small loop take priority over adding effects.

## Verification and acceptance

Add meaningful model tests for:
- the initial imbalance and its direction;
- the valid solution and an incorrect distance;
- valid distance bounds / invalid input behavior;
- refusing to start crossing while unbalanced;
- avoiding repeated starts and resetting from crossing/completion.

Preserve the existing placement tests. Run if your environment supports it:

    ./gradlew :app:assembleDebug :app:testDebugUnitTest :app:lintDebug

The owner's low-memory Windows command is already in README.md. Verify any new SceneView API
against release 4.23.0 documentation/source. Prefer already compiling node APIs.
If you cannot run Gradle or use a physical phone, say exactly that. Record those checks as
"not run"; never infer successful compilation from reading code.

Acceptance for this milestone:
1. Launch and open preview without granting camera permission.
2. Place the puzzle, change distances, and observe corresponding tilt.
3. Correctly balance; crossing becomes available.
4. Trigger one crossing; complete; read the explanation.
5. Play again and complete a second run without restarting.
6. Reset placement; re-place; switch modes without stale puzzle state or crashes.
7. The same puzzle content can be placed through the existing AR paths. Actual anchoring and
   tracking-recovery acceptance remain phone checks.

## Deliverables

- Implement the code and tests against the attached current project.
- Update README.md, docs/STATUS.md, and docs/VERIFICATION.md to describe the final behavior,
  commands actually run, and remaining device checks.
- Give a concise changed-file list and any files that must be deleted.
- If working in chat rather than editing the repo, return a unified patch plus complete contents
  of new files, or a ZIP of ONLY changed/new files with paths relative to the project root.
  Do not nest the repository under app/src/main/java or include caches, build outputs,
  local.properties, signing files, or unrelated files.
- Keep packaging simple: use literal directory paths; inspect the final archive for junk
  brace-expansion directory names before delivering.
- End with a short phone checklist and an honest statement of what remains unverified.
- If response limits interrupt work, stop at a coherent checkpoint, list what remains, and
  continue the same implementation on the next turn. Do not mark the milestone complete early.

Read the current files, then implement the smallest complete playable loop above.
