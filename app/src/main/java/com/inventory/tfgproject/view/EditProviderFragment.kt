package com.inventory.tfgproject.view

import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.inventory.tfgproject.repository.ProviderRepository
import com.inventory.tfgproject.modelFactory.ProviderViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentEditProviderBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.viewmodel.ProviderViewModel
import java.util.UUID

class EditProviderFragment : Fragment() {
    private var _binding: FragmentEditProviderBinding? = null
    private val binding get() = _binding!!

    private val providerViewModel: ProviderViewModel by viewModels {
        ProviderViewModelFactory(ProviderRepository())
    }

    private var providerId: String? = null
    private var providerName: String? = null
    private var defaultPictureUrl: String? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uploadProfilePicture(uri)
    }

    companion object {
        fun newInstance(providerId: String, providerName: String): EditProviderFragment {
            val fragment = EditProviderFragment()
            val args = Bundle()
            args.putString("provider_id", providerId)
            args.putString("provider_name", providerName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProviderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        providerId = arguments?.getString("provider_id")
        providerName = arguments?.getString("provider_name")

        initVisibility()
        initViewModel()
        initListeners()
        loadData()
    }

    private fun initVisibility() {
        binding.pbEditProduct.visibility = View.VISIBLE
        binding.scViewProduct.visibility = View.GONE
    }

    private fun initViewModel() {
        providerViewModel.providers.observe(viewLifecycleOwner) { providers ->
            providers.find { it.id == providerId }?.let { provider ->
                updateUI(provider)
                binding.pbEditProduct.visibility = View.GONE
                binding.scViewProduct.visibility = View.VISIBLE
            }
        }

        providerViewModel.deleteProviderStatus.observe(viewLifecycleOwner) { success ->
            if (success) {
                toast("Proveedor eliminado exitosamente", LENGTH_SHORT)
            } else {
                toast("Error al eliminar el proveedor", LENGTH_SHORT)
                binding.pbEditProduct.visibility = View.GONE
            }
        }
    }

    private fun initListeners() {
        binding.imgBtnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.imgProvider.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSaveChanges.setOnClickListener {
            if (validateInputs()) {
                showSaveConfirmationDialog()
            } else {
                toast("Por favor, corrija los errores", LENGTH_SHORT)
            }
        }

        binding.btnDelete.setOnClickListener {
            showDeleteDialog()
        }
    }

    private fun showDeleteDialog() {
        val dialog = DialogSafeChangeFragment.newInstance(
            "Estás a punto de eliminar este proveedor",
            "Eliminar"
        )
        dialog.onDoItClick = {
            deleteProvider()
        }
        dialog.show(parentFragmentManager, "DeleteProviderDialog")
    }

    private fun deleteProvider() {
        binding.pbEditProduct.visibility = View.VISIBLE
        providerId?.let { id ->
            providerViewModel.deleteProvider(id)
        }
        Handler(Looper.getMainLooper()).postDelayed({
            (activity as? MainMenu)?.replaceFragment(ProviderFragment())
        }, 500)
    }

    private fun showSaveConfirmationDialog() {
        val dialog = DialogSafeChangeFragment.newInstance(
            "¿Guardar cambios?",
            "Guardar"
        )
        dialog.onDoItClick = {
            saveChanges()
            (activity as? MainMenu)?.replaceFragment(ProviderFragment())
        }
        dialog.show(parentFragmentManager, "SaveChangesDialog")
    }


    private fun loadData() {
        providerViewModel.loadProviders()
    }

    private fun updateUI(provider: Providers) {
        with(binding) {
            edtNameProvider.setText(provider.name)
            edtAddressProvider.setText(provider.address)
            edtEmailProvider.setText(provider.email)
            edtPhoneProvider.setText(provider.phoneNumber)

            if (provider.imageUrl?.isNotEmpty() == true) {
                Glide.with(requireContext())
                    .load(provider.imageUrl)
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.error_image)
                    .into(imgProvider)
            }
        }
    }

    private fun uploadProfilePicture(uri: Uri?) {
        if (uri == null) {
            defaultPictureUrl = "https://firebasestorage.googleapis.com/v0/b/your-default-image-url"
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val pictureRef = storageRef.child("providers/${UUID.randomUUID()}.jpg")

        pictureRef.putFile(uri)
            .addOnSuccessListener {
                pictureRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    defaultPictureUrl = downloadUri.toString()
                    if (isAdded && context != null) {
                        Glide.with(requireContext())
                            .load(uri)
                            .into(binding.imgProvider)
                    }
                }
            }
            .addOnFailureListener { exception ->
                Log.e("Firebase", "Error uploading image", exception)
                toast("Error al subir la imagen", LENGTH_SHORT)
            }
    }

    private fun saveChanges() {
        val updates = mutableMapOf<String, Any>()
        binding.apply {
            updates["name"] = edtNameProvider.text.toString()
            updates["address"] = edtAddressProvider.text.toString()
            updates["email"] = edtEmailProvider.text.toString()
            updates["phoneNumber"] = edtPhoneProvider.text.toString()

            if (defaultPictureUrl != null) {
                updates["imageUrl"] = defaultPictureUrl!!
            }
        }

        providerId?.let { id ->
            providerViewModel.updateProvider(id, updates)
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true
        binding.apply {
            if (edtNameProvider.text.toString().isBlank()) {
                edtNameProvider.error = "El nombre no puede estar vacío"
                isValid = false
            }

            val email = edtEmailProvider.text.toString()
            if (email.isNotEmpty() && !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                edtEmailProvider.error = "Formato de correo electrónico inválido"
                isValid = false
            }

            val phone = edtPhoneProvider.text.toString()
            if (phone.isNotEmpty() && !phone.matches(Regex("^[0-9]{9}$"))) {
                edtPhoneProvider.error = "El número debe tener 9 dígitos"
                isValid = false
            }

            val address = edtAddressProvider.text.toString()
            if (address.isNotEmpty() && address.length < 5) {
                edtAddressProvider.error = "La dirección debe tener al menos 5 caracteres"
                isValid = false
            }
        }
        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}