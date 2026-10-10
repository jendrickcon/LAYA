package com.laya.desk.game

import kotlin.math.roundToInt

/** A visual, uncommitted position. Only a release selects an integer puzzle distance. */
data class WeightDragState(val position: Float) {
    init { require(position.isFinite() && position in 1f..3f) }

    fun move(value: Float): WeightDragState =
        if (value.isFinite()) copy(position = value.coerceIn(1f, 3f)) else this

    val nearestSlot: Int get() = position.roundToInt().coerceIn(1, 3)
}
