package com.inventory.tfgproject.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.GenericTypeIndicator
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.Category
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers
import com.inventory.tfgproject.model.Subcategory

class ProductRepository {
    private val db = FirebaseDatabase.getInstance()
    private val categoriesRef = db.getReference("categories")
    private val providersRef = db.getReference("providers")
    private val productsRef = db.getReference("products")

    private val currentUser = FirebaseAuth.getInstance().currentUser


    fun updateProductQuantity(productId: String, newQuantity: Int) {
        if(currentUser == null) {
            Log.e("ProductRepository", "No user logged in")
            return
        }

        productsRef
            .child(currentUser.uid)
            .child(productId)
            .child("stock")
            .setValue(newQuantity)
            .addOnSuccessListener {
                Log.d("ProductRepository", "Successfully updated quantity to $newQuantity for product $productId")
            }
            .addOnFailureListener { e ->
                Log.e("ProductRepository", "Failed to update quantity: ${e.message}", e)
            }
    }
    fun getProducts(callback: (List<Product>) -> Unit){
        if(currentUser == null) {
            Log.e("ProductRepository","No user logged in")
            callback(emptyList())
            return
        }
        val userProductRef = productsRef.child(currentUser.uid)
        userProductRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ProductRepository", "Data snapshot received: ${snapshot.exists()}")
                val userProducts = mutableListOf<Product>()
                snapshot.children.forEach { productSnapshot ->
                    val id = productSnapshot.key?: ""
                    val name = productSnapshot.child("name").getValue(String::class.java) ?:""
                    val stock = productSnapshot.child("stock").getValue(Int::class.java)?: 0
                    val price = productSnapshot.child("price").getValue(Double::class.java)?: 0.0
                    val weight = productSnapshot.child("weight").getValue(Double::class.java)?:0.0
                    val categoryId = productSnapshot.child("categoryId").getValue(String::class.java)?:""
                    val subcategoryId = productSnapshot.child("subcategoryId").getValue(String::class.java)?:""
                    val providerId = productSnapshot.child("providerId").getValue(String::class.java)?:""
                    val imageUrl = productSnapshot.child("imageUrl").getValue(String::class.java)?:""

                    val product = Product(
                        id = id,
                        name = name,
                        stock = stock,
                        price = price,
                        currencyUnit = productSnapshot.child("currencyUnit").getValue(String::class.java) ?: "EUR",
                        weight = weight,
                        weightUnit = productSnapshot.child("weightUnit").getValue(String::class.java) ?: "kg",
                        categoryId = categoryId,
                        subcategoryId = subcategoryId,
                        providerId = providerId,
                        imageUrl = imageUrl
                    )
                    userProducts.add(product)
                }
                callback(userProducts)
            }
            override fun onCancelled(error: DatabaseError) {
                callback(emptyList())
            }
        })
    }


    fun getCategories(callback: (List<Category>) -> Unit) {
        if (currentUser == null) {
            Log.e("ProductRepository", "No user logged in")
            callback(emptyList())
            return
        }

        val userCategoriesRef = categoriesRef.child(currentUser.uid)

        userCategoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userCategories = mutableListOf<Category>()

                snapshot.children.forEach { categorySnapshot ->
                    val id = categorySnapshot.key ?: ""
                    val name = categorySnapshot.child("name").getValue(String::class.java) ?: ""
                    val subcategoryMap = categorySnapshot.child("subcategory").getValue(
                        object : GenericTypeIndicator<Map<String?, Subcategory>>() {}
                    ) ?: emptyMap()

                    val category = Category(
                        id = id,
                        name = name,
                        subcategory = subcategoryMap
                    )

                    Log.d("ProductRepository", "Category found: $category")
                    userCategories.add(category)
                }

                Log.d("ProductRepository", "Total categories loaded: ${userCategories.size}")
                callback(userCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "Error fetching categories: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun getSubCategories(callback: (List<Subcategory>) -> Unit) {
        if (currentUser == null) {
            Log.e("ProductRepository", "No user logged in")
            callback(emptyList())
            return
        }

        val userSubCategoriesRef = categoriesRef.child(currentUser.uid)

        userSubCategoriesRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userSubCategories = mutableListOf<Subcategory>()

                snapshot.children.forEach { categorySnapshot ->
                    if (categorySnapshot.hasChild("subcategory")) {
                        val subcategorySnapshot = categorySnapshot.child("subcategory")

                        subcategorySnapshot.children.forEach { subCategoryItemSnapshot ->
                            val id = subCategoryItemSnapshot.key ?: ""
                            val name = subCategoryItemSnapshot.child("name").getValue(String::class.java) ?: ""

                            val subcategory = Subcategory(
                                id = id,
                                name = name
                            )

                            Log.d("ProductRepository", "Subcategory found: $subcategory")
                            userSubCategories.add(subcategory)
                        }
                    }
                }

                Log.d("ProductRepository", "Total subcategories loaded: ${userSubCategories.size}")
                callback(userSubCategories)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "Error fetching subcategories: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun getProviders(callback: (List<Providers>) -> Unit) {
        if (currentUser == null) {
            Log.e("ProductRepository", "No user logged in")
            callback(emptyList())
            return
        }

        val userProvidersRef = providersRef.child(currentUser.uid)

        userProvidersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProviders = mutableListOf<Providers>()

                snapshot.children.forEach { providerSnapshot ->
                    val id = providerSnapshot.key ?: ""
                    val name = providerSnapshot.child("name").getValue(String::class.java) ?: ""

                    val provider = Providers(id = id, name = name)
                    userProviders.add(provider)
                }

                Log.d("ProductRepository", "User Providers loaded: $userProviders")
                callback(userProviders)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "Error fetching user providers: ${error.message}")
                callback(emptyList())
            }
        })
    }

    fun addProduct(newProduct: Product, callback: (String) -> Unit) {
        currentUser?.let { user ->
            val userProductRef = productsRef.child(user.uid)
            val newProductRef = userProductRef.push()
            val productId = newProductRef.key

            if (productId != null) {
                val productWithId = newProduct.copy(
                    id = productId
                )
                newProductRef.setValue(productWithId)
                    .addOnSuccessListener {
                        Log.d("ProductRepository", "Producto agregado exitosamente")
                        callback(productId)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ProductRepository", "Error al agregar producto", exception)
                        callback(null.toString())
                    }
            }
        }
    }

    fun updateProduct(productId: String, updatedProduct: Map<String, Any>, callback: (Boolean) -> Unit) {
        if(currentUser == null) {
            Log.e("ProductRepository", "No user logged in")
            callback(false)
            return
        }

        productsRef
            .child(currentUser.uid)
            .child(productId)
            .updateChildren(updatedProduct)
            .addOnSuccessListener {
                Log.d("ProductRepository", "Successfully updated product $productId")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("ProductRepository", "Failed to update product: ${e.message}", e)
                callback(false)
            }
    }

    fun deleteProduct(productId: String, callback: (Boolean) -> Unit) {
        if(currentUser == null) {
            Log.e("ProductRepository", "No user logged in")
            callback(false)
            return
        }

        productsRef
            .child(currentUser.uid)
            .child(productId)
            .removeValue()
            .addOnSuccessListener {
                Log.d("ProductRepository", "Successfully deleted product $productId")
                callback(true)
            }
            .addOnFailureListener { e ->
                Log.e("ProductRepository", "Failed to delete product: ${e.message}", e)
                callback(false)
            }
    }

    fun getProduct(productId: String, callback: (Product?) -> Unit) {
        if(currentUser == null) {
            callback(null)
            return
        }

        productsRef
            .child(currentUser.uid)
            .child(productId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val product = snapshot.getValue(Product::class.java)
                    callback(product)
                }

                override fun onCancelled(error: DatabaseError) {
                    callback(null)
                }
            })
    }


}