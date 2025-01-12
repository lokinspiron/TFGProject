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
    emptyStateContainer: View?,
    recyclerView: RecyclerView?,
    loadingProgressBar: View?,
    private val maxItems: Int
) : BaseListAdapter<OrderWithProduct>(
    emptyStateContainer,
    recyclerView,
    loadingProgressBar,
    "No hay pedidos pendientes"
) {

    inner class OrderViewHolder(private val binding: ItemOrderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(orderWithProduct: OrderWithProduct) {
            binding.apply {
                tvOrderProductName.text = orderWithProduct.productName
                txtState.text = orderWithProduct.order.estado

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return OrderViewHolder(
            ItemOrderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as OrderViewHolder).bind(items[position])
    }

}