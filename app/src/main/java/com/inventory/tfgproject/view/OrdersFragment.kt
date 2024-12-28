package com.inventory.tfgproject.view

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import com.inventory.tfgproject.OrderAdapter
import com.inventory.tfgproject.OrderRepository
import com.inventory.tfgproject.OrderViewAdapter
import com.inventory.tfgproject.OrderViewModelFactory
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.FragmentMenuMainBinding
import com.inventory.tfgproject.databinding.FragmentOrdersBinding
import com.inventory.tfgproject.viewmodel.OrderViewModel

class OrdersFragment : Fragment() {

    private lateinit var binding : FragmentOrdersBinding
    private lateinit var orderViewAdapter: OrderViewAdapter
    private val orderViewModel: OrderViewModel by viewModels {
        OrderViewModelFactory(OrderRepository())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       binding = FragmentOrdersBinding.inflate(inflater,container,false)
        setupRecyclerView()
        setupObservers()
        return binding.root
    }

    private fun setupRecyclerView() {
        orderViewAdapter = OrderViewAdapter { orderWithProduct, newQuantity ->
            orderViewModel.updateOrderWithProductQuantity(orderWithProduct, newQuantity)
        }
        binding.rvViewOrder.apply {
            adapter = orderViewAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        orderViewModel.ordersWithProducts.observe(viewLifecycleOwner) { orders ->
            orderViewAdapter.updateOrders(orders)
            binding.rvViewOrder.visibility = if (orders.isEmpty()) View.GONE else View.VISIBLE
        }
        orderViewModel.loadOrders()
    }


}