package com.inventory.tfgproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.databinding.ItemProviderBinding
import com.inventory.tfgproject.databinding.ItemProviderMenuBinding
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.view.MainMenu
import com.inventory.tfgproject.view.ProviderViewFragment

class ProvidersMenuAdapter(
    emptyStateContainer: View?,
    recyclerView: RecyclerView?,
    loadingProgressBar: View?
) : BaseListAdapter<Providers>(
    emptyStateContainer,
    recyclerView,
    loadingProgressBar,
    "No hay proveedores disponibles"
) {

    inner class ProviderViewHolder(private val binding: ItemProviderMenuBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(provider: Providers) {
            binding.apply {
                tvProviderName.text = provider.name
                root.setOnClickListener {
                    val fragment = ProviderViewFragment.newInstance(
                        providerId = provider.id,
                        providerName = provider.name
                    )
                    val activity = itemView.context as? MainMenu
                    activity?.replaceFragment(fragment, "Proveedores")
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return ProviderViewHolder(
            ItemProviderMenuBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        (holder as ProviderViewHolder).bind(items[position])
    }
}