@file:OptIn(ExperimentalForeignApi::class) // <--- ESTA ES LA LLAVE MAESTRA

package org.frontapp.project

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.interop.UIKitView
import kotlinx.cinterop.*
import platform.AVFoundation.*
import platform.CoreGraphics.CGRect
import platform.Foundation.NSError
import platform.QuartzCore.CATransaction
import platform.QuartzCore.kCATransactionDisableActions
import platform.UIKit.*
import platform.darwin.* // <--- ESTO ARREGLA LOS ERRORES DE DISPATCH

@Composable
actual fun CameraPreview(
    reductionFactor: Float,
    onCameraStatusChanged: (Boolean, Boolean) -> Unit,
    onQrDetected: (String) -> Unit
) {
    UIKitView(
        factory = {
            val cameraController = IOSCameraController(onQrDetected)

            val status = AVCaptureDevice.authorizationStatusForMediaType(AVMediaTypeVideo)
            if (status == AVAuthorizationStatusAuthorized) {
                onCameraStatusChanged(true, false)
                cameraController.startCapture()
            } else {
                AVCaptureDevice.requestAccessForMediaType(AVMediaTypeVideo) { granted ->
                    dispatch_async(dispatch_get_main_queue()) {
                        onCameraStatusChanged(granted, !granted)
                        if (granted) cameraController.startCapture()
                    }
                }
            }

            cameraController.view
        },
        modifier = Modifier.fillMaxSize(),
        onResize = { view, rect ->
            val layer = view.layer.sublayers?.firstOrNull() as? AVCaptureVideoPreviewLayer
            layer?.frame = rect
        }
    )
}

// ==================================================================
// LÓGICA NATIVA DE IOS
// ==================================================================

private class IOSCameraController(
    private val onQrDetected: (String) -> Unit
) : UIViewController(null, null), AVCaptureMetadataOutputObjectsDelegateProtocol {

    private val captureSession = AVCaptureSession()
    private var previewLayer: AVCaptureVideoPreviewLayer? = null

    override fun viewDidLoad() {
        super.viewDidLoad()
        view.backgroundColor = UIColor.blackColor
        setupCamera()
    }

    private fun setupCamera() {
        val device = AVCaptureDevice.defaultDeviceWithMediaType(AVMediaTypeVideo) ?: return

        try {
            val input = AVCaptureDeviceInput.deviceInputWithDevice(device, null) as AVCaptureDeviceInput
            if (captureSession.canAddInput(input)) {
                captureSession.addInput(input)
            } else {
                return
            }

            val metadataOutput = AVCaptureMetadataOutput()
            if (captureSession.canAddOutput(metadataOutput)) {
                captureSession.addOutput(metadataOutput)

                metadataOutput.setMetadataObjectsDelegate(this, dispatch_get_main_queue())
                metadataOutput.metadataObjectTypes = listOf(AVMetadataObjectTypeQRCode)
            } else {
                return
            }

            previewLayer = AVCaptureVideoPreviewLayer(session = captureSession).apply {
                videoGravity = AVLayerVideoGravityResizeAspectFill
                frame = view.bounds
            }
            view.layer.addSublayer(previewLayer!!)

            startCapture()

        } catch (e: Exception) {
            println("Error al iniciar cámara iOS: ${e.message}")
        }
    }

    fun startCapture() {
        if (!captureSession.running) {
            // Usamos el Global Queue para no congelar la UI al iniciar la cámara
            dispatch_async(dispatch_get_global_queue(DISPATCH_QUEUE_PRIORITY_DEFAULT.toLong(), 0u)) {
                captureSession.startRunning()
            }
        }
    }

    fun stopCapture() {
        if (captureSession.running) {
            captureSession.stopRunning()
        }
    }

    override fun viewWillLayoutSubviews() {
        super.viewWillLayoutSubviews()
        previewLayer?.frame = view.bounds
    }

    override fun captureOutput(
        output: AVCaptureOutput,
        didOutputMetadataObjects: List<*>,
        fromConnection: AVCaptureConnection
    ) {
        val metadataObject = didOutputMetadataObjects.firstOrNull() as? AVMetadataMachineReadableCodeObject
        metadataObject?.stringValue?.let { qrContent ->
            onQrDetected(qrContent)
        }
    }
}