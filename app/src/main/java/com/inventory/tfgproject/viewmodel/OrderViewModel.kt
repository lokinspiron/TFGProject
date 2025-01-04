package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.inventory.tfgproject.repository.OrderRepository
import com.inventory.tfgproject.model.OrderWithProduct
import com.inventory.tfgproject.model.Orders
import com.inventory.tfgproject.model.Providers
import kotlinx.coroutines.launch

class OrderViewModel(private val repository: OrderRepository): ViewModel() {
    private val _ordersWithProducts = MutableLiveData<List<OrderWithProduct>>()
    val ordersWithProducts: LiveData<List<OrderWithProduct>> = _ordersWithProducts

    private val _providers = MutableLiveData<List<Providers>>()
    val providers: LiveData<List<Providers>> = _providers

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _selectedProvider = MutableLiveData<Providers?>()
    val selectedProvider: LiveData<Providers?> = _selectedProvider


    fun setSelectedProvider(provider: Providers?) {
        _selectedProvider.value = provider
    }

    fun loadProviders() {
        _isLoading.value = true
        repository.getProviders { providers ->
            _providers.postValue(providers)
            _isLoading.value = false
        }
    }

    fun loadOrders() {
        _isLoading.value = true
        repository.getOrders { orders ->
            Log.d("OrderViewModel", "Orders loaded: ${orders.size}")
            _ordersWithProducts.postValue(orders)
            _isLoading.postValue(false)
        }
    }

    fun saveOrder(order: Orders) {
        _isLoading.value = true
        repository.addOrder(order) { newOrdersList ->
            _ordersWithProducts.postValue(newOrdersList)
            _isLoading.value = false
        }
    }

    fun updateOrderQuantity(order: Orders, newQuantity: Int) {
        _isLoading.value = true
        repository.updateOrderQuantity(order.id, newQuantity) { updatedOrders ->
            _ordersWithProducts.postValue(updatedOrders)
            _isLoading.postValue(false)
        }
    }

    fun updateOrderWithProductQuantity(orderWithProduct: OrderWithProduct, newQuantity: Int) {
        updateOrderQuantity(orderWithProduct.order, newQuantity)
    }

    fun updateOrderState(orderWithProduct: OrderWithProduct, newState: String) {
        _isLoading.value = true
        viewModelScope.launch {
            repository.updateOrderState(orderWithProduct.order.id, newState) { updatedOrders ->
                _ordersWithProducts.value = updatedOrders
                _isLoading.value = false
            }
        }
    }

    fun deleteOrder(orderWithProduct: OrderWithProduct) {
        orderWithProduct.order.id.let { orderId ->
            repository.deleteOrder(orderId) { success ->
                if (success) {
                    loadOrders()
                }
            }
        }
    }
}