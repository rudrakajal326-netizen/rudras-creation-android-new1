package com.rudras.creation.data

data class Product(val id:String,val sku:String,val name:String,val category:String,val subcategory:String?,val price:Double,val oldPrice:Double?,val imageUrl:String?,val description:String?,val rating:Double,val reviews:Int,val colors:List<String>,val sizes:List<String>,val stock:Int)
data class CartItem(val product:Product,val qty:Int,val color:String="",val size:String="")
data class Order(val id:String,val customerName:String,val total:Double,val status:String,val createdAt:String)

