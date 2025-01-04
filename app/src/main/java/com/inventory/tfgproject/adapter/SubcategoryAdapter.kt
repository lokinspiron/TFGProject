package com.inventory.tfgproject.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.R
import com.inventory.tfgproject.model.Subcategory

class SubcategoryAdapter(
    private val subcategories: MutableList<Subcategory>,
    private val onClick: (Subcategory) -> Unit
) : RecyclerView.Adapter<SubcategoryAdapter.SubcategoryViewHolder>() {

    inner class SubcategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(subcategory: Subcategory) {
            // Set the subcategory name
            itemView.findViewById<TextView>(R.id.tvSubcategoryName).text = subcategory.name

            itemView.setOnClickListener {
                onClick(subcategory)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_subcategory, parent, false)
        return SubcategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        holder.bind(subcategories[position])
    }

    override fun getItemCount(): Int = subcategories.size

    fun updateSubcategories(newSubcategories: List<Subcategory>) {
        subcategories.clear()
        subcategories.addAll(newSubcategories)
        notifyDataSetChanged()
    }
}