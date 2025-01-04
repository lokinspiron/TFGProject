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
    private val emptyStateContainer: View?,
    private val recyclerView: RecyclerView?,
    private val loadingProgressBar: View?
) : RecyclerView.Adapter<ProductLowAdapter.ProductLowViewHolder>() {

    private var products: List<Product> = listOf()
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

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductLowViewHolder {
        val binding = ItemLowstockProductBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ProductLowViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ProductLowViewHolder, position: Int) {
        holder.bind(products[position])
    }

    override fun getItemCount(): Int = products.size

    fun updateProducts(newProducts: List<Product>) {
        products = newProducts
        notifyDataSetChanged()
        Log.d("ProductLowAdapter", "Updated adapter with ${products.size} products")
    }

    private fun updateEmptyState(isEmpty: Boolean) {
        loadingProgressBar?.visibility = View.GONE

        emptyStateContainer?.let { container ->
            container.visibility = if (isEmpty) View.VISIBLE else View.GONE
            container.findViewById<TextView>(R.id.tvEmptyState)?.apply {
                text = "No hay productos con stock bajo"
                visibility = if (isEmpty) View.VISIBLE else View.GONE
            }
            container.findViewById<ImageView>(R.id.ivEmptyState)?.visibility =
                if (isEmpty) View.VISIBLE else View.GONE
        }

        recyclerView?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }
}