package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryBinding

private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"


class Inventory_Fragment : Fragment() {
    private var param1: String? = null
    private var param2: String? = null

    private var _binding: FragmentInventoryBinding? = null
    private val binding get() = _binding!!

    private val rotateOpen: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_open_anim)}
    private val rotateClose: Animation by lazy {AnimationUtils.loadAnimation(requireContext(),R.anim.rotate_close_anim)}
    private val fromBottom: Animation by lazy { AnimationUtils.loadAnimation(requireContext(),R.anim.from_bottom_anim)}
    private val toBottom: Animation by lazy {AnimationUtils.loadAnimation(requireContext(),R.anim.to_bottom_anim)}

    private var clicked = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentInventoryBinding.inflate(inflater,container,false)
        initListeners()
        return binding.root
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
            Toast.makeText(requireContext(),"Añadir Productos",Toast.LENGTH_SHORT).show()
        }
        binding.fabEditProducts.setOnClickListener {
            Toast.makeText(requireContext(),"Editar Productos",Toast.LENGTH_SHORT).show()
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

    companion object {
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            Inventory_Fragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }
}