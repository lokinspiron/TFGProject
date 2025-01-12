package com.inventory.tfgproject.view

import android.graphics.Color
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.TextView
import android.widget.Toast.LENGTH_SHORT
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.inventory.tfgproject.repository.ProductRepository
import com.inventory.tfgproject.modelFactory.ProductViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentEditProductBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.model.Subcategory
import com.inventory.tfgproject.viewmodel.ProductViewModel
import java.util.UUID


class EditProductFragment : Fragment() {

    private var _binding: FragmentEditProductBinding? = null
    private val binding get() = _binding!!

    private var subcategoryAdapter: ArrayAdapter<Subcategory>? = null


    private val productViewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(ProductRepository())
    }
    private var productId: String? = null
    private var productName: String? = null

    private var defaultPictureUrl : String? = null

    private var isInitialLoad = true
    private var shouldUpdateDatabase = false

    private var isCategoriesLoaded = false
    private var isProductsLoaded = false
    private var isProvidersLoaded = false



    private var selectedCategory: Category? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uploadProfilePicture(uri)
    }

    companion object {
        fun newInstance(productId: String, productName: String, categoryId: String? = null, categoryName: String? = null, subcategoryId: String? = null, subcategoryName: String? = null): EditProductFragment {
            val fragment = EditProductFragment()
            val args = Bundle()
            args.putString("product_id", productId)
            args.putString("product_name", productName)
            args.putString("category_id", categoryId)
            args.putString("category_name", categoryName)
            args.putString("subcategory_id", subcategoryId)
            args.putString("subcategory_name", subcategoryName)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        productId = arguments?.getString("product_id")
        productName = arguments?.getString("product_name")
        Log.d("EditProduct", "onCreate productId: ${arguments?.getString("product_id")}")
        productId = arguments?.getString("product_id")
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentEditProductBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        productId = arguments?.getString("product_id")
        productName = arguments?.getString("product_name")

        isCategoriesLoaded = false
        isProvidersLoaded = false
        isProductsLoaded = false

        if (productId != null) {
            productViewModel.loadProduct(productId!!)
        }
        initVisibility()
        initViewModel()
        initListeners()
        loadData()
    }

    private fun initViewModel() {
        productViewModel.categories.observe(viewLifecycleOwner) { categories ->
            isCategoriesLoaded = true
            updateCategorySpinner(categories)
            checkDataLoaded()
        }

        productViewModel.providers.observe(viewLifecycleOwner) { providers ->
            isProvidersLoaded = true
            updateProviderSpinner(providers)
            checkDataLoaded()
        }

        productViewModel.products.observe(viewLifecycleOwner) { products ->
            Log.d("EditProduct", "Products loaded: ${products.size}")
            isProductsLoaded = true
            if (isCategoriesLoaded && isProvidersLoaded) {
                products.find { it.id == productId }?.let { product ->
                    updateUI(product)
                }
            } else {
                Log.d("EditProduct", "Still waiting for data. Categories: $isCategoriesLoaded, Providers: $isProvidersLoaded")
            }
        }
    }

    private fun initListeners(){
        binding.imgProduct.setOnClickListener{
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }

        binding.btnSaveChanges.setOnClickListener{
            saveChanges()
            toast("Se ha actualizado el producto", LENGTH_SHORT)
        }

        binding.btnDelete.setOnClickListener{
            showDeleteDialog()
        }

        binding.imgBtnBack.setOnClickListener{
            (activity as? MainMenu)?.navigateBack()
        }
    }

    private fun showDeleteDialog() {
        val dialog = DialogSafeChangeFragment.newInstance(
            "Estás a punto de eliminar este producto?",
            "Eliminar"
        )
        dialog.onDoItClick = {
            deleteProduct()
            toast("Se ha eliminado el producto")
        }
        dialog.show(parentFragmentManager, "DeleteProductDialog")
    }

    private fun deleteProduct() {
        binding.pbEditProduct.visibility = View.VISIBLE
        productId?.let { id ->
            productViewModel.deleteProduct(id)
            val categoryId = arguments?.getString("category_id")
            val categoryName = arguments?.getString("category_name")
            val subcategoryId = arguments?.getString("subcategory_id")
            val subcategoryName = arguments?.getString("subcategory_name")

            val inventoryFragment = when {
                subcategoryId != null -> {
                    InventoryMenuFragment.newInstanceForSubcategory(
                        subcategoryId = subcategoryId,
                        subcategoryName = subcategoryName
                    )
                }
                categoryId != null -> {
                    InventoryMenuFragment.newInstanceForCategory(
                        categoryId = categoryId,
                        categoryName = categoryName ?: "Todo"
                    )
                }
                else -> {
                    InventoryMenuFragment.newInstanceForCategory(
                        categoryId = "",
                        categoryName = "Todo"
                    )
                }
            }
            (activity as? MainMenu)?.replaceFragment(inventoryFragment)
        }
    }

    private fun loadData() {
        productViewModel.loadCategories()
        productViewModel.categories.observe(viewLifecycleOwner) { categories ->
            productViewModel.loadProviders()
            productViewModel.providers.observe(viewLifecycleOwner) { providers ->
                productId?.let {
                    productViewModel.loadProduct(it)
                }
            }
        }
    }

    private fun initVisibility() {
        binding.apply {
            pbEditProduct.visibility = View.VISIBLE
            cltEditProduct.visibility = View.GONE
            scViewProduct.visibility = View.GONE
        }
    }
    private fun setupPriceEditText() {
        binding.edtPriceProduct.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}

            override fun afterTextChanged(s: Editable?) {
                try {
                    if (!s.isNullOrBlank()) {
                        val valueString = s.toString().replace(',', '.')
                        val value = valueString.toDouble()
                        if (value < 0) {
                            binding.edtPriceProduct.error = "El precio no puede ser negativo"
                        }
                    }
                } catch (e: NumberFormatException) {
                    binding.edtPriceProduct.error = "Formato de precio inválido"
                }
            }
        })
    }

    private fun initSpinners() {
        val currencyUnits = resources.getStringArray(R.array.currency_array)
        val weightUnits = resources.getStringArray(R.array.weight_array)
        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.convergence)

        val currencyAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            currencyUnits
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.text = currencyUnits[position]
                textView.setTextColor(Color.BLACK)
                textView.typeface = customTypeface
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.text = currencyUnits[position]
                textView.typeface = customTypeface
                return textView
            }
        }
        currencyAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = currencyAdapter

        binding.spinnerCurrency.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

        val weightAdapter = object : ArrayAdapter<String>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            weightUnits
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.text = weightUnits[position]
                textView.setTextColor(Color.BLACK)
                textView.typeface = customTypeface
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.text = weightUnits[position]
                textView.typeface = customTypeface
                return textView
            }
        }

        weightAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerWeight.adapter = weightAdapter

        binding.spinnerWeight.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }


    private fun updateUI(product: Product) {
        with(binding) {
            edtNameProduct.setText(product.name)
            edtPriceProduct.setText(String.format(getString(R.string.product_price_formats), product.price))
            edtWeightProduct.setText(String.format(getString(R.string.product_weight_formats), product.weight))
            edtQuantityProduct.setText(context?.getString(R.string.product_quantity_format, product.stock))
            spinnerCurrency.setSelection(getIndexForCurrency(product.currencyUnit))
            spinnerWeight.setSelection(getIndexForWeight(product.weightUnit))

            if(isCategoriesLoaded && isProvidersLoaded){
                if (product.imageUrl?.isNotEmpty() == true) {
                    Glide.with(requireContext())
                        .load(product.imageUrl)
                        .placeholder(R.drawable.loading_image)
                        .error(R.drawable.error_image)
                        .into(imgProduct)
                }

                val categoryAdapter = spinnerCategory.adapter as? ArrayAdapter<Category>
                if (categoryAdapter != null) {
                    for (i in 0 until categoryAdapter.count) {
                        val category = categoryAdapter.getItem(i)
                        if (category?.id == product.categoryId) {
                            selectedCategory = category
                            spinnerCategory.setSelection(i)

                            val subcategories = category.subcategory?.values?.toList() ?: emptyList()
                            updateSubCategorySpinner(subcategories)

                            selectSubcategory(product.subcategoryId)
                            break
                        }
                    }
                }

                val providerAdapter = spinnerProvider.adapter as? ArrayAdapter<Providers>
                for (i in 0 until (providerAdapter?.count ?: 0)) {
                    val provider = providerAdapter?.getItem(i)
                    if (provider?.id == product.providerId) {
                        spinnerProvider.setSelection(i)
                        break
                    }
                }
            }
            binding.pbEditProduct.visibility = View.GONE
            binding.scViewProduct.visibility = View.VISIBLE
        }
    }

    private fun updateCategorySpinner(categories: List<Category>) {
        val itemList = mutableListOf(Category(name = "Selecciona una categoría"))
        itemList.addAll(categories)
        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.convergence)

        val adapter = object : ArrayAdapter<Category>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            itemList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = itemList[position].name
                textView.setTextColor(Color.BLACK)
                textView.typeface = customTypeface
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = itemList[position].name
                textView.typeface = customTypeface
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                val selectedCategory = parent.getItemAtPosition(position) as? Category
                if (position > 0 && selectedCategory != null) {
                    val subcategories = selectedCategory.subcategory?.values?.toList() ?: emptyList()
                    updateSubCategorySpinner(subcategories)

                } else {
                    updateSubCategorySpinner(emptyList())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>) {
                updateSubCategorySpinner(emptyList())
            }
        }
    }

    private fun updateProviderSpinner(providers: List<Providers>) {
        val itemList = mutableListOf(Providers(name = "Selecciona un proveedor"))
        itemList.addAll(providers)

        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.convergence)

        val adapter = object : ArrayAdapter<Providers>(
            requireContext(),
            android.R.layout.simple_spinner_item,
            itemList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = itemList[position].name
                textView.setTextColor(Color.BLACK)
                textView.typeface = customTypeface
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(android.R.id.text1)
                textView.text = itemList[position].name
                textView.typeface = customTypeface
                return view
            }
        }

        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
        binding.spinnerProvider.adapter = adapter

        binding.spinnerProvider.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {

            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }

    }

    private fun uploadProfilePicture(uri: Uri?) {
        if (uri == null) {
            defaultPictureUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.firebasestorage.app/o/default%2Flogo_dstock.png?alt=media&token=afc390aa-dc96-42a5-b96f-1f85c5effa83"
            Log.d("Firebase", "Usando imagen predeterminada: $defaultPictureUrl")
            if (isAdded) {
                toast("Imagen predeterminada", LENGTH_SHORT)
            }
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val defaultPictureRef = storageRef.child("products/${UUID.randomUUID()}.jpg")

        defaultPictureRef.putFile(uri)
            .addOnSuccessListener {
                defaultPictureRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    defaultPictureUrl = downloadUri.toString()

                    if (isAdded && context != null) {
                        Glide.with(requireContext())
                            .load(uri)
                            .into(binding.imgProduct)
                    }
                    toast("Imagen subidad correctamente")
                }
            }
            .addOnFailureListener { exception ->
                defaultPictureUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.firebasestorage.app/o/default%2Flogo_dstock.png?alt=media&token=afc390aa-dc96-42a5-b96f-1f85c5effa83"

                if (isAdded) {
                    toast("Error al subir la imagen. Usando imagen predeterminada", LENGTH_SHORT)
                }
                Log.e("Firebase", "Error subiendo la imagen", exception)
            }
    }

    private fun updateSubCategorySpinner(subCategories: List<Subcategory>) {
        val itemList = mutableListOf(Subcategory(name = "Selecciona una subcategoría"))
        itemList.addAll(subCategories)

        val customTypeface = ResourcesCompat.getFont(requireContext(), R.font.convergence)

        if (subcategoryAdapter == null) {
            subcategoryAdapter = object : ArrayAdapter<Subcategory>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                itemList
            ) {
                override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.text = getItem(position)?.name
                    textView.setTextColor(Color.BLACK)
                    textView.typeface = customTypeface
                    return view
                }

                override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                    val view = super.getDropDownView(position, convertView, parent)
                    val textView = view.findViewById<TextView>(android.R.id.text1)
                    textView.text = getItem(position)?.name
                    textView.typeface = customTypeface
                    return view
                }
            }.apply {
                setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            }

            binding.spinnerSubcategory.adapter = subcategoryAdapter

        } else {
            subcategoryAdapter?.clear()
            subcategoryAdapter?.addAll(itemList)
            subcategoryAdapter?.notifyDataSetChanged()
        }

        Log.d("Spinner", "Subcategorías actualizadas: ${itemList.map { it.name }}")
    }

    private fun checkDataLoaded() {
        Log.d("EditProduct", "Checking data: Categories: $isCategoriesLoaded, Products: $isProductsLoaded, Providers: $isProvidersLoaded")
        if (isCategoriesLoaded && isProductsLoaded && isProvidersLoaded) {
            initSpinners()
            binding.apply {
                pbEditProduct.visibility = View.GONE
                scViewProduct.visibility = View.VISIBLE
                cltEditProduct.visibility = View.VISIBLE
            }
        }
    }

    private fun selectSubcategory(subcategoryId: String?) {
        if (subcategoryId == null) return

        val subcategoryAdapter = binding.spinnerSubcategory.adapter as? ArrayAdapter<Subcategory>
        if (subcategoryAdapter != null) {
            for (j in 0 until subcategoryAdapter.count) {
                val subcategory = subcategoryAdapter.getItem(j)
                if (subcategory?.id == subcategoryId) {
                    binding.spinnerSubcategory.setSelection(j)
                    break
                }
            }
        }
    }

    private fun saveChanges() {
        val updates = mutableMapOf<String, Any>()
        binding.apply {
            val name = edtNameProduct.text.toString()
            if (name.isBlank()) {
                edtNameProduct.error = "El nombre no puede estar vacío"
                return
            }

            val priceText = edtPriceProduct.text.toString().replace(',', '.')
            val price = priceText.toDoubleOrNull()
            if (price == null || price <= 0) {
                edtPriceProduct.error = "Ingrese un precio válido"
                return
            }

            val weightText = edtWeightProduct.text.toString().replace(',', '.')
            val weight = weightText.toDoubleOrNull()
            if (weight == null || weight <= 0) {
                edtWeightProduct.error = "Ingrese un peso válido"
                return
            }

            val stockText = edtQuantityProduct.text.toString()
            val stock = stockText.toIntOrNull()
            if (stock == null || stock < 0) {
                edtQuantityProduct.error = "Ingrese una cantidad válida"
                return
            }

            val selectedCategory = spinnerCategory.selectedItem as? Category
            val selectedSubcategory = spinnerSubcategory.selectedItem as? Subcategory
            val selectedProvider = spinnerProvider.selectedItem as? Providers

            selectedCategory?.id?.let { updates["categoryId"] = it }
            selectedSubcategory?.id?.let { updates["subcategoryId"] = it }
            selectedProvider?.id?.let { updates["providerId"] = it }
            updates["currencyUnit"] = spinnerCurrency.selectedItem.toString()
            updates["weightUnit"] = spinnerWeight.selectedItem.toString()

            updates["name"] = name
            updates["price"] = price
            updates["weight"] = weight
            updates["stock"] = stock
            updates["currencyUnit"] = spinnerCurrency.selectedItem.toString()
            updates["weightUnit"] = spinnerWeight.selectedItem.toString()
            if (defaultPictureUrl != null) {
                updates["imageUrl"] = defaultPictureUrl!!
            }
        }

        productId?.let { id ->
            if (updates.isNotEmpty()) {
                productViewModel.updateProduct(id, updates)
            }
        }
    }


    private fun getIndexForCurrency(currency: String): Int {
        return resources.getStringArray(R.array.currency_array).indexOf(currency)
    }

    private fun getIndexForWeight(weight: String): Int {
        return resources.getStringArray(R.array.weight_array).indexOf(weight)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}