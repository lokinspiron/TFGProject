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
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.firebase.storage.FirebaseStorage
import com.inventory.tfgproject.UserRepository
import com.inventory.tfgproject.ProfileUserViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentUserProfileBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.User
import com.inventory.tfgproject.viewmodel.ProfileUserViewModel
import com.inventory.tfgproject.viewmodel.UserViewModel
import java.util.UUID

class UserProfileFragment : Fragment() {
    private var _binding : FragmentUserProfileBinding? = null
    private val binding get() = _binding!!
    private val userViewModel: ProfileUserViewModel by viewModels{
        ProfileUserViewModelFactory(UserRepository())
    }

    private var defaultPictureUrl : String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentUserProfileBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupObservers()
        setupUI()
        userViewModel.loadUserData()
    }

    private fun setupObservers() {
        userViewModel.userData.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                updateUserData(user)
                uploadProfilePicture(user.profilePictureUrl)
            } else {
                showEmptyState()
            }
        }

        userViewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            errorMessage?.let {
                toast(it, LENGTH_SHORT)
                showEmptyState()
            }
        }
    }

    private fun setupUI() {
        binding.apply {
            imgBtnBack.setOnClickListener {
                activity?.supportFragmentManager?.popBackStack()
            }

            btnEditProfile.setOnClickListener {
                (activity as? MainMenu)?.replaceFragment(EditProfileFragment(), "Editar Perfil")
            }
        }
    }

    private fun updateUserData(user: User) {
        binding.apply {
            txtNameUser.text = user.name.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)
            txtSurnameUser.text = user.surname.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)
            txtBirthdayUser.text = user.birthDate?.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)
            txtEmailUser.text = user.email.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)
            txtPhoneUser.text = user.phoneNumber?.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)
            txtAddressUser.text = user.address?.takeIf { it.isNotBlank() } ?: getString(R.string.no_data)
        }
    }

    private fun showEmptyState() {
        binding.apply {
            txtNameUser.text = getString(R.string.no_data)
            txtSurnameUser.text = getString(R.string.no_data)
            txtBirthdayUser.text = getString(R.string.no_data)
            txtEmailUser.text = getString(R.string.no_data)
            txtPhoneUser.text = getString(R.string.no_data)
            txtAddressUser.text = getString(R.string.no_data)
            imgProfilePhoto.setImageResource(R.drawable.ic_user_image)
        }
    }

    private fun uploadProfilePicture(imageUrl: String?) {
        if (imageUrl.isNullOrEmpty()) {
            if (isAdded) {
                Glide.with(requireContext())
                    .load(R.drawable.ic_user_image)
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.error_image)
                    .into(binding.imgProfilePhoto)
            }
            return
        }
        if (isAdded) {
            Glide.with(requireContext())
                .load(imageUrl)
                .placeholder(R.drawable.ic_user_image)
                .error(R.drawable.ic_user_image)
                .into(binding.imgProfilePhoto)
        }
    }


    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}