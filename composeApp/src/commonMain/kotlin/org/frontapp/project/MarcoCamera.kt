package org.frontapp.project

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp

// Este componente dibuja las 4 esquinas blancas sobre la cámara
@Composable
fun MarcoCamera(modifier: Modifier = Modifier) {
    Box(modifier = modifier, contentAlignment = Alignment.Center) {
        Canvas(modifier = Modifier.fillMaxSize()) {
            val strokeWidth = 6.dp.toPx() // Grosor de la línea
            val cornerLength = 40.dp.toPx() // Largo de la esquina
            val radius = 16.dp.toPx() // Curva de la esquina

            // 1. Esquina Superior Izquierda
            val pathTL = Path().apply {
                moveTo(cornerLength, 0f)
                lineTo(radius, 0f)
                arcTo(Rect(0f, 0f, radius * 2, radius * 2), 270f, -90f, false)
                lineTo(0f, cornerLength)
            }
            drawPath(pathTL, Color.White, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // 2. Esquina Superior Derecha
            val pathTR = Path().apply {
                moveTo(size.width - cornerLength, 0f)
                lineTo(size.width - radius, 0f)
                arcTo(Rect(size.width - radius * 2, 0f, size.width, radius * 2), 270f, 90f, false)
                lineTo(size.width, cornerLength)
            }
            drawPath(pathTR, Color.White, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // 3. Esquina Inferior Izquierda
            val pathBL = Path().apply {
                moveTo(0f, size.height - cornerLength)
                lineTo(0f, size.height - radius)
                arcTo(Rect(0f, size.height - radius * 2, radius * 2, size.height), 180f, -90f, false)
                lineTo(cornerLength, size.height)
            }
            drawPath(pathBL, Color.White, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))

            // 4. Esquina Inferior Derecha
            val pathBR = Path().apply {
                moveTo(size.width - cornerLength, size.height)
                lineTo(size.width - radius, size.height)
                arcTo(Rect(size.width - radius * 2, size.height - radius * 2, size.width, size.height), 90f, -90f, false)
                lineTo(size.width, size.height - cornerLength)
            }
            drawPath(pathBR, Color.White, style = Stroke(strokeWidth, cap = StrokeCap.Round, join = StrokeJoin.Round))
        }
    }
}