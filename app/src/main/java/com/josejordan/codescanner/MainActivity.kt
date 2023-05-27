package com.josejordan.codescanner

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.util.SparseArray
import android.view.SurfaceHolder
import android.view.SurfaceView
import android.webkit.URLUtil
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
    private var lastQrCode: String? = null
    private lateinit var graphicOverlay: GraphicOverlay

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        surfaceView = findViewById(R.id.surfaceView)
        graphicOverlay = findViewById(R.id.graphicOverlay)

        setupPermissions()
        setupBarcodeDetector()
        setupCameraSource()
    }

/*    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
        }
    }*/

    private fun setupPermissions() {
        val permission = ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)

        if (permission != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CAMERA
            )
        } else {
            setupBarcodeDetector()
            setupCameraSource()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_REQUEST_CAMERA -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    setupBarcodeDetector()
                    setupCameraSource()
                } else {
                    Toast.makeText(this, "Permiso de cámara denegado", Toast.LENGTH_SHORT).show()
                }
                return
            }
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
                graphicOverlay.clear()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes: SparseArray<Barcode> = detections.detectedItems
                if (barcodes.size() > 0) {
                    graphicOverlay.clear()
                    val barcode = barcodes.valueAt(0)
                    val barcodeGraphic = BarcodeGraphic(graphicOverlay, barcode)
                    graphicOverlay.add(barcodeGraphic)
                    when (barcode.valueFormat) {
                        Barcode.URL -> {
                            val url = barcode.url.url
                            if (URLUtil.isValidUrl(url) && url != lastQrCode) {
                                lastQrCode = url
                                val browserIntent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
                                startActivity(browserIntent)
                            }
                        }
                        Barcode.TEXT -> {
                            val text = barcode.displayValue
                            if (text != lastQrCode) {
                                lastQrCode = text
                                runOnUiThread {
                                    Toast.makeText(applicationContext, "Texto detectado: $text", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        Barcode.CONTACT_INFO -> {
                            val contactInfo = barcode.contactInfo
                            val contactDisplay = """
                    Nombre: ${contactInfo.title} ${contactInfo.name.first} ${contactInfo.name.last}
                    Teléfono: ${contactInfo.phones.firstOrNull()?.number}
                    Email: ${contactInfo.emails.firstOrNull()?.address}
                """.trimIndent()
                            if (contactDisplay != lastQrCode) {
                                lastQrCode = contactDisplay
                                runOnUiThread {
                                    Toast.makeText(applicationContext, "Contacto detectado: $contactDisplay", Toast.LENGTH_LONG).show()
                                }
                            }
                        }
                        // Puedes añadir más casos aquí para otros tipos de datos
                        else -> {
                            // Haz algo para los códigos QR que no se ajustan a los casos anteriores
                        }
                    }
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
                    if (ContextCompat.checkSelfPermission(
                            this@MainActivity,
                            Manifest.permission.CAMERA
                        ) == PackageManager.PERMISSION_GRANTED
                    ) {
                        cameraSource.start(holder)
                        graphicOverlay.setCameraInfo(cameraSource.previewSize.width, cameraSource.previewSize.height)
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(
                holder: SurfaceHolder,
                format: Int,
                width: Int,
                height: Int
            ) {
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

