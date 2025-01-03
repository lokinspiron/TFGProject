package com.inventory.tfgproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.tfgproject.ProviderRepository
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import kotlinx.coroutines.launch

class ProviderViewModel(private val repository: ProviderRepository): ViewModel() {

    private val _providers  = MutableLiveData<List<Providers>>()
    val providers: LiveData<List<Providers>> = _providers

    private val _providerProducts = MutableLiveData<List<Product>>()
    val providerProducts: LiveData<List<Product>> = _providerProducts

    private val _saveProviderStatus = MutableLiveData<Pair<Boolean, String?>>()
    val saveProviderStatus: LiveData<Pair<Boolean, String?>> = _saveProviderStatus

    private val _updateProviderStatus = MutableLiveData<Boolean>()
    val updateProviderStatus: LiveData<Boolean> = _updateProviderStatus

    private val _deleteProviderStatus = MutableLiveData<Boolean>()
    val deleteProviderStatus: LiveData<Boolean> = _deleteProviderStatus

    private val _selectedProvider = MutableLiveData<Providers?>()
    val selectedProvider: LiveData<Providers?> = _selectedProvider


    fun loadProviders(){
        repository.getProviders { providers ->
            _providers.postValue(providers)
        }
    }

    fun saveProvider(provider: Providers){
        repository.addProvider(provider){success,providers ->
            _saveProviderStatus.postValue(Pair(success,providers))
        }
    }

    fun loadProviderProducts(providerId: String) {
        repository.getProductsByProviderId(providerId) { products ->
            _providerProducts.value = products
        }
    }

    fun updateProvider(providerId: String, updates: Map<String, Any>) {
        repository.updateProvider(providerId, updates) { success ->
            _updateProviderStatus.postValue(success)
        }
    }

    fun deleteProvider(providerId: String) {
        repository.deleteProvider(providerId) { success ->
            _deleteProviderStatus.postValue(success)
        }
    }

    fun setSelectedProvider(provider: Providers?) {
        _selectedProvider.value = provider
    }
}