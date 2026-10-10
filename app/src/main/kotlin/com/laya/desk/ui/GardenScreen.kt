package com.laya.desk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.android.filament.MaterialInstance
import com.laya.desk.game.EscapePhase
import com.laya.desk.game.EscapeState
import com.laya.desk.game.GardenState
import io.github.sceneview.SceneScope
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.math.Size

private data class GardenMaterials(
    val sky: MaterialInstance, val frame: MaterialInstance, val soil: MaterialInstance,
    val leaf: MaterialInstance, val petal: MaterialInstance, val bud: MaterialInstance,
)

/** Shallow, bounded playfield inside the same renderer and anchor as the tabletop experience. */
@Composable
internal fun SceneScope.GardenScreen(garden: GardenState, escape: EscapeState) {
    val m = remember(materialLoader) {
        fun color(value: Long) = materialLoader.createColorInstance(Color(value), roughness = 0.9f)
        GardenMaterials(materialLoader.createUnlitColorInstance(Color(0xFF82B9B5)),
            color(0xFF795B40), color(0xFFCDB88E),
            color(0xFF6B874A), color(0xFFFFF1D7),
            materialLoader.createUnlitColorInstance(Color(0xFFFFE7A0)))
    }
    val floor = ExperienceGeometry.AVATAR_Y
    CubeNode(size = Size(0.54f, 0.28f, 0.012f), position = Position(0f, 0.455f, 0.010f),
        materialInstance = m.sky)
    for (side in listOf(-1, 1)) {
        CubeNode(size = Size(0.012f, 0.304f, 0.018f),
            position = Position(side * 0.276f, 0.455f, 0.012f), materialInstance = m.frame)
        CubeNode(size = Size(0.564f, 0.012f, 0.018f),
            position = Position(0f, 0.455f + side * 0.146f, 0.012f), materialInstance = m.frame)
    }
    // Every decoration is bounded to the display. Only the fairy ever leaves it.
    SphereNode(radius = 1f, stacks = 10, slices = 16, scale = Scale(0.018f, 0.018f, 0.004f),
        position = Position(-0.22f, 0.554f, 0.020f), materialInstance = m.petal)
    for (index in 0..2) {
        SphereNode(radius = 1f, stacks = 8, slices = 12, scale = Scale(0.040f, 0.010f, 0.003f),
            position = Position(-0.12f + index * 0.125f, 0.562f - (index % 2) * 0.015f, 0.020f),
            materialInstance = m.petal)
    }
    CubeNode(size = Size(0.525f, 0.029f, 0.028f), position = Position(0f, floor - 0.0165f, 0.035f),
        materialInstance = m.soil)
    CubeNode(size = Size(0.525f, 0.004f, 0.025f), position = Position(0f, floor - 0.001f, 0.037f),
        materialInstance = m.leaf)
    for (index in 0..4) {
        val x = -0.23f + index * 0.10f
        CubeNode(size = Size(0.003f, 0.042f, 0.003f), position = Position(x, floor + 0.021f, 0.023f),
            materialInstance = m.leaf)
        SphereNode(radius = 1f, stacks = 6, slices = 10, scale = Scale(0.008f, 0.014f, 0.002f),
            position = Position(x + 0.005f, floor + 0.025f, 0.024f),
            rotation = Rotation(0f, 0f, -35f), materialInstance = m.leaf)
        SphereNode(radius = 0.006f, stacks = 6, slices = 10,
            position = Position(x, floor + 0.044f, 0.024f), materialInstance = m.petal)
    }
    CubeNode(size = Size(0.044f, 0.029f, 0.025f),
        position = Position(0f, floor + 0.0145f, ExperienceGeometry.AVATAR_Z),
        materialInstance = m.frame)
    if (!garden.budCollected) {
        Node(position = Position(GardenState.BUD_TILE * GardenState.STEP_METERS, floor + 0.058f, 0.056f)) {
            SphereNode(radius = 0.008f, stacks = 8, slices = 12, materialInstance = m.bud)
            for (side in listOf(-1, 1)) {
                SphereNode(radius = 1f, stacks = 6, slices = 8, scale = Scale(0.004f, 0.009f, 0.003f),
                    position = Position(side * 0.005f, 0.002f, 0f),
                    rotation = Rotation(0f, 0f, side * 30f), materialInstance = m.petal)
            }
        }
    }
    // A flower gate is the destination; no extraction hint is visible during garden play.
    for (side in listOf(-1, 1)) {
        CubeNode(size = Size(0.005f, 0.080f, 0.006f),
            position = Position(ExperienceGeometry.AVATAR_X + side * 0.041f, floor + 0.04f, 0.025f),
            materialInstance = m.leaf)
    }
    for (index in -2..2) {
        SphereNode(radius = 0.007f, stacks = 6, slices = 10,
            position = Position(ExperienceGeometry.AVATAR_X + index * 0.018f,
                floor + 0.087f + (2 - kotlin.math.abs(index)) * 0.006f, 0.025f), materialInstance = m.petal)
    }
    if (escape.phase == EscapePhase.EXIT_READY || escape.phase == EscapePhase.DRAGGING ||
        escape.phase == EscapePhase.RETURNING) {
        CubeNode(size = Size(0.11f, 0.005f, 0.008f),
            position = Position(ExperienceGeometry.AVATAR_X,
                floor - ExperienceGeometry.PULL_METERS * EscapeState.RELEASE_THRESHOLD,
                ExperienceGeometry.AVATAR_Z + ExperienceGeometry.PULL_OUT_METERS * EscapeState.RELEASE_THRESHOLD),
            materialInstance = if (escape.readyToRelease) m.bud else m.petal,
            apply = { isTouchable = false })
    }
}
