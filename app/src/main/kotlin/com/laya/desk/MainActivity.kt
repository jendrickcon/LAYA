package com.laya.desk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
import androidx.compose.runtime.key
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.laya.desk.ui.ArPlacementScreen
import com.laya.desk.ui.CameraPermissionGate
import com.laya.desk.ui.PlacementPreviewScreen

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            MaterialTheme {
                var preview by rememberSaveable { mutableStateOf(false) }
                var portalLab by rememberSaveable { mutableStateOf(false) }
                // Changing experiment disposes its entire host/anchor/resources before normal play.
                key(portalLab) {
                    if (preview) {
                        PlacementPreviewScreen(onReturnToAr = { preview = false },
                            portalLab = portalLab, onTogglePortal = { portalLab = !portalLab })
                    } else {
                        CameraPermissionGate(onOpenPreview = { preview = true },
                            onOpenPortal = { portalLab = true; preview = true }) {
                            ArPlacementScreen(onOpenPreview = { preview = true },
                                portalLab = portalLab, onTogglePortal = { portalLab = !portalLab })
                        }
                    }
                }
            }
        }
    }
}
