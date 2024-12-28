package com.inventory.tfgproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.databinding.ItemOrderBinding
import com.inventory.tfgproject.model.OrderWithProduct
import com.inventory.tfgproject.model.Orders


class OrderAdapter(
    private var orders: MutableList<OrderWithProduct>,
    private val maxItems: Int,
    private val onQuantityChanged: (Orders, Int) -> Unit
) : RecyclerView.Adapter<OrderAdapter.OrderViewHolder>() {

    fun setOrders(newOrders: List<OrderWithProduct>) {
        Log.d("OrderAdapter", "Setting orders: ${newOrders.size}")
        orders.clear()
        orders.addAll(newOrders.take(maxItems))
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
                tvOrderProductName.text = orderWithProduct.productName ?: "Unknown"
                txtState.text = orderWithProduct.order.estado ?: "Unknown"
            }
        }
    }
}