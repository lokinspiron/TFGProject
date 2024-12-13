package com.inventory.tfgproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.model.Subcategory

class SubcategoryAdapter(
    private var subcategories: List<Subcategory>,
    private val onSubcategoryClick: (Subcategory) -> Unit
) : RecyclerView.Adapter<SubcategoryAdapter.SubcategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SubcategoryViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_subcategory, parent, false)
        return SubcategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: SubcategoryViewHolder, position: Int) {
        val subcategory = subcategories[position]
        holder.bind(subcategory)
        holder.itemView.setOnClickListener {
            onSubcategoryClick(subcategory)
        }
    }

    override fun getItemCount(): Int = subcategories.size

    fun updateSubcategories(newSubcategories: List<Subcategory>) {
        subcategories = newSubcategories
        notifyDataSetChanged()
    }

    class SubcategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        fun bind(subcategory: Subcategory) {
            itemView.findViewById<TextView>(R.id.tvSubcategoryName).text = subcategory.name
        }
    }
}