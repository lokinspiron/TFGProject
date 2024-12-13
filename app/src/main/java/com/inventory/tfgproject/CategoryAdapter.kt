package com.inventory.tfgproject

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.BuildConfig
import com.inventory.tfgproject.model.Category

class CategoryAdapter(
    private var categories: MutableList<Category>,
    private val onCategoryClickListener: ((Category) -> Unit)? = null,
    private val onAddSubcategoryClickListener: ((Category) -> Unit)? = null
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        val category = categories[position]
        holder.bind(category)
    }

    override fun getItemCount() = categories.size

    fun updateCategories(newCategories: List<Category>) {
        categories.clear()
        categories.addAll(newCategories)
        notifyDataSetChanged()
    }

    inner class CategoryViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCategoryName: TextView = itemView.findViewById(R.id.tvCategoryName)
        private val btnCategoryAction: ImageView = itemView.findViewById(R.id.btnCategoryAction)
        private val rvSubcategories: RecyclerView = itemView.findViewById(R.id.rvSubcategories)
        private val divider: View = itemView.findViewById(R.id.divider)
        private val btnAddSubcategory: TextView = itemView.findViewById(R.id.btnAddSubcategory)

        private var subcategoryAdapter: SubcategoryAdapter? = null

        fun bind(category: Category) {
            tvCategoryName.text = category.name

            // Setup subcategory RecyclerView
            subcategoryAdapter = SubcategoryAdapter(
                category.subcategory?.values?.toMutableList() ?: mutableListOf(),
                { subcategory ->
                    // Handle subcategory click if needed
                }
            )
            rvSubcategories.layoutManager = LinearLayoutManager(itemView.context)
            rvSubcategories.adapter = subcategoryAdapter

            rvSubcategories.visibility = View.GONE
            divider.visibility = View.GONE
            btnAddSubcategory.visibility = View.GONE

            itemView.setOnClickListener {
                onCategoryClickListener?.invoke(category)
            }

            btnCategoryAction.setOnClickListener {
                if (rvSubcategories.visibility == View.VISIBLE) {
                    rvSubcategories.visibility = View.GONE
                    divider.visibility = View.GONE
                    btnAddSubcategory.visibility = View.GONE
                } else {
                    rvSubcategories.visibility = View.VISIBLE

                    if ((category.subcategory?.size ?: 0) > 0) {
                        divider.visibility = View.VISIBLE
                    }

                    btnAddSubcategory.visibility = View.VISIBLE
                }
            }

            btnAddSubcategory.setOnClickListener {
                onAddSubcategoryClickListener?.invoke(category)
            }

            category.subcategory?.let { subcategories ->
                if (subcategories.isNotEmpty()) {
                    subcategoryAdapter?.updateSubcategories(subcategories.values.toList())
                }
            }
        }
    }
}