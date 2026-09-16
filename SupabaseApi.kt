package com.rudras.creation.data

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.json.JSONArray
import org.json.JSONObject
import java.net.HttpURLConnection
import java.net.URL

object SupabaseApi {
    // Same public Supabase project used by the supplied Rudras Creation website.
    const val BASE_URL = "https://mxiaapcbzrllvthpnwbs.supabase.co"
    const val ANON_KEY = "sb_publishable_LAN8mZy5IfcEfAmkym7nIA_xVMYEtYC"

    private suspend fun request(path:String, method:String="GET", body:String?=null, token:String?=null):String = withContext(Dispatchers.IO) {
        val c=URL(BASE_URL+path).openConnection() as HttpURLConnection
        c.requestMethod=method; c.setRequestProperty("apikey",ANON_KEY); c.setRequestProperty("Content-Type","application/json")
        if(token!=null)c.setRequestProperty("Authorization","Bearer $token")
        if(body!=null){c.doOutput=true;c.outputStream.use{it.write(body.toByteArray())}}
        val stream=if(c.responseCode in 200..299)c.inputStream else c.errorStream
        val text=stream.bufferedReader().readText(); if(c.responseCode !in 200..299) error(text); text
    }
    suspend fun products():List<Product>{
        val a=JSONArray(request("/rest/v1/rpc/get_public_products","POST","{}")); return (0 until a.length()).map{parseProduct(a.getJSONObject(it))}
    }
    private fun parseProduct(o:JSONObject)=Product(o.optString("id"),o.optString("sku"),o.optString("name"),o.optString("category"),o.optString("subcategory",null),o.optDouble("price"),if(o.isNull("old_price"))null else o.optDouble("old_price"),o.optString("image_url",null),o.optString("description",null),o.optDouble("rating"),o.optInt("reviews"),jsonStrings(o.optJSONArray("colors")),jsonStrings(o.optJSONArray("sizes")),o.optInt("stock_total"))
    private fun jsonStrings(a:JSONArray?):List<String> = a?.let{(0 until it.length()).map{j->it.optString(j)}}?:emptyList()
    suspend fun createOrder(orderId:String,name:String,phone:String,email:String,address:String,city:String,state:String,pincode:String,delivery:String,payment:String,items:JSONArray,total:Double,notes:String):String {
        val b=JSONObject().apply{put("p_order_id",orderId);put("p_customer_name",name);put("p_phone",phone);put("p_email",if(email.isBlank())JSONObject.NULL else email);put("p_address",address);put("p_city",city);put("p_state",state);put("p_pincode",pincode);put("p_delivery_method",delivery);put("p_payment_method",payment);put("p_items",items);put("p_total",total);put("p_notes",notes)}
        return request("/rest/v1/rpc/create_public_order","POST",b.toString()).trim('"')
    }
    suspend fun login(email:String,password:String):JSONObject {
        return JSONObject(request("/auth/v1/token?grant_type=password","POST",JSONObject().put("email",email).put("password",password).toString()))
    }
}
