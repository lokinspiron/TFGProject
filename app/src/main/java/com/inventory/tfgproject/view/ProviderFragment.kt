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
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.viewmodel.ProviderViewModel

class ProviderFragment : Fragment() {
    private var _binding: FragmentProviderBinding? = null
    private val binding get() = _binding!!

    private val providerViewModel: ProviderViewModel by viewModels() {
        ProviderViewModelFactory(ProviderRepository())
    }
    private lateinit var recyclerView: RecyclerView
    private lateinit var providerAdapter: ProviderAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        initVisibility()
        initListeners()
        initViewModel()
    }

    private fun setupRecyclerView() {
        recyclerView = binding.rvProviders
        providerAdapter = ProviderAdapter(
            providers = mutableListOf(),
            onCreateOrderClick = { provider ->
                val dialog = DialogContactProviderFragment.newInstance(provider)
                dialog.show(childFragmentManager, "Contact Provider")
            },
            onProviderClick = { provider ->
                Log.d("ProviderClick", "Clicked provider ${provider.name}")
                navigateToProviderView(provider)
            }
        )
        recyclerView.layoutManager = GridLayoutManager(requireContext(), 2)
        recyclerView.adapter = providerAdapter
    }

    private fun navigateToProviderView(provider: Providers) {
        Log.d("Navigation", "Attempting to navigate to ProviderViewFragment")
        val fragment = ProviderViewFragment.newInstance(provider.id, provider.name)
        (activity as? MainMenu)?.replaceFragment(fragment)
    }

    private fun initViewModel() {
        providerViewModel.providers.observe(viewLifecycleOwner) { providers ->
            Log.d("ProviderFragment", "Providers received: ${providers.size}")

            providerAdapter.updateProviders(providers.toMutableList())

            updateVisibility(providers.isEmpty())
        }
        providerViewModel.loadProviders()
    }

    private fun updateVisibility(isEmpty: Boolean) {
        binding.apply {
            pbProvider.visibility = View.GONE
            rvProviders.visibility = View.VISIBLE
            divider.visibility = View.VISIBLE
            btnAddProvider.visibility = View.VISIBLE

            imgNoContent.visibility = if (isEmpty) View.VISIBLE else View.GONE
            txtEmptyList.visibility = if (isEmpty) View.VISIBLE else View.GONE
            txtAddProviders.visibility = if (isEmpty) View.VISIBLE else View.GONE
        }
    }

    private fun initVisibility() {
        binding.apply {
            pbProvider.visibility = View.VISIBLE
            rvProviders.visibility = View.GONE
            divider.visibility = View.GONE
            btnAddProvider.visibility = View.GONE
            imgNoContent.visibility = View.GONE
            txtAddProviders.visibility = View.GONE
            txtEmptyList.visibility = View.GONE
        }
    }

    private fun initListeners() {
        binding.btnAddProvider.setOnClickListener {
            (activity as? MainMenu)?.replaceFragment(
                AddProviderFragment(), "Añadir Proveedor"
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}