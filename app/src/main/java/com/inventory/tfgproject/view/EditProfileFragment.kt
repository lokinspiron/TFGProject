package com.inventory.tfgproject.view

import android.app.DatePickerDialog
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.android.gms.tasks.OnFailureListener
import com.google.android.gms.tasks.OnSuccessListener
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.inventory.tfgproject.ProfileUserViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.UserRepository
import com.inventory.tfgproject.databinding.FragmentEditProfileBinding
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.User
import com.inventory.tfgproject.viewmodel.ProfileUserViewModel
import java.io.ByteArrayOutputStream
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.UUID


class EditProfileFragment : Fragment() {

    private var _binding: FragmentEditProfileBinding? = null
    private val binding get() = _binding!!

    private val userViewModel: ProfileUserViewModel by viewModels{
        ProfileUserViewModelFactory(UserRepository())
    }

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        if (uri != null) {
            uploadProfilePicture(uri)
        }
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

        binding.imgBtnCamera.setOnClickListener {
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                toast(it, LENGTH_SHORT)
            }
        }

        userViewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.btnSaveProfile.isEnabled = !isLoading
        }

        userViewModel.updateSuccess.observe(viewLifecycleOwner) { success ->
            if (success) {
                toast("Profile updated successfully", LENGTH_SHORT)
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            imgBtnBack.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }

            binding.imgBtnCamera.setOnClickListener {
                pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
            }

            edtNameUser.setOnClickListener {
                if (!edtNameUser.isEnabled) {
                    showNonEditableFieldToast()
                }
            }
            tilUsernameContainer.setOnClickListener {
                if (!edtNameUser.isEnabled) {
                    showNonEditableFieldToast()
                }
            }

            edtSurnameUser.setOnClickListener {
                if (!edtSurnameUser.isEnabled) {
                    showNonEditableFieldToast()
                }
            }
            tilSurnameContainer.setOnClickListener {
                if (!edtSurnameUser.isEnabled) {
                    showNonEditableFieldToast()
                }
            }

            tilBirthdayContainer.setOnClickListener {
                if (!edtBirthdayUser.isEnabled) {
                    showNonEditableFieldToast()
                } else {
                    showDatePicker()
                }
            }

            edtBirthdayUser.setOnClickListener {
                if (!edtBirthdayUser.isEnabled) {
                    showNonEditableFieldToast()
                } else {
                    showDatePicker()
                }
            }

            btnSaveProfile.setOnClickListener {
                if (validateFields()) {
                    saveUserData()
                } else {
                    Log.d("EditProfileFragment", "Validation failed")
                }
            }

            btnDelete.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }
        }
    }

    private fun validateFields(): Boolean {
        binding.apply {
            if (edtBirthdayUser.text.toString().trim().isNotEmpty()) {
                val today = Calendar.getInstance()
                val minimumAge = 18
                today.add(Calendar.YEAR, -minimumAge)

                val birthDate = try {
                    val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    sdf.parse(edtBirthdayUser.text.toString().trim())?.time ?: 0
                } catch (e: Exception) {
                    0
                }

                if (birthDate > today.timeInMillis) {
                    edtBirthdayUser.error = "Debes ser mayor de $minimumAge años"
                    edtBirthdayUser.requestFocus()
                    return false
                }
            }

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

            edtBirthdayUser.isEnabled = user.birthDate.isNullOrBlank()

            edtNameUser.isEnabled = user.name.isBlank()
            edtSurnameUser.isEnabled = user.surname.isBlank()
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
        Log.d("EditProfileFragment", "saveUserData() called")

        val currentUser = userViewModel.userData.value
        if (currentUser == null) {
            Log.e("EditProfileFragment", "currentUser is null")
            return
        }

        val updatedUser = currentUser.copy(
            email = binding.edtEmailUser.text.toString().trim(),
            phoneNumber = binding.edtPhoneUser.text.toString().trim(),
            address = binding.edtAddressUser.text.toString().trim(),
            birthDate = binding.edtBirthdayUser.text.toString().trim()
        )

        userViewModel.updateUserData(updatedUser)
    }

    private fun uploadProfilePicture(uri: Uri) {
        val storageRef = FirebaseStorage.getInstance().reference
        val imageRef = storageRef.child("profile_pictures/${UUID.randomUUID()}.jpg")

        binding.imgProfilePhoto.setImageResource(R.drawable.loading_image)

        imageRef.putFile(uri)
            .addOnSuccessListener {
                imageRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    if (isAdded) {
                        userViewModel.updateProfilePicture(downloadUri.toString())

                        Glide.with(requireContext())
                            .load(uri)
                            .placeholder(R.drawable.loading_image)
                            .error(R.drawable.ic_user_image)
                            .into(binding.imgProfilePhoto)

                        toast("Imagen subida con éxito", LENGTH_SHORT)
                    }
                }
            }
            .addOnFailureListener { exception ->
                if (isAdded) {
                    toast("Error al subir la imagen", LENGTH_SHORT)
                    Log.e("ProfilePicture", "Error uploading image", exception)

                    Glide.with(requireContext())
                        .load(R.drawable.ic_user_image)
                        .into(binding.imgProfilePhoto)
                }
            }
    }

    private fun showDatePicker() {
        val calendar = Calendar.getInstance()

        val currentDate = binding.edtBirthdayUser.text.toString()
        if (currentDate.isNotEmpty()) {
            try {
                val sdf = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                val date = sdf.parse(currentDate)
                date?.let {
                    calendar.time = it
                }
            } catch (e: Exception) {
                Log.e("DatePicker", "Error parsing date", e)
            }
        }

        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePickerDialog = DatePickerDialog(
            requireContext(),
            { _, selectedYear, selectedMonth, selectedDay ->
                val selectedDate = Calendar.getInstance()
                selectedDate.set(selectedYear, selectedMonth, selectedDay)

                val today = Calendar.getInstance()
                var age = today.get(Calendar.YEAR) - selectedYear
                if (today.get(Calendar.DAY_OF_YEAR) < selectedDate.get(Calendar.DAY_OF_YEAR)) {
                    age--
                }

                if (age >= 18) {
                    val dateFormat = SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                    val formattedDate = dateFormat.format(selectedDate.time)
                    binding.edtBirthdayUser.setText(formattedDate)
                } else {
                    toast("Debes ser mayor de 18 años", Toast.LENGTH_SHORT)
                }
            },
            year,
            month,
            day
        )

        val maxDate = Calendar.getInstance()
        maxDate.add(Calendar.YEAR, -18)
        datePickerDialog.datePicker.maxDate = maxDate.timeInMillis

        val minDate = Calendar.getInstance()
        minDate.add(Calendar.YEAR, -100)
        datePickerDialog.datePicker.minDate = minDate.timeInMillis

        datePickerDialog.show()
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

    private fun showNonEditableFieldToast() {
        toast("Este campo no se puede editar", LENGTH_SHORT)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentEditProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

}