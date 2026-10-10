package com.laya.desk.game

enum class EscapePhase { TRAPPED, DRAGGING, RETURNING, ESCAPING, PLAYING }

/** Touch escape rules and animation clock, independent of Android and scene coordinates. */
data class EscapeState(
    val phase: EscapePhase = EscapePhase.TRAPPED,
    val pull: Float = 0f,
    val animationStartPull: Float = 0f,
    val elapsedMillis: Long = 0L,
) {
    val isAnimating: Boolean get() = phase == EscapePhase.RETURNING || phase == EscapePhase.ESCAPING
    val readyToRelease: Boolean get() = phase == EscapePhase.DRAGGING && pull >= RELEASE_THRESHOLD
    val descentProgress: Float
        get() = if (phase == EscapePhase.PLAYING) 1f
        else if (phase == EscapePhase.ESCAPING) smooth(elapsedMillis.toFloat() / DESCENT_MILLIS) else 0f

    fun grab(): EscapeState = if (phase == EscapePhase.TRAPPED) copy(phase = EscapePhase.DRAGGING) else this

    fun drag(value: Float): EscapeState =
        if (phase == EscapePhase.DRAGGING && value.isFinite()) copy(pull = value.coerceIn(0f, 1f)) else this

    fun release(): EscapeState = if (phase != EscapePhase.DRAGGING) this
        else if (readyToRelease) free() else cancelDrag()

    fun cancelDrag(): EscapeState = if (phase == EscapePhase.DRAGGING) {
        if (pull == 0f) EscapeState()
        else copy(phase = EscapePhase.RETURNING, animationStartPull = pull, elapsedMillis = 0L)
    } else this

    fun free(): EscapeState = when (phase) {
        EscapePhase.TRAPPED, EscapePhase.DRAGGING, EscapePhase.RETURNING ->
            copy(phase = EscapePhase.ESCAPING, animationStartPull = pull, elapsedMillis = 0L)
        else -> this
    }

    fun advance(deltaMillis: Long): EscapeState {
        if (!isAnimating || deltaMillis <= 0L) return this
        val duration = if (phase == EscapePhase.RETURNING) RETURN_MILLIS else DESCENT_MILLIS
        val elapsed = (elapsedMillis + deltaMillis.coerceAtMost(100L)).coerceAtMost(duration)
        if (elapsed == duration) return if (phase == EscapePhase.RETURNING) EscapeState()
            else copy(phase = EscapePhase.PLAYING, elapsedMillis = elapsed)
        return copy(
            elapsedMillis = elapsed,
            pull = if (phase == EscapePhase.RETURNING)
                animationStartPull * (1f - smooth(elapsed.toFloat() / duration)) else pull,
        )
    }

    companion object {
        const val RELEASE_THRESHOLD = 0.65f
        private const val RETURN_MILLIS = 280L
        private const val DESCENT_MILLIS = 1_100L
        private fun smooth(value: Float): Float = value.coerceIn(0f, 1f).let { it * it * (3f - 2f * it) }
    }
}
