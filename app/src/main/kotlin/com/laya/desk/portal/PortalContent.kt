package com.laya.desk.portal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.google.android.filament.Renderer
import io.github.sceneview.SceneScope
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation

enum class PortalStatus(val message: String) {
    WAITING("Place the portal to begin."),
    ACTIVE("Move sideways: the gold post is near; the blue arch is farther away."),
    PAUSED("Tracking paused. Point the camera back toward the portal."),
    OUTSIDE("Face the front of the window from 8 cm to 2.5 m away; avoid very steep angles."),
    FAILED("Portal renderer unavailable. Return to LAYA to continue."),
}

class PortalController {
    internal var node: PortalNode? = null
    var status by mutableStateOf(PortalStatus.WAITING)
        internal set
    var error by mutableStateOf<String?>(null)
        internal set

    fun render(viewerWorld: Position, tracking: Boolean = true) {
        val current = node ?: return
        if (error != null) return
        if (!tracking) {
            current.hideInterior()
            status = PortalStatus.PAUSED
            return
        }
        try {
            status = if (current.renderFrom(viewerWorld)) PortalStatus.ACTIVE else PortalStatus.OUTSIDE
        } catch (failure: Exception) {
            current.hideInterior()
            error = failure.message ?: failure.javaClass.simpleName
            status = PortalStatus.FAILED
        }
    }
}

/** Standard public Node DSL owns a parent; the imperative portal owns its complete resource tree. */
@Composable
fun SceneScope.PortalContent(
    controller: PortalController,
    renderer: Renderer,
    facingDegrees: Float = 0f,
) {
    Node(position = Position(0f, PortalProjection.CENTER_HEIGHT, 0f),
        rotation = Rotation(0f, facingDegrees, 0f)) {
        val parent = parentNode
        // Allocate in the effect, so abandoned compositions cannot orphan native resources.
        DisposableEffect(parent, renderer) {
            var portal: PortalNode? = null
            controller.error = null
            controller.status = PortalStatus.WAITING
            try {
                portal = PortalNode(engine, materialLoader, renderer)
                parent.addChildNode(portal)
                controller.node = portal
            } catch (failure: Exception) {
                portal?.destroy()
                portal = null
                controller.error = failure.message ?: failure.javaClass.simpleName
                controller.status = PortalStatus.FAILED
            }
            onDispose {
                if (controller.node === portal) controller.node = null
                portal?.destroy()
                controller.status = PortalStatus.WAITING
            }
        }
    }
}
