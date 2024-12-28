package com.inventory.tfgproject

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.inventory.tfgproject.model.OrderWithProduct
import com.inventory.tfgproject.model.Orders
import com.inventory.tfgproject.model.Product
import com.inventory.tfgproject.model.Providers

class OrderRepository {

    private val db = FirebaseDatabase.getInstance()
    private val ordersRef = db.getReference("orders")
    private val providersRef = db.getReference("providers")
    private val productsRef = db.getReference("products")

    private val currentUser = FirebaseAuth.getInstance().currentUser

    fun addOrder(newOrder: Orders, callback: (List<OrderWithProduct>) -> Unit) {
        currentUser?.let { user ->
            val userOrdersRef = ordersRef.child(user.uid)
            val newProductRef = userOrdersRef.push()
            val orderId = newProductRef.key

            if (orderId != null) {
                val orderWithId = newOrder.copy(id = orderId)
                newProductRef.setValue(orderWithId)
                    .addOnSuccessListener {
                        getOrders(callback)
                    }
                    .addOnFailureListener { exception ->
                        Log.e("OrderRepository", "Error adding order", exception)
                        callback(emptyList())
                    }
            }
        } ?: callback(emptyList())
    }

    fun getOrders(callback: (List<OrderWithProduct>) -> Unit) {
        currentUser?.let { user ->
            val userOrdersRef = ordersRef.child(user.uid)
            userOrdersRef.addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    val ordersList = mutableListOf<OrderWithProduct>()
                    val totalOrders = snapshot.childrenCount.toInt()
                    var processedOrders = 0

                    Log.d("OrderRepository", "Total orders to process: $totalOrders")

                    if (totalOrders == 0) {
                        callback(emptyList())
                        return
                    }

                    snapshot.children.forEach { orderSnapshot ->
                        val order = orderSnapshot.getValue(Orders::class.java)
                        order?.let { safeOrder ->
                            Log.d("OrderRepository", "Processing order with product ID: ${safeOrder.productId}")
                            val orderWithId = safeOrder.copy(id = orderSnapshot.key ?: safeOrder.id)

                            getProduct(safeOrder.productId) { product ->
                                Log.d("OrderRepository", "Adding order with product name: ${product.name}")
                                ordersList.add(OrderWithProduct(orderWithId, product.name))
                                processedOrders++

                                if (processedOrders == totalOrders) {
                                    Log.d("OrderRepository", "All orders processed. Total: ${ordersList.size}")
                                    callback(ordersList)
                                }
                            }
                        }
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OrderRepository", "Error fetching orders: ${error.message}")
                    callback(emptyList())
                }
            })
        } ?: callback(emptyList())
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

    fun getProduct(productId: String, callback: (Product) -> Unit) {
        currentUser?.let { user ->
            Log.d("OrderRepository", "Fetching product for user: ${user.uid}, productId: $productId")
            productsRef.child(user.uid).child(productId).addListenerForSingleValueEvent(object : ValueEventListener {
                override fun onDataChange(snapshot: DataSnapshot) {
                    Log.d("OrderRepository", "Product snapshot: $snapshot")
                    val product = snapshot.getValue(Product::class.java)
                    if (product != null) {
                        Log.d("OrderRepository", "Product found: ${product.name}")
                        callback(product)
                    } else {
                        Log.e("OrderRepository", "Product not found for ID: $productId")
                        callback(Product(name = "Product Not Found"))
                    }
                }

                override fun onCancelled(error: DatabaseError) {
                    Log.e("OrderRepository", "Error fetching product: ${error.message}")
                    callback(Product(name = "Error Loading Product"))
                }
            })
        } ?: callback(Product(name = "No User"))
    }


    fun updateOrderQuantity(orderId: String, newQuantity: Int, callback: (List<OrderWithProduct>) -> Unit) {
        currentUser?.let { user ->
            val userOrderRef = ordersRef.child(user.uid).child(orderId)

            userOrderRef.child("cantidad").setValue(newQuantity)
                .addOnSuccessListener {
                    getOrders(callback)
                }
                .addOnFailureListener { exception ->
                    Log.e("OrderRepository", "Error updating quantity", exception)
                    getOrders(callback)
                }
        } ?: callback(emptyList())
    }
}