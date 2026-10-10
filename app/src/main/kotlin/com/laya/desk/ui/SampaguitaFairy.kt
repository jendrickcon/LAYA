package com.laya.desk.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.android.filament.MaterialInstance
import io.github.sceneview.SceneScope
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

private data class FairyMaterials(
    val skin: MaterialInstance, val leaf: MaterialInstance, val petal: MaterialInstance,
    val wing: MaterialInstance, val hair: MaterialInstance, val eye: MaterialInstance,
    val gold: MaterialInstance,
)

/** Original primitive fairy. One stable named root is used inside and outside the screen. */
@Composable
internal fun SceneScope.SampaguitaFairy(
    position: Position,
    selected: Boolean,
    motion: Float,
    facing: Float,
) {
    val m = remember(materialLoader) {
        fun color(value: Long) = materialLoader.createColorInstance(Color(value), roughness = 0.85f)
        FairyMaterials(color(0xFFAF7654), color(0xFF68783D), color(0xFFFFF1D7),
            color(0xFFDCE6BE), color(0xFF39291F), color(0xFF271C19), color(0xFFE7C56B))
    }
    val flutter = sin(motion * 4f * PI).toFloat() * 14f
    val stride = sin(motion * 2f * PI).toFloat() * 12f
    Node(position = position, rotation = Rotation(0f, facing, 0f),
        apply = { name = ExperienceGeometry.AVATAR }) {
        // Opaque, thin leaf wings keep the silhouette inexpensive and predictable on mobile.
        for (side in listOf(-1, 1)) {
            Node(position = Position(side * 0.008f, 0.057f, -0.013f),
                rotation = Rotation(0f, side * (12f + flutter), 0f)) {
                SphereNode(radius = 1f, stacks = 8, slices = 12,
                    scale = Scale(0.016f, 0.030f, 0.0028f),
                    position = Position(side * 0.017f, 0.017f, 0f),
                    rotation = Rotation(0f, 0f, -side * 26f), materialInstance = m.wing)
                SphereNode(radius = 1f, stacks = 8, slices = 12,
                    scale = Scale(0.011f, 0.021f, 0.0028f),
                    position = Position(side * 0.018f, -0.012f, 0f),
                    rotation = Rotation(0f, 0f, side * 28f), materialInstance = m.wing)
            }
            CylinderNode(radius = 0.003f, height = 0.015f, sideCount = 8,
                position = Position(side * 0.007f, 0.012f, 0f), materialInstance = m.skin)
            SphereNode(radius = 1f, stacks = 6, slices = 10,
                scale = Scale(0.0045f, 0.003f, 0.008f),
                position = Position(side * 0.007f, 0.004f, 0.005f), materialInstance = m.leaf)
            Node(position = Position(side * 0.013f, 0.060f, 0f),
                rotation = Rotation(stride * side, 0f, side * 18f)) {
                CylinderNode(radius = 0.0035f, height = 0.025f, sideCount = 8,
                    position = Position(side * 0.004f, -0.011f, 0f), materialInstance = m.skin)
                SphereNode(radius = 0.004f, stacks = 6, slices = 8,
                    position = Position(side * 0.004f, -0.024f, 0f), materialInstance = m.skin)
            }
        }
        ConeNode(radius = 0.025f, height = 0.035f, sideCount = 12,
            position = Position(0f, 0.030f, 0f), materialInstance = m.petal)
        // Five overlapping petals soften the bell skirt.
        for (petal in 0..4) {
            val angle = petal * 2.0 * PI / 5.0
            SphereNode(radius = 1f, stacks = 8, slices = 10,
                scale = Scale(0.010f, 0.020f, 0.006f),
                position = Position(sin(angle).toFloat() * 0.014f, 0.031f, cos(angle).toFloat() * 0.014f),
                rotation = Rotation(0f, (petal * 72).toFloat(), 0f), materialInstance = m.petal)
        }
        SphereNode(radius = 1f, stacks = 8, slices = 12,
            scale = Scale(0.011f, 0.016f, 0.008f),
            position = Position(0f, 0.057f, 0f), materialInstance = m.leaf)
        CylinderNode(radius = 0.012f, height = 0.003f, sideCount = 12,
            position = Position(0f, 0.047f, 0f), materialInstance = m.gold)
        SphereNode(radius = 0.014f, stacks = 10, slices = 14,
            position = Position(0f, 0.082f, 0.003f), materialInstance = m.skin)
        SphereNode(radius = 1f, stacks = 8, slices = 12,
            scale = Scale(0.015f, 0.014f, 0.008f),
            position = Position(0f, 0.079f, -0.007f), materialInstance = m.hair)
        SphereNode(radius = 1f, stacks = 8, slices = 12,
            scale = Scale(0.015f, 0.008f, 0.012f),
            position = Position(0f, 0.092f, -0.001f), materialInstance = m.hair)
        for (side in listOf(-1, 1)) {
            SphereNode(radius = 0.0017f, stacks = 6, slices = 8,
                position = Position(side * 0.0045f, 0.084f, 0.016f), materialInstance = m.eye)
        }
        SphereNode(radius = 1f, stacks = 6, slices = 8, scale = Scale(0.003f, 0.001f, 0.001f),
            position = Position(0f, 0.077f, 0.016f), materialInstance = m.gold)
        for (petal in 0..4) {
            val angle = petal * 2.0 * PI / 5.0
            SphereNode(radius = 0.0035f, stacks = 6, slices = 8,
                position = Position(0.008f + cos(angle).toFloat() * 0.004f,
                    0.099f + sin(angle).toFloat() * 0.004f, 0.009f), materialInstance = m.petal)
        }
        SphereNode(radius = 0.0025f, stacks = 6, slices = 8,
            position = Position(0.008f, 0.099f, 0.012f), materialInstance = m.gold)
        if (selected) {
            CylinderNode(radius = 0.031f, height = 0.002f, sideCount = 20,
                position = Position(0f, 0.001f, 0f), materialInstance = m.gold)
        }
    }
}
