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
import androidx.compose.material3.Slider
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
import io.github.sceneview.rememberRenderer
import com.laya.desk.portal.PortalContent
import com.laya.desk.portal.PortalController
import com.laya.desk.portal.PortalProjection

/** Development fallback. No AR session is created and no real-world anchoring is claimed. */
@Composable
fun PlacementPreviewScreen(
    onReturnToAr: () -> Unit,
    portalLab: Boolean = false,
    onTogglePortal: () -> Unit = {},
) {
    var placed by remember { mutableStateOf(false) }
    val game = rememberBalanceBridgeGame(canPlay = placed)
    val engine = rememberEngine()
    val view = rememberView(engine)
    val renderer = rememberRenderer(engine)
    val portal = remember { PortalController() }
    var portalX by remember { mutableStateOf(0f) }
    var portalY by remember { mutableStateOf(0.09f) }
    val touch = remember(game, view) { ExperienceTouchController(game, view) }
    val home = remember { Position(0.22f, 0.53f, 1.20f) }
    val target = remember { Position(0f, 0.30f, 0f) }
    val cameraNode = rememberCameraNode(engine) {
        position = home
        lookAt(target)
    }
    val manipulator = if (portalLab) null else
        rememberCameraManipulator(orbitHomePosition = home, targetPosition = target)
    BackHandler(onBack = onReturnToAr)

    Surface(Modifier.fillMaxSize()) {
        Column(Modifier.fillMaxSize().safeDrawingPadding()) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 20.dp, vertical = 12.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(if (portalLab) "Portal lab · Experimental" else "LAYA", style = MaterialTheme.typography.headlineSmall)
                Text("3D preview · No camera", style = MaterialTheme.typography.labelLarge)
                Text(
                    if (portalLab) "Look through the window. Near and far objects should shift differently."
                    else if (placed) "Drag empty space to look around; pinch to zoom."
                    else "Tap the scene or use Place experience to begin.",
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
            SceneView(
                modifier = Modifier.weight(1f).fillMaxWidth(),
                engine = engine,
                view = view,
                renderer = renderer,
                onTouchEvent = if (portalLab) null else touch::onTouch,
                cameraNode = cameraNode,
                cameraManipulator = manipulator,
                autoCenterContent = false,
                isOpaque = false,
                onFrame = {
                    if (portalLab) {
                        cameraNode.position = Position(portalX, PortalProjection.CENTER_HEIGHT + portalY, 0.65f)
                        cameraNode.lookAt(Position(0f, PortalProjection.CENTER_HEIGHT, 0f))
                        if (placed) portal.render(cameraNode.worldPosition)
                    }
                },
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
                    if (portalLab) PortalContent(portal, renderer) else BalanceBridgeScene(game)
                } else {
                    val screenMaterial = remember(materialLoader) {
                        materialLoader.createColorInstance(Color(0xFF82B9B5), roughness = 0.9f)
                    }
                    CubeNode(
                        size = if (portalLab) Size(0.30f, 0.20f, 0.004f) else Size(0.54f, 0.28f, 0.012f),
                        center = if (portalLab) Position(0f, PortalProjection.CENTER_HEIGHT, 0f)
                            else Position(0f, 0.455f, 0.010f),
                        materialInstance = screenMaterial,
                    )
                }
            }

            Column(
                modifier = Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(4.dp),
            ) {
                if (placed) {
                    if (portalLab) {
                        Text(portal.status.message, style = MaterialTheme.typography.bodySmall)
                        portal.error?.let { Text(it.take(160), style = MaterialTheme.typography.bodySmall) }
                        Text("Viewpoint · left / right", style = MaterialTheme.typography.labelMedium)
                        Slider(value = portalX, onValueChange = { portalX = it }, valueRange = -0.32f..0.32f)
                        Text("Viewpoint · below / above", style = MaterialTheme.typography.labelMedium)
                        Slider(value = portalY, onValueChange = { portalY = it }, valueRange = -0.12f..0.25f)
                    } else BalanceBridgePanel(game, canPlay = true)
                    TextButton(onClick = {
                        placed = false
                        game.reset()
                    }) { Text("Reset placement") }
                } else {
                    Button(onClick = {
                        game.reset()
                        placed = true
                    }, modifier = Modifier.fillMaxWidth()) { Text(if (portalLab) "Place portal" else "Place experience") }
                }
                OutlinedButton(onClick = onReturnToAr, modifier = Modifier.fillMaxWidth()) { Text("Back to AR") }
                TextButton(onClick = onTogglePortal) {
                    Text(if (portalLab) "Return to LAYA" else "Portal lab (experimental)")
                }
            }
        }
    }
}
