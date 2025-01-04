package com.inventory.tfgproject.view

import android.R
import android.content.pm.PackageManager
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
import androidx.camera.core.AspectRatio
import androidx.camera.core.CameraSelector
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.bumptech.glide.Glide
import com.google.firebase.storage.FirebaseStorage
import com.inventory.tfgproject.repository.ProductRepository
import com.inventory.tfgproject.modelFactory.ProductViewModelFactory
import com.inventory.tfgproject.databinding.FragmentAddProductBinding
import com.inventory.tfgproject.extension.toast
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.model.Subcategory
import com.inventory.tfgproject.viewmodel.ProductViewModel
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException


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

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted ->
        if (isGranted) {
            //initBarcodeScanner()
        } else {
            toast("Se requiere permiso de cámara para escanear códigos de barras", LENGTH_SHORT)
        }
    }

    companion object {
        private const val CAMERA_PERMISSION_REQUEST_CODE = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
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

        binding.btnCancelAddProduct.setOnClickListener{
            requireActivity().supportFragmentManager.popBackStack()
        }

        binding.btnBarcodeScan.setOnClickListener {
            requestCameraPermission()
        }
    }

    private fun requestCameraPermission() {
        when {
            ContextCompat.checkSelfPermission(
                requireContext(),
                android.Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED -> {
                Log.d("CameraPermission", "Permission already granted")
                initBarcodeScanner()
            }
            shouldShowRequestPermissionRationale(android.Manifest.permission.CAMERA) -> {
                toast("Se requiere permiso de cámara para escanear códigos de barras", LENGTH_SHORT)
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
            else -> {
                requestPermissionLauncher.launch(android.Manifest.permission.CAMERA)
            }
        }
    }


    private fun saveProduct() {
        if (validateInputs()) {
            val name = binding.edtNameProductAdd.text.toString()
            val barcode = binding.edtBarcodeProductAdd.text.toString()
            val stock = binding.edtQuantityProductAdd.text.toString().toIntOrNull() ?: 0
            val weightValue = binding.edtWeightProductAdd.text.toString().toDoubleOrNull() ?: 0.0
            val priceValue = binding.edtPriceProductAdd.text.toString().toDoubleOrNull() ?: 0.0

            val weightUnit = binding.spinnerWeight.selectedItem.toString()
            val currencyUnit = binding.spinnerCurrency.selectedItem.toString()

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
                barcode = barcode,
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
            clearForm()
            toast("Se ha añadido el producto", LENGTH_SHORT)
        }

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

        if (binding.edtPriceProductAdd.text.isNullOrBlank()) {
            binding.priceProductAddContainer.error = "Precio es requerido"
            isValid = false
        } else {
            binding.priceProductAddContainer.error = null
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
        binding.edtBarcodeProductAdd.text?.clear()
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
        val customTypeface = ResourcesCompat.getFont(requireContext(), com.inventory.tfgproject.R.font.convergence)

        val currencyAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.simple_spinner_item,
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
        currencyAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerCurrency.adapter = currencyAdapter

        val weightAdapter = object : ArrayAdapter<String>(
            requireContext(),
            R.layout.simple_spinner_item,
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

        weightAdapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerWeight.adapter = weightAdapter
    }

    private fun updateCategorySpinner(categories: List<Category>) {
        val itemList = mutableListOf(Category(name= "Selecciona una categoría"))
        itemList.addAll(categories)

        val customTypeface = ResourcesCompat.getFont(requireContext(), com.inventory.tfgproject.R.font.convergence)

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
                textView.typeface = customTypeface
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.typeface = customTypeface
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerCategory.adapter = adapter
    }

    private fun updateSubCategorySpinner(subCategories: List<Subcategory>) {
        val itemList = mutableListOf(Subcategory(name = "Selecciona una subcategoría"))
        itemList.addAll(subCategories)

        val customTypeface = ResourcesCompat.getFont(requireContext(), com.inventory.tfgproject.R.font.convergence)

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
                textView.typeface = customTypeface
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.typeface = customTypeface
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

        val customTypeface = ResourcesCompat.getFont(requireContext(), com.inventory.tfgproject.R.font.convergence)

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
                textView.typeface = customTypeface
                return view
            }

            override fun getDropDownView(position: Int, convertView: View?, parent: ViewGroup): View {
                val view = super.getDropDownView(position, convertView, parent)
                val textView = view.findViewById<TextView>(R.id.text1)
                textView.text = itemList[position].name
                textView.typeface = customTypeface
                return view
            }
        }

        adapter.setDropDownViewResource(R.layout.simple_spinner_dropdown_item)
        binding.spinnerProvider.adapter = adapter
    }

    private fun uploadProfilePicture(uri: Uri?) {
        if (uri == null) {
            defaultPictureUrl = "https://firebasestorage.googleapis.com/v0/b/d-stock-01.appspot.com/o/default%2Flogo_dstock.png?alt=media&token=afc390aa-dc96-42a5-b96f-1f85c5effa83"
            return
        }

        val storageRef = FirebaseStorage.getInstance().reference
        val defaultPictureRef = storageRef.child("default/${UUID.randomUUID()}.jpg")

        try {
            val uploadTask = defaultPictureRef.putFile(uri)

            uploadTask.addOnProgressListener { taskSnapshot ->
                val progress = (100.0 * taskSnapshot.bytesTransferred / taskSnapshot.totalByteCount)
                Log.d("Upload", "Progress: $progress%")
            }

            uploadTask.continueWithTask { task ->
                if (!task.isSuccessful) {
                    throw task.exception ?: Exception("Unknown error")
                }
                defaultPictureRef.downloadUrl
            }.addOnSuccessListener { downloadUri ->
                defaultPictureUrl = downloadUri.toString()
                Glide.with(requireContext()).load(uri).into(binding.imgProduct)
                toast("Imagen subida exitosamente", LENGTH_SHORT)
            }.addOnFailureListener { e ->
                Log.e("Upload", "Error específico: ${e.message}", e)
                toast("Error: ${e.message}", LENGTH_SHORT)
            }
        } catch (e: Exception) {
            Log.e("Upload", "Error general: ${e.message}", e)
            toast("Error general: ${e.message}", LENGTH_SHORT)
        }
    }

    @androidx.annotation.OptIn(ExperimentalGetImage::class)
    private fun initBarcodeScanner() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(requireContext())

        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get(20, TimeUnit.SECONDS)
                cameraProvider.unbindAll()

                // Configuración de la cámara
                val preview = Preview.Builder()
                    .setTargetAspectRatio(AspectRatio.RATIO_16_9)
                    .build()

                val cameraSelector = CameraSelector.Builder()
                    .requireLensFacing(CameraSelector.LENS_FACING_BACK)
                    .build()

                cameraProvider.bindToLifecycle(
                    viewLifecycleOwner,
                    cameraSelector,
                    preview.also { it.surfaceProvider = binding.previewView.surfaceProvider }
                )
            } catch (e: TimeoutException) {
                Log.e("CameraX", "Timeout al inicializar la cámara", e)
                toast("La cámara tardó demasiado en inicializar", LENGTH_SHORT)
            } catch (e: Exception) {
                Log.e("CameraX", "Error al inicializar la cámara", e)
                toast("Error al inicializar la cámara", LENGTH_SHORT)
            }
        }, ContextCompat.getMainExecutor(requireContext()))
    }

    override fun onDestroyView() {
        try {
            ProcessCameraProvider.getInstance(requireContext()).get().unbindAll()
        } catch (e: Exception) {
            Log.e("Camera", "Error unbinding camera use cases", e)
        }
        _binding = null
        super.onDestroyView()
    }
}