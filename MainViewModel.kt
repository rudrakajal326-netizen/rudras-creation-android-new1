package com.rudras.creation.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.rudras.creation.data.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.json.JSONArray
import org.json.JSONObject
import kotlin.random.Random

class MainViewModel:ViewModel(){
    private val _products=MutableStateFlow<List<Product>>(emptyList()); val products=_products.asStateFlow()
    private val _cart=MutableStateFlow<List<CartItem>>(emptyList()); val cart=_cart.asStateFlow()
    private val _loading=MutableStateFlow(true); val loading=_loading.asStateFlow()
    var lastOrderId:String?=null; private set
    init{load()}
    fun load(){viewModelScope.launch{_loading.value=true;runCatching{SupabaseApi.products()}.onSuccess{_products.value=it}.onFailure{_products.value=emptyList()};_loading.value=false}}
    fun add(p:Product){_cart.value=_cart.value.toMutableList().also{val i=it.indexOfFirst{c->c.product.sku==p.sku};if(i>=0)it[i]=it[i].copy(qty=it[i].qty+1)else it.add(CartItem(p,1))}}
    fun remove(p:Product){_cart.value=_cart.value.mapNotNull{if(it.product.sku==p.sku)if(it.qty>1)it.copy(qty=it.qty-1)else null else it}}
    fun clear(){_cart.value=emptyList()}
    fun total(discount:Double=0.0)=(_cart.value.sumOf{it.product.price*it.qty}-discount).coerceAtLeast(0.0)
    fun placeOrder(name:String,phone:String,email:String,address:String,city:String,state:String,pincode:String,delivery:String,payment:String,notes:String,onDone:(String)->Unit,onError:(String)->Unit){viewModelScope.launch{try{val id="RUDRA-${System.currentTimeMillis()}-${Random.nextInt(100,999)}";val items=JSONArray().apply{_cart.value.forEach{c->put(JSONObject().apply{put("sku",c.product.sku);put("name",c.product.name);put("qty",c.qty);put("price",c.product.price);put("color",c.color);put("size",c.size)})}};SupabaseApi.createOrder(id,name,phone,email,address,city,state,pincode,delivery,payment,items,total(),notes);lastOrderId=id;clear();onDone(id)}catch(e:Exception){onError(e.message?:("Order failed"))}}}
}
