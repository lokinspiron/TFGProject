package com.inventory.tfgproject

import android.util.Log
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.databinding.ItemEvOrderBinding
import com.inventory.tfgproject.model.OrderWithProduct

class OrderViewAdapter(
    private val onQuantityChanged: (OrderWithProduct, Int) -> Unit
) : RecyclerView.Adapter<OrderViewAdapter.OrderViewHolder>() {
    private var orderList = mutableListOf<OrderWithProduct>()

    class OrderViewHolder(val binding: ItemEvOrderBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemEvOrderBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        val orderWithProduct = orderList[position]
        holder.binding.apply {
            tvOrderProductName.text = orderWithProduct.productName
            txtQuantity.text = orderWithProduct.order.cantidad.toString()
            txtState.text = orderWithProduct.order.estado

            btnPlus.setOnClickListener {
                Log.d("OrderViewAdapter", "Plus button clicked. Current quantity: ${orderWithProduct.order.cantidad}")
                val newQuantity = orderWithProduct.order.cantidad + 1
                Log.d("OrderViewAdapter", "New quantity: $newQuantity")
                onQuantityChanged(orderWithProduct, newQuantity)
                txtQuantity.text = newQuantity.toString()
            }

            btnMinus.setOnClickListener {
                Log.d("OrderViewAdapter", "Minus button clicked. Current quantity: ${orderWithProduct.order.cantidad}")
                if (orderWithProduct.order.cantidad > 1) {
                    val newQuantity = orderWithProduct.order.cantidad - 1
                    Log.d("OrderViewAdapter", "New quantity: $newQuantity")
                    onQuantityChanged(orderWithProduct, newQuantity)
                    txtQuantity.text = newQuantity.toString()
                }
            }
        }
    }

    override fun getItemCount() = orderList.size

    fun updateOrders(newOrders: List<OrderWithProduct>) {
        Log.d("OrderViewAdapter", "Updating orders. New size: ${newOrders.size}")
        orderList.clear()
        orderList.addAll(newOrders)
        notifyDataSetChanged()
    }
}
