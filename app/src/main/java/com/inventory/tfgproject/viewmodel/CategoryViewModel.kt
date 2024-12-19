package com.inventory.tfgproject.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.distinctUntilChanged
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Subcategory

class CategoryViewModel : ViewModel() {
    private val _categories = MutableLiveData<List<Category>>()
    val categories: LiveData<List<Category>> = _categories.distinctUntilChanged()

    private val _subcategories = MutableLiveData<List<Subcategory>>()
    val subcategories: LiveData<List<Subcategory>> = _subcategories

    private val _selectedCategory = MutableLiveData<Category?>()
    val selectedCategory: LiveData<Category?> = _selectedCategory

    private val database = FirebaseDatabase.getInstance()
    private val categoriesRef = database.getReference("categories")

    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun fetchCategories() {
        currentUser?.let { user ->
            val userCategoriesRef = categoriesRef.child(user.uid)

            userCategoriesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val categoryList = mutableListOf<Category>()

                    if (!snapshot.exists() || snapshot.childrenCount == 0L) {
                        addDefaultCategory()
                        return
                    }

                    snapshot.children.forEach { categorySnapshot ->
                        val category = Category(
                            id = categorySnapshot.key ?: "",
                            name = categorySnapshot.child("name").getValue(String::class.java) ?: "",
                            subcategory = categorySnapshot.child("subcategory").getValue(
                                object : GenericTypeIndicator<Map<String?, Subcategory>>() {}
                            )
                        )
                        categoryList.add(category)
                    }

                    val sortedCategories = categoryList.sortedBy {
                        if (it.name == "Todo") 0 else 1
                    }

                    _categories.value = sortedCategories
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CategoryViewModel", "Error fetching categories", error.toException())
                }
            })
        }
    }

    fun getSubcategoriesForCategory(categoryId: String) {
        currentUser?.let { user ->
            val userSubcategoriesRef = categoriesRef.child(user.uid).child(categoryId).child("subcategories")

            userSubcategoriesRef.addValueEventListener(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val subcategoryList = mutableListOf<Subcategory>()

                    snapshot.children.forEach { subcategorySnapshot ->
                        val subcategory = Subcategory(
                            id = subcategorySnapshot.key ?: "",
                            name = subcategorySnapshot.child("name").getValue(String::class.java) ?: ""
                        )
                        subcategoryList.add(subcategory)
                    }

                    _subcategories.postValue(subcategoryList)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("CategoryViewModel", "Error fetching subcategories", error.toException())
                    _subcategories.postValue(emptyList())
                }
            })
        }
    }


    private fun addDefaultCategory() {
        currentUser?.let { user ->
            val userCategoriesRef = categoriesRef.child(user.uid)
            val newCategoryRef = userCategoriesRef.push()

            val defaultCategory = Category(
                id = newCategoryRef.key ?: "",
                name = "Todo",
                subcategory = null
            )

            newCategoryRef.setValue(defaultCategory)
                .addOnSuccessListener {
                    Log.d("CategoryViewModel", "Default 'Todo' category added successfully")
                    fetchCategories()
                }
                .addOnFailureListener { exception ->
                    Log.e("CategoryViewModel", "Error adding default category", exception)
                }
        }
    }

    fun addCategory(name: String, subcategory: Subcategory? = null) {
        currentUser?.let { user ->
            val userCategoriesRef = categoriesRef.child(user.uid)
            val newCategoryRef = userCategoriesRef.push()

            val newCategory = Category(
                id = newCategoryRef.key ?: "",
                name = name,
                subcategory = if (subcategory != null) {
                    mapOf(subcategory.id to subcategory)
                } else {
                    null
                }
            )

            newCategoryRef.setValue(newCategory)
                .addOnSuccessListener {
                    Log.d("CategoryViewModel", "Categoría y subcategoría guardadas exitosamente.")
                }
                .addOnFailureListener { exception ->
                    Log.e("CategoryViewModel", "Error al guardar la categoría", exception)
                }
        }
    }

    fun addSubcategoryToCategory(category: Category, subcategory: Subcategory) {
        currentUser?.let { user ->
            val categoryRef = categoriesRef.child(user.uid).child(category.id)

            val currentSubcategories = category.subcategory?.toMutableMap() ?: mutableMapOf()

            currentSubcategories[subcategory.id] = subcategory

            categoryRef.child("subcategory").setValue(currentSubcategories)
                .addOnSuccessListener {
                    Log.d("CategoryViewModel", "Subcategory added successfully")
                }
                .addOnFailureListener { exception ->
                    Log.e("CategoryViewModel", "Error adding subcategory", exception)
                }
        }
    }

    fun selectCategory(category: Category) {
        _selectedCategory.value = category
        getSubcategoriesForCategory(category.id)
    }
}