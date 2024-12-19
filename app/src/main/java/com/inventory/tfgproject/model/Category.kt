package com.inventory.tfgproject.model

data class Category(
    var id: String = "",
    val name:String = "",
    val subcategory: Map<String?, Subcategory>? = null

)

data class Subcategory (
    val id : String? = null,
    val name: String? = null,
)
