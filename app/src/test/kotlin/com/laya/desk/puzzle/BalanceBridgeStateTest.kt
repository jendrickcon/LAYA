package com.laya.desk.puzzle

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BalanceBridgeStateTest {
    private fun crossing() = BalanceBridgeState().selectDistance(2).startCrossing(canPlay = true)
    private fun completed(): BalanceBridgeState {
        var state = crossing()
        repeat(30) { state = state.advanceCrossing(100, canPlay = true) }
        return state
    }

    @Test fun initialPuzzleTipsTowardTheFixedWeight() {
        val state = BalanceBridgeState()
        assertEquals(-1, state.signedTurningEffect)
        assertEquals(BalanceDirection.FIXED_SIDE_DOWN, state.balanceDirection)
        assertFalse(state.isBalanced)
        assertEquals(BridgePhase.BALANCING, state.phase)
    }

    @Test fun lighterWeightAtDoubleDistanceBalances() {
        val state = BalanceBridgeState().selectDistance(2)
        assertEquals(state.fixedTurningEffect, state.movableTurningEffect)
        assertTrue(state.isBalanced)
        assertEquals(BalanceDirection.LEVEL, state.balanceDirection)
    }

    @Test fun movingTooFarTipsTowardTheLighterWeight() {
        val state = BalanceBridgeState().selectDistance(3)
        assertEquals(1, state.signedTurningEffect)
        assertEquals(BalanceDirection.MOVABLE_SIDE_DOWN, state.balanceDirection)
        assertFalse(state.isBalanced)
    }

    @Test fun distanceInputStaysInsideTheAvailableSlots() {
        val state = BalanceBridgeState()
        assertEquals(1, state.selectDistance(Int.MIN_VALUE).distance)
        assertEquals(3, state.selectDistance(Int.MAX_VALUE).distance)
        BalanceBridgeState.DISTANCES.forEach { assertEquals(it, state.selectDistance(it).distance) }
    }

    @Test fun crossingCannotStartUntilBalanced() {
        val state = BalanceBridgeState()
        assertFalse(state.canStartCrossing(canPlay = true))
        assertEquals(state, state.startCrossing(canPlay = true))
    }

    @Test fun trackingLossPreventsStartingEvenAtTheSolution() {
        val state = BalanceBridgeState().selectDistance(2)
        assertFalse(state.canStartCrossing(canPlay = false))
        assertEquals(state, state.startCrossing(canPlay = false))
    }

    @Test fun crossingLocksWeightAndRejectsRepeatedStarts() {
        val state = crossing().advanceCrossing(80, canPlay = true)
        assertEquals(BridgePhase.CROSSING, state.phase)
        assertEquals(state, state.selectDistance(3))
        assertEquals(state, state.startCrossing(canPlay = true))
    }

    @Test fun trackingPausePreservesProgressAndResumeContinues() {
        val beforePause = crossing().advanceCrossing(60, canPlay = true)
        val paused = beforePause.advanceCrossing(2_000, canPlay = false)
        assertEquals(beforePause, paused)
        assertEquals(100L, paused.advanceCrossing(40, canPlay = true).crossingMillis)
    }

    @Test fun invalidOrDelayedFrameTimesCannotSkipTheWalk() {
        val state = crossing()
        assertEquals(state, state.advanceCrossing(0, canPlay = true))
        assertEquals(state, state.advanceCrossing(-100, canPlay = true))
        val delayed = state.advanceCrossing(Long.MAX_VALUE, canPlay = true)
        assertEquals(100L, delayed.crossingMillis)
        assertEquals(BridgePhase.CROSSING, delayed.phase)
    }

    @Test fun threeSecondsOfActiveFramesCompletesExactlyOnce() {
        val state = completed()
        assertEquals(BridgePhase.COMPLETE, state.phase)
        assertEquals(1f, state.crossingProgress, 0f)
        assertEquals(state, state.advanceCrossing(100, canPlay = true))
        assertEquals(state, state.startCrossing(canPlay = true))
        assertEquals(state, state.selectDistance(1))
    }

    @Test fun resetClearsBothAnInterruptedWalkAndACompletedRun() {
        assertEquals(BalanceBridgeState(), crossing().advanceCrossing(50, true).replay())
        assertEquals(BalanceBridgeState(), completed().replay())
    }

    @Test fun framesOutsideCrossingNeverAdvanceThePuzzle() {
        val initial = BalanceBridgeState()
        val balanced = initial.selectDistance(2)
        assertEquals(initial, initial.advanceCrossing(100, true))
        assertEquals(balanced, balanced.advanceCrossing(100, true))
    }
}
