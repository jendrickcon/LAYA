package com.laya.desk.ui

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.FilterChip
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.laya.desk.game.EscapePhase
import com.laya.desk.game.EscapeState
import com.laya.desk.puzzle.BalanceDirection
import com.laya.desk.puzzle.BridgePhase

/** Shared controls/teaching feedback; screen placement and AR controls stay with the host. */
@Composable
fun BalanceBridgePanel(
    game: BalanceBridgeGame,
    canPlay: Boolean,
    modifier: Modifier = Modifier,
) {
    val state = game.state
    val playable = canPlay && game.puzzleEnabled && game.weightDrag == null
    val scroll = rememberScrollState()
    LaunchedEffect(game.inputEpoch, game.escape.phase == EscapePhase.PLAYING, state.phase) {
        scroll.scrollTo(0)
    }
    Surface(modifier = modifier.fillMaxWidth(), tonalElevation = 4.dp, shape = MaterialTheme.shapes.large) {
        Column(
            // Keep the preview viewport stable when changing from escape to puzzle to explanation.
            modifier = Modifier.heightIn(min = 260.dp, max = 260.dp).verticalScroll(scroll).padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            if (game.escape.phase != EscapePhase.PLAYING) {
                Text(
                    if (game.escape.phase == EscapePhase.ESCAPING) "She's free!" else "Drag her down to set her free.",
                    style = MaterialTheme.typography.titleMedium,
                )
                Text(when {
                    !canPlay -> "Find the board with your camera to continue."
                    game.escape.phase == EscapePhase.ESCAPING -> "She's landing on the starting platform."
                    game.escape.readyToRelease -> "Release now to set her free!"
                    game.escape.phase == EscapePhase.RETURNING -> "Try again — pull her feet down to the marker line."
                    else -> "Touch the little spirit, pull her feet down to the marker line, then release."
                }, modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                LinearProgressIndicator(
                    progress = {
                        if (game.escape.phase == EscapePhase.ESCAPING) game.escape.descentProgress
                        else (game.escape.pull / EscapeState.RELEASE_THRESHOLD).coerceIn(0f, 1f)
                    },
                    modifier = Modifier.fillMaxWidth(),
                )
                Button(onClick = game::freeAvatar,
                    enabled = canPlay && game.active && game.escape.phase != EscapePhase.ESCAPING,
                    modifier = Modifier.fillMaxWidth()) { Text("Free her") }
                Text("The bridge unlocks once she lands.", style = MaterialTheme.typography.bodySmall)
            } else when (state.phase) {
                BridgePhase.BALANCING -> {
                    Text("Move the 1-unit weight", style = MaterialTheme.typography.titleMedium)
                    Text(
                        game.weightDrag?.let { "Release to snap to slot ${it.nearestSlot}. The balance updates on release." }
                            ?: "Drag the single teal block along the bridge, or choose a distance below.",
                        style = MaterialTheme.typography.bodySmall,
                    )
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                        for (distance in 1..3) {
                            FilterChip(
                                selected = state.distance == distance,
                                onClick = { if (playable) game.selectDistance(distance) },
                                enabled = playable,
                                modifier = Modifier.weight(1f).heightIn(min = 48.dp),
                                label = { Text("$distance ${if (distance == 1) "step" else "steps"}") },
                            )
                        }
                    }
                    Text(
                        "Fixed: 2 × 1 = 2   |   Movable: 1 × ${state.distance} = ${state.movableTurningEffect}",
                        style = MaterialTheme.typography.bodyMedium,
                    )
                    Text(
                        when (state.balanceDirection) {
                            BalanceDirection.FIXED_SIDE_DOWN -> "The 2-unit side tips down."
                            BalanceDirection.LEVEL -> "Balanced — both turning effects are equal!"
                            BalanceDirection.MOVABLE_SIDE_DOWN -> "The 1-unit side tips down. Try moving it closer."
                        },
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    Button(
                        onClick = { game.cross(canPlay) },
                        enabled = state.canStartCrossing(playable),
                        modifier = Modifier.fillMaxWidth(),
                    ) { Text("Cross the bridge") }
                }
                BridgePhase.CROSSING -> {
                    Text(
                        if (canPlay) "She's crossing!" else "Crossing paused",
                        style = MaterialTheme.typography.titleMedium,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite },
                    )
                    Text(if (canPlay) "Keep the bridge steady." else "Find the board with your camera to continue.")
                    LinearProgressIndicator(progress = { state.crossingProgress }, modifier = Modifier.fillMaxWidth())
                }
                BridgePhase.COMPLETE -> {
                    Text("You made it!", style = MaterialTheme.typography.titleLarge,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    Text("A lighter weight can balance a heavier one when it is farther from the pivot. Turning effect depends on weight × distance.")
                    Text("2 × 1 = 1 × 2", style = MaterialTheme.typography.titleMedium)
                    Button(onClick = game::reset, modifier = Modifier.fillMaxWidth()) { Text("Play again") }
                }
            }
            game.inputHint?.let { Text(it, style = MaterialTheme.typography.bodySmall) }
        }
    }
}
