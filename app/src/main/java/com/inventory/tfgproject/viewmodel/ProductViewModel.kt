package com.inventory.tfgproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inventory.tfgproject.ProductRepository
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.model.Subcategory

class ProductViewModel(private val repository: ProductRepository) : ViewModel() {
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

    fun loadProducts(){
        repository.getProducts { products ->
            _products.postValue(products)
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



}