package com.laya.desk.game

import org.junit.Assert.assertEquals
import org.junit.Test

class PlacementPhaseTest {
    @Test
    fun trackingLossOverridesEverything() {
        assertEquals(
            PlacementPhase.TRACKING_LOST,
            derivePhase(cameraTracking = false, horizontalPlaneFound = true, placed = true),
        )
    }

    @Test
    fun placedWinsOverPlaneFoundWhileTracking() {
        assertEquals(
            PlacementPhase.PLACED,
            derivePhase(cameraTracking = true, horizontalPlaneFound = true, placed = true),
        )
    }

    @Test
    fun planeFoundBeforePlacementIsReady() {
        assertEquals(
            PlacementPhase.READY_TO_PLACE,
            derivePhase(cameraTracking = true, horizontalPlaneFound = true, placed = false),
        )
    }

    @Test
    fun noPlaneMeansScanning() {
        assertEquals(
            PlacementPhase.SCANNING,
            derivePhase(cameraTracking = true, horizontalPlaneFound = false, placed = false),
        )
    }

    @Test
    fun quickPlacementDoesNotRequireAPlane() {
        assertEquals(
            PlacementPhase.QUICK_READY,
            derivePhase(true, false, false, quickPlacementEnabled = true),
        )
    }

    @Test
    fun quickPlacementStillRequiresTracking() {
        assertEquals(
            PlacementPhase.TRACKING_LOST,
            derivePhase(false, false, false, quickPlacementEnabled = true),
        )
    }

    @Test
    fun detectedSurfaceIsPreferredInQuickMode() {
        assertEquals(
            PlacementPhase.READY_TO_PLACE,
            derivePhase(true, true, false, quickPlacementEnabled = true),
        )
    }

    @Test
    fun placedQuickCubeDoesNotInviteMorePlacement() {
        assertEquals(
            PlacementPhase.PLACED,
            derivePhase(true, false, true, quickPlacementEnabled = true),
        )
    }
}
