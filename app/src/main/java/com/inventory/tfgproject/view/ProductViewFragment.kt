package com.inventory.tfgproject.view

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.viewModels
import com.inventory.tfgproject.ProductRepository
import com.inventory.tfgproject.ProductViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.databinding.FragmentProductViewBinding
import com.inventory.tfgproject.viewmodel.ProductViewModel
import com.bumptech.glide.Glide
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Providers


class ProductViewFragment : Fragment() {

    private var _binding: FragmentProductViewBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(ProductRepository())
    }
    private var productId : String? = null
    private var productName : String? = null

    private var categoriesMap = mutableMapOf<String, Category>()
    private var providersMap = mutableMapOf<String, Providers>()

    companion object{
        fun newInstance(productId: String, productName: String): ProductViewFragment{
            val fragment = ProductViewFragment()
            val args = Bundle()
            args.putString("product_id", productId)
            args.putString("product_name", productName)
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
        _binding = FragmentProductViewBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = arguments?.getString("product_id")
        productName = arguments?.getString("product_name")

        initVisibility()
        initViewModel()
        initListeners()
        loadData()
    }

    private fun initListeners(){
        binding.btnMakeOrder.setOnClickListener {
            productViewModel.products.value?.find { it.id == productId }?.let { product ->
                val dialog = CreateOrderDialogFragment.newInstance(
                    providerId = product.providerId,
                    productId = product.id
                )
                dialog.show(childFragmentManager, "CreateOrder")
            }
        }

        binding.imgEditProduct.setOnClickListener {
            productViewModel.products.value?.find { it.id == productId }?.let { product ->
                (activity as? MainMenu)?.replaceFragment(
                    EditProductFragment.newInstance(
                        productId = product.id,
                        productName = product.name
                    )
                )
            }
        }


    }

    private fun initVisibility(){
        binding.pbViewProduct.visibility = View.VISIBLE
        binding.scViewProduct.visibility = View.GONE
    }

    private fun initViewModel(){
        productViewModel.products.observe(viewLifecycleOwner) { products ->
            products.find { it.id == productId }?.let { product ->
                updateUI(product)
                binding.pbViewProduct.visibility = View.GONE
                binding.scViewProduct.visibility = View.VISIBLE
            }
        }

        productViewModel.categories.observe(viewLifecycleOwner) { categories ->
            categoriesMap.clear()
            categories.forEach { category ->
                categoriesMap[category.id] = category
            }
            productViewModel.products.value?.find { it.id == productId }?.let {
                updateUI(it)
            }
        }

        productViewModel.providers.observe(viewLifecycleOwner) { providers ->
            providersMap.clear()
            providers.forEach { provider ->
                providersMap[provider.id] = provider
            }
            productViewModel.products.value?.find { it.id == productId }?.let {
                updateUI(it)
            }
        }
    }

    private fun loadData() {
        productViewModel.loadProducts()
        productViewModel.loadCategories()
        productViewModel.loadProviders()
    }

    private fun updateUI(product: Product) {
        with(binding) {
            txtNameProduct.text = getString(R.string.product_name_format,product.name)
            txtQuantityProduct.text = getString(R.string.product_quantity_format, product.stock)
            txtPriceperProduct.text = getString(R.string.product_price_format, product.price,product.currencyUnit)
            txtWeightperProduct.text = getString(R.string.product_weight_format, product.weight, product.weightUnit)

            val category = categoriesMap[product.categoryId]
            txtCategoryProduct.text = getString(
                R.string.product_category_format,
                category?.name ?: "N/A"
            )

            val provider = providersMap[product.providerId]
            txtProviderProduct.text = getString(
                R.string.product_provider_format,
                provider?.name ?: "N/A"
            )

            if (product.subcategoryId.isNotEmpty()) {
                txtSubcategoryProduct.visibility = View.VISIBLE
                val subcategoryName = category?.subcategory?.get(product.subcategoryId)?.name
                Log.d("ProductView", "SubcategoryId: ${product.subcategoryId}")
                Log.d("ProductView", "Category: ${category?.name}")
                Log.d("ProductView", "Subcategories: ${category?.subcategory}")
                Log.d("ProductView", "Found subcategory name: $subcategoryName")

                txtSubcategoryProduct.text = getString(
                    R.string.product_subcategory_format,
                    subcategoryName ?: "N/A"
                )
            } else {
                txtSubcategoryProduct.text = "N/A"
            }

            if (product.imageUrl?.isNotEmpty() == true) {
                Glide.with(requireContext())
                    .load(product.imageUrl)
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