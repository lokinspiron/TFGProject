package com.inventory.tfgproject.view

import android.app.AlertDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.gms.vision.CameraSource
import com.google.android.gms.vision.barcode.Barcode
import com.google.android.gms.vision.barcode.BarcodeDetector
import com.inventory.tfgproject.CaptureAct
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ActivityRegisterScreenInfoBinding
import com.inventory.tfgproject.databinding.FragmentProviderBinding
import com.inventory.tfgproject.databinding.FragmentScanCodeBinding
import com.journeyapps.barcodescanner.ScanOptions


class ScanCodeFragment : Fragment() {
    private var requestCamara : ActivityResultLauncher<String>? = null
    private lateinit var barcodeDetector : BarcodeDetector
    private lateinit var cameraSource : CameraSource
    private var _binding: FragmentScanCodeBinding? = null
    private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        requestCamara = registerForActivityResult(ActivityResultContracts
            .RequestPermission(),) {
            if(it) {

            }else {
                Toast.makeText(requireActivity(),"Permiso no dado",Toast.LENGTH_SHORT).show()
            }
        }
        initListener()

    }

    private fun initListener() {
        binding.btnScan.setOnClickListener(){
            requestCamara?.launch(android.Manifest.permission.CAMERA)
        }
    }

    private fun iniBc(){
        barcodeDetector = BarcodeDetector.Builder(this)
            .setBarcodeFormats(Barcode.ALL_FORMATS)
            .build()
        cameraSource = CameraSource.Builder(this,barcodeDetector)
            .setRequestedPreviewSize(1920,1080)
            .setAutoFocusEnabled(true)
            .setFacing(CameraSource.CAMERA_FACING_FRONT)
            .build()
        binding.surfaceView!!.holder.addCallback(object : )
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentScanCodeBinding.inflate(inflater,container,false)
        return binding.root
    }
}
