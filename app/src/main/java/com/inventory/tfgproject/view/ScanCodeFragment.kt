package com.inventory.tfgproject.view

import android.Manifest
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.SurfaceHolder
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
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

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }


    private fun initBc(){
        barcodeDetector = BarcodeDetector.Builder(requireContext())
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(requireContext(),barcodeDetector)
            .setRequestedPreviewSize(1920,1080)
            .setAutoFocusEnabled(true)
            //.setFacing(CameraSource.CAMERA_FACING_FRONT)
            .build()
        binding.cameraScan.holder.addCallback(object : SurfaceHolder.Callback{
            @SuppressLint("MissingPermission")
            override fun surfaceCreated(p0: SurfaceHolder){
                try{
                    cameraSource.start(binding.cameraScan.holder)
                }catch(e: IOException){
                    e.printStackTrace()
                }
            }

            override fun surfaceChanged(p0: SurfaceHolder, p1: Int, p2: Int, p3: Int) {

            }
            override fun surfaceDestroyed(p0: SurfaceHolder) {
                cameraSource.stop()
            }
        })

        barcodeDetector.setProcessor(object : Detector.Processor<Barcode>{
            override fun release() {
                Toast.makeText(requireContext(),"barcode scanner has been stopped",Toast.LENGTH_SHORT).show()
            }

            override fun receiveDetections(detections: Detector.Detections<Barcode>) {
                val barcodes = detections.detectedItems
                if(barcodes.size()!=0){
                    binding.txtBarcode.post{
                        binding.btnScan.text = "SEARCH_ITEM"
                        intentData = barcodes.valueAt(0).displayValue
                        binding.txtBarcode.setText(intentData)
                        //finish()
                    }
                }
            }

        })

    }

    override fun onPause(){
        super.onPause()
        if (::cameraSource.isInitialized) {
            cameraSource.release()
        }
    }

    override fun onResume() {
        super.onResume()
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            initBc()
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanCodeBinding.inflate(inflater,container,false)
        initRequest()
        return binding.root
    }

    private fun initRequest(){
        requestCamara = registerForActivityResult(
            ActivityResultContracts.RequestPermission(),) { it ->
            if (it) {
                initScan()
            } else {
                Toast.makeText(
                    requireActivity(),
                    "Camera permission is required",
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun initScan(){
        requestCamara?.launch(Manifest.permission.CAMERA)
        initBc()
    }

}
