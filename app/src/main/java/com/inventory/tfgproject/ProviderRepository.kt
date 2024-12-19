package com.inventory.tfgproject

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.Providers

class ProviderRepository {
    private val db = FirebaseDatabase.getInstance()
    private val providersRef = db.getReference("providers")
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
}