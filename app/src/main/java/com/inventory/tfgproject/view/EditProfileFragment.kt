package com.inventory.tfgproject.view

import android.graphics.Bitmap
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.inventory.tfgproject.ProfileUserViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.UserRepository
import com.inventory.tfgproject.databinding.FragmentEditProfileBinding
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.model.User
import com.inventory.tfgproject.viewmodel.ProfileUserViewModel
import java.io.ByteArrayOutputStream


class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: ProfileUserViewModel by viewModels{
        ProfileUserViewModelFactory(UserRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupUI()
        userViewModel.loadUserData()
    }

    private fun setupObservers() {
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            user?.let {
                updateUserData(it)
                uploadProfilePicture(it.profilePictureUrl)
            }
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }

        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSaveProfile.isEnabled = !isLoading
        }
    }

    private fun setupUI() {
        binding.apply {
            imgBtnBack.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }

            btnSaveProfile.setOnClickListener {
                if (validateFields()) {
                    saveUserData()
                }
            }

            btnDelete.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }

            edtBirthdayUser.setOnClickListener {
                if (edtBirthdayUser.isEnabled) {
                    showDatePicker()
                }
            }
        }
    }

    private fun validateFields(): Boolean {
        binding.apply {
            if (edtEmailUser.text.toString().trim().isEmpty()) {
                edtEmailUser.error = "El email no puede estar vacío"
                edtEmailUser.requestFocus()
                return false
            }
            if (!isValidEmail(edtEmailUser.text.toString().trim())) {
                edtEmailUser.error = "Email inválido"
                edtEmailUser.requestFocus()
                return false
            }

            if (edtPhoneUser.text.toString().trim().isNotEmpty() && !isValidPhone(edtPhoneUser.text.toString().trim())) {
                edtPhoneUser.error = "Número de teléfono inválido"
                edtPhoneUser.requestFocus()
                return false
            }

            if (edtAddressUser.text.toString().trim().isEmpty()) {
                edtAddressUser.error = "La dirección no puede estar vacía"
                edtAddressUser.requestFocus()
                return false
            }

            return true
        }
    }

    private fun updateUserData(user: User) {
        binding.apply {
            edtNameUser.setText(user.name)
            edtSurnameUser.setText(user.surname)
            edtBirthdayUser.setText(user.birthDate)
            edtEmailUser.setText(user.email)
            edtPhoneUser.setText(user.phoneNumber)
            edtAddressUser.setText(user.address)

            edtNameUser.isEnabled = user.name.isBlank()
            edtSurnameUser.isEnabled = user.surname.isBlank()
            edtBirthdayUser.isEnabled = user.birthDate.isNullOrBlank()

            edtEmailUser.isEnabled = true
            edtPhoneUser.isEnabled = true
            edtAddressUser.isEnabled = true
        }
    }

    private fun uploadProfilePicture(imageUrl: String?) {
        if (isAdded) {
            Glide.with(requireContext())
                .load(imageUrl ?: R.drawable.ic_user_image)
                .placeholder(R.drawable.loading_image)
                .error(R.drawable.ic_user_image)
                .into(binding.imgProfilePhoto)
        }
    }

    private fun saveUserData() {
        val currentUser = userViewModel.userData.value ?: return

        val updatedUser = currentUser.copy(
            email = binding.edtEmailUser.text.toString().trim(),
            phoneNumber = binding.edtPhoneUser.text.toString().trim(),
            address = binding.edtAddressUser.text.toString().trim()
        )

        userViewModel.updateUserData(updatedUser)
    }

    private fun showDatePicker() {

    }

    private fun isValidEmail(email: String): Boolean {
        val pattern = Patterns.EMAIL_ADDRESS
        return pattern.matcher(email).matches()
    }

    private fun isValidPhone(phone: String): Boolean {
        val pattern = "^[0-9]{9}$|^[0-9]{3}[-\\s][0-9]{3}[-\\s][0-9]{3}$"
        return phone.matches(Regex(pattern))
    }

    private fun String.cleanPhoneNumber(): String {
        return this.replace("[^0-9]".toRegex(), "")
    }



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

}