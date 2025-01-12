package com.inventory.tfgproject.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.databinding.ItemLowstockProductBinding
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.view.MainMenu
import com.inventory.tfgproject.view.ProductViewFragment

class ProductLowAdapter(
    emptyStateContainer: View?,
    recyclerView: RecyclerView?,
    loadingProgressBar: View?
) : BaseListAdapter<Product>(
    emptyStateContainer,
    recyclerView,
    loadingProgressBar,
    "No hay productos con stock bajo"
) {
    private val LOW_STOCK_THRESHOLD = 5

    inner class ProductLowViewHolder(private val binding: ItemLowstockProductBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(product: Product) {
            binding.apply {
                tvProductName.text = product.name
                txtStock.apply {
                    text = "Stock: ${product.stock}"
                    setTextColor(
                        if (product.stock <= 2)
                            ContextCompat.getColor(context, R.color.red_button)
                        else
                            ContextCompat.getColor(context, R.color.letters)
                    )
                }

                root.setOnClickListener {
                    val fragment = ProductViewFragment.newInstance(
                        productId = product.id,
                        productName = product.name
                    )
                    val activity = itemView.context as? MainMenu
                    activity?.replaceFragment(fragment, "Inventario")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProductLowViewHolder(
            ItemLowstockProductBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ProductLowViewHolder).bind(items[position])
    }

}