package com.inventory.tfgproject.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers

class ProviderRepository {
    private val db = FirebaseDatabase.getInstance()
    private val providersRef = db.getReference("providers")
    private val productsRef = db.getReference("products")
    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun getProviders(callback: (List<Providers>) -> Unit){
        if (currentUser == null) {
            Log.e("ProviderRepository", "No user logged in")
            callback(emptyList())
            return
        }

        val userProvidersRef = providersRef.child(currentUser.uid)

        Log.d("ProviderRepository", "Fetching providers for user: ${currentUser.uid}")

        userProvidersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                Log.d("ProviderRepository", "Provider snapshot children count: ${snapshot.childrenCount}")
                val userProviders = mutableListOf<Providers>()

                snapshot.children.forEach { providerSnapshot ->
                    val id = providerSnapshot.key ?: ""
                    val name = providerSnapshot.child("name").getValue(String::class.java) ?: ""
                    val address = providerSnapshot.child("address").getValue(String::class.java) ?: ""
                    val email = providerSnapshot.child("email").getValue(String::class.java) ?:""
                    val phoneNumber = providerSnapshot.child("phoneNumber").getValue(String::class.java)?:""
                    val imageUrl = providerSnapshot.child("imageUrl").getValue(String::class.java)?:""

                    val provider = Providers(
                        id = id,
                        name = name,
                        address = address,
                        email = email,
                        phoneNumber = phoneNumber,
                        imageUrl = imageUrl
                    )

                    userProviders.add(provider)
                }

                Log.d("ProductRepository", "Total user categories loaded: ${userProviders.size}")
                callback(userProviders)
            }

            override fun onCancelled(error: DatabaseError) {
                Log.e("ProductRepository", "Error fetching user categories: ${error.message}")
                callback(emptyList())
            }
        })
    }


    fun addProvider(newProvider: Providers, callback: (Boolean, String?) -> Unit) {
        currentUser?.let { user ->
            val userProvidersRef = providersRef.child(user.uid)
            val newProviderRef = userProvidersRef.push()
            val providerId = newProviderRef.key

            if (providerId != null) {
                val providerWithId = newProvider.copy(
                    id = providerId
                )

                newProviderRef.setValue(providerWithId)
                    .addOnSuccessListener {
                        Log.d("ProviderRepository", "Proveedor agregado exitosamente")
                        callback(true, providerId)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("ProviderRepository", "Error al agregar proveedor", exception)
                        callback(false, null)
                    }
            }
        }
    }

    fun getProductsByProviderId(providerId: String, callback: (List<Product>) -> Unit) {
        if (currentUser == null) {
            Log.e("ProviderRepository", "No user logged in")
            callback(emptyList())
            return
        }

        val userProductsRef = productsRef.child(currentUser.uid)

        userProductsRef.orderByChild("providerId").equalTo(providerId)
            .addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val products = mutableListOf<Product>()

                    snapshot.children.forEach { productSnapshot ->
                        val product = Product(
                            id = productSnapshot.key ?: "",
                            name = productSnapshot.child("name").getValue(String::class.java) ?: "",
                            stock = productSnapshot.child("stock").getValue(Int::class.java) ?: 0,
                            price = productSnapshot.child("price").getValue(Double::class.java) ?: 0.0,
                            weight = productSnapshot.child("weight").getValue(Double::class.java) ?: 0.0,
                            providerId = productSnapshot.child("providerId").getValue(String::class.java) ?: "",
                            categoryId = productSnapshot.child("categoryId").getValue(String::class.java) ?: "",
                            subcategoryId = productSnapshot.child("subcategoryId").getValue(String::class.java) ?: "",
                            imageUrl = productSnapshot.child("imageUrl").getValue(String::class.java),
                            weightUnit = productSnapshot.child("weightUnit").getValue(String::class.java) ?: "",
                            currencyUnit = productSnapshot.child("currencyUnit").getValue(String::class.java) ?: ""
                        )
                        products.add(product)
                    }

                    Log.d("ProviderRepository", "Products found for provider $providerId: ${products.size}")
                    callback(products)
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("ProviderRepository", "Error fetching products: ${error.message}")
                    callback(emptyList())
                }
            })
    }

    fun updateProvider(providerId: String, updates: Map<String, Any>, callback: (Boolean) -> Unit) {
        if(currentUser == null) {
            callback(false)
            return
        }

        providersRef
            .child(currentUser.uid)
            .child(providerId)
            .updateChildren(updates)
            .addOnSuccessListener {
                callback(true)
            }
            .addOnFailureListener {
                callback(false)
            }
    }

    fun deleteProvider(providerId: String, callback: (Boolean) -> Unit) {
        currentUser?.let { user ->
            providersRef
                .child(user.uid)
                .child(providerId)
                .removeValue()
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener {
                    callback(false)
                }
        } ?: callback(false)
    }

}