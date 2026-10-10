package com.laya.desk.ui

import android.view.MotionEvent
import com.google.android.filament.View
import com.laya.desk.game.EscapePhase
import com.laya.desk.game.GardenState
import io.github.sceneview.collision.HitResult
import io.github.sceneview.math.Position
import io.github.sceneview.node.Node
import io.github.sceneview.utils.screenToRay
import kotlin.math.abs
import kotlin.math.sqrt

internal object ExperienceGeometry {
    const val BOARD = "laya-board"
    const val AVATAR = "laya-avatar"
    const val WEIGHT = "laya-movable-weight"
    const val LEVER = "laya-lever"
    const val STEP_METERS = 0.052f
    const val WEIGHT_Y = 0.0275f
    const val AVATAR_X = GardenState.EXIT_TILE * GardenState.STEP_METERS
    const val AVATAR_Y = 0.352f
    const val AVATAR_Z = 0.055f
    const val PULL_METERS = 0.14f
    const val PULL_OUT_METERS = 0.085f
}

/** Owns an entire grabbed pointer sequence, so the orbit camera never sees part of that drag. */
internal class ExperienceTouchController(private val game: BalanceBridgeGame, private val view: View) {
    private var ownsSequence = false
    private var pointerId = -1
    private var epoch = 0
    private enum class Target { AVATAR, WEIGHT }
    private var target = Target.AVATAR
    private var interactionSpace: Node? = null
    private var grabCoordinate = 0f
    private var initialWeightDistance = 1f

    // SceneView delivers viewport-local MotionEvents (not raw display coordinates). The same
    // projection works in preview's Column and in the full-screen AR surface.
    fun onTouch(event: MotionEvent, hit: HitResult?): Boolean {
        if (event.actionMasked == MotionEvent.ACTION_DOWN) {
            clear()
            if (!game.active) return false
            val touched = hit?.nodeOrNull ?: return false
            val avatar = touched.ancestor(ExperienceGeometry.AVATAR)
            val weight = touched.ancestor(ExperienceGeometry.WEIGHT)
            val origin = when {
                avatar != null && game.escape.phase == EscapePhase.EXIT_READY -> {
                    target = Target.AVATAR
                    avatar.ancestor(ExperienceGeometry.BOARD)
                }
                weight != null && game.canAdjustWeight -> {
                    target = Target.WEIGHT
                    initialWeightDistance = weight.position.x / ExperienceGeometry.STEP_METERS
                    weight.ancestor(ExperienceGeometry.LEVER)
                }
                // Objects which are currently locked absorb touches too, without moving the camera.
                avatar != null || weight != null -> {
                    ownsSequence = true
                    return true
                }
                else -> return false
            } ?: return false
            ownsSequence = true
            pointerId = event.getPointerId(0)
            epoch = game.inputEpoch
            interactionSpace = origin
            val point = project(event.x, event.y)
            if (point == null) {
                game.projectionUnavailable()
                interactionSpace = null
            } else {
                val grabbed = when (target) {
                    Target.AVATAR -> game.grabAvatar()
                    Target.WEIGHT -> game.grabWeight(initialWeightDistance)
                }
                if (grabbed) grabCoordinate = if (target == Target.AVATAR) point.y else point.x
                else interactionSpace = null
            }
            return true
        }
        if (!ownsSequence) return false
        // Even after cancellation, consume the rest of this stream; orbit resumes on a fresh DOWN.
        if (!game.active || game.inputEpoch != epoch) cancel()
        when (event.actionMasked) {
            MotionEvent.ACTION_POINTER_DOWN, MotionEvent.ACTION_CANCEL -> cancel()
            MotionEvent.ACTION_MOVE, MotionEvent.ACTION_UP -> {
                val index = event.findPointerIndex(pointerId)
                if (interactionSpace != null && index >= 0) {
                    val point = project(event.getX(index), event.getY(index))
                    if (point == null) {
                        game.projectionUnavailable()
                        interactionSpace = null
                    } else when (target) {
                        Target.AVATAR -> game.dragAvatar((grabCoordinate - point.y) / ExperienceGeometry.PULL_METERS)
                        Target.WEIGHT -> game.dragWeight(initialWeightDistance +
                            (point.x - grabCoordinate) / ExperienceGeometry.STEP_METERS)
                    }
                } else cancel()
                if (event.actionMasked == MotionEvent.ACTION_UP) {
                    if (interactionSpace != null) when (target) {
                        Target.AVATAR -> game.releaseAvatar()
                        Target.WEIGHT -> game.releaseWeight()
                    }
                    clear()
                }
            }
            MotionEvent.ACTION_POINTER_UP -> if (event.getPointerId(event.actionIndex) == pointerId) cancel()
        }
        if (event.actionMasked == MotionEvent.ACTION_CANCEL) clear()
        return true
    }

    private fun cancel() { game.cancelInteraction(); interactionSpace = null }
    private fun clear() {
        if (ownsSequence) cancel()
        ownsSequence = false
        pointerId = -1
        interactionSpace = null
    }

    /** Avatar: board-local vertical plane. Weight: lever-local horizontal plane through its center.
     * Both inherit the AR anchor. The lever's transform also accounts for its current tilt. */
    private fun project(x: Float, y: Float): Position? {
        if (view.viewport.width <= 0 || view.viewport.height <= 0) return null
        val root = interactionSpace ?: return null
        val ray = view.screenToRay(x, y) ?: return null
        val origin = root.getLocalPosition(ray.origin)
        // Scalar operations keep this app's JVM 17 target; kotlin-math's inline vector
        // operators in the pinned transitive artifact were compiled for JVM 21.
        val end = root.getLocalPosition(Position(ray.origin.x + ray.direction.x,
            ray.origin.y + ray.direction.y, ray.origin.z + ray.direction.z))
        val direction = Position(end.x - origin.x, end.y - origin.y, end.z - origin.z)
        val length = sqrt(direction.x * direction.x + direction.y * direction.y + direction.z * direction.z)
        val normalComponent = if (target == Target.AVATAR) direction.z else direction.y
        if (!length.isFinite() || length < 0.0001f || abs(normalComponent) / length < 0.15f) return null
        val t = if (target == Target.AVATAR) (ExperienceGeometry.AVATAR_Z - origin.z) / direction.z
            else (ExperienceGeometry.WEIGHT_Y - origin.y) / direction.y
        if (!t.isFinite() || t < 0f || t * length > 8f) return null
        return Position(origin.x + direction.x * t, origin.y + direction.y * t, origin.z + direction.z * t)
            .takeIf { it.x.isFinite() && it.y.isFinite() && it.z.isFinite() }
    }
}

private fun Node.ancestor(name: String): Node? {
    var current: Node? = this
    while (current != null) {
        if (current.name == name) return current
        current = current.parent
    }
    return null
}
