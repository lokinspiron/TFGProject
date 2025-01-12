package com.inventory.tfgproject.adapter

import android.util.Log
import android.view.View
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.inventory.tfgproject.R

abstract class BaseListAdapter<T>(
    private val emptyStateContainer: View?,
    private val recyclerView: RecyclerView?,
    private val loadingProgressBar: View?,
    private val emptyStateMessage: String
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    protected var items = mutableListOf<T>()

    override fun getItemCount() = items.size

    fun updateItems(newItems: List<T>, filter: ((T) -> Boolean)? = null) {
        val filteredItems = filter?.let { newItems.filter(it) } ?: newItems
        items.clear()
        items.addAll(filteredItems)
        notifyDataSetChanged()
        updateVisibilityStates()
    }

    private fun updateVisibilityStates() {
        loadingProgressBar?.visibility = View.GONE

        val isEmpty = items.isEmpty()

        emptyStateContainer?.visibility = if (isEmpty) View.VISIBLE else View.GONE
        recyclerView?.visibility = if (isEmpty) View.GONE else View.VISIBLE
    }

    fun showLoading() {
        loadingProgressBar?.visibility = View.VISIBLE
        recyclerView?.visibility = View.GONE
        emptyStateContainer?.visibility = View.GONE
    }

    fun hideLoading() {
        loadingProgressBar?.visibility = View.GONE
        updateVisibilityStates()
    }
}
