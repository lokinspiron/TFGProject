package com.inventory.tfgproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.inventory.tfgproject.R
import com.inventory.tfgproject.model.Providers

class ProviderAdapter(
    private var providers : MutableList<Providers>,
    private val onProviderClick: ((Providers) -> Unit)? = null,
    private val onCreateOrderClick: ((Providers) -> Unit)? = null
    ): RecyclerView.Adapter<ProviderAdapter.ProviderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProviderViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_provider,parent,false)
        return ProviderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProviderViewHolder, position: Int) {
        val provider = providers[position]
        holder.bind(provider)
    }

    override fun getItemCount() = providers.size

    inner class ProviderViewHolder(itemView : View): RecyclerView.ViewHolder(itemView){
        private val imgProviders : ImageView = itemView.findViewById(R.id.imgProviders)
        private val txtNameProviders : TextView = itemView.findViewById(R.id.txtNameProviders)
        private val txtPhone :TextView = itemView.findViewById(R.id.txtPhone)
        private val btnMakeOrders = itemView.findViewById<Button>(R.id.btnMakeOrders)

        fun bind(providers: Providers){
            txtNameProviders.text = providers.name
            txtPhone.text = providers.phoneNumber
            if (providers.imageUrl.isNullOrEmpty()) {
                imgProviders.setImageResource(R.drawable.logo_dstock)
            } else {
                Glide.with(itemView.context)
                    .load(providers.imageUrl)
                    .into(imgProviders)
            }

            itemView.setOnClickListener{
                onProviderClick?.invoke(providers)
            }

            btnMakeOrders.setOnClickListener{
                onCreateOrderClick?.invoke(providers)
            }
        }

    }
    fun updateProviders(newProviders: MutableList<Providers>) {
        providers = newProviders
        notifyDataSetChanged()
    }
}