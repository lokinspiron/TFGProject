package com.inventory.tfgproject.view

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.Detector
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.inventory.tfgproject.databinding.FragmentScanCodeBinding
import java.io.IOException


class ScanCodeFragment : Fragment() {
    private var requestCamara : ActivityResultLauncher<String>? = null
    private lateinit var barcodeDetector : BarcodeDetector
    private lateinit var cameraSource : CameraSource
    private var _binding: FragmentScanCodeBinding? = null
    private val binding get() = _binding!!
    var intentData = ""
    private var isCameraInitialized = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCamara = registerForActivityResult(
            ActivityResultContracts.RequestPermission()
        ) { isGranted ->
            if (isGranted) {
                //initScan()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Se necesita permiso para usar la cámara",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanCodeBinding.inflate(inflater,container,false)
        checkAndRequestCameraPermission()
        return binding.root
    }

    private fun checkAndRequestCameraPermission() {
        if (ContextCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            //initScan()
        } else {
            requestCamara?.launch(Manifest.permission.CAMERA)
        }
    }

    /*private fun initScan(){
        if (isCameraInitialized) return

        barcodeDetector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()

        cameraSource = CameraSource.Builder(requireContext(), barcodeDetector)
            .setRequestedPreviewSize(1920, 1080)
            .setAutoFocusEnabled(true)
            .build()
        binding.cameraScan.holder.addCallback(object : SurfaceHolder.Callback {
            override fun surfaceCreated(holder: SurfaceHolder) {
                if (ContextCompat.checkSelfPermission(
                        requireContext(),
                        Manifest.permission.CAMERA
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    try {
                        cameraSource.start(holder)
                        isCameraInitialized = true
                    } catch (e: IOException) {
                        e.printStackTrace()
                    }
                } else {
                    // Handle permission not granted case (e.g., show a toast)
                }
            }

            override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {}

            override fun surfaceDestroyed(holder: SurfaceHolder) {
                cameraSource.stop()
                isCameraInitialized = false
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode> {
            override fun release() {
                Toast.makeText(requireContext(), "El escáner se ha detenido.", Toast.LENGTH_SHORT).show()
            }
            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if (barcodes.size() != 0) {
                    binding.txtBarcode.post {
                        intentData = barcodes.valueAt(0).displayValue
                        binding.txtBarcode.setText(intentData)
                        binding.btnScan.text = "Buscar artículo"
                    }
                }
            }
        })
    }*/



    override fun onPause(){
        super.onPause()
        if (::cameraSource.isInitialized) {
            cameraSource.release()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            //initScan()
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
