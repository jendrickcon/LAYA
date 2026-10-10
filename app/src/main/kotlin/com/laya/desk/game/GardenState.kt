package com.laya.desk.game

enum class GardenPhase { EXPLORING, EXIT_READY }

data class GardenMove(val from: Int, val to: Int, val jumping: Boolean, val elapsed: Long = 0L) {
    val duration: Long get() = if (jumping) 640L else 260L
    val progress: Float get() = (elapsed.toFloat() / duration).coerceIn(0f, 1f)
}

/** One bounded garden lane. Walking cannot cross tile zero; a hop must land clear of it. */
data class GardenState(
    val tile: Int = FIRST_TILE,
    val facing: Int = 1,
    val budCollected: Boolean = false,
    val phase: GardenPhase = GardenPhase.EXPLORING,
    val move: GardenMove? = null,
    val hint: String = "Walk toward the bud. Jump over the fallen branch.",
) {
    val canMove: Boolean get() = phase == GardenPhase.EXPLORING && move == null
    val positionSteps: Float get() = move?.let { it.from + (it.to - it.from) * it.progress } ?: tile.toFloat()
    val hopHeight: Float get() = move?.takeIf { it.jumping }?.let {
        4f * it.progress * (1f - it.progress) * 0.08f
    } ?: 0f

    fun step(direction: Int): GardenState {
        if (!canMove) return this
        val dir = if (direction < 0) -1 else 1
        val target = (tile + dir).coerceIn(FIRST_TILE, EXIT_TILE)
        if (target == 0) return copy(facing = dir, hint = "The branch blocks the path. Tap Jump to hop across.")
        if (target == tile) return copy(facing = dir)
        return copy(facing = dir, move = GardenMove(tile, target, false))
    }

    /** Jump travels two tiles in the last chosen direction; no mid-air steering is needed. */
    fun jump(): GardenState {
        if (!canMove) return this
        val target = (tile + facing * 2).coerceIn(FIRST_TILE, EXIT_TILE)
        if (target == 0) return copy(hint = "Move one step closer to the branch, then jump.")
        if (target == tile) return this
        return copy(move = GardenMove(tile, target, true))
    }

    fun advance(deltaMillis: Long): GardenState {
        val current = move ?: return this
        if (deltaMillis <= 0L) return this
        val elapsed = (current.elapsed + deltaMillis.coerceAtMost(100L)).coerceAtMost(current.duration)
        if (elapsed < current.duration) return copy(move = current.copy(elapsed = elapsed))
        val collected = budCollected || current.to == BUD_TILE
        val ready = collected && current.to == EXIT_TILE
        return copy(
            tile = current.to, move = null, budCollected = collected,
            phase = if (ready) GardenPhase.EXIT_READY else GardenPhase.EXPLORING,
            hint = when {
                ready -> "The garden is complete. Something is happening at the edge..."
                current.to == EXIT_TILE -> "The bud is still behind you. Walk back to collect it."
                collected -> "Bud collected! Keep moving right to the flower gate."
                else -> "Collect the glowing bud, then reach the flower gate on the right."
            },
        )
    }

    /** Accessible shortcut follows an animated hop to the very same exit pose. */
    fun skip(): GardenState {
        if (!canMove) return this
        return if (tile == EXIT_TILE) copy(budCollected = true, phase = GardenPhase.EXIT_READY)
        else copy(budCollected = true, facing = 1, move = GardenMove(tile, EXIT_TILE, true),
            hint = "Heading to the flower gate...")
    }

    companion object {
        const val FIRST_TILE = -4
        const val EXIT_TILE = 4
        const val BUD_TILE = 2
        const val STEP_METERS = 0.05f
    }
}
