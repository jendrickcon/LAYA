package com.laya.desk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.SideEffect
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.withFrameNanos
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.lifecycle.compose.currentStateAsState
import com.laya.desk.game.EscapePhase
import com.laya.desk.game.EscapeState
import com.laya.desk.game.GardenPhase
import com.laya.desk.game.GardenState
import com.laya.desk.game.WeightDragState
import com.laya.desk.puzzle.BalanceBridgeState
import com.laya.desk.puzzle.BridgePhase
import kotlinx.coroutines.isActive

@Stable
class BalanceBridgeGame {
    var state by mutableStateOf(BalanceBridgeState())
        private set
    var escape by mutableStateOf(EscapeState())
        private set
    var garden by mutableStateOf(GardenState())
        private set
    var active by mutableStateOf(false)
        private set
    var inputEpoch by mutableStateOf(0)
        private set
    var inputHint by mutableStateOf<String?>(null)
        private set
    var weightDrag by mutableStateOf<WeightDragState?>(null)
        private set

    val puzzleEnabled: Boolean get() = active && escape.phase == EscapePhase.PLAYING
    val canAdjustWeight: Boolean get() = puzzleEnabled && state.phase == BridgePhase.BALANCING
    val gardenEnabled: Boolean get() = active && escape.phase == EscapePhase.SCREEN_PLAY && garden.canMove
    val needsAnimation: Boolean get() = garden.move != null || escape.isAnimating || state.phase == BridgePhase.CROSSING

    fun gardenStep(direction: Int) { if (gardenEnabled) garden = garden.step(direction) }
    fun gardenJump() { if (gardenEnabled) garden = garden.jump() }
    fun skipGarden() {
        if (gardenEnabled) {
            garden = garden.skip()
            enterExitIfReady()
        }
    }
    private fun enterExitIfReady() {
        if (escape.phase == EscapePhase.SCREEN_PLAY && garden.phase == GardenPhase.EXIT_READY) {
            escape = EscapeState(phase = EscapePhase.EXIT_READY)
        }
    }

    fun updateAvailability(value: Boolean) {
        if (active && !value) {
            cancelInteraction()
            inputEpoch++
        }
        active = value
    }

    fun grabAvatar(): Boolean {
        if (!active || escape.phase != EscapePhase.EXIT_READY) return false
        escape = escape.grab()
        inputHint = null
        return true
    }
    fun dragAvatar(pull: Float) { if (active) escape = escape.drag(pull) }
    fun releaseAvatar() { escape = if (active) escape.release() else escape.cancelDrag() }
    fun freeAvatar() {
        if (active) {
            escape = escape.free()
            inputHint = null
        }
    }
    fun cancelInteraction() {
        escape = escape.cancelDrag()
        weightDrag = null
    }
    fun projectionUnavailable() {
        cancelInteraction()
        inputHint = "Try a different viewing angle, or use the buttons below."
    }
    fun grabWeight(visibleDistance: Float): Boolean {
        if (!canAdjustWeight || weightDrag != null || !visibleDistance.isFinite()) return false
        weightDrag = WeightDragState(visibleDistance.coerceIn(1f, 3f))
        inputHint = null
        return true
    }
    fun dragWeight(distance: Float) {
        if (canAdjustWeight) weightDrag = weightDrag?.move(distance)
    }
    fun releaseWeight() {
        val slot = weightDrag?.nearestSlot
        weightDrag = null
        if (slot != null && canAdjustWeight) selectDistance(slot)
    }
    fun selectDistance(distance: Int) {
        if (canAdjustWeight && weightDrag == null) {
            state = state.selectDistance(distance)
            inputHint = null
        }
    }
    fun cross(canPlay: Boolean) {
        state = state.startCrossing(canPlay && puzzleEnabled && weightDrag == null)
    }
    fun advance(elapsedMillis: Long) {
        if (!active) return
        if (escape.phase == EscapePhase.SCREEN_PLAY) {
            garden = garden.advance(elapsedMillis)
            enterExitIfReady()
        } else escape = escape.advance(elapsedMillis)
        if (escape.phase == EscapePhase.PLAYING) state = state.advanceCrossing(elapsedMillis, true)
    }
    fun reset() {
        inputEpoch++
        inputHint = null
        weightDrag = null
        escape = EscapeState()
        garden = GardenState()
        state = BalanceBridgeState()
    }
}

@Composable
fun rememberBalanceBridgeGame(canPlay: Boolean): BalanceBridgeGame {
    val game = remember { BalanceBridgeGame() }
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    val lifecycleState by lifecycle.currentStateAsState()
    val active = canPlay && lifecycleState.isAtLeast(Lifecycle.State.RESUMED)
    SideEffect { game.updateAvailability(active) }
    DisposableEffect(game) { onDispose { game.updateAvailability(false) } }
    // Restart the frame clock after every tracking/lifecycle pause; paused time never advances play.
    LaunchedEffect(game, active, game.needsAnimation) {
        if (active && game.needsAnimation) {
            var previous = withFrameNanos { it }
            while (isActive && game.active && game.needsAnimation) {
                val now = withFrameNanos { it }
                game.advance((now - previous) / 1_000_000L)
                previous = now
            }
        }
    }
    return game
}
