package com.inventory.tfgproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.tfgproject.repository.ProductRepository
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.model.Subcategory
import kotlinx.coroutines.launch

open class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories

    private val _subCategories = MutableLiveData<List<Subcategory>>()
    val subCategories: LiveData<List<Subcategory>> = _subCategories

    private val _providers = MutableLiveData<List<Providers>>()
    val providers: LiveData<List<Providers>> = _providers

    private val _products = MutableLiveData<List<Product>>()
    val products: LiveData<List<Product>> = _products

    private val _saveProductStatus = MutableLiveData<Pair<Boolean, String?>>()
    val saveProductStatus: LiveData<Pair<Boolean, String?>> = _saveProductStatus

    private val _updateProductStatus = MutableLiveData<Boolean>()
    val updateProductStatus: LiveData<Boolean> = _updateProductStatus

    private val _deleteProductStatus = MutableLiveData<Boolean>()
    val deleteProductStatus: LiveData<Boolean> = _deleteProductStatus

    private val _isLoadingProducts = MutableLiveData<Boolean>()
    val isLoadingProducts: LiveData<Boolean> = _isLoadingProducts


    fun loadProducts() {
        _isLoadingProducts.postValue(true)
        repository.getProducts { products ->
            _products.postValue(products)
            _isLoadingProducts.postValue(false)
        }
    }

    fun loadCategories() {
        repository.getCategories { categories ->
            _categories.postValue(categories)
        }
    }

    fun loadSubCategories() {
        repository.getSubCategories { subCategories ->
            _subCategories.postValue(subCategories)
        }
    }

    fun loadProviders() {
        repository.getProviders { providers ->
            _providers.postValue(providers)
        }
    }

    fun saveProduct(product: Product) {
        repository.addProduct(product) { productId ->
            _saveProductStatus.postValue(Pair(true, productId))
        }
    }

    fun updateProductQuantity(productId: String, newQuantity: Int) {
        viewModelScope.launch {
            repository.updateProductQuantity(productId, newQuantity)

            val currentProducts = _products.value?.toMutableList() ?: mutableListOf()
            val updatedProducts = currentProducts.map { product ->
                if (product.id == productId) {
                    product.copy(stock = newQuantity)
                } else {
                    product
                }
            }
            _products.postValue(updatedProducts)
        }
    }

    fun updateProduct(productId: String, updates: Map<String, Any>) {
        viewModelScope.launch {
            repository.updateProduct(productId, updates) { success ->
                if (success) {
                    val currentProducts = _products.value?.toMutableList() ?: mutableListOf()
                    val updatedProducts = currentProducts.map { product ->
                        if (product.id == productId) {
                            product.copy().apply {
                                updates.forEach { (key, value) ->
                                    when (key) {
                                        "name" -> name = value as String
                                        "price" -> price = value as Double
                                        "weight" -> weight = value as Double
                                        "stock" -> stock = value as Int
                                        "categoryId" -> categoryId = value as String
                                        "subcategoryId" -> subcategoryId = value as String
                                        "providerId" -> providerId = value as String
                                        "imageUrl" -> imageUrl = value as String
                                        "currencyUnit" -> currencyUnit = value as String
                                        "weightUnit" -> weightUnit = value as String
                                    }
                                }
                            }
                        } else {
                            product
                        }
                    }
                    _products.postValue(updatedProducts)
                }
                _updateProductStatus.postValue(success)
            }
        }
    }
    fun deleteProduct(productId: String) {
        viewModelScope.launch {
            repository.deleteProduct(productId) { success ->
                if (success) {
                    val currentProducts = _products.value?.toMutableList() ?: mutableListOf()
                    val updatedProducts = currentProducts.filter { it.id != productId }
                    _products.postValue(updatedProducts)
                }
                _deleteProductStatus.postValue(success)
            }
        }
    }

    fun loadProduct(productId: String) {
        repository.getProduct(productId) { product ->
            product?.let {
                val currentProducts = _products.value ?: emptyList()
                _products.postValue(currentProducts + it)
            }
        }
    }

}