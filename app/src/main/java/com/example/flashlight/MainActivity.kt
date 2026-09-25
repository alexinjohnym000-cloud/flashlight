package com.example.flashlight

import android.Manifest
import android.content.pm.PackageManager
import android.hardware.camera2.CameraCharacteristics
import android.hardware.camera2.CameraManager
import android.os.Bundle
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat

class MainActivity : AppCompatActivity() {

    private lateinit var cameraManager: CameraManager
    private var cameraId: String? = null
    private var isFlashOn = false

    companion object {
        private const val CAMERA_PERMISSION_REQUEST = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        cameraManager = getSystemService(CAMERA_SERVICE) as CameraManager

        val toggleButton = findViewById<Button>(R.id.toggleButton)
        toggleButton.setOnClickListener {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED
            ) {
                ActivityCompat.requestPermissions(
                    this,
                    arrayOf(Manifest.permission.CAMERA),
                    CAMERA_PERMISSION_REQUEST
                )
            } else {
                toggleFlash()
            }
        }

        findCameraWithFlash()
        updateUi()
    }

    private fun findCameraWithFlash() {
        try {
            cameraId = cameraManager.cameraIdList.firstOrNull { id ->
                cameraManager.getCameraCharacteristics(id)
                    .get(CameraCharacteristics.FLASH_INFO_AVAILABLE) == true
            }
        } catch (e: Exception) {
            cameraId = null
        }
    }

    private fun toggleFlash() {
        val id = cameraId
        if (id == null) {
            Toast.makeText(this, "This device does not have a flashlight.", Toast.LENGTH_SHORT).show()
            return
        }

        try {
            isFlashOn = !isFlashOn
            cameraManager.setTorchMode(id, isFlashOn)
            updateUi()
        } catch (e: Exception) {
            isFlashOn = false
            updateUi()
            Toast.makeText(this, "Could not control the flashlight.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun updateUi() {
        val status = findViewById<TextView>(R.id.statusText)
        val button = findViewById<Button>(R.id.toggleButton)

        if (isFlashOn) {
            status.text = "Flashlight is ON"
            button.text = "TURN OFF"
        } else {
            status.text = "Flashlight is OFF"
            button.text = "TURN ON"
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == CAMERA_PERMISSION_REQUEST &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            toggleFlash()
        } else {
            Toast.makeText(
                this,
                "Camera permission is required to control the flashlight.",
                Toast.LENGTH_LONG
            ).show()
        }
    }

    override fun onStop() {
        super.onStop()
        if (isFlashOn) {
            try {
                cameraId?.let { cameraManager.setTorchMode(it, false) }
            } catch (_: Exception) {
            }
            isFlashOn = false
            updateUi()
        }
    }
}
