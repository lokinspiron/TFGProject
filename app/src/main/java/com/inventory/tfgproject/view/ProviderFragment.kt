package com.inventory.tfgproject.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.ProviderAdapter
import com.inventory.tfgproject.ProviderRepository
import com.inventory.tfgproject.ProviderViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentProviderBinding
import com.inventory.tfgproject.viewmodel.ProviderViewModel

class ProviderFragment : Fragment() {
    private var _binding: FragmentProviderBinding? = null
    private val binding get() = _binding!!

    private val providerViewModel : ProviderViewModel by viewModels(){
        ProviderViewModelFactory(ProviderRepository())
    }
    private lateinit var recyclerView : RecyclerView
    private lateinit var providerAdapter : ProviderAdapter


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.rvProviders
        providerAdapter = ProviderAdapter(
            mutableListOf()
        ) { provider ->
            Log.d("ProviderClick", "Clicked provider ${provider.name}")

        }

        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        recyclerView.adapter = providerAdapter

        initVisibility()
        initListeners()
        initViewModel()

    }

    private fun initViewModel() {
        providerViewModel.providers.observe(viewLifecycleOwner, Observer { providers ->
            Log.d("ProviderFragment", "Providers received: ${providers.size}")

            providerAdapter = ProviderAdapter(providers.toMutableList()) { provider ->
                Log.d("ProviderClick", "Clicked provider ${provider.name}")
            }
            recyclerView.adapter = providerAdapter

            binding.pbProvider.visibility = View.GONE
            binding.rvProviders.visibility = View.VISIBLE
            binding.divider.visibility = View.VISIBLE
            binding.btnAddProvider.visibility = View.VISIBLE

            if(providers.size == 0){
                binding.imgNoContent.visibility = View.VISIBLE
                binding.txtEmptyList.visibility = View.VISIBLE
                binding.txtAddProviders.visibility = View.VISIBLE
            } else {
                binding.imgNoContent.visibility = View.GONE
                binding.txtEmptyList.visibility = View.GONE
                binding.txtAddProviders.visibility = View.GONE
            }
        })
        providerViewModel.loadProviders()
    }

    private fun initVisibility(){
        binding.pbProvider.visibility = View.VISIBLE
        binding.rvProviders.visibility = View.GONE
        binding.divider.visibility = View.GONE
        binding.btnAddProvider.visibility = View.GONE
        binding.imgNoContent.visibility = View.GONE
        binding.txtAddProviders.visibility = View.GONE
        binding.txtEmptyList.visibility = View.GONE
    }

    private fun initListeners() {
        binding.btnAddProvider.setOnClickListener{
            (activity as? MainMenu)?.replaceFragment(
                AddProviderFragment(),"Añadir Proveedor")
        }
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentProviderBinding.inflate(inflater,container,false)
        initListeners()
        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}