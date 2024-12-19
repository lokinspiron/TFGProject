package com.inventory.tfgproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inventory.tfgproject.ProviderRepository
import com.inventory.tfgproject.model.Providers

class ProviderViewModel(private val repository: ProviderRepository): ViewModel() {

    private val _providers  = MutableLiveData<List<Providers>>()
    val providers: LiveData<List<Providers>> = _providers

    private val _saveProviderStatus = MutableLiveData<Pair<Boolean, String?>>()
    val saveProviderStatus: LiveData<Pair<Boolean, String?>> = _saveProviderStatus

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


}