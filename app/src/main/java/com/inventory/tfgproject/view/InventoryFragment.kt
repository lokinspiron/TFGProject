package com.inventory.tfgproject.view

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryBinding


class InventoryFragment : Fragment() {

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_open_anim)}
    private val rotateClose: Animation by lazy {AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_close_anim)}
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.from_bottom_anim)}
    private val toBottom: Animation by lazy {AnimationUtils.loadAnimation(requireContext(),R.anim.to_bottom_anim)}

    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initListeners()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun initListeners() {
        binding.fabProducts.setOnClickListener{
            onAddButtonClicked()
        }
        binding.fabAddProducts.setOnClickListener {
            (activity as? MainMenu)?.replaceFragment(AddProductFragment(), " Añadir Producto")

        }
        binding.fabEditProducts.setOnClickListener {

        }
        binding.btnAddCategory.setOnClickListener {
            val dialogFragment: DialogFragment = DialogCreateCategoryFragment()
            dialogFragment.show(childFragmentManager, "CreateCategory")
        }
    }


    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked : Boolean) {
        if(!clicked){
            binding.fabEditProducts.startAnimation(fromBottom)
            binding.fabAddProducts.startAnimation(fromBottom)
            binding.fabProducts.startAnimation(rotateOpen)
        }else {
            binding.fabEditProducts.startAnimation(toBottom)
            binding.fabAddProducts.startAnimation(toBottom)
            binding.fabProducts.startAnimation(rotateClose)
        }

    }

    private fun setVisibility(clicked: Boolean) {
        if(!clicked){
            binding.fabAddProducts.visibility = View.VISIBLE
            binding.fabEditProducts.visibility = View.VISIBLE
        }else {
            binding.fabAddProducts.visibility = View.INVISIBLE
            binding.fabEditProducts.visibility = View.INVISIBLE
        }
    }

    private fun setClickable(clicked: Boolean){
        if(!clicked){
            binding.fabEditProducts.isClickable = true
            binding.fabAddProducts.isClickable = true
        }else {
            binding.fabEditProducts.isClickable = false
            binding.fabAddProducts.isClickable = false
        }
    }

    private fun replaceFragment(fragment: Fragment, greetingMessage:String? = null) {
        val fragmentTag = fragment.javaClass.simpleName

        val fragmentTransaction = parentFragmentManager.beginTransaction()

        val existingFragment = parentFragmentManager.findFragmentByTag(fragmentTag)

        if (existingFragment == null) {
            fragmentTransaction.replace(R.id.fcvContent, fragment, fragmentTag)
            fragmentTransaction.commit()
        } else {
            parentFragmentManager.beginTransaction()
                .show(existingFragment)
                .commit()
        }
        greetingMessage?.let {
            binding.txtWave.text = it
        }
    }
}