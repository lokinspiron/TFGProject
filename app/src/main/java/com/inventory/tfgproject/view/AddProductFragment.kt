package com.inventory.tfgproject.view

import android.R
import android.graphics.Color
import android.net.Uri
import android.os.Bundle
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
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.inventory.tfgproject.ProductRepository
import com.inventory.tfgproject.ProductViewModelFactory
import com.inventory.tfgproject.databinding.FragmentAddProductBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.model.Subcategory
import com.inventory.tfgproject.viewmodel.ProductViewModel
import java.util.UUID

class AddProductFragment : Fragment() {

    private var _binding: FragmentAddProductBinding? = null
    private val binding get() = _binding!!
    private val productViewModel: ProductViewModel by viewModels {
        ProductViewModelFactory(ProductRepository())
    }
    private val _subCategories = MutableLiveData<List<Subcategory>>()
    val subCategories: LiveData<List<Subcategory>> = _subCategories

    private var defaultPictureUrl : String? = null

    private val pickMedia = registerForActivityResult(ActivityResultContracts.PickVisualMedia()) { uri ->
        uploadProfilePicture(uri)
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAddProductBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initForm()
        initListeners()
        setupSpinner()
    }

    private fun initListeners(){
        binding.btnSaveChangesAddProduct.setOnClickListener{
            saveProduct()
        }
        binding.imgBtnBack.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.imgProduct.setOnClickListener{
            pickMedia.launch(PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly))
        }
    }

    private fun saveProduct() {
        if (validateInputs()) {
            val name = binding.edtNameProductAdd.text.toString()
            val stock = binding.edtQuantityProductAdd.text.toString().toIntOrNull() ?: 0
            val weightValue = binding.edtWeightProductAdd.text.toString().toDoubleOrNull() ?: 0.0
            val priceValue = binding.edtPriceProductAdd.text.toString().toDoubleOrNull() ?: 0.0

            val weightUnit = binding.spinnerWeight.selectedItem.toString()
            val currencyUnit = binding.spinnerPrice.selectedItem.toString()

            val category = binding.spinnerCategory.selectedItem as? Category
            val subCategory = binding.spinnerSubCategory.selectedItem as? Subcategory
            val provider = binding.spinnerProvider.selectedItem as? Providers

            if (category == null || category.name == "Selecciona una categoría" ||
                provider == null || provider.name == "Selecciona un proveedor") {
                toast("Selecciona categoría y proveedor", LENGTH_SHORT)
                return
            }

            val product = Product(
                name = name,
                stock = stock,
                weight = weightValue,
                weightUnit = weightUnit,
                price = priceValue,
                currencyUnit = currencyUnit,
                categoryId = category.id,
                subcategoryId = subCategory?.id ?: "",
                providerId = provider.id,
                imageUrl = defaultPictureUrl
            )
            productViewModel.saveProduct(product)
        }
        clearForm()
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.edtNameProductAdd.text.isNullOrBlank()) {
            binding.nameProductAddContainer.error = "Nombre es requerido"
            isValid = false
        } else {
            binding.nameProductAddContainer.error = null
        }

        if (binding.edtQuantityProductAdd.text.isNullOrBlank()) {
            binding.quantityProductAddContainer.error = "Cantidad es requerida"
            isValid = false
        } else {
            binding.quantityProductAddContainer.error = null
        }

        if (binding.edtWeightProductAdd.text.isNullOrBlank()) {
            binding.weightProductAddContainer.error = "Peso es requerido"
            isValid = false
        } else {
            binding.weightProductAddContainer.error = null
        }

        return isValid
    }

    private fun clearForm() {
        defaultPictureUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.firebasestorage.app/o/default%2Flogo_dstock.png?alt=media&token=afc390aa-dc96-42a5-b96f-1f85c5effa83"

        binding.edtNameProductAdd.text?.clear()
        binding.edtQuantityProductAdd.text?.clear()
        binding.edtWeightProductAdd.text?.clear()
        binding.edtPriceProductAdd.text?.clear()
        binding.spinnerCategory.setSelection(0)
        binding.spinnerSubCategory.setSelection(0)
        binding.spinnerProvider.setSelection(0)
        Glide.with(requireContext())
            .load(defaultPictureUrl)
            .into(binding.imgProduct)
    }

    private fun initForm(){
        binding.nameProductAddContainer.helperText = null
        binding.quantityProductAddContainer.helperText = null
        binding.weightProductAddContainer.helperText = null
        binding.priceProductAddContainer.helperText = null
    }

    private fun setupSpinner() {
        productViewModel.loadCategories()
        productViewModel.categories.observe(viewLifecycleOwner) { categories ->
            Log.d("AddProductFragment", "Categorías observadas: $categories")
            updateCategorySpinner(categories)
        }

        binding.spinnerCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                val selectedCategory = parent?.getItemAtPosition(position) as? Category

                if (selectedCategory != null && selectedCategory.id.isNotEmpty()) {
                    Log.d("Spinner", "Categoría seleccionada: ${selectedCategory.name}")

                    val subcategoriesList = selectedCategory.subcategory?.values?.toList() ?: emptyList()
                    updateSubCategorySpinner(subcategoriesList)
                } else {
                    Log.d("Spinner", "No se seleccionó una categoría válida.")
                    updateSubCategorySpinner(emptyList())
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                updateSubCategorySpinner(emptyList())
            }
        }

        productViewModel.loadProviders()
        productViewModel.providers.observe(viewLifecycleOwner) { providers ->
            updateProviderSpinner(providers)
        }
        initSpinners()
    }

    private fun initSpinners() {
        val currencyUnits = arrayOf("EUR", "USD")
        val weightUnits = arrayOf("kg", "g", "lb", "lt")

        val currencyAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.simple_spinner_item,
            currencyUnits
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.text = currencyUnits[position]
                textView.setTextColor(Color.BLACK)
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.text = currencyUnits[position]
                textView.setTextColor(Color.BLACK)
                return textView
            }
        }
        currencyAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerPrice.adapter = currencyAdapter

        val weightAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.simple_spinner_item,
            weightUnits
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getView(position, convertView, parent) as TextView
                textView.text = weightUnits[position]
                textView.setTextColor(Color.BLACK)
                return textView
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val textView = super.getDropDownView(position, convertView, parent) as TextView
                textView.text = weightUnits[position]
                textView.setTextColor(Color.BLACK)
                return textView
            }
        }

        weightAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerWeight.adapter = weightAdapter
    }

    private fun updateCategorySpinner(categories: List<Category>) {
        val itemList = mutableListOf(Category(name= "Selecciona una categoría"))
        itemList.addAll(categories)

        val adapter = object : ArrayAdapter<Category>(
            requireContext(),
            R.layout.simple_spinner_item,
            itemList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)

                textView.text = itemList[position].name

                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun updateSubCategorySpinner(subCategories: List<Subcategory>) {
        val itemList = mutableListOf(Subcategory(name = "Selecciona una subcategoría"))
        itemList.addAll(subCategories)

        val adapter = object : ArrayAdapter<Subcategory>(
            requireContext(),
            R.layout.simple_spinner_item,
            itemList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerSubCategory.adapter = adapter
        Log.d("Spinner", "Subcategorías actualizadas: ${itemList.map { it.name }}")
    }

    private fun updateProviderSpinner(providers: List<Providers>) {
        val itemList = mutableListOf(Providers(name = "Selecciona un proveedor"))
        itemList.addAll(providers)

        val adapter = object : ArrayAdapter<Providers>(
            requireContext(),
            R.layout.simple_spinner_item,
            itemList
        ) {
            override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.setTextColor(Color.BLACK)
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerProvider.adapter = adapter
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
        val defaultPictureRef = storageRef.child("default/${UUID.randomUUID()}.jpg")

        defaultPictureRef.putFile(uri)
            .addOnSuccessListener {
                defaultPictureRef.downloadUrl.addOnSuccessListener { downloadUri ->
                    defaultPictureUrl = downloadUri.toString()

                    if (isAdded) {
                        Glide.with(requireContext())
                            .load(uri)
                            .into(binding.imgProduct)

                        Log.d("Firebase", "Imagen subida correctamente: $defaultPictureUrl")
                        toast("Imagen subida con éxito", LENGTH_SHORT)
                    }
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

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}