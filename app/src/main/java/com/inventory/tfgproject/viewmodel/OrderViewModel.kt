package com.inventory.tfgproject.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.inventory.tfgproject.OrderRepository
import com.inventory.tfgproject.model.Orders
import com.inventory.tfgproject.model.Providers

class OrderViewModel(private val repository : OrderRepository): ViewModel() {

    private val _orders = MutableLiveData<List<Orders>>()
    val orders: LiveData<List<Orders>> = _orders

    private val _providers = MutableLiveData<List<Providers>>()
    val providers: LiveData<List<Providers>> = _providers

    fun loadProviders() {
        repository.getProviders { providers ->
            _providers.postValue(providers)
        }
    }

    fun loadOrders() {
        repository.getOrders { newOrders ->
            val currentOrders = _orders.value
            if (currentOrders != newOrders) {
                _orders.postValue(newOrders)
            }
        }
    }

    fun saveOrder(order: Orders) {
        repository.addOrder(order) { newOrdersList ->
            _orders.postValue(newOrdersList)
        }
    }
}