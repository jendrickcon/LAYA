package com.laya.desk.game

/** Milestone 1 flow. Pure Kotlin so it can be unit tested without Android. */
enum class PlacementPhase { SCANNING, READY_TO_PLACE, QUICK_READY, PLACED, TRACKING_LOST }

fun derivePhase(
    cameraTracking: Boolean,
    horizontalPlaneFound: Boolean,
    placed: Boolean,
    quickPlacementEnabled: Boolean = false,
): PlacementPhase = when {
    !cameraTracking -> PlacementPhase.TRACKING_LOST
    placed -> PlacementPhase.PLACED
    horizontalPlaneFound -> PlacementPhase.READY_TO_PLACE
    quickPlacementEnabled -> PlacementPhase.QUICK_READY
    else -> PlacementPhase.SCANNING
}

fun instructionFor(phase: PlacementPhase): String = when (phase) {
    PlacementPhase.SCANNING -> "Move slowly over a flat surface: a floor or book works too. Or try Quick placement / 3D preview."
    PlacementPhase.READY_TO_PLACE -> "Surface found. Tap it to place Balance Bridge."
    PlacementPhase.QUICK_READY -> "Quick placement: tap to place about 50 cm away. Its position may adjust as tracking improves."
    PlacementPhase.PLACED -> "Board placed. Balance the bridge to help the spirit cross."
    PlacementPhase.TRACKING_LOST -> "Tracking lost. Point at something detailed in good light, or use 3D preview. Quick placement also needs tracking."
}
