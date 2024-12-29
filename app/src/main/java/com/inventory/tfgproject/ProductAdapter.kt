package com.inventory.tfgproject

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.inventory.tfgproject.model.Product
import java.util.Locale

class ProductAdapter(
    var product : MutableList<Product>,
    private val onProductClick: ((Product) -> Unit)? = null,
    private val onButtonProductClick : ((Product) -> Unit)? = null,
    private val onDeleteClick: ((Product) -> Unit)? = null,
    private val onQuantityChanged: (Product,Int) -> Unit
): RecyclerView.Adapter<ProductAdapter.ProductViewHolder>() {

    var isEditMode = false
        set(value) {
            field = value
            notifyDataSetChanged()
        }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product,parent,false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        holder.bind(product[position], null)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int,payloads: MutableList<Any>) {
        if (payloads.isNotEmpty()) {
            val payload = payloads[0]
            if (payload is Int) {
                holder.updateStock(payload)
            }
        } else {
            onBindViewHolder(holder, position)
        }
    }

    override fun getItemCount() = product.size

    inner class ProductViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        private val imgProduct : ImageView = itemView.findViewById(R.id.imgProduct)
        private val txtNameProduct : TextView = itemView.findViewById(R.id.txtNameProduct)
        private val txtPhone : TextView = itemView.findViewById(R.id.txtPhone)
        private val btnMakeOrders = itemView.findViewById<Button>(R.id.btnMakeOrders)
        private val txtQuantity: TextView = itemView.findViewById(R.id.txtQuantity)
        private val btnIncrement: ImageButton = itemView.findViewById(R.id.btnPlus)
        private val btnDecrement: ImageButton = itemView.findViewById(R.id.btnMinus)
        private val deleteIcon: ImageView = itemView.findViewById(R.id.iconDelete)
        private val containerLayout: ConstraintLayout = itemView.findViewById(R.id.cltProducts)

        fun bind(product: Product, payload: Int?) {
            if (payload == null) {
                txtNameProduct.text = product.name

                itemView.setOnClickListener {
                    onProductClick?.invoke(product)
                }

                if (product.imageUrl.isNullOrEmpty()) {
                    imgProduct.setImageResource(R.drawable.logo_dstock)
                } else {
                    Glide.with(itemView.context)
                        .load(product.imageUrl)
                        .diskCacheStrategy(DiskCacheStrategy.NONE)
                        .skipMemoryCache(true)
                        .placeholder(R.drawable.loading_image)
                        .error(R.drawable.error_image)
                        .into(imgProduct)
                }
            }

            updateStockDisplay(product.stock)

            btnIncrement.setOnClickListener {
                val newQuantity = product.stock + 1
                onQuantityChanged(product, newQuantity)
            }

            btnDecrement.setOnClickListener {
                if (product.stock > 0) {
                    val newQuantity = product.stock - 1
                    onQuantityChanged(product, newQuantity)
                }
            }

            btnMakeOrders.setOnClickListener{
                onButtonProductClick?.invoke(product)
            }

            if (product.stock == 0) {
                StockNotificationWorker.scheduleNotification(
                    itemView.context,
                    product.name
                )
            }

            deleteIcon.visibility = if (isEditMode) View.VISIBLE else View.GONE
            deleteIcon.setOnClickListener {
                onDeleteClick?.invoke(product)
            }
        }

        private fun updateStockDisplay(stock: Int) {
            txtQuantity.text = String.format(Locale.ROOT, "%d", stock)

            if (stock == 0) {
                containerLayout.setBackgroundResource(R.drawable.border_red)
                txtQuantity.setTextColor(ContextCompat.getColor(itemView.context, android.R.color.holo_red_dark))
            } else {
                containerLayout.setBackgroundResource(R.drawable.border_view_elements)
                txtQuantity.setTextColor(ContextCompat.getColor(itemView.context, R.color.letters))
            }
        }

        fun updateStock(newStock: Int) {
            updateStockDisplay(newStock)
        }
    }

    fun updateProduct(productId: String, newQuantity: Int) {
        val position = product.indexOfFirst { it.id == productId }
        if (position != -1) {
            product[position].stock = newQuantity
            notifyItemChanged(position, newQuantity)
        }
    }
}