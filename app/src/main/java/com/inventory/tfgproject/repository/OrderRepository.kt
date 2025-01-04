package com.inventory.tfgproject.repository

import android.util.Log
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.ChildEventListener
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
        val userProvidersRef = providersRef.child(currentUser?.uid ?: return callback(emptyList()))

        userProvidersRef.addListenerForSingleValueEvent(object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val userProviders = snapshot.children.mapNotNull { providerSnapshot ->
                    Providers(
                        id = providerSnapshot.key ?: "",
                        name = providerSnapshot.child("name").getValue(String::class.java) ?: "",
                        email = providerSnapshot.child("email").getValue(String::class.java) ?: "",
                        phoneNumber = providerSnapshot.child("phoneNumber").getValue(String::class.java) ?: "",
                        address = providerSnapshot.child("address").getValue(String::class.java) ?: "",
                        imageUrl = providerSnapshot.child("imageUrl").getValue(String::class.java) ?: ""
                    )
                }
                callback(userProviders)
            }

            override fun onCancelled(error: DatabaseError) {
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

            userOrderRef.child("stock").setValue(newQuantity)
                .addOnSuccessListener {
                    getOrders(callback)
                }
                .addOnFailureListener { exception ->
                    Log.e("OrderRepository", "Error updating quantity", exception)
                    getOrders(callback)
                }
        } ?: callback(emptyList())
    }

    fun updateOrderState(orderId: String, newState: String, callback: (List<OrderWithProduct>) -> Unit) {
        currentUser?.let { user ->
            val userOrderRef = ordersRef.child(user.uid).child(orderId)

            userOrderRef.get().addOnSuccessListener { snapshot ->
                val currentOrder = snapshot.getValue(Orders::class.java)
                val currentState = currentOrder?.estado

                userOrderRef.child("estado").setValue(newState)
                    .addOnSuccessListener {
                        Log.d("OrderRepository", "Order state updated from $currentState to: $newState")

                        currentOrder?.let { safeOrder ->
                            when {
                                currentState != "Completado" && newState == "Completado" -> {
                                    updateProductQuantity(
                                        safeOrder.productId,
                                        safeOrder.stock
                                    ) { success ->
                                        if (success) {
                                            Log.d("OrderRepository", "Added ${safeOrder.stock} to product stock")
                                        } else {
                                            Log.e("OrderRepository", "Failed to add to product stock")
                                        }
                                        getOrders(callback)
                                    }
                                }
                                currentState == "Completado" && newState != "Completado" -> {
                                    updateProductQuantity(
                                        safeOrder.productId,
                                        -safeOrder.stock
                                    ) { success ->
                                        if (success) {
                                            Log.d("OrderRepository", "Removed ${safeOrder.stock} from product stock")
                                        } else {
                                            Log.e("OrderRepository", "Failed to remove from product stock")
                                        }
                                        getOrders(callback)
                                    }
                                }
                                else -> {
                                    getOrders(callback)
                                }
                            }
                        } ?: run {
                            Log.e("OrderRepository", "Order is null")
                            getOrders(callback)
                        }
                    }
                    .addOnFailureListener { e ->
                        Log.e("OrderRepository", "Error updating order state", e)
                        getOrders(callback)
                    }
            }.addOnFailureListener { e ->
                Log.e("OrderRepository", "Error getting order", e)
                getOrders(callback)
            }
        } ?: callback(emptyList())
    }

    fun deleteOrder(orderId: String, callback: (Boolean) -> Unit) {
        currentUser?.let { user ->
            val userOrderRef = ordersRef.child(user.uid).child(orderId)
            userOrderRef.removeValue()
                .addOnSuccessListener {
                    callback(true)
                }
                .addOnFailureListener { exception ->
                    Log.e("OrderRepository", "Error deleting order", exception)
                    callback(false)
                }
        } ?: callback(false)
    }

     fun updateProductQuantity(productId: String, quantityToChange: Int, callback: (Boolean) -> Unit) {
        currentUser?.let { user ->
            val productRef = productsRef.child(user.uid).child(productId)

            productRef.get().addOnSuccessListener { snapshot ->
                val product = snapshot.getValue(Product::class.java)

                if (product != null) {
                    val newQuantity = product.stock + quantityToChange

                    if (newQuantity >= 0) {
                        productRef.child("stock").setValue(newQuantity)
                            .addOnSuccessListener {
                                Log.d("OrderRepository", "Product stock updated from ${product.stock} to $newQuantity")
                                callback(true)
                            }
                            .addOnFailureListener { e ->
                                Log.e("OrderRepository", "Error updating product stock", e)
                                callback(false)
                            }
                    } else {
                        Log.e("OrderRepository", "Cannot update stock: would result in negative quantity")
                        callback(false)
                    }
                } else {
                    Log.e("OrderRepository", "Product not found with ID: $productId")
                    callback(false)
                }
            }.addOnFailureListener { e ->
                Log.e("OrderRepository", "Error getting product", e)
                callback(false)
            }
        } ?: callback(false)
    }

    fun setupProductDeletionListener() {
        currentUser?.let { user ->
            productsRef.child(user.uid).addChildEventListener(object : ChildEventListener {
                override fun onChildRemoved(snapshot: DataSnapshot) {
                    val deletedProductId = snapshot.key ?: return
                    deleteOrdersForProduct(deletedProductId)
                }

                override fun onChildAdded(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildChanged(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onChildMoved(snapshot: DataSnapshot, previousChildName: String?) {}
                override fun onCancelled(error: DatabaseError) {
                    Log.e("OrderRepository", "Product listener cancelled: ${error.message}")
                }
            })
        }
    }

    private fun deleteOrdersForProduct(productId: String) {
        currentUser?.let { user ->
            val userOrdersRef = ordersRef.child(user.uid)

            userOrdersRef.orderByChild("productId").equalTo(productId)
                .addListenerForSingleValueEvent(object : ValueEventListener {
                    override fun onDataChange(snapshot: DataSnapshot) {
                        snapshot.children.forEach { orderSnapshot ->
                            orderSnapshot.ref.removeValue()
                                .addOnFailureListener { e ->
                                    Log.e("OrderRepository", "Error deleting order for product $productId", e)
                                }
                        }
                    }

                    override fun onCancelled(error: DatabaseError) {
                        Log.e("OrderRepository", "Error querying orders for product $productId: ${error.message}")
                    }
                })
        }
    }


}