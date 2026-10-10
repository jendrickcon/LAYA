package com.laya.desk

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.getValue
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
                if (preview) {
                    PlacementPreviewScreen(onReturnToAr = { preview = false })
                } else {
                    CameraPermissionGate(onOpenPreview = { preview = true }) {
                        ArPlacementScreen(onOpenPreview = { preview = true })
                    }
                }
            }
        }
    }
}
