package com.inventory.tfgproject.view

import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast.LENGTH_LONG
import android.widget.Toast.LENGTH_SHORT
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.ExcelExporter
import com.inventory.tfgproject.Manifest
import com.inventory.tfgproject.ProductAdapter
import com.inventory.tfgproject.ProductRepository
import com.inventory.tfgproject.ProductViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentInventoryMenuBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.viewmodel.ProductViewModel

class InventoryMenuFragment : Fragment() {
    private var _binding: FragmentInventoryMenuBinding? = null
    private val binding get() = _binding!!

    private val productViewModel: ProductViewModel by viewModels() {
        ProductViewModelFactory(ProductRepository())
    }

    private lateinit var recyclerView : RecyclerView
    private lateinit var productAdapter : ProductAdapter

    private lateinit var excelExporter: ExcelExporter

    private val rotateOpen: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_open_anim
        )
    }
    private val rotateClose: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.rotate_close_anim
        )
    }
    private val fromBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.from_bottom_anim
        )
    }
    private val toBottom: Animation by lazy {
        AnimationUtils.loadAnimation(
            requireContext(),
            R.anim.to_bottom_anim
        )
    }

    private var clicked = false

    private var categoryId: String? = null
    private var categoryName: String? = null

    private var subcategoryId: String? = null
    private var subcategoryName: String? = null

    companion object {
        private const val STORAGE_PERMISSION_CODE = 100

        fun newInstanceForCategory(
            categoryId: String,
            categoryName: String
        ): InventoryMenuFragment {
            val fragment = InventoryMenuFragment()
            val args = Bundle()
            args.putString("category_id", categoryId)
            args.putString("category_name", categoryName)
            args.putBoolean("is_category", true)
            fragment.arguments = args
            return fragment
        }

        fun newInstanceForSubcategory(
            subcategoryId: String?,
            subcategoryName: String?
        ): InventoryMenuFragment {
            val fragment = InventoryMenuFragment()
            val args = Bundle()
            args.putString("subcategory_id", subcategoryId)
            args.putString("subcategory_name", subcategoryName)
            fragment.arguments = args
            return fragment
        }


    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        categoryId = arguments?.getString("category_id")
        categoryName = arguments?.getString("category_name")
        subcategoryId = arguments?.getString("subcategory_id")
        subcategoryName = arguments?.getString("subcategory_name")
    }


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentInventoryMenuBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        recyclerView = binding.rvProducts
        productAdapter = ProductAdapter(
            mutableListOf(),
            onProductClick = { product ->
                Log.d("ProductClick", "Clicked product ${product.name}")
                (activity as? MainMenu)?.replaceFragment(
                    ProductViewFragment.newInstance(product.id,product.name))
            },
            onQuantityChanged = { product, newQuantity ->
                productViewModel.updateProductQuantity(product.id, newQuantity)
                productAdapter.updateProduct(product.id, newQuantity)
            },
            onButtonProductClick = { product ->
                val dialog = CreateOrderDialogFragment.newInstance(
                    providerId = product.providerId,
                    productId = product.id
                )
                dialog.show(childFragmentManager, "CreateOrder")
            },
            onDeleteClick = { product ->
                showDeleteConfirmationDialog(product)
            }
        )

        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)
        recyclerView.adapter = productAdapter

        initVisibility()
        initListener()
        initViewModel()
    }

    private fun showDeleteConfirmationDialog(product: Product) {
        val deleteDialog = DialogSafeChangeFragment.newInstance(
            dynamicText = "¿Estás seguro de que deseas eliminar este producto?",
            doItText = "Eliminar Producto"
        )
        deleteDialog.setOnPositiveClickListener {
            productViewModel.deleteProduct(product.id)
        }
        deleteDialog.show(childFragmentManager, "DeleteConfirmation")
    }

    private fun initViewModel() {
        productViewModel.products.observe(viewLifecycleOwner) { allProducts ->
            binding.pbProduct.visibility = View.GONE
            binding.rvProducts.visibility = View.VISIBLE
            binding.txtCategory.visibility = View.VISIBLE
            binding.fabProducts.visibility = View.VISIBLE

            val filteredProducts = when {
                categoryName == "Todo" -> allProducts
                !subcategoryId.isNullOrEmpty() -> allProducts.filter { it.subcategoryId == subcategoryId }
                !categoryId.isNullOrEmpty() -> allProducts.filter { it.categoryId == categoryId }
                else -> emptyList()
            }

            if (productAdapter.product != filteredProducts) {
                productAdapter.product.clear()
                productAdapter.product.addAll(filteredProducts)
                productAdapter.notifyDataSetChanged()
            }

            val displayName = subcategoryName ?: categoryName
            val productCount = filteredProducts.size
            binding.txtCategory.text = getString(R.string.category_products, displayName, productCount)

            if (filteredProducts.isEmpty()) {
                binding.imgNoContent.visibility = View.VISIBLE
                binding.txtEmptyListProducts.visibility = View.VISIBLE
                binding.txtAddProducts.visibility = View.VISIBLE
            } else {
                binding.imgNoContent.visibility = View.GONE
                binding.txtEmptyListProducts.visibility = View.GONE
                binding.txtAddProducts.visibility = View.GONE
            }
        }
        productViewModel.loadProducts()
    }

    private fun initVisibility(){
        binding.txtCategory.visibility = View.GONE
        binding.rvProducts.visibility = View.GONE
        binding.imgNoContent.visibility = View.GONE
        binding.txtEmptyListProducts.visibility = View.GONE
        binding.txtAddProducts.visibility = View.GONE
        binding.fabProducts.visibility = View.GONE
        binding.pbProduct.visibility = View.VISIBLE
    }

    private fun initListener() {
        binding.fabProducts.setOnClickListener {
            onAddButtonClicked()
        }
        binding.fabAddProducts.setOnClickListener {
            (activity as? MainMenu)?.replaceFragment(AddProductFragment(), " Añadir Producto")

        }
        binding.fabEditProducts.setOnClickListener {
            productAdapter.isEditMode = !productAdapter.isEditMode
            binding.fabEditProducts.setImageResource(
                if (productAdapter.isEditMode) R.drawable.ic_close
                else R.drawable.ic_edit
            )
        }

//        binding.fabExportProducts.setOnClickListener {
//            if (checkPermissions()) {
//                handleExportToExcel()
//            } else {
//                requestPermissions()
//            }
//        }
    }

//    private fun handleExportToExcel() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//            val success = excelExporter.exportProducts(productAdapter.product)
//            if (!success) {
//                toast("Error al exportar el archivo Excel", LENGTH_LONG)
//            }
//        } else {
//            toast("Esta función requiere Android 10 o superior", LENGTH_LONG)
//        }
//    }
//
//    private fun checkPermissions(): Boolean {
//        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.READ_MEDIA_IMAGES
//            ) == PackageManager.PERMISSION_GRANTED
//        } else {
//            ContextCompat.checkSelfPermission(
//                requireContext(),
//                Manifest.permission.WRITE_EXTERNAL_STORAGE
//            ) == PackageManager.PERMISSION_GRANTED
//        }
//    }
//
//    private fun requestPermissions() {
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.READ_MEDIA_IMAGES),
//                STORAGE_PERMISSION_CODE
//            )
//        } else {
//            ActivityCompat.requestPermissions(
//                requireActivity(),
//                arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE),
//                STORAGE_PERMISSION_CODE
//            )
//        }
//    }
//
//    override fun onRequestPermissionsResult(
//        requestCode: Int,
//        permissions: Array<out String>,
//        grantResults: IntArray
//    ) {
//        when (requestCode) {
//            STORAGE_PERMISSION_CODE -> {
//                if (grantResults.isNotEmpty() &&
//                    grantResults[0] == PackageManager.PERMISSION_GRANTED) {
//                    handleExportToExcel()
//                } else {
//                    toast("Permiso denegado para exportar Excel", LENGTH_SHORT)
//                }
//            }
//            else -> super.onRequestPermissionsResult(requestCode, permissions, grantResults)
//        }
//    }

    private fun onAddButtonClicked() {
        setVisibility(clicked)
        setAnimation(clicked)
        setClickable(clicked)
        clicked = !clicked
    }

    private fun setAnimation(clicked: Boolean) {
        if (!clicked) {
            binding.fabEditProducts.startAnimation(fromBottom)
            binding.fabAddProducts.startAnimation(fromBottom)
            binding.fabProducts.startAnimation(rotateOpen)
        } else {
            binding.fabEditProducts.startAnimation(toBottom)
            binding.fabAddProducts.startAnimation(toBottom)
            binding.fabProducts.startAnimation(rotateClose)
        }
    }

    private fun setVisibility(clicked: Boolean) {
        if (!clicked) {
            binding.fabAddProducts.visibility = View.VISIBLE
            binding.fabEditProducts.visibility = View.VISIBLE
        } else {
            binding.fabAddProducts.visibility = View.INVISIBLE
            binding.fabEditProducts.visibility = View.INVISIBLE
        }
    }

    private fun setClickable(clicked: Boolean) {
        if (!clicked) {
            binding.fabEditProducts.isClickable = true
            binding.fabAddProducts.isClickable = true
        } else {
            binding.fabEditProducts.isClickable = false
            binding.fabAddProducts.isClickable = false
        }
    }
}