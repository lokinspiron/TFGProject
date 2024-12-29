package com.inventory.tfgproject

import android.content.res.ColorStateList
import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.databinding.ItemEvOrderBinding
import com.inventory.tfgproject.model.OrderWithProduct

class OrderViewAdapter(
    private val onQuantityChanged: (OrderWithProduct, Int) -> Unit,
    private val onStateChanged: (OrderWithProduct, String) -> Unit
) : RecyclerView.Adapter<OrderViewAdapter.OrderViewHolder>() {

    private var orders = listOf<OrderWithProduct>()

    private val states = listOf(
        StateInfo("Completado", Color.parseColor("#4CAF50")),
        StateInfo("Pendiente", Color.parseColor("#F44336"))
    )

    inner class OrderViewHolder(private val binding: ItemEvOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bind(orderWithProduct: OrderWithProduct) {
            binding.apply {
                tvOrderProductName.text = orderWithProduct.productName
                txtQuantity.text = orderWithProduct.order.stock.toString()

                val currentState = states.find { it.name == orderWithProduct.order.estado }
                    ?: states[1]

                updateStateView(currentState)

                txtState.setOnClickListener {
                    val currentIndex = states.indexOfFirst { it.name == orderWithProduct.order.estado }
                    val nextIndex = (currentIndex + 1) % states.size
                    val newState = states[nextIndex]

                    updateStateView(newState)
                    onStateChanged(orderWithProduct, newState.name)
                }

                btnPlus.setOnClickListener {
                    val newQuantity = orderWithProduct.order.stock + 1
                    txtQuantity.text = newQuantity.toString()
                    onQuantityChanged(orderWithProduct, newQuantity)
                }

                btnMinus.setOnClickListener {
                    if (orderWithProduct.order.stock > 1) {
                        val newQuantity = orderWithProduct.order.stock - 1
                        txtQuantity.text = newQuantity.toString()
                        onQuantityChanged(orderWithProduct, newQuantity)
                    }
                }
            }
        }

        private fun updateStateView(stateInfo: StateInfo) {
            binding.txtState.apply {
                text = stateInfo.name
                setBackgroundTintList(ColorStateList.valueOf(stateInfo.color))
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderViewHolder {
        val binding = ItemEvOrderBinding.inflate(
            LayoutInflater.from(parent.context), parent, false
        )
        return OrderViewHolder(binding)
    }

    override fun onBindViewHolder(holder: OrderViewHolder, position: Int) {
        holder.bind(orders[position])
    }

    override fun getItemCount() = orders.size

    fun updateOrders(newOrders: List<OrderWithProduct>) {
        orders = newOrders.sortedWith(compareBy {
            when (it.order.estado) {
                "Completado" -> 1
                "Pendiente" -> 0
                else -> 2
            }
        })
        notifyDataSetChanged()
    }

    fun getOrderAt(position: Int): OrderWithProduct {
        return orders[position]
    }

    data class StateInfo(val name: String, val color: Int)
}
