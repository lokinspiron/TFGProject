package com.inventory.tfgproject.view

import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.inventory.tfgproject.repository.ProviderRepository
import com.inventory.tfgproject.modelFactory.ProviderViewModelFactory
import com.inventory.tfgproject.databinding.FragmentAddProviderBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.viewmodel.ProviderViewModel
import java.util.UUID


class AddProviderFragment : Fragment() {

    private var _binding : FragmentAddProviderBinding? = null
    private val binding get() = _binding!!

    private val providerViewModel : ProviderViewModel by viewModels {
        ProviderViewModelFactory(ProviderRepository())
    }

    private var defaultProviderUrl : String? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uploadProfilePicture(uri)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProviderBinding.inflate(inflater,container,false)
        initListeners()
        initEditText()
        return binding.root
    }

    private fun initListeners(){
        binding.btnAddProvider.setOnClickListener{
            saveProvider()

        }
        binding.imgBtnBack.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnCancel.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.imgProvider.setOnClickListener{
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun saveProvider() {
        if (!validateInputs()) {
            return
        }

        val name = binding.edtNameProviderAdd.text.toString().trim()
        val address = binding.edtAddressProviderAdd.text.toString().trim()
        val email = binding.edtEmailProviderAdd.text.toString().trim()
        val phone = binding.edtPhoneProviderAdd.text.toString().trim()

        if (name.isEmpty()) {
            binding.tilNameProvider.error = "El nombre es requerido"
            binding.edtNameProviderAdd.requestFocus()
            return
        }

        val provider = Providers(
            name = name,
            address = address,
            email = email,
            phoneNumber = phone,
            imageUrl = defaultProviderUrl
        )

        providerViewModel.saveProvider(provider)
        providerViewModel.saveSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                toast("Se ha añadido correctamente al proveedor", LENGTH_SHORT)
                clearForm()
                requireActivity().supportFragmentManager.popBackStack()
            } else {
                toast("Error al guardar el proveedor", LENGTH_SHORT)
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.edtNameProviderAdd.text.toString().trim().isEmpty()) {
            binding.tilNameProvider.error = "El nombre es requerido"
            binding.edtNameProviderAdd.requestFocus()
            isValid = false
        } else {
            binding.tilNameProvider.error = null
        }

        val email = binding.edtEmailProviderAdd.text.toString().trim()
        if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
            binding.tilEmail.error = "Email inválido"
            if (isValid) binding.edtEmailProviderAdd.requestFocus()
            isValid = false
        } else {
            binding.tilEmail.error = null
        }

        val phone = binding.edtPhoneProviderAdd.text.toString().trim()
        if (phone.isNotEmpty() && !phone.matches(Regex("^[0-9]{9}$"))) {
            binding.tilPhone.error = "Teléfono inválido (9 dígitos)"
            if (isValid) binding.edtPhoneProviderAdd.requestFocus()
            isValid = false
        } else {
            binding.tilPhone.error = null
        }

        return isValid
    }

    private fun clearForm() {
        binding.edtNameProviderAdd.text?.clear()
        binding.edtAddressProviderAdd.text?.clear()
        binding.edtEmailProviderAdd.text?.clear()
        binding.edtPhoneProviderAdd.text?.clear()
    }

    private fun uploadProfilePicture(uri: Uri?) {
        if (uri == null) {
            defaultProviderUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.firebasestorage.app/o/default%2Flogo_dstock.png?alt=media&token=afc390aa-dc96-42a5-b96f-1f85c5effa83"
            Log.d("Firebase", "Usando imagen de perfil predeterminada: $defaultProviderUrl")
            toast("Imagen de perfil predeterminada", Toast.LENGTH_SHORT)
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val defaultPictureRef = storageRef.child("default/${UUID.randomUUID()}.jpg")

        defaultPictureRef.putFile(uri)
            .addOnSuccessListener {
                defaultPictureRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    defaultProviderUrl = downloadUri.toString()

                    Glide.with(requireContext())
                        .load(uri)
                        .into(binding.imgProvider)

                    Log.d("Firebase", "Foto subida correctamente: $defaultProviderUrl")
                    toast("Foto de perfil subida con éxito", LENGTH_SHORT)
                }
            }
            .addOnFailureListener { exception ->
                defaultProviderUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.firebasestorage.app/o/default%2Flogo_dstock.png?alt=media&token=afc390aa-dc96-42a5-b96f-1f85c5effa83"
                toast("Error al subir la foto. Usando imagen predeterminada", LENGTH_SHORT)
                Log.e("Firebase", "Error uploading profile picture", exception)
            }
    }

    private fun initEditText(){
        binding.tilNameProvider.helperText = null
        binding.tilAddress.helperText = null
        binding.tilPhone.helperText = null
        binding.tilEmail.helperText = null
    }
}