package org.frontapp.project

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier

@Composable
actual fun CameraPreview(
    reductionFactor: Float,
    onCameraStatusChanged: (Boolean, Boolean) -> Unit,
    onQrDetected: (String) -> Unit
) {
    // Por ahora, como estamos trabajando en Android,
    // dejamos este cuadro de texto para que iOS no de error.
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Text("Cámara de iOS en desarrollo...")
    }
}