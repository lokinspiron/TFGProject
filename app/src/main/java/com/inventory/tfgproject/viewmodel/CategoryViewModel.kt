package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.Category

class CategoryViewModel: ViewModel() {
    private val db = FirebaseDatabase.getInstance().getReference("categories")

    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> get() = _categories

    init {
        fetchCategories()
    }

    fun fetchCategories() {
        db.addValueEventListener(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val categoryList = mutableListOf<Category>()
                snapshot.children.forEach { child ->
                    val category = child.getValue(Category::class.java)
                    if (category != null) {
                        categoryList.add(category)
                    }
                }

                val todoCategory = Category(
                    id = "TODO",
                    name = "Todo"
                )
                if (categoryList.none { it.id == "TODO" }) {
                    categoryList.add(0, todoCategory)
                }

                _categories.value = categoryList
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("FirebaseError", "Error fetching categories: ${error.message}")
            }
        })
    }

    fun addCategory(category: Category) {
        val newCategoryRef = db.push()
        category.id = newCategoryRef.key ?: ""
        newCategoryRef.setValue(category)
    }

}