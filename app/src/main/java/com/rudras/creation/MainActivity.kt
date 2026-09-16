package com.rudras.creation

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.VideoView
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.*
import coil.compose.AsyncImage
import com.rudras.creation.data.Product
import com.rudras.creation.ui.MainViewModel

private val Cream=Color(0xFFF8F1E8); private val Wine=Color(0xFF5B0713); private val Gold=Color(0xFFB68A3A); private val Ink=Color(0xFF211815)

class MainActivity:ComponentActivity(){override fun onCreate(b:Bundle?){super.onCreate(b);setContent{RudrasApp()}}}

@Composable fun RudrasApp(vm:MainViewModel=viewModel()){
    val nav=rememberNavController()
    val context=LocalContext.current
    var update by remember { mutableStateOf<AppUpdate?>(null) }
    LaunchedEffect(Unit){ update=UpdateChecker.check(BuildConfig.VERSION_CODE) }
    MaterialTheme(colorScheme=lightColorScheme(primary=Wine,secondary=Gold,background=Cream,surface=Cream)){
        NavHost(nav,"home"){
            composable("home"){Home(vm,{nav.navigate("products")},{nav.navigate("cart")},{nav.navigate("account")})}
            composable("products"){Products(vm,{nav.navigate("detail/${it.sku}")})}
            composable("detail/{sku}"){val sku=it.arguments?.getString("sku");val p=vm.products.collectAsState().value.firstOrNull{x->x.sku==sku};if(p!=null)Detail(p,vm,{nav.navigate("cart")},{nav.popBackStack()})}
            composable("cart"){Cart(vm,{nav.navigate("checkout")},{nav.popBackStack()})}
            composable("checkout"){Checkout(vm,{id->nav.navigate("success/${id}")},{nav.popBackStack()})}
            composable("success/{id}"){Success(it.arguments?.getString("id")?:(vm.lastOrderId?:""),{nav.navigate("home")})}
            composable("account"){Account({nav.popBackStack()})}
        }
        update?.let{available->AlertDialog(onDismissRequest={update=null},title={Text("Update available")},text={Text(available.notes.ifBlank{"A newer version of Rudras Creation is ready."})},confirmButton={TextButton(onClick={context.startActivity(Intent(Intent.ACTION_VIEW,Uri.parse(available.apkUrl)));update=null}){Text("Download")}},dismissButton={TextButton(onClick={update=null}){Text("Later")}})}
    }
}

@Composable fun Top(title:String,onBack:(()->Unit)?=null){Row(Modifier.fillMaxWidth().padding(18.dp),verticalAlignment=Alignment.CenterVertically){if(onBack!=null){Text("‹",fontSize=32.sp,modifier=Modifier.clickable{onBack()});Spacer(Modifier.width(10.dp))};Image(painterResource(R.drawable.rudras_creation_logo_header),contentDescription=title,modifier=Modifier.height(34.dp).widthIn(max=165.dp),contentScale=ContentScale.Fit);Spacer(Modifier.weight(1f));Text("RC",fontSize=20.sp,color=Gold,fontWeight=FontWeight.Bold)}}

@Composable fun Home(vm:MainViewModel,shop:()->Unit,cart:()->Unit,account:()->Unit){val ps=vm.products.collectAsState().value;Scaffold(bottomBar={BottomBar(shop,cart,account)}){p->LazyColumn(Modifier.fillMaxSize().background(Cream).padding(p)){item{Top("RUDRAS CREATION")};item{HeroBanner();Spacer(Modifier.height(16.dp))};item{Text("Shop by Category",fontSize=20.sp,fontWeight=FontWeight.Bold,modifier=Modifier.padding(horizontal=18.dp));Spacer(Modifier.height(10.dp));Row(Modifier.padding(horizontal=18.dp),horizontalArrangement=Arrangement.spacedBy(10.dp)){listOf("Sarees","Lehengas","Jewellery","Beauty").forEach{Box(Modifier.weight(1f).clip(RoundedCornerShape(14.dp)).background(Color.White).padding(vertical=18.dp),contentAlignment=Alignment.Center){Text(it,fontSize=12.sp)}}};Spacer(Modifier.height(22.dp))};item{Row(Modifier.fillMaxWidth().padding(horizontal=18.dp),verticalAlignment=Alignment.CenterVertically){Text("Featured Products",fontSize=20.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.weight(1f));Text("View all",color=Wine,modifier=Modifier.clickable{shop()})};Spacer(Modifier.height(10.dp))};if(ps.isEmpty())item{EmptyCatalogue{vm.load()}}else items(ps.take(6)){ProductCard(it){vm.add(it)}}}}}

@Composable private fun HeroBanner(){Box(Modifier.fillMaxWidth().height(230.dp).padding(horizontal=18.dp).clip(RoundedCornerShape(22.dp)).background(Wine)){AndroidView(factory={context->VideoView(context).apply{setVideoURI(Uri.parse("android.resource://${context.packageName}/${R.raw.rudras_creation_campaign_9s}"));setOnPreparedListener{player->player.isLooping=true;player.setVolume(0f,0f);start()}}},modifier=Modifier.fillMaxSize());Box(Modifier.fillMaxSize().background(Color.Black.copy(alpha=.28f)));Column(Modifier.align(Alignment.CenterStart).padding(24.dp)){Text("RUDRA'S CREATION",color=Color.White,fontSize=13.sp,fontWeight=FontWeight.Bold,letterSpacing=2.sp);Spacer(Modifier.height(8.dp));Text("Modern Grace
in Every Thread",color=Color.White,fontSize=28.sp,fontWeight=FontWeight.Bold);Spacer(Modifier.height(10.dp));Text("Shop the new collection",color=Color.White)}}}

@Composable fun BottomBar(shop:()->Unit,cart:()->Unit,account:()->Unit){NavigationBar(containerColor=Cream){NavigationBarItem(selected=true,onClick={},label={Text("Home")},icon={Text("⌂")});NavigationBarItem(selected=false,onClick=shop,label={Text("Shop")},icon={Text("▦")});NavigationBarItem(selected=false,onClick=cart,label={Text("Cart")},icon={Text("🛍")});NavigationBarItem(selected=false,onClick=account,label={Text("Account")},icon={Text("◯")})}}

@Composable fun Products(vm:MainViewModel,open:(Product)->Unit){val ps=vm.products.collectAsState().value;val loading=vm.loading.collectAsState().value;Column(Modifier.fillMaxSize().background(Cream)){Top("Shop");when{loading->Box(Modifier.fillMaxSize(),contentAlignment=Alignment.Center){CircularProgressIndicator(color=Wine)};ps.isEmpty()->EmptyCatalogue{vm.load()};else->LazyColumn(contentPadding=PaddingValues(18.dp),verticalArrangement=Arrangement.spacedBy(12.dp)){items(ps){ProductCard(it){open(it)}}}}}}

@Composable private fun EmptyCatalogue(retry:()->Unit){Box(Modifier.fillMaxWidth().padding(30.dp),contentAlignment=Alignment.Center){Column(horizontalAlignment=Alignment.CenterHorizontally){Text("No products available",fontSize=19.sp,fontWeight=FontWeight.SemiBold,color=Ink);Spacer(Modifier.height(6.dp));Text("Please check back soon.",color=Color.Gray);Spacer(Modifier.height(14.dp));OutlinedButton(onClick=retry){Text("Retry")}}}}

@Composable
fun ProductCard(p: Product, action: () -> Unit) {
    Row(
        Modifier.fillMaxWidth().clip(RoundedCornerShape(18.dp)).background(Color.White).clickable { action() }.padding(10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AsyncImage(p.imageUrl, contentDescription = p.name, modifier = Modifier.size(105.dp).clip(RoundedCornerShape(14.dp)), contentScale = ContentScale.Crop)
        Spacer(Modifier.width(12.dp))
        Column {
            Text(p.name, fontWeight = FontWeight.SemiBold)
            Text(p.category, color = Color.Gray, fontSize = 12.sp)
            Spacer(Modifier.height(5.dp))
            Text("₹" + "%.0f".format(p.price), fontWeight = FontWeight.Bold, color = Wine)
            if ((p.oldPrice ?: 0.0) > p.price) {
                Text("₹" + "%.0f".format(p.oldPrice), fontSize = 11.sp, color = Color.Gray)
            }
        }
    }
}

@Composable
fun Detail(p: Product, vm: MainViewModel, cart: () -> Unit, back: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cream)) {
        Top("Product", back)
        AsyncImage(p.imageUrl, p.name, Modifier.fillMaxWidth().height(330.dp), contentScale = ContentScale.Crop)
        Column(Modifier.padding(20.dp)) {
            Text(p.name, fontSize = 25.sp, fontWeight = FontWeight.Bold)
            Text("★ " + p.rating + " (" + p.reviews + ")", color = Gold)
            Text("₹" + "%.0f".format(p.price), fontSize = 24.sp, fontWeight = FontWeight.Bold, color = Wine)
            Spacer(Modifier.height(12.dp))
            Text(p.description ?: "Elegant handcrafted style from Rudras Creation.", color = Color.DarkGray)
            Spacer(Modifier.height(20.dp))
            Button(onClick = { vm.add(p); cart() }, Modifier.fillMaxWidth(), colors = ButtonDefaults.buttonColors(containerColor = Wine)) {
                Text("ADD TO CART")
            }
        }
    }
}

@Composable
fun Cart(vm: MainViewModel, checkout: () -> Unit, back: () -> Unit) {
    val cartItems = vm.cart.collectAsState().value
    Column(Modifier.fillMaxSize().background(Cream)) {
        Top("My Cart", back)
        if (cartItems.isEmpty()) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) { Text("Your cart is empty.") }
        } else {
            LazyColumn(Modifier.weight(1f), contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(cartItems) { item ->
                    Row(Modifier.fillMaxWidth().background(Color.White).padding(10.dp), verticalAlignment = Alignment.CenterVertically) {
                        AsyncImage(item.product.imageUrl, item.product.name, Modifier.size(72.dp).clip(RoundedCornerShape(10.dp)), contentScale = ContentScale.Crop)
                        Column(Modifier.padding(start = 10.dp)) {
                            Text(item.product.name, fontWeight = FontWeight.Bold)
                            Text("₹" + "%.0f".format(item.product.price) + " × " + item.qty)
                            Row {
                                Text("−", Modifier.clickable { vm.remove(item.product) }.padding(8.dp))
                                Text(item.qty.toString(), Modifier.padding(8.dp))
                                Text("+", Modifier.clickable { vm.add(item.product) }.padding(8.dp))
                            }
                        }
                    }
                }
            }
            Text("Total  ₹" + "%.0f".format(vm.total()), fontSize = 20.sp, fontWeight = FontWeight.Bold, modifier = Modifier.padding(18.dp))
            Button(onClick = checkout, Modifier.fillMaxWidth().padding(18.dp), colors = ButtonDefaults.buttonColors(containerColor = Wine)) {
                Text("PROCEED TO CHECKOUT")
            }
        }
    }
}

@Composable
fun Checkout(vm: MainViewModel, done: (String) -> Unit, back: () -> Unit) {
    var name by remember { mutableStateOf("") }
    var phone by remember { mutableStateOf("") }
    var address by remember { mutableStateOf("") }
    var city by remember { mutableStateOf("") }
    var state by remember { mutableStateOf("") }
    var pin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf("") }
    Column(Modifier.fillMaxSize().background(Cream)) {
        Top("Checkout", back)
        LazyColumn(contentPadding = PaddingValues(18.dp), verticalArrangement = Arrangement.spacedBy(10.dp)) {
            item { Field("Full name", name) { name = it } }
            item { Field("Phone", phone) { phone = it } }
            item { Field("Address", address) { address = it } }
            item { Field("City", city) { city = it } }
            item { Field("State", state) { state = it } }
            item { Field("Pincode", pin) { pin = it } }
            item { Text("Payment: Cash on Delivery", fontWeight = FontWeight.SemiBold) }
            item { if (error.isNotBlank()) Text(error, color = Color.Red) }
            item {
                Button(
                    onClick = {
                        if (name.isBlank() || phone.isBlank() || address.isBlank() || city.isBlank() || state.isBlank() || pin.isBlank()) {
                            error = "Please complete all required details"
                        } else {
                            vm.placeOrder(name, phone, "", address, city, state, pin, "Standard Delivery", "Cash on Delivery", "", { done(it) }) { error = it }
                        }
                    },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = Wine)
                ) { Text("PLACE ORDER") }
            }
        }
    }
}

@Composable
fun Field(label: String, value: String, onValue: (String) -> Unit) {
    OutlinedTextField(value = value, onValueChange = onValue, label = { Text(label) }, modifier = Modifier.fillMaxWidth(), singleLine = true)
}

@Composable
fun Success(id: String, home: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cream).padding(28.dp), horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.Center) {
        Text("Order Confirmed", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Wine)
        Spacer(Modifier.height(12.dp))
        Text(id)
        Spacer(Modifier.height(25.dp))
        Button(onClick = home, colors = ButtonDefaults.buttonColors(containerColor = Wine)) { Text("CONTINUE SHOPPING") }
    }
}

@Composable
fun Account(back: () -> Unit) {
    Column(Modifier.fillMaxSize().background(Cream)) {
        Top("My Account", back)
        listOf("My Orders", "Wishlist", "Addresses", "Coupons & Offers", "Help & Support", "Settings").forEach {
            Row(Modifier.fillMaxWidth().clickable { }.padding(20.dp)) {
                Text(it, fontSize = 17.sp)
                Spacer(Modifier.weight(1f))
                Text("›", fontSize = 24.sp)
            }
        }
    }
}
