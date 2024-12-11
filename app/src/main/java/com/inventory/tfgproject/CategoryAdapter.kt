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
    private val onCategoryClick: (Category) -> Unit,
    private val onAddSubcategoryClick: (Category) -> Unit
) : RecyclerView.Adapter<CategoryAdapter.CategoryViewHolder>() {
    val listCategories: List<Category>
        get() = this.categories

    inner class CategoryViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvCategoryName)
        val btnCategoryAction: ImageView = view.findViewById(R.id.btnCategoryAction)
        val rvSubcategories: RecyclerView = view.findViewById(R.id.rvSubcategories)

        fun bind(category: Category) {
            name.text = category.name

            // Handle subcategories
            val subcategories = category.subcategory?.values?.toList() ?: emptyList()

            if (subcategories.isNotEmpty()) {
                // Setup subcategory RecyclerView
                val subcategoryAdapter = SubcategoryAdapter(subcategories) { subcategory ->
                    // Handle subcategory click if needed
                }
                rvSubcategories.layoutManager = LinearLayoutManager(itemView.context)
                rvSubcategories.adapter = subcategoryAdapter

                // Set click listener to toggle subcategories visibility
                itemView.setOnClickListener {
                    val isVisible = rvSubcategories.visibility == View.VISIBLE
                    rvSubcategories.visibility = if (isVisible) View.GONE else View.VISIBLE
                }

                // Change action button to show details
                btnCategoryAction.setImageResource(R.drawable.ic_add) // Create an expand icon
            } else {
                // No subcategories, show add subcategory button
                btnCategoryAction.setImageResource(R.drawable.ic_add)
                btnCategoryAction.setOnClickListener {
                    onAddSubcategoryClick(category)
                }

                rvSubcategories.visibility = View.GONE
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CategoryViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category, parent, false)
        return CategoryViewHolder(view)
    }

    override fun onBindViewHolder(holder: CategoryViewHolder, position: Int) {
        Log.d("CategoryAdapter", "Binding position: $position, Category: ${categories[position].name}")
        holder.bind(categories[position])
    }

    override fun getItemCount(): Int {
        if (BuildConfig.DEBUG) { // Solo en modo debug
            if (categories.size != lastLoggedSize) {
                Log.d("CategoryAdapter", "Item count: ${categories.size}")
                lastLoggedSize = categories.size
            }
        }
        return categories.size
    }

    private var lastLoggedSize: Int = -1

    fun updateCategories(newCategories: List<Category>) {
        val diffResult = DiffUtil.calculateDiff(CategoryDiffCallback(categories, newCategories))
        categories.clear()
        categories.addAll(newCategories)
        diffResult.dispatchUpdatesTo(this)
    }
}

class CategoryDiffCallback(
    private val oldList: List<Category>,
    private val newList: List<Category>
) : DiffUtil.Callback() {

    override fun getOldListSize(): Int = oldList.size

    override fun getNewListSize(): Int = newList.size

    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition].id == newList[newItemPosition].id
    }

    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
        return oldList[oldItemPosition] == newList[newItemPosition]
    }
}
