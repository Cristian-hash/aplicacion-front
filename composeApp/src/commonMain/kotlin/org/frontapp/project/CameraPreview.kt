package org.frontapp.project

import androidx.compose.runtime.Composable

@Composable
expect fun CameraPreview(
    reductionFactor: Float,
    onCameraStatusChanged: (Boolean, Boolean) -> Unit,
    onQrDetected: (String) -> Unit
)