package com.example.tripsplit

import android.R.attr.data
import android.content.Context
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.animateContentSize
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Person
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.livedata.observeAsState
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.input.TextFieldValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.Image
import androidx.compose.foundation.border
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowForward
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import javax.net.ssl.SSLContext
import javax.net.ssl.TrustManager
import javax.net.ssl.X509TrustManager
import kotlin.random.Random

val PrimaryColor = Color(0xFF6750A4)
val SecondaryColor = Color(0xFFEADDFF)
val BackgroundColor = Color(0xFFFEF7FF)

data class NavItem(
    val title: String,
    val icon: Int,
    val route: String
)




data class ApiResponse(
    @SerializedName("success") val success: Boolean,
    @SerializedName("message") val message: String
)

object RetrofitClient {
    private const val BASE_URL = "https://trip-split.visoft.dev/"

    val instance: ApiService by lazy {
        val interceptor = HttpLoggingInterceptor().apply {
            level = HttpLoggingInterceptor.Level.BODY
        }

        val client = OkHttpClient.Builder()
            .addInterceptor(interceptor)
            .build()

        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

    private fun getUnsafeOkHttpClient(): OkHttpClient {

        try {
            // Create a trust manager that does not validate certificate chains
            val trustAllCerts = arrayOf<TrustManager>(object : X509TrustManager {
                override fun checkClientTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun checkServerTrusted(chain: Array<X509Certificate>, authType: String) {}
                override fun getAcceptedIssuers(): Array<X509Certificate> = arrayOf()
            })

            // Install the all-trusting trust manager
            val sslContext = SSLContext.getInstance("SSL")
            sslContext.init(null, trustAllCerts, java.security.SecureRandom())

            // Create an ssl socket factory with our all-trusting manager
            val sslSocketFactory = sslContext.socketFactory

            return OkHttpClient.Builder()
                .sslSocketFactory(sslSocketFactory, trustAllCerts[0] as X509TrustManager)
                .hostnameVerifier { _, _ -> true }
                .addInterceptor(HttpLoggingInterceptor().apply {
                    level = HttpLoggingInterceptor.Level.BODY
                })
                .build()
        } catch (e: Exception) {
            throw RuntimeException(e)
        }
    }

    val unsafeInstance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(getUnsafeOkHttpClient())
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }

}

data class Trip(
    val id: String,
    val name: String,
    val dateRange: String,
    val nextActivity: String,
    val progress: Float,
    val imageResId: Int
)

data class TripEvent(
    val id: Int,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val color: Color
)

val tripList = listOf(
    Trip("1", "Summer Europe Trip", "Jul 15 - Aug 2", "Flight booking", 0.4f, R.drawable.europe),
    Trip("2", "Asia Backpacking", "Dec 1 - Jan 15", "Visa applications", 0.2f, R.drawable.zhongnahai),
    Trip("3", "Weekend Ski Trip", "Feb 10 - Feb 12", "Equipment rental", 0.8f, R.drawable.ski)
)


fun String.toLocalDate(): LocalDate {
    val formatterWithYear = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH)
    val formatterWithoutYear = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

    return if (this.trim().length > 6) {  // Checks if the string already includes a year
        LocalDate.parse(this, formatterWithYear)
    } else {
        LocalDate.parse("$this ${Year.now().value}", formatterWithYear)
    }
}


fun parseDateRange(dateRange: String): Pair<LocalDate, LocalDate> {
    val parts = dateRange.split(" - ")
    val startDate = parts[0].toLocalDate()
    var endDate = parts[1].toLocalDate()

    // Handle year-wrap (e.g., Dec 31 - Jan 2)
    if (endDate.isBefore(startDate)) {
        endDate = endDate.plusYears(1)
    }
    return startDate to endDate
}

fun generatePersistentColor(tripId: String): Color {
    val random = Random(tripId.hashCode().toLong())
    return Color(
        red = random.nextFloat().coerceIn(0.3f, 0.7f),
        green = random.nextFloat().coerceIn(0.3f, 0.7f),
        blue = random.nextFloat().coerceIn(0.3f, 0.7f)
    )
}


class AuthViewModel : ViewModel() {
    sealed class AuthState {
        object Idle : AuthState()
        object Loading : AuthState()
        data class Success(val email: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    fun registerUser(name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = NetworkClient.apiService.createUser(
                    UserRegistration(name, email, password)
                )

                if (response.isSuccessful) {
                    _authState.value = AuthState.Success(email)
                } else {
                    _authState.value = AuthState.Error(
                        response.errorBody()?.string() ?: "Registration failed"
                    )
                }
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }
}



class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            TripsApp()
        }
    }
}



@Composable
fun MyTripsScreen(navController: NavController, tripsViewModel: TripsViewModel = viewModel()) {
    var showJoinDialog by remember { mutableStateOf(false) }
    val filterOptions = listOf("All", "Active", "Upcoming", "Completed")
    var selectedFilter by remember { mutableStateOf(0) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Header Section (unchanged)
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "My Trips",
                style = MaterialTheme.typography.h4,
                color = PrimaryColor
            )
            IconButton(
                onClick = { showJoinDialog = true },
                modifier = Modifier
                    .size(48.dp)
            ) {
                Icon(
                    painter = painterResource(R.drawable.join),
                    contentDescription = "Join Trip",
                    tint = PrimaryColor
                )
            }
        }

        if (showJoinDialog) {
            JoinCodeDialog(
                onDismiss = { showJoinDialog = false },
                onCodeEntered = { code ->
                    showJoinDialog = false
                    Log.d("JoinCode", "Entered code: $code")
                }
            )
        }

        // Scrollable trip list
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 16.dp)
        ) {
            items(tripList) { trip ->
                TripCard(
                    trip = trip,
                    onTripClick = {
                        tripsViewModel.selectTrip(trip.name)
                        navController.navigate("this_trip/${trip.name}")
                    }
                )
                Spacer(modifier = Modifier.height(8.dp))
            }
        }
    }
}

@Composable
fun TripCard(trip: Trip, onTripClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onTripClick(trip.id) },
        elevation = 2.dp,
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // Image with fixed aspect ratio
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .clip(RoundedCornerShape(12.dp))
            ) {
                Image(
                    painter = painterResource(id = trip.imageResId),
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Trip details
            Column(modifier = Modifier.padding(horizontal = 8.dp)) {
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.h6,
                    color = Color.Black
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = "Next activity: ${trip.nextActivity}",
                    style = MaterialTheme.typography.caption,
                    color = Color.Gray
                )
                Spacer(modifier = Modifier.height(8.dp))
                TripProgressBar(progress = trip.progress)
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = trip.dateRange,
                    style = MaterialTheme.typography.caption,
                    color = PrimaryColor
                )
            }
        }
    }
}



@Composable
fun JoinCodeDialog(
    onDismiss: () -> Unit,
    onCodeEntered: (String) -> Unit
) {
    var code by remember { mutableStateOf(TextFieldValue("")) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Text(
                text = "Join Trip",
                style = MaterialTheme.typography.h6,
                color = PrimaryColor
            )
        },
        text = {
            Column {
                OutlinedTextField(
                    value = code,
                    onValueChange = { code = it },
                    label = { Text("Enter Trip Code") },
                    singleLine = true,
                    shape = RoundedCornerShape(8.dp),
                    colors = TextFieldDefaults.outlinedTextFieldColors(
                        focusedBorderColor = PrimaryColor,
                        cursorColor = PrimaryColor
                    ),
                    modifier = Modifier.fillMaxWidth()
                )
                Text(
                    text = "Get the code from your trip organizer",
                    style = MaterialTheme.typography.caption,
                    modifier = Modifier.padding(top = 8.dp)
                )
            }
        },
        confirmButton = {
            TextButton(
                onClick = { onCodeEntered(code.text) },
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryColor,
                    contentColor = Color.White
                )
            ) {
                Text("Join Trip")
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(
                    contentColor = PrimaryColor
                )
            ) {
                Text("Cancel")
            }
        },
        shape = RoundedCornerShape(16.dp),
        backgroundColor = Color.White
    )
}


@Composable
fun TripItem(tripName: String, onTripClick: (String) -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
            .clickable { onTripClick(tripName) } // Trigger trip selection onclick
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = tripName, style = MaterialTheme.typography.h6)
            Text(text = "Updated today", style = MaterialTheme.typography.body2)
        }
    }
}

@Composable
fun TripsApp() {
    val navController = rememberNavController()
    val tripsViewModel: TripsViewModel = viewModel()

    Scaffold(
        bottomBar = { BottomNavBar(navController, tripsViewModel) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = "my_trips",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("my_trips") { MyTripsScreen(navController, tripsViewModel) }
            composable("calendar") { CalendarScreen(tripList) }  // Changed from "this_trip" to "calendar"
            composable("my_profile") { ProfileScreenWrapper() }
            composable("this_trip/{tripName}") { backStackEntry ->
                val tripName = backStackEntry.arguments?.getString("tripName")
                ThisTripScreen(navController, tripName)
            }
        }
    }
}


@Composable
fun BottomNavBar(navController: NavHostController, tripsViewModel: TripsViewModel) {
    val navItems = listOf(
        NavItem("My Trips", R.drawable.trips, "my_trips"),
        NavItem("Calendar", R.drawable.newcalendar, "calendar"),  // cooked
        NavItem("Profile", R.drawable.person, "my_profile")
    )

    BottomNavigation(
        backgroundColor = Color.White,
        elevation = 8.dp
    ) {
        val navBackStackEntry by navController.currentBackStackEntryAsState()
        val currentRoute = navBackStackEntry?.destination?.route

        navItems.forEach { item ->
            BottomNavigationItem(
                icon = {
                    Icon(
                        painter = painterResource(id = item.icon),
                        contentDescription = item.title,
                        modifier = Modifier.size(24.dp)
                    )
                },
                label = {
                    Text(
                        item.title,
                        fontSize = 12.sp,
                        softWrap = false
                    )
                },
                selected = currentRoute == item.route,
                onClick = {
                    if (currentRoute != item.route) {
                        navController.navigate(item.route) {
                            popUpTo(navController.graph.startDestinationId) {
                                saveState = true
                            }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                selectedContentColor = PrimaryColor,
                unselectedContentColor = Color.Gray,
                alwaysShowLabel = true
            )
        }
    }
}
@Composable
fun ThisTripScreen(
    navController: NavController,
    tripName: String?
) {
    val trip = tripList.find { it.name == tripName }

    if (trip == null) {
        LaunchedEffect(Unit) {
            navController.popBackStack()
        }
        return
    }

    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    val expenses = remember { mutableStateListOf<Expense>() }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
            .padding(16.dp)
    ) {
        // Header with Back and Quit Trip buttons
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = { navController.popBackStack() }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Back", tint = PrimaryColor)
            }
            TextButton(onClick = { /* TODO: Handle quit trip */ }) {
                Text("Quit Trip", color = PrimaryColor)
            }
        }

        // Trip Header Section
        Text(
            text = trip.name,
            style = MaterialTheme.typography.h4,
            color = PrimaryColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        // Trip Info Cards
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 8.dp),
            elevation = 4.dp
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(
                    text = "📅 ${trip.dateRange}",
                    style = MaterialTheme.typography.body1
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Next Activity: ${trip.nextActivity}",
                    style = MaterialTheme.typography.body1,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // People List Section
        Text(
            text = "Travel Companions",
            style = MaterialTheme.typography.h6,
            color = PrimaryColor,
            modifier = Modifier.padding(vertical = 8.dp)
        )

        val people = listOf("Alice", "Bob", "Charlie", "David")
        Column {
            people.forEach { person ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier.padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Person",
                            tint = PrimaryColor
                        )
                        Text(
                            text = person,
                            modifier = Modifier.padding(start = 8.dp),
                            style = MaterialTheme.typography.body1
                        )
                    }
                }
            }
        }

        // Expenses Section
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Expenses",
                style = MaterialTheme.typography.h6,
                color = PrimaryColor
            )
            Button(
                onClick = { showExpenseDialog = true },
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryColor,
                    contentColor = Color.White
                )
            ) {
                Text("Record Expense")
            }
        }

        LazyColumn(modifier = Modifier.weight(1f)) {
            items(expenses) { expense ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    elevation = 2.dp
                ) {
                    Row(
                        modifier = Modifier
                            .padding(16.dp)
                            .fillMaxWidth(),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Text(text = expense.title, style = MaterialTheme.typography.body1)
                        Text(
                            text = "%.2f PLN".format(expense.amount),
                            style = MaterialTheme.typography.body1,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }
    }

    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Record New Expense", color = PrimaryColor) },
            text = {
                Column {
                    TextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        label = { Text("Expense Title") },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                    TextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount (PLN)") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        expenseAmount.toDoubleOrNull()?.let {
                            expenses.add(Expense(expenseTitle, it))
                            expenseTitle = ""
                            expenseAmount = ""
                            showExpenseDialog = false
                        }
                    },
                    colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor)
                ) {
                    Text("Add", color = Color.White)
                }
            },
            dismissButton = {
                TextButton(onClick = { showExpenseDialog = false }) {
                    Text("Cancel", color = PrimaryColor)
                }
            }
        )
    }
}

data class Expense(
    val title: String,
    val amount: Double
)


@Composable
fun PersonItem(name: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
    ) {
        Icon(Icons.Filled.Person, contentDescription = name)
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(text = name, style = MaterialTheme.typography.h6)
            Text(text = "Supporting line text lorem ipsum...", style = MaterialTheme.typography.body2)
        }
    }
}




@Composable
fun TripsScreen() {
    Column(modifier = Modifier.fillMaxSize()) {
        // filters and sorting options
        FiltersSection()

        // Trip list
        TripList(trips = listOf("Joined Trip 1", "Joined Trip 2", "Joined Trip 3"))
    }
}

@Composable
fun FiltersSection() {
    Column(modifier = Modifier
        .fillMaxWidth()
        .padding(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            // Filters Buttons
            FilterButton(text = "Filter0")
            FilterButton(text = "Filter1", isSelected = true)
            FilterButton(text = "Filter2")
            FilterButton(text = "Filter3")
        }

        // Sorting Option
        Text(
            text = "Sorting by Name",
            fontSize = 16.sp,
            modifier = Modifier.padding(top = 8.dp)
        )
    }
}

@Composable
fun FilterButton(text: String, isSelected: Boolean = false) {
    Button(
        onClick = { /* Filter click */ },
        shape = RoundedCornerShape(20.dp),
        colors = ButtonDefaults.buttonColors(
            backgroundColor = if (isSelected) Color(0xFFD0BCFF) else Color.LightGray
        ),
        modifier = Modifier.padding(4.dp)
    ) {
        Text(text = text)
    }
}


@Composable
fun TripList(trips: List<String>) {
    LazyColumn(modifier = Modifier.fillMaxSize()) {
        items(trips.size) { index ->
            TripItem(tripName = trips[index], lastUpdated = "Updated ${index} days ago")
        }
    }
}

@Composable
fun TripItem(tripName: String, lastUpdated: String) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .clickable { /* item click */ },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // img placeholder
            Box(
                modifier = Modifier
                    .size(120.dp, 60.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                // for later..
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text(text = tripName, fontSize = 18.sp)
            Text(text = lastUpdated, fontSize = 14.sp, color = Color.Gray)
        }
    }
}


@Preview(showBackground = true)
@Composable
fun TripsScreenPreview() {
    TripsScreen()
}



@Composable
fun ProfileScreenWrapper() {
    var isLoggedIn by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("") }

    if (isLoggedIn) {
        ProfileScreen(
            email = userEmail,
            onLogout = {
                isLoggedIn = false
                userEmail = ""
            }
        )
    } else {
        LoginScreen(
            onLogin = { email ->
                isLoggedIn = true
                userEmail = email
            }
        )
    }
}

@Composable
fun LoginScreen(onLogin: (String) -> Unit) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    val authViewModel: AuthViewModel = viewModel()
    val context = LocalContext.current

    LaunchedEffect(authViewModel.authState.value) {
        when (val state = authViewModel.authState.value) {
            is AuthViewModel.AuthState.Success -> {
                onLogin(state.email)
                Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
            }
            is AuthViewModel.AuthState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = if (isRegistering) "Create Account" else "Log In",
            fontSize = 24.sp,
            modifier = Modifier.padding(bottom = 24.dp)
        )

        if (isRegistering) {
            OutlinedTextField(
                value = name,
                onValueChange = { name = it },
                label = { Text("Name") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(8.dp))

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(16.dp))

        Button(
            onClick = {
                if (isRegistering) {
                    if (name.isBlank() || email.isBlank() || password.isBlank()) {
                        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
                        return@Button
                    }
                    authViewModel.registerUser(name, email, password)
                } else {
                    // Handle login
                }
            },
            modifier = Modifier.fillMaxWidth(),
            enabled = authViewModel.authState.value !is AuthViewModel.AuthState.Loading
        ) {
            if (authViewModel.authState.value is AuthViewModel.AuthState.Loading) {
                CircularProgressIndicator(modifier = Modifier.size(24.dp))
            } else {
                Text(if (isRegistering) "Create Account" else "Log In")
            }
        }

        TextButton(onClick = { isRegistering = !isRegistering }) {
            Text(
                text = if (isRegistering) "Already have an account? Log In"
                else "Don't have an account? Create Account",
                color = PrimaryColor
            )
        }
    }
}

//private fun registerUser(
//    name: String,
//    email: String,
//    password: String,
//    context: Context,
//    onSuccess: (String) -> Unit,
//    onLoading: (Boolean) -> Unit
//) {
//
//    if (name.isBlank() || email.isBlank() || password.isBlank()) {
//        Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
//        return
//    }
//
//    CoroutineScope(Dispatchers.IO).launch {
//        onLoading(true)
//        try {
//            val response = RetrofitClient.instance.registerUser(
//                UserRegistration(name, email, password)
//            )
//
//            withContext(Dispatchers.Main) {
//                if (response.isSuccessful && response.body()?.success == true) {
//                    onSuccess(email)
//                    Toast.makeText(context, "Registration successful!", Toast.LENGTH_SHORT).show()
//                } else {
//                    val error = response.errorBody()?.string() ?: "Unknown error"
//                    Toast.makeText(context, "Registration failed: $error", Toast.LENGTH_LONG).show()
//                }
//            }
//        } catch (e: Exception) {
//            withContext(Dispatchers.Main) {
//                Toast.makeText(context, "Network error: ${e.message}", Toast.LENGTH_LONG).show()
//            }
//        } finally {
//            onLoading(false)
//        }
//    }
//}

@Composable
fun ProfileScreen(email: String, onLogout: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Spacer(modifier = Modifier.height(32.dp))

        Box(contentAlignment = Alignment.BottomEnd) {
            Image(
                painter = painterResource(R.drawable.ic_placeholder),
                contentDescription = "Profile Picture",
                modifier = Modifier
                    .size(120.dp)
                    .clip(CircleShape)
            )
            IconButton(
                onClick = { /* Handle photo change */ },
                modifier = Modifier
                    .size(32.dp)
                    .background(PrimaryColor, CircleShape)
            ) {
                Icon(
                    painter = painterResource(R.drawable.ic_placeholder),
                    contentDescription = "Change Photo",
                    tint = Color.White
                )
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            text = "Ed Wood",
            style = MaterialTheme.typography.h4,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Text(
            text = email,
            style = MaterialTheme.typography.subtitle1,
            color = Color.Gray,
            modifier = Modifier.align(Alignment.CenterHorizontally)
        )

        Spacer(modifier = Modifier.height(32.dp))

        ProfileOptionItem(
            icon = R.drawable.ic_placeholder,
            text = "Account Settings"
        )
        ProfileOptionItem(
            icon = R.drawable.ic_placeholder,
            text = "Notifications"
        )
        ProfileOptionItem(
            icon = R.drawable.ic_placeholder,
            text = "Privacy & Security"
        )
        ProfileOptionItem(
            icon = R.drawable.ic_placeholder,
            text = "Help & Support"
        )
        Button(
            onClick = onLogout,
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 16.dp),
            colors = ButtonDefaults.buttonColors(
                backgroundColor = Color.Red,
                contentColor = Color.White
            )
        ) {
            Text("Log Out")
        }
    }
}

@Composable
fun ProfileOptionItem(icon: Int, text: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { /* Handle click */ }
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            painter = painterResource(icon),
            contentDescription = null,
            tint = PrimaryColor,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.width(16.dp))
        Text(
            text = text,
            style = MaterialTheme.typography.body1
        )
    }
}

@Composable
fun ProfileOption(optionText: String) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp)
    ) {
        Text(text = optionText, fontSize = 18.sp, color = Color.Black)
        Divider(color = Color.LightGray, thickness = 1.dp)
    }
}


@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    ProfileScreenWrapper()
}
@Composable
fun TripProgressBar(progress: Float) {
    LinearProgressIndicator(
        progress = progress,
        modifier = Modifier
            .fillMaxWidth()
            .height(8.dp)
            .clip(RoundedCornerShape(4.dp)),
        color = PrimaryColor,
        backgroundColor = Color.LightGray
    )
}


@Composable
fun TripDayDetailItem(trip: Trip) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp,
        backgroundColor = MaterialTheme.colors.surface
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(generatePersistentColor(trip.id))
                )
                Spacer(modifier = Modifier.width(8.dp))
                Text(
                    text = trip.name,
                    style = MaterialTheme.typography.subtitle1,
                    fontWeight = FontWeight.Medium
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    text = trip.dateRange,
                    style = MaterialTheme.typography.caption
                )
                Text(
                    text = trip.nextActivity,
                    style = MaterialTheme.typography.caption,
                    color = MaterialTheme.colors.primary
                )
            }
        }
    }
}


@Composable
fun CalendarScreen(tripList: List<Trip>) {
    val currentDate = remember { mutableStateOf(LocalDate.now()) }
    var selectedDay by remember { mutableStateOf<LocalDate?>(null) }
    val daysOfWeek = listOf("Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun")

    // Convert Trips to calendar events with colors
    val tripEvents = remember(tripList) {
        tripList.map { trip ->
            val (startDate, endDate) = parseDateRange(trip.dateRange)
            TripEvent(
                id = trip.id.hashCode(),
                title = trip.name,
                startDate = startDate,
                endDate = endDate,
                color = generatePersistentColor(trip.id)
            )
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {

        // Month header with navigation
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            IconButton(onClick = {
                currentDate.value = currentDate.value.minusMonths(1)
            }) {
                Icon(Icons.Default.ArrowBack, contentDescription = "Previous Month")
            }

            Text(
                text = currentDate.value.format(DateTimeFormatter.ofPattern("MMMM yyyy")),
                style = MaterialTheme.typography.h5,
                modifier = Modifier.padding(8.dp)
            )

            IconButton(onClick = {
                currentDate.value = currentDate.value.plusMonths(1)
            }) {
                Icon(Icons.Default.ArrowForward, contentDescription = "Next Month")
            }
        }



        // Days of week header
        Row(modifier = Modifier.fillMaxWidth()) {
            daysOfWeek.forEach { day ->
                Text(
                    text = day,
                    modifier = Modifier
                        .weight(1f)
                        .padding(4.dp),
                    textAlign = TextAlign.Center,
                    color = MaterialTheme.colors.primary
                )
            }
        }


        // Calendar days grid
        LazyVerticalGrid(
            columns = GridCells.Fixed(7),
            modifier = Modifier.weight(1f)
        ) {
            items(getCalendarDays(currentDate.value)) { day ->
                val dayEvents = tripEvents.filter {
                    !day.isBefore(it.startDate) && !day.isAfter(it.endDate)
                }
                val isSelected = day == selectedDay
                val isToday = day == LocalDate.now()

                DayCell(
                    day = day,
                    isCurrentMonth = day.month == currentDate.value.month,
                    isToday = isToday,
                    isSelected = isSelected,  // Pass selection state
                    trips = dayEvents,
                    onClick = { selectedDay = day },
                    modifier = Modifier
                        .aspectRatio(1f)
                        .padding(2.dp)
                )
            }
        }

        selectedDay?.let { day ->
            val daysTrips = tripList.filter { trip ->
                val (start, end) = parseDateRange(trip.dateRange)
                !day.isBefore(start) && !day.isAfter(end)
            }

            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(8.dp)
                    .animateContentSize()
            ) {
                Text(
                    text = day.format(DateTimeFormatter.ofPattern("EEEE, MMM d")),
                    style = MaterialTheme.typography.h6,
                    color = MaterialTheme.colors.primary
                )

                Spacer(modifier = Modifier.height(8.dp))

                if (daysTrips.isEmpty()) {
                    Text("No trips on this day", fontStyle = FontStyle.Italic)
                } else {
                    daysTrips.forEach { trip ->
                        TripDayDetailItem(trip = trip)
                    }
                }
            }
        }
    }
}



private fun getCalendarDays(date: LocalDate): List<LocalDate> {
    val yearMonth = YearMonth.from(date)
    val firstDayOfMonth = yearMonth.atDay(1)
    val firstDayOfWeek = firstDayOfMonth.dayOfWeek.value % 7  // Monday start

    return List(42) { index ->  // 6 weeks * 7 days
        when {
            index < firstDayOfWeek -> firstDayOfMonth.minusDays((firstDayOfWeek - index).toLong())
            index >= firstDayOfWeek + yearMonth.lengthOfMonth() ->
                firstDayOfMonth.plusDays((index - firstDayOfWeek).toLong())
            else -> firstDayOfMonth.plusDays((index - firstDayOfWeek).toLong())
        }
    }
}

@Composable
fun DayCell(
    day: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    isSelected: Boolean,  // New parameter
    trips: List<TripEvent>,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clickable(onClick = onClick)
            .background(
                color = when {
                    isSelected -> MaterialTheme.colors.primary.copy(alpha = 0.3f)
                    isToday -> MaterialTheme.colors.primary.copy(alpha = 0.2f)
                    !isCurrentMonth -> Color.LightGray.copy(alpha = 0.3f)
                    else -> Color.Transparent
                },
                shape = RoundedCornerShape(8.dp)
            )
            .border(
                width = 2.dp,
                color = if (isSelected) MaterialTheme.colors.primary
                else if (isCurrentMonth) Color.LightGray
                else Color.Transparent,
                shape = RoundedCornerShape(8.dp)
            )
    ) {
        Column {
            Text(
                text = day.dayOfMonth.toString(),
                modifier = Modifier
                    .align(Alignment.End)
                    .padding(4.dp),
                color = when {
                    !isCurrentMonth -> Color.Gray
                    isToday -> MaterialTheme.colors.primary
                    else -> MaterialTheme.colors.onBackground
                }
            )

            // Trip indicators
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 4.dp),
                horizontalArrangement = Arrangement.SpaceEvenly
            ) {
                trips.take(3).forEach { trip ->
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(trip.color)
                    )
                }
            }

        }
    }
}

