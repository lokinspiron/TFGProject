package com.inventory.tfgproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers

class ProductAdapter(
    private var product : MutableList<Product>,
    private val onProviderClickListener: ((Product) -> Unit)? = null
): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product,parent,false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = product[position]
        holder.bind(product)
    }

    override fun getItemCount() = product.size

    inner class ProductViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        private val imgProduct : ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtNameProduct : TextView = itemView.findViewById(R.id.txtNameProduct)
        private val txtPhone : TextView = itemView.findViewById(R.id.txtPhone)
        private val btnMakeOrders = itemView.findViewById<Button>(R.id.btnMakeOrders)

        fun bind(product: Product){
            txtNameProduct.text = product.name
            txtPhone
            if (product.imageUrl.isNullOrEmpty()) {
                imgProduct.setImageResource(R.drawable.logo_dstock)
            } else {
                Glide.with(itemView.context)
                    .load(product.imageUrl)
                    .into(imgProduct)
            }
        }

    }
}