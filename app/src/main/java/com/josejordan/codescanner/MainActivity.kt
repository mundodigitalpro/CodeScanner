package com.josejordan.codescanner

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector

class MainActivity : AppCompatActivity() {

    private lateinit var cameraSource: CameraSource
    private lateinit var detector: BarcodeDetector
    private lateinit var surfaceView: SurfaceView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)

        setupPermissions()
        setupBarcodeDetector()
        setupCameraSource()
    }

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.CAMERA), PERMISSION_REQUEST_CAMERA)
        }
    }

    private fun setupBarcodeDetector() {
        detector = BarcodeDetector.Builder(applicationContext)
            .setBarcodeFormats(Barcode.QR_CODE)
            .build()

        if (!detector.isOperational) {
            Log.e("MainActivity", "Detector de códigos de barras no está operacional")
        } else {
            Log.i("MainActivity", "Detector de códigos de barras está operacional")
        }


        detector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                // TODO: Implementar la liberación de recursos si es necesario
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes: SparseArray<Barcode> = detections.detectedItems
                Log.i("MainActivity", "Número de códigos de barras detectados: ${barcodes.size()}")
                if (barcodes.size() > 0) {
                    val barcode = barcodes.valueAt(0)
                    Log.i("MainActivity", "Código QR detectado: ${barcode.displayValue}")
                }
            }

        })
    }

    private fun setupCameraSource() {
        cameraSource = CameraSource.Builder(applicationContext, detector)
            .setRequestedPreviewSize(640, 480)
            .setAutoFocusEnabled(true)
            .setRequestedFps(15.0f)
            .build()

        surfaceView.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                try {
                    if (ContextCompat.checkSelfPermission(this@MainActivity, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                        cameraSource.start(holder)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
                // No se requiere ninguna acción en este método para esta implementación
            }

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
            }
        })
    }

    companion object {
        const val PERMISSION_REQUEST_CAMERA = 1001
    }
}

