package com.inventory.tfgproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.EmptyStateBinding
import com.inventory.tfgproject.databinding.ItemOrderBinding
import com.inventory.tfgproject.model.OrderWithProduct


class OrderAdapter(
    private var orders: MutableList<OrderWithProduct>,
    private val maxItems: Int,
    private val emptyStateBinding: EmptyStateBinding,
    private val recyclerView: RecyclerView
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    fun setOrders(newOrders: List<OrderWithProduct>) {
        Log.d("OrderAdapter", "Setting orders: ${newOrders.size}")
        val pendingOrders = newOrders.filter { it.order.estado?.lowercase() == "pendiente" }

        orders.clear()
        orders.addAll(pendingOrders.take(maxItems))

        emptyStateBinding.root.visibility = if (pendingOrders.isEmpty()) View.VISIBLE else View.GONE
        recyclerView.visibility = if (pendingOrders.isEmpty()) View.GONE else View.VISIBLE

        if (pendingOrders.isEmpty()) {
            emptyStateBinding.apply {
                tvEmptyState.text = "No hay pedidos pendientes"
                ivEmptyState.setImageResource(R.drawable.ic_empty)
            }
        }

        notifyDataSetChanged()
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemOrderBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orderWithProduct = orders[position]
        holder.bind(orderWithProduct)
    }

    override fun getItemCount(): Int = orders.size

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderWithProduct: OrderWithProduct) {
            Log.d("OrderAdapter", "Binding order: ${orderWithProduct.productName}")
            binding.apply {
                tvOrderProductName.text = orderWithProduct.productName
                txtState.text = orderWithProduct.order.estado
            }
        }
    }
}