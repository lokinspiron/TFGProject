package com.inventory.tfgproject.view

import android.graphics.Bitmap
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentEditProfileBinding
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import java.io.ByteArrayOutputStream


class EditProfileFragment : Fragment() {
    val storageRef: StorageReference = FirebaseStorage.getInstance()
        .getReferenceFromUrl("gs://{your-storage-name-from-firebase}.appspot.com")
    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        initListener()
        return binding.root
    }

    private fun initListener() {

    }


}