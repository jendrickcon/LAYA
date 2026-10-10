package com.laya.desk.ui

import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.snap
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import com.google.android.filament.MaterialInstance
import com.laya.desk.game.EscapePhase
import com.laya.desk.game.GardenState
import com.laya.desk.puzzle.BalanceBridgeState
import com.laya.desk.puzzle.BridgePhase
import io.github.sceneview.SceneScope
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Size
import io.github.sceneview.math.Scale
import kotlin.math.PI
import kotlin.math.sin

private const val STEP_METERS = ExperienceGeometry.STEP_METERS
private const val DECK_TOP = 0.12f
private const val PLATFORM_X = 0.267f

private data class BridgeMaterials(
    val base: MaterialInstance,
    val platform: MaterialInstance,
    val plank: MaterialInstance,
    val pivot: MaterialInstance,
    val fixed: MaterialInstance,
    val movable: MaterialInstance,
    val cream: MaterialInstance,
    val spirit: MaterialInstance,
    val ink: MaterialInstance,
)

/** The same metre-scale scene is attached to either a virtual origin or an AR anchor. */
@Composable
fun SceneScope.BalanceBridgeScene(game: BalanceBridgeGame, facingDegrees: Float = 0f) {
    val state = game.state
    val escape = game.escape
    val materials = remember(materialLoader) {
        fun color(value: Long) = materialLoader.createColorInstance(Color(value), roughness = 0.85f)
        BridgeMaterials(
            base = color(0xFFE0D5BF),
            platform = color(0xFFB7A584),
            plank = color(0xFF638873),
            pivot = color(0xFF73533E),
            fixed = color(0xFFCA8B48),
            movable = color(0xFF327D80),
            cream = color(0xFFFFF3D7),
            spirit = color(0xFFFFD88A),
            ink = color(0xFF302C29),
        )
    }
    val tilt by animateFloatAsState(
        targetValue = -state.signedTurningEffect * 10f,
        animationSpec = tween(220),
        label = "Bridge tilt",
    )
    val weightX by animateFloatAsState(
        targetValue = (game.weightDrag?.position ?: state.distance.toFloat()) * STEP_METERS,
        animationSpec = if (game.weightDrag != null) snap() else tween(220),
        label = "Weight position",
    )

    // One named origin lives under either the AR anchor or the preview scene. Touch rays are
    // converted into this local coordinate system before any movement is interpreted.
    Node(rotation = Rotation(0f, facingDegrees, 0f), apply = { name = ExperienceGeometry.BOARD }) {
        val reveal = when (escape.phase) {
            EscapePhase.ESCAPING -> (escape.descentProgress / 0.4f).coerceIn(0f, 1f)
            EscapePhase.PLAYING -> 1f
            else -> 0f
        }
        if (reveal > 0f) {
            Node(scale = Scale(reveal.coerceAtLeast(0.001f))) {
                CubeNode(size = Size(0.68f, 0.02f, 0.36f), center = Position(0f, 0.01f, 0f),
                    materialInstance = materials.base)
                for (side in listOf(-1, 1)) {
                    CubeNode(size = Size(0.12f, 0.10f, 0.20f), position = Position(side * PLATFORM_X, 0.07f, 0f),
                        materialInstance = materials.platform)
                    CubeNode(size = Size(0.115f, 0.003f, 0.195f),
                        position = Position(side * PLATFORM_X, DECK_TOP + 0.0015f, 0f),
                        materialInstance = materials.cream)
                }
                ConeNode(radius = 0.045f, height = 0.08f, sideCount = 4,
                    position = Position(0f, 0.06f, 0f), materialInstance = materials.pivot)

                // The pivot is the rotation origin. Weights and markers inherit the lever's tilt.
                Node(position = Position(0f, 0.11f, 0f), rotation = Rotation(0f, 0f, tilt),
                    apply = { name = ExperienceGeometry.LEVER }) {
                    CubeNode(size = Size(0.42f, 0.02f, 0.16f), materialInstance = materials.plank)
                    for (unit in 0..1) {
                        CubeNode(size = Size(0.035f), position = Position(-STEP_METERS, 0.0275f + unit * 0.035f, -0.032f),
                            materialInstance = materials.fixed)
                    }
                    CubeNode(size = Size(0.035f), position = Position(weightX, ExperienceGeometry.WEIGHT_Y, -0.032f),
                        materialInstance = if (game.weightDrag != null) materials.spirit else materials.movable,
                        apply = { name = ExperienceGeometry.WEIGHT })
                    if (game.weightDrag != null) {
                        // A larger pad gives shape feedback as well as a color change while selected.
                        CubeNode(size = Size(0.045f, 0.003f, 0.045f), position = Position(weightX, 0.012f, -0.032f),
                            materialInstance = materials.cream, apply = { isTouchable = false })
                    }
                    // One, two and three dots mark each distance without relying on color.
                    for (distance in BalanceBridgeState.DISTANCES) {
                        for (dot in 0 until distance) {
                            SphereNode(radius = 0.003f, stacks = 6, slices = 8,
                                position = Position(distance * STEP_METERS + (dot - (distance - 1) / 2f) * 0.008f, 0.011f, 0.04f),
                                materialInstance = materials.cream)
                        }
                    }
                    CubeNode(size = Size(0.004f, 0.002f, 0.14f), position = Position(0f, 0.011f, 0f),
                        materialInstance = materials.cream)
                }
            }
        }

        GardenScreen(game.garden, escape)
        val progress = state.crossingProgress
        val walking = state.phase == BridgePhase.CROSSING
        val bob = if (walking) sin(progress * 12f * PI).toFloat() * 0.003f else 0f
        val landing = Position(-PLATFORM_X, DECK_TOP + 0.003f, ExperienceGeometry.AVATAR_Z)
        val pulled = Position(ExperienceGeometry.AVATAR_X,
            ExperienceGeometry.AVATAR_Y - escape.pull * ExperienceGeometry.PULL_METERS,
            ExperienceGeometry.AVATAR_Z + escape.pull * ExperienceGeometry.PULL_OUT_METERS)
        val descent = ((escape.descentProgress - 0.2f) / 0.8f).coerceIn(0f, 1f)
        val fairyPosition = when (escape.phase) {
            EscapePhase.SCREEN_PLAY -> Position(
                game.garden.positionSteps * GardenState.STEP_METERS,
                ExperienceGeometry.AVATAR_Y + game.garden.hopHeight,
                ExperienceGeometry.AVATAR_Z)
            EscapePhase.PLAYING -> Position(landing.x + progress * 2f * PLATFORM_X, landing.y + bob, landing.z)
            EscapePhase.ESCAPING -> Position(
                pulled.x + (landing.x - pulled.x) * descent,
                pulled.y + (landing.y - pulled.y) * descent,
                pulled.z + (landing.z - pulled.z) * descent +
                    sin(escape.descentProgress * PI).toFloat() * 0.14f,
            )
            else -> pulled
        }
        // This call stays at one composition location and under one parent through every phase.
        // The first fifth of descent comes forward out of the screen before lowering to the board.
        val motion = game.garden.move?.progress ?: when {
            escape.phase == EscapePhase.ESCAPING -> escape.descentProgress
            walking -> progress
            else -> 0f
        }
        SampaguitaFairy(fairyPosition, escape.phase == EscapePhase.DRAGGING, motion,
            if (escape.phase == EscapePhase.SCREEN_PLAY) game.garden.facing * 18f else 0f)
    }
}
