package com.laya.desk.puzzle

enum class BridgePhase { BALANCING, CROSSING, COMPLETE }
enum class BalanceDirection { FIXED_SIDE_DOWN, LEVEL, MOVABLE_SIDE_DOWN }

/**
 * One lever puzzle, independent of Android/rendering.
 * Signed distance: fixed mass 2 at -1; movable mass 1 at +distance.
 * A common gravity factor cancels when comparing the two turning effects.
 */
data class BalanceBridgeState(
    val distance: Int = 1,
    val phase: BridgePhase = BridgePhase.BALANCING,
    val crossingMillis: Long = 0L,
) {
    init {
        require(distance in DISTANCES)
        require(crossingMillis in 0L..CROSSING_DURATION_MILLIS)
        require(phase != BridgePhase.BALANCING || crossingMillis == 0L)
        require(phase == BridgePhase.BALANCING || distance == 2)
        require(phase != BridgePhase.CROSSING || crossingMillis < CROSSING_DURATION_MILLIS)
        require(phase != BridgePhase.COMPLETE || crossingMillis == CROSSING_DURATION_MILLIS)
    }

    val fixedTurningEffect: Int get() = 2 * 1
    val movableTurningEffect: Int get() = 1 * distance
    val signedTurningEffect: Int get() = movableTurningEffect - fixedTurningEffect
    val isBalanced: Boolean get() = signedTurningEffect == 0
    val balanceDirection: BalanceDirection
        get() = when {
            signedTurningEffect < 0 -> BalanceDirection.FIXED_SIDE_DOWN
            signedTurningEffect > 0 -> BalanceDirection.MOVABLE_SIDE_DOWN
            else -> BalanceDirection.LEVEL
        }
    val crossingProgress: Float get() = crossingMillis.toFloat() / CROSSING_DURATION_MILLIS

    fun selectDistance(value: Int): BalanceBridgeState =
        if (phase == BridgePhase.BALANCING) copy(distance = value.coerceIn(DISTANCES)) else this

    fun canStartCrossing(canPlay: Boolean): Boolean =
        canPlay && phase == BridgePhase.BALANCING && isBalanced

    fun startCrossing(canPlay: Boolean): BalanceBridgeState =
        if (canStartCrossing(canPlay)) copy(phase = BridgePhase.CROSSING) else this

    fun advanceCrossing(elapsedMillis: Long, canPlay: Boolean): BalanceBridgeState {
        if (!canPlay || phase != BridgePhase.CROSSING || elapsedMillis <= 0L) return this
        // A delayed frame must not skip the whole walk (or overflow after an extreme delta).
        val elapsed = crossingMillis + elapsedMillis.coerceAtMost(MAX_FRAME_MILLIS)
        return if (elapsed >= CROSSING_DURATION_MILLIS) {
            copy(phase = BridgePhase.COMPLETE, crossingMillis = CROSSING_DURATION_MILLIS)
        } else {
            copy(crossingMillis = elapsed)
        }
    }

    fun replay(): BalanceBridgeState = BalanceBridgeState()

    companion object {
        val DISTANCES: IntRange = 1..3
        const val CROSSING_DURATION_MILLIS = 3_000L
        private const val MAX_FRAME_MILLIS = 100L
    }
}
