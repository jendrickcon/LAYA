package com.laya.desk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.FilledTonalButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.google.ar.core.Anchor
import com.google.ar.core.Config
import com.google.ar.core.InstantPlacementPoint
import com.google.ar.core.Plane
import com.google.ar.core.TrackingFailureReason
import com.google.ar.core.TrackingState
import com.google.ar.core.exceptions.NotTrackingException
import com.google.ar.core.exceptions.ResourceExhaustedException
import com.laya.desk.game.PlacementPhase
import com.laya.desk.game.EscapePhase
import com.laya.desk.game.derivePhase
import com.laya.desk.game.instructionFor
import io.github.sceneview.ar.ARSceneView
import io.github.sceneview.rememberARView
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberRenderer
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.math.Position
import com.laya.desk.portal.PortalContent
import com.laya.desk.portal.PortalController
import java.util.concurrent.atomic.AtomicReference
import kotlin.math.atan2

private fun hintFor(reason: TrackingFailureReason?): String? = when (reason) {
    TrackingFailureReason.INSUFFICIENT_LIGHT -> "Not enough light. Try a brighter area."
    TrackingFailureReason.EXCESSIVE_MOTION -> "Moving too fast. Slow down."
    TrackingFailureReason.INSUFFICIENT_FEATURES ->
        "Too little detail. Try a book, patterned fabric, or floor in good light. Or use 3D preview."
    TrackingFailureReason.CAMERA_UNAVAILABLE -> "Camera unavailable. Close other camera apps."
    TrackingFailureReason.BAD_STATE -> "AR error. Use 3D preview, or reopen the app."
    else -> null
}

/** Surface placement plus an explicitly selected approximate AR fallback. */
@Composable
fun ArPlacementScreen(
    onOpenPreview: () -> Unit,
    portalLab: Boolean = false,
    onTogglePortal: () -> Unit = {},
) {
    var anchor by remember { mutableStateOf<Anchor?>(null) }
    var boardFacingDegrees by remember { mutableStateOf(0f) }
    var cameraTracking by remember { mutableStateOf(false) }
    var anchorTracking by remember { mutableStateOf<TrackingState?>(null) }
    var planeFound by remember { mutableStateOf(false) }
    var failureReason by remember { mutableStateOf<TrackingFailureReason?>(null) }
    var sessionError by remember { mutableStateOf<String?>(null) }
    var quickPlacement by remember { mutableStateOf(false) }
    var estimatedPlacement by remember { mutableStateOf(false) }
    var placementError by remember { mutableStateOf<String?>(null) }
    val pendingTap = remember { AtomicReference<Pair<Float, Float>?>(null) }
    val canPlay = anchor != null && cameraTracking &&
        anchorTracking == TrackingState.TRACKING && sessionError == null
    val game = rememberBalanceBridgeGame(canPlay)
    val engine = rememberEngine()
    val view = rememberARView(engine)
    val renderer = rememberRenderer(engine)
    val portal = remember { PortalController() }
    val touch = remember(game, view) { ExperienceTouchController(game, view) }

    // SceneView's AnchorNode owns the anchor and detaches it when the node is removed.

    val phase = derivePhase(cameraTracking, planeFound, placed = anchor != null, quickPlacementEnabled = quickPlacement)
    val message = when {
        sessionError != null -> "AR could not start: $sessionError. You can use 3D preview."
        phase == PlacementPhase.TRACKING_LOST -> hintFor(failureReason) ?: instructionFor(phase)
        anchor != null && anchorTracking == TrackingState.STOPPED ->
            "Board tracking ended. Reset placement to try again."
        anchor != null && anchorTracking != TrackingState.TRACKING ->
            "Board tracking paused. Point your camera back at the board."
        placementError != null -> placementError!!
        anchor != null && estimatedPlacement ->
            "LAYA · Estimated placement may shift as tracking improves."
        anchor != null && portalLab -> "Portal lab · Experimental"
        anchor != null -> if (game.escape.phase == EscapePhase.PLAYING)
            "LAYA · Balance Bridge" else "LAYA · Sampaguita Garden"
        else -> if (portalLab && phase == PlacementPhase.READY_TO_PLACE)
            "Surface found. Tap to place the floating window."
        else instructionFor(phase)
    }

    Box(Modifier.fillMaxSize()) {
        ARSceneView(
            modifier = Modifier.fillMaxSize(),
            engine = engine,
            view = view,
            renderer = renderer,
            onTouchEvent = if (portalLab) null else touch::onTouch,
            planeRenderer = anchor == null,
            planeFindingMode = Config.PlaneFindingMode.HORIZONTAL,
            depthMode = Config.DepthMode.DISABLED,
            // Enabled for the session, but used only after the user chooses Quick placement.
            instantPlacementMode = Config.InstantPlacementMode.LOCAL_Y_UP,
            onSessionFailed = { e -> sessionError = e.message ?: e.javaClass.simpleName },
            onSessionResumed = {
                sessionError = null
                pendingTap.set(null)
            },
            onSessionPaused = {
                cameraTracking = false
                anchorTracking = null
                pendingTap.set(null)
                if (portalLab) portal.render(Position(0f), tracking = false)
            },
            onTrackingFailureChanged = { failureReason = it },
            onGestureListener = rememberOnGestureListener(
                onSingleTapConfirmed = { event, _ ->
                    if (anchor == null) {
                        placementError = null
                        pendingTap.set(event.x to event.y)
                    }
                },
            ),
            onSessionUpdated = { session, frame ->
                cameraTracking = frame.camera.trackingState == TrackingState.TRACKING
                anchorTracking = anchor?.trackingState
                planeFound = session.getAllTrackables(Plane::class.java).any {
                    it.trackingState == TrackingState.TRACKING &&
                        it.type == Plane.Type.HORIZONTAL_UPWARD_FACING
                }
                val tap = pendingTap.getAndSet(null)
                if (tap != null && anchor == null && cameraTracking) {
                    try {
                        // Prefer a real horizontal plane even in Quick placement mode.
                        val surfaceHit = frame.hitTest(tap.first, tap.second).firstOrNull {
                            val t = it.trackable
                            t is Plane &&
                                t.trackingState == TrackingState.TRACKING &&
                                t.type == Plane.Type.HORIZONTAL_UPWARD_FACING &&
                                t.isPoseInPolygon(it.hitPose)
                        }
                        val hit = surfaceHit ?: if (quickPlacement) {
                            frame.hitTestInstantPlacement(tap.first, tap.second, 0.5f).firstOrNull()
                        } else null
                        if (hit != null) {
                            val placedAnchor = hit.createAnchor()
                            // Face the player once, then keep this heading under the shared anchor.
                            // The screen stays fixed in the room as the player walks around it.
                            val localCamera = placedAnchor.pose.inverse().transformPoint(frame.camera.pose.translation)
                            boardFacingDegrees = Math.toDegrees(atan2(localCamera[0], localCamera[2]).toDouble()).toFloat()
                            anchor = placedAnchor
                            anchorTracking = anchor?.trackingState
                            game.reset()
                            estimatedPlacement = hit.trackable is InstantPlacementPoint
                            placementError = null
                        } else {
                            placementError = if (quickPlacement) {
                                "Not enough detail for Quick placement yet. Move slowly, or use 3D preview."
                            } else {
                                "No surface at that spot. Tap a detected surface, or try Quick placement / 3D preview."
                            }
                        }
                    } catch (_: NotTrackingException) {
                        cameraTracking = false
                        placementError = null
                    } catch (_: ResourceExhaustedException) {
                        placementError = "AR resources are full. Use 3D preview, or reopen the app."
                    }
                }
                if (portalLab && anchor != null) {
                    val eye = frame.camera.pose.translation
                    portal.render(Position(eye[0], eye[1], eye[2]),
                        cameraTracking && anchorTracking == TrackingState.TRACKING)
                }
            },
        ) {
            anchor?.let { a ->
                AnchorNode(anchor = a) {
                    if (portalLab) PortalContent(portal, renderer, boardFacingDegrees)
                    else BalanceBridgeScene(game, boardFacingDegrees)
                }
            }
        }

        Surface(
            modifier = Modifier.align(Alignment.TopCenter).safeDrawingPadding().padding(16.dp),
            tonalElevation = 6.dp,
            shape = MaterialTheme.shapes.medium,
        ) {
            Text(message, modifier = Modifier.padding(12.dp))
        }

        Column(
            modifier = Modifier.align(Alignment.BottomCenter).fillMaxWidth().safeDrawingPadding().padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (anchor != null) {
                if (portalLab) {
                    Surface(tonalElevation = 6.dp, shape = MaterialTheme.shapes.medium) {
                        Column(Modifier.fillMaxWidth().padding(12.dp)) {
                            Text(portal.status.message, style = MaterialTheme.typography.bodyMedium)
                            portal.error?.let { Text(it.take(160), style = MaterialTheme.typography.bodySmall) }
                        }
                    }
                } else BalanceBridgePanel(game, canPlay)
            }
            Surface(tonalElevation = 6.dp, shape = MaterialTheme.shapes.large) {
                if (anchor != null) {
                    Row(Modifier.fillMaxWidth().padding(4.dp), horizontalArrangement = Arrangement.SpaceEvenly) {
                        TextButton(onClick = {
                            pendingTap.set(null)
                            anchor = null
                            anchorTracking = null
                            estimatedPlacement = false
                            placementError = null
                            game.reset()
                        }) { Text("Reset placement") }
                        TextButton(onClick = onOpenPreview) { Text("3D preview") }
                    }
                } else {
                    Column(Modifier.fillMaxWidth().padding(8.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        OutlinedButton(onClick = {
                            pendingTap.set(null)
                            quickPlacement = !quickPlacement
                            placementError = null
                        }) { Text(if (quickPlacement) "Use surface detection" else "Quick placement") }
                        FilledTonalButton(onClick = onOpenPreview) { Text("3D preview (no camera)") }
                    }
                }
            }
            FilledTonalButton(onClick = onTogglePortal, modifier = Modifier.fillMaxWidth()) {
                Text(if (portalLab) "Return to LAYA" else "Portal lab (experimental)")
            }
        }
    }
}
