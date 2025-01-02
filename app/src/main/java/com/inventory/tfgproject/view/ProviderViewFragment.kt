package com.inventory.tfgproject.view

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.inventory.tfgproject.ProviderRepository
import com.inventory.tfgproject.ProviderViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryBinding
import com.inventory.tfgproject.databinding.FragmentProductViewBinding
import com.inventory.tfgproject.databinding.FragmentProviderViewBinding
import com.inventory.tfgproject.databinding.FragmentSettingsBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.viewmodel.ProviderViewModel

class ProviderViewFragment : Fragment() {
    private var _binding: FragmentProviderViewBinding? = null
    private val binding get() = _binding!!

    private val providerViewModel: ProviderViewModel by viewModels {
        ProviderViewModelFactory(ProviderRepository())
    }

    private var providerId: String? = null
    private var providerName: String? = null
    private var selectedProductId: String? = null

    companion object {
        fun newInstance(providerId: String, providerName: String): ProviderViewFragment {
            val fragment = ProviderViewFragment()
            val args = Bundle()
            args.putString("provider_id", providerId)
            args.putString("provider_name", providerName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProviderViewBinding.inflate(inflater, container, false)
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

    private fun initListeners() {
        binding.imgBtnBack.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.imgEditProduct.setOnClickListener {
            providerId?.let { id ->

            }
        }

        binding.btnMakeOrder.setOnClickListener {
            providerId?.let { id ->
                if (selectedProductId != null) {
                    val dialog = CreateOrderDialogFragment.newInstance(
                        providerId = id,
                        productId = selectedProductId!!
                    )
                    dialog.show(childFragmentManager, "CreateOrder")
                } else {
                    toast("Por favor seleccione un producto", LENGTH_SHORT)
                }
            }
        }

        binding.btnLookProduct.setOnClickListener {
            selectedProductId?.let { productId ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fcvContent, ProductViewFragment.newInstance(productId, ""))
                    .addToBackStack(null)
                    .commit()
            }
        }

        binding.imgEditProduct.setOnClickListener{
            providerId?.let { id ->
                parentFragmentManager.beginTransaction()
                    .replace(R.id.fcvContent, EditProviderFragment.newInstance(id, providerName ?: ""))
                    .addToBackStack(null)
                    .commit()
            }
        }

    }

    private fun setupProductsSpinner(products: List<Product>) {
        val productNames = products.map { it.name }

        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.convergence)

        val adapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            productNames
        ){
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.text = productNames[position]
                textView.setTextColor(Color.BLACK)
                textView.typeface = customTypeface
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.text = productNames[position]
                textView.typeface = customTypeface
                return textView
            }
        }
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProduct.adapter = adapter

        binding.spinnerProduct.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                selectedProductId = products[position].id
                binding.btnLookProduct.isEnabled = true
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                selectedProductId = null
                binding.btnLookProduct.isEnabled = false
            }
        }
    }

    private fun initVisibility() {
        binding.pbProvider.visibility = View.VISIBLE
        binding.scViewProvider.visibility = View.GONE
    }

    private fun initViewModel() {
        providerViewModel.providers.observe(viewLifecycleOwner) { providers ->
            binding.pbProvider.visibility = View.GONE
            binding.scViewProvider.visibility = View.VISIBLE
            providers.find { it.id == providerId }?.let { provider ->
                updateUI(provider)
            } ?: run {
                binding.txtNameProvider.text = "Sin datos"
                binding.txtAddressProvider.text = "Sin datos"
                binding.txtEmail.text = "Sin datos"
                binding.txtPhone.text = "Sin datos"
            }
        }

        providerViewModel.providerProducts.observe(viewLifecycleOwner) { products ->
            setupProductsSpinner(products)
        }
    }

    private fun loadData() {
        providerViewModel.loadProviders()
        providerId?.let { id ->
            providerViewModel.loadProviderProducts(id)
        }
    }

    private fun updateUI(provider: Providers) {
        with(binding) {
            txtNameProvider.text = provider.name
            txtAddressProvider.text = if (provider.address.isNullOrEmpty()) "Sin datos" else provider.address
            txtEmail.text = if (provider.email.isNullOrEmpty()) "Sin datos" else provider.email
            txtPhone.text = if (provider.phoneNumber.isNullOrEmpty()) "Sin datos" else provider.phoneNumber

            if (provider.imageUrl?.isNotEmpty() == true) {
                Glide.with(requireContext())
                    .load(provider.imageUrl)
                    .placeholder(R.drawable.loading_image)
                    .error(R.drawable.error_image)
                    .into(imgProduct)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}