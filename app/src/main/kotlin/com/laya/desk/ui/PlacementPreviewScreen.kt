package com.laya.desk.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.TextButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import io.github.sceneview.SceneView
import io.github.sceneview.math.Position
import io.github.sceneview.math.Size
import io.github.sceneview.rememberCameraManipulator
import io.github.sceneview.rememberCameraNode
import io.github.sceneview.rememberEngine
import io.github.sceneview.rememberOnGestureListener
import io.github.sceneview.rememberView

/** Development fallback. No AR session is created and no real-world anchoring is claimed. */
@Composable
fun PlacementPreviewScreen(onReturnToAr: () -> Unit) {
    var placed by remember { mutableStateOf(false) }
    val game = rememberBalanceBridgeGame(canPlay = placed)
    val engine = rememberEngine()
    val view = rememberView(engine)
    val touch = remember(game, view) { ExperienceTouchController(game, view) }
    val home = remember { Position(0.40f, 0.53f, 0.95f) }
    val target = remember { Position(0f, 0.20f, 0f) }
    val cameraNode = rememberCameraNode(engine) {
        position = home
        lookAt(target)
    }
    val manipulator = rememberCameraManipulator(orbitHomePosition = home, targetPosition = target)
    BackHandler(onBack = onReturnToAr)

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text("LAYA · The Desk Breakout", style = MaterialTheme.typography.headlineSmall)
                Text("3D preview · No camera", style = MaterialTheme.typography.labelLarge)
                Text(
                    if (placed) "Free her, then balance the bridge. Drag empty space to look around; pinch to zoom."
                    else "Tap the virtual board or use Place board to begin.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            SceneView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                engine = engine,
                view = view,
                onTouchEvent = touch::onTouch,
                cameraNode = cameraNode,
                cameraManipulator = manipulator,
                autoCenterContent = false,
                isOpaque = false,
                onGestureListener = rememberOnGestureListener(
                    onSingleTapConfirmed = { _, _ ->
                        if (!placed) {
                            game.reset()
                            placed = true
                        }
                    },
                ),
            ) {
                if (placed) {
                    BalanceBridgeScene(game)
                } else {
                    val boardMaterial = remember(materialLoader) {
                        materialLoader.createColorInstance(Color(0xFFB9A782), roughness = 0.9f)
                    }
                    CubeNode(
                        size = Size(0.68f, 0.02f, 0.36f),
                        center = Position(0f, 0.01f, 0f),
                        materialInstance = boardMaterial,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (placed) {
                    BalanceBridgePanel(game, canPlay = true)
                    TextButton(onClick = {
                        placed = false
                        game.reset()
                    }) { Text("Reset placement") }
                } else {
                    Button(onClick = {
                        game.reset()
                        placed = true
                    }, modifier = Modifier.fillMaxWidth()) { Text("Place board") }
                }
                OutlinedButton(onClick = onReturnToAr, modifier = Modifier.fillMaxWidth()) { Text("Back to AR") }
            }
        }
    }
}
