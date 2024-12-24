package com.inventory.tfgproject

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.Orders
import com.inventory.tfgproject.model.Providers

class OrderRepository {

    private val db = FirebaseDatabase.getInstance()
    private val ordersRef = db.getReference("orders")
    private val providersRef = db.getReference("providers")

    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun addOrder(newOrder: Orders, callback: (List<Orders>) -> Unit) {
        currentUser?.let { user ->
            val userOrdersRef = ordersRef.child(user.uid)
            val newProductRef = userOrdersRef.push()
            val orderId = newProductRef.key

            if (orderId != null) {
                val orderWithId = newOrder.copy(
                    id = orderId
                )
                newProductRef.setValue(orderWithId)
                    .addOnSuccessListener {
                        getOrders { ordersList ->
                            callback(ordersList)
                        }
                    }
                    .addOnFailureListener { exception ->
                        Log.e("OrderRepository", "Error al agregar pedido", exception)
                        callback(emptyList())
                    }
            }
        }
    }

    fun getOrders(callback: (List<Orders>) -> Unit) {
        currentUser?.let { user ->
            val userOrdersRef = ordersRef.child(user.uid)
            userOrdersRef.get().addOnSuccessListener { snapshot ->
                val ordersList = mutableListOf<Orders>()
                for (orderSnapshot in snapshot.children) {
                    orderSnapshot.getValue(Orders::class.java)?.let {
                        ordersList.add(it)
                    }
                }
                callback(ordersList)
            }.addOnFailureListener {
                callback(emptyList())
            }
        }
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
}