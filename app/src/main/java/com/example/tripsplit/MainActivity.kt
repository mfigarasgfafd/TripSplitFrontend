package com.example.tripsplit
import org.json.JSONObject
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.rememberDatePickerState
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.time.format.DateTimeFormatter
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
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.window.Dialog
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.navigation.NavGraph.Companion.findStartDestination
import androidx.navigation.compose.currentBackStackEntryAsState
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.security.cert.X509Certificate
import java.text.SimpleDateFormat
//import java.time.LocalDate
import java.time.Year
import java.time.YearMonth
import java.time.ZoneOffset
import java.time.temporal.ChronoUnit
//import java.time.format.DateTimeFormatter
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





data class Trip(
    val id: String,
    val name: String,
    val dateRange: String,
    val nextActivity: String,
    val progress: Float,
    val imageResId: Int,
    val isFromBackend: Boolean = false // To distinguish mock vs real trips
)

data class TripEvent(
    val id: Int,
    val title: String,
    val startDate: LocalDate,
    val endDate: LocalDate,
    val color: Color
)
object AuthStateManager {
    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "auth_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_NAME = "user_name"

    fun saveAuthState(context: Context, token: String, userId: Int, email: String, name: String) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit()
            .putString(KEY_TOKEN, token)
            .putInt(KEY_USER_ID, userId)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_NAME, name)
            .apply()
    }

    fun getToken(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_TOKEN, null)
    }

    fun getUserId(context: Context): Int {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getInt(KEY_USER_ID, 0)
    }

    fun getUserEmail(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_EMAIL, null)
    }

    fun getUserName(context: Context): String? {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        return prefs.getString(KEY_USER_NAME, null)
    }

    fun clearAuthState(context: Context) {
        val prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
        prefs.edit().clear().apply()
    }

    fun isLoggedIn(context: Context): Boolean {
        return getToken(context) != null
    }
}



fun String.toLocalDate(): LocalDate {
    val formatterWithYear = DateTimeFormatter.ofPattern("MMM d yyyy", Locale.ENGLISH)
    val formatterWithoutYear = DateTimeFormatter.ofPattern("MMM d", Locale.ENGLISH)

    return if (this.trim().length > 6) {  // Checks if the string already includes a year
        LocalDate.parse(this, formatterWithYear)
    } else {
        LocalDate.parse("$this ${Year.now().value}", formatterWithYear)
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DatePickerDialog(
    onDismissRequest: () -> Unit,
    onDateSelected: (LocalDate) -> Unit,
    initialDate: LocalDate = LocalDate.now()
) {
    val datePickerState = rememberDatePickerState(
        initialSelectedDateMillis = initialDate
            .atStartOfDay(ZoneOffset.UTC)  // Use UTC timezone
            .toInstant()
            .toEpochMilli()
    )

    Dialog(onDismissRequest = onDismissRequest) {
        Card(
            modifier = Modifier
                .wrapContentSize()
                .padding(16.dp),
            shape = RoundedCornerShape(16.dp),
            elevation = 8.dp
        ) {
            Column(
                modifier = Modifier.padding(8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                DatePicker(
                    state = datePickerState,
                    modifier = Modifier.wrapContentWidth(),
                    title = {
                        Text(
                            "Select date",
                            modifier = Modifier.padding(12.dp),
                            style = MaterialTheme.typography.h6
                        )
                    }
                )

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(8.dp),
                    horizontalArrangement = Arrangement.End
                ) {
                    TextButton(onClick = onDismissRequest) {
                        Text("Cancel")
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Button(
                        onClick = {
                            datePickerState.selectedDateMillis?.let { millis ->
                                val date = Instant.ofEpochMilli(millis)
                                    .atZone(ZoneOffset.UTC)  // UTC timezone
                                    .toLocalDate()
                                onDateSelected(date)
                            }
                            onDismissRequest()
                        },
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("OK")
                    }
                }
            }
        }
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
        data class Success(val token: String, val email: String, val name: String) : AuthState()
        data class Error(val message: String) : AuthState()
    }

    private val _authState = mutableStateOf<AuthState>(AuthState.Idle)
    val authState: State<AuthState> = _authState

    // Add function to fetch user ID (later)
    private suspend fun fetchUserId(token: String, email: String): Int {
        // This is a placeholder - implement your actual user ID endpoint
        return 1 // For demo purposes
    }

    fun registerUser(context: Context, name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = NetworkClient.apiService.register(
                    AuthRequest(name = name, email = email, password = password)
                )
                handleAuthResponse(context, response, name, email)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    fun loginUser(context: Context, name: String, email: String, password: String) {
        viewModelScope.launch {
            _authState.value = AuthState.Loading
            try {
                val response = NetworkClient.apiService.login(
                    AuthRequest(name = name, email = email, password = password)
                )
                handleAuthResponse(context, response, name, email)
            } catch (e: Exception) {
                _authState.value = AuthState.Error("Network error: ${e.message}")
            }
        }
    }

    private suspend fun handleAuthResponse(
        context: Context,
        response: Response<ResponseBody>,
        name: String,
        email: String
    ) {
        if (response.isSuccessful) {
            val token = response.body()?.string()?.trim() ?: ""
            if (token.isNotEmpty()) {
                // Fetch user ID - you need to implement this API endpoint
                val userId = fetchUserId(token, email)

                // Save authentication state
                AuthStateManager.saveAuthState(context, token, userId, email, name)

                _authState.value = AuthState.Success(token, email, name)
            } else {
                _authState.value = AuthState.Error("Authentication failed: Empty token")
            }
        } else {
            val errorBody = try {
                response.errorBody()?.string() ?: "Unknown error"
            } catch (e: Exception) {
                "Failed to read error response"
            }
            _authState.value = AuthState.Error("Authentication failed: ${response.code()} - $errorBody")
        }
    }
}

class GroupViewModel : ViewModel() {
    private val apiService = NetworkClient.apiService
    private val _allGroups = mutableStateListOf<Group>()
    val allGroups: List<Group> = _allGroups

    val users = mutableStateListOf<User>()
    val createGroupState = mutableStateOf<CreateGroupState>(CreateGroupState.Idle)
    val joinGroupState = mutableStateOf<JoinGroupState>(JoinGroupState.Idle)

    sealed class CreateGroupState {
        object Idle : CreateGroupState()
        object Loading : CreateGroupState()
        data class Success(val message: String) : CreateGroupState()
        data class Error(val message: String) : CreateGroupState()
    }

    sealed class JoinGroupState {
        object Idle : JoinGroupState()
        object Loading : JoinGroupState()
        data class Success(val message: String) : JoinGroupState()
        data class Error(val message: String) : JoinGroupState()
    }


    suspend fun loadAllGroups(apiKey: String) {
        try {
            val response = NetworkClient.apiService.getUserGroups(apiKey)
            if (response.isSuccessful) {
                _allGroups.clear()
                response.body()?.let { groups ->
                    _allGroups.addAll(groups)
                }
            } else {
                Log.e("GroupViewModel", "Failed to load groups: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("GroupViewModel", "Error loading groups", e)
        }
    }

    fun getGroupById(groupId: Int): Group? {
        return _allGroups.find { it.id == groupId }
    }

    fun loadUsers(apiKey: String) {
        viewModelScope.launch {
            try {
                val response = apiService.getUsers(apiKey)
                if (response.isSuccessful) {
                    users.clear()
                    users.addAll(response.body() ?: emptyList())
                } else {
                    Log.e("GroupViewModel", "Failed to load users: ${response.message()}")
                }
            } catch (e: Exception) {
                Log.e("GroupViewModel", "Error loading users", e)
            }
        }
    }

    private val _expenses = mutableStateListOf<Expense>()
    val expenses: List<Expense> = _expenses

    private val _transactions = mutableStateListOf<Transaction>()
    val transactions: List<Transaction> = _transactions

    private val _totalSpent = mutableStateOf(0.0)
    val totalSpent: State<Double> = _totalSpent

    private val _isCalculating = mutableStateOf(false)
    val isCalculating: State<Boolean> = _isCalculating

    private val _addExpenseState = mutableStateOf<AddExpenseState>(AddExpenseState.Idle)
    val addExpenseState: State<AddExpenseState> = _addExpenseState

    sealed class AddExpenseState {
        object Idle : AddExpenseState()
        object Loading : AddExpenseState()
        data class Success(val message: String) : AddExpenseState()
        data class Error(val message: String) : AddExpenseState()
    }

    suspend fun calculateGroup(apiKey: String, groupId: Int) {
        _isCalculating.value = true
        try {
            val response = apiService.calculateGroup(apiKey, CalculateRequest(groupId))
            if (response.isSuccessful) {
                val result = response.body()
                if (result != null) {
                    _expenses.clear()
                    _expenses.addAll(result.expenses)
                    _transactions.clear()
                    _transactions.addAll(result.transactions)
                    _totalSpent.value = result.totalSpent
                }
            } else {
                Log.e("GroupViewModel", "Calculate error: ${response.errorBody()?.string()}")
            }
        } catch (e: Exception) {
            Log.e("GroupViewModel", "Calculate error", e)
        } finally {
            _isCalculating.value = false
        }
    }

    fun addExpense(apiKey: String, groupId: Int, expense: Expense) {
        viewModelScope.launch {
            _addExpenseState.value = AddExpenseState.Loading
            try {
                val request = AddExpenseRequest(expense, groupId)
                val response = apiService.addExpense(apiKey, request)

                if (response.isSuccessful) {
                    _addExpenseState.value = AddExpenseState.Success("Expense added successfully")
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _addExpenseState.value = AddExpenseState.Error("Failed to add expense: $errorBody")
                }
            } catch (e: Exception) {
                _addExpenseState.value = AddExpenseState.Error("Network error: ${e.message}")
            }
        }
    }


    fun createGroup(
        apiKey: String,
        name: String,
        description: String? = null,
        location: String? = null,
        startDate: String? = null,
        endDate: String? = null
    ) {
        viewModelScope.launch {
            createGroupState.value = CreateGroupState.Loading
            try {
                val request = CreateGroupRequest(
                    name = name,
                    description = description,
                    location = location,
                    groupStartDate = startDate,
                    groupEndDate = endDate
                )

                val response = apiService.createGroup(apiKey, request)

                if (response.isSuccessful) {
                    // Handle both possible responses: JSON or plain text
                    val responseBody = response.body()?.string() ?: ""
                    if (responseBody.startsWith("Group created")) {
                        // This is the success text response
                        createGroupState.value = CreateGroupState.Success("Group created successfully")
                    } else {
                        try {
                            // Try to parse as JSON
                            val group = Gson().fromJson(responseBody, Group::class.java)
                            createGroupState.value = CreateGroupState.Success("Group created: ${group.name}")
                        } catch (e: Exception) {
                            // Fallback to text response
                            createGroupState.value = CreateGroupState.Success(responseBody)
                        }
                    }
                } else {
                    // Handle error responses
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    createGroupState.value = CreateGroupState.Error("Failed to create group: $errorBody")
                }
            } catch (e: Exception) {
                createGroupState.value = CreateGroupState.Error("Network error: ${e.message}")
            }
        }
    }

    fun joinGroup(apiKey: String, groupId: Int) {
        viewModelScope.launch {
            joinGroupState.value = JoinGroupState.Loading
            try {
                val response = apiService.joinGroup(apiKey, JoinGroupRequest(groupId))
                if (response.isSuccessful) {
                    // API returns a boolean value
                    val success = response.body()?.string().toBoolean()
                    if (success) {
                        joinGroupState.value = JoinGroupState.Success("Successfully joined group")
                    } else {
                        joinGroupState.value = JoinGroupState.Error("Failed to join group")
                    }
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    joinGroupState.value = JoinGroupState.Error("Failed to join group: $errorBody")
                }
            } catch (e: Exception) {
                joinGroupState.value = JoinGroupState.Error("Network error: ${e.message}")
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
fun MyTripsScreen(
    navController: NavController,
    userId: Int,
    apiKey: String,
    tripsViewModel: TripsViewModel = viewModel(),
    groupViewModel: GroupViewModel = viewModel()
) {
    var showJoinDialog by remember { mutableStateOf(false) }
    var showCreateGroupDialog by remember { mutableStateOf(false) }
    val context = LocalContext.current


    // Load users when screen appears or API key changes
    LaunchedEffect(apiKey) {
        groupViewModel.loadUsers(apiKey)
    }


    // Load trips when screen appears or userId/apiKey changes
    LaunchedEffect(apiKey) {
        if (apiKey.isNotEmpty()) {
            tripsViewModel.loadUserTrips(apiKey) // Only pass apiKey
        }
    }



    LaunchedEffect(groupViewModel.createGroupState.value) {
        when (val state = groupViewModel.createGroupState.value) {
            is GroupViewModel.CreateGroupState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                showCreateGroupDialog = false
                tripsViewModel.loadUserTrips(apiKey) // Refresh trips
                groupViewModel.createGroupState.value = GroupViewModel.CreateGroupState.Idle
            }

            is GroupViewModel.CreateGroupState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                groupViewModel.createGroupState.value = GroupViewModel.CreateGroupState.Idle
            }

            else -> {}
        }
    }
    LaunchedEffect(groupViewModel.joinGroupState.value) {
        when (val state = groupViewModel.joinGroupState.value) {
            is GroupViewModel.JoinGroupState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                tripsViewModel.loadUserTrips(apiKey) // Refresh trips
                groupViewModel.joinGroupState.value = GroupViewModel.JoinGroupState.Idle
            }
            is GroupViewModel.JoinGroupState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
                groupViewModel.joinGroupState.value = GroupViewModel.JoinGroupState.Idle
            }
            else -> {}
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(BackgroundColor)
    ) {
        // Header Section
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

            Row {
                // Create Group button
                IconButton(
                    onClick = { showCreateGroupDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.ic_create_group),
                        contentDescription = "Create Group",
                        tint = PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }

                // Join Trip button
                IconButton(
                    onClick = { showJoinDialog = true },
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        painter = painterResource(R.drawable.join),
                        contentDescription = "Join Trip",
                        tint = PrimaryColor,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }

        // Error state
        tripsViewModel.errorState.value?.let { error ->
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = error,
                    color = Color.Red,
                    textAlign = TextAlign.Center
                )
            }
        }

        // Loading indicator
        if (tripsViewModel.isLoading.value) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = PrimaryColor)
            }
        }
        // Show trips
        else if (tripsViewModel.trips.isNotEmpty()) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                items(tripsViewModel.trips) { trip ->
                    TripCard(
                        trip = trip,
                        onTripClick = {
                            navController.navigate("this_trip/${trip.id}")
                        }
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }
        }
        // Empty state
        else {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text("No trips found", style = MaterialTheme.typography.h6)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "Create a new group or join an existing one to get started",
                        textAlign = TextAlign.Center
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Button(
                        onClick = { showCreateGroupDialog = true },
                        colors = ButtonDefaults.buttonColors(backgroundColor = PrimaryColor)
                    ) {
                        Text("Create Group", color = Color.White)
                    }
                }
            }
        }

        // Dialogs
        if (showJoinDialog) {
            JoinCodeDialog(
                onDismiss = { showJoinDialog = false },
                onCodeEntered = { code ->
                    showJoinDialog = false
                    try {
                        val groupId = code.toInt()
                        groupViewModel.joinGroup(apiKey, groupId)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(context, "Invalid group code", Toast.LENGTH_LONG).show()
                    }
                }
            )
        }

        if (showCreateGroupDialog) {
            CreateGroupDialog(
                onDismiss = { showCreateGroupDialog = false },
                onCreate = { name, description, location, startDate, endDate ->
                    groupViewModel.createGroup(
                        apiKey = apiKey,
                        name = name,
                        description = description,
                        location = location,
                        startDate = startDate,
                        endDate = endDate
                    )
                },
                isLoading = groupViewModel.createGroupState.value is GroupViewModel.CreateGroupState.Loading
            )
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
fun CreateGroupDialog(
    onDismiss: () -> Unit,
    onCreate: (String, String, String, String, String) -> Unit,
    isLoading: Boolean
) {
    var groupName by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var location by remember { mutableStateOf("") }

    // State for dates
    var startDate by remember { mutableStateOf<LocalDate?>(null) }
    var endDate by remember { mutableStateOf<LocalDate?>(null) }

    // State for date picker visibility
    var showStartDatePicker by remember { mutableStateOf(false) }
    var showEndDatePicker by remember { mutableStateOf(false) }

    // Date formatters
    val displayFormatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
    val backendFormatter = DateTimeFormatter.ISO_INSTANT

    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("Create New Group", color = PrimaryColor) },
        text = {
            Column {
                // Group Name
                TextField(
                    value = groupName,
                    onValueChange = { groupName = it },
                    label = { Text("Group Name*") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Description
                TextField(
                    value = description,
                    onValueChange = { description = it },
                    label = { Text("Description") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(8.dp))

                // Location
                TextField(
                    value = location,
                    onValueChange = { location = it },
                    label = { Text("Location") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = !isLoading
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Start Date Picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("Start Date:", style = MaterialTheme.typography.body1)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = startDate?.format(displayFormatter) ?: "Not selected",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { showStartDatePicker = true },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Select")
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // End Date Picker
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text("End Date:", style = MaterialTheme.typography.body1)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = endDate?.format(displayFormatter) ?: "Not selected",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.weight(1f)
                    )
                    Button(
                        onClick = { showEndDatePicker = true },
                        enabled = !isLoading,
                        colors = ButtonDefaults.buttonColors(
                            backgroundColor = PrimaryColor,
                            contentColor = Color.White
                        )
                    ) {
                        Text("Select")
                    }
                }

                // Date validation
                if (startDate != null && endDate != null) {
                    if (startDate!!.isAfter(endDate)) {
                        Text(
                            "End date must be after start date",
                            color = Color.Red,
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    } else {
                        Text(
                            "Duration: ${ChronoUnit.DAYS.between(startDate, endDate) + 1} days",
                            style = MaterialTheme.typography.caption,
                            modifier = Modifier.padding(top = 4.dp)
                        )
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    val formattedStart = startDate?.atStartOfDay(ZoneOffset.UTC)?.format(backendFormatter) ?: ""
                    val formattedEnd = endDate?.atStartOfDay(ZoneOffset.UTC)?.format(backendFormatter) ?: ""
                    onCreate(groupName, description, location, formattedStart, formattedEnd)
                },
                enabled = !isLoading &&
                        groupName.isNotBlank() &&
                        startDate != null &&
                        endDate != null &&
                        (startDate!!.isBefore(endDate) || startDate!!.isEqual(endDate)),
                colors = ButtonDefaults.buttonColors(
                    backgroundColor = PrimaryColor,
                    contentColor = Color.White
                )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = Color.White
                    )
                } else {
                    Text("Create", color = Color.White)
                }
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                enabled = !isLoading
            ) {
                Text("Cancel", color = PrimaryColor)
            }
        }
    )

    // Date picker dialogs
    if (showStartDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showStartDatePicker = false },
            onDateSelected = { date ->
                startDate = date
                showStartDatePicker = false
                // Auto-set end date if not set
                if (endDate == null || date.isAfter(endDate)) {
                    endDate = date.plusDays(1)
                }
            },
            initialDate = startDate ?: LocalDate.now()
        )
    }

    if (showEndDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showEndDatePicker = false },
            onDateSelected = { date ->
                endDate = date
                showEndDatePicker = false
            },
            initialDate = endDate ?: (startDate ?: LocalDate.now()).plusDays(1)
        )
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
fun TripsApp() {
    val navController = rememberNavController()
    val context = LocalContext.current
    val isLoggedIn = remember { mutableStateOf(AuthStateManager.isLoggedIn(context)) }

    Scaffold(
        bottomBar = { BottomNavBar(navController) }
    ) { innerPadding ->
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn.value) "my_trips" else "login",
            modifier = Modifier.padding(innerPadding)
        ) {
            composable("my_trips") {
                if (isLoggedIn.value) {
                    MyTripsScreen(
                        navController = navController,
                        userId = AuthStateManager.getUserId(context),
                        apiKey = AuthStateManager.getToken(context) ?: ""
                    )
                } else {
                    LaunchedEffect(Unit) {
                        navController.navigate("login") {
                            popUpTo("my_trips") { inclusive = true }
                        }
                    }
                }
            }
            composable("calendar") { CalendarScreen(emptyList()) }
            composable("my_profile") {
                ProfileScreenWrapper(
                    onLogout = {
                        isLoggedIn.value = false
                        navController.navigate("login") {
                            popUpTo("my_profile") { inclusive = true }
                        }
                    },
                    context = context
                )
            }
            composable("this_trip/{tripId}") { backStackEntry ->
                val context = LocalContext.current
                val tripId = backStackEntry.arguments?.getString("tripId")
                val apiKey = AuthStateManager.getToken(context) ?: "" // No need for remember

                ThisTripScreen(navController = navController, tripId = tripId, apiKey = apiKey)
            }
            composable("login") {
                LoginScreen(
                    onLoginSuccess = {
                        isLoggedIn.value = true
                        navController.navigate("my_trips") {
                            popUpTo("login") { inclusive = true }
                        }
                    },
                    context = context
                )
            }
        }
    }
}

@Composable
fun BottomNavBar(navController: NavHostController) {
    val navItems = listOf(
        NavItem("My Trips", R.drawable.trips, "my_trips"),
        NavItem("Calendar", R.drawable.newcalendar, "calendar"),
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
                            // Pop up to the start destination of the graph to
                            // avoid building up a large stack of destinations
                            popUpTo(navController.graph.findStartDestination().id) {
                                saveState = true
                            }
                            // Avoid multiple copies of the same destination when
                            // reselecting the same item
                            launchSingleTop = true
                            // Restore state when reselecting a previously selected item
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
fun ExpenseItem(expense: Expense) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        elevation = 2.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    expense.description,
                    fontWeight = FontWeight.Bold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Text(
                    "${"%.2f".format(expense.amount)} PLN",
                    fontWeight = FontWeight.Bold,
                    color = PrimaryColor
                )
            }

            Spacer(modifier = Modifier.height(8.dp))

            Text("Paid by: User ${expense.payerId}")

            Text(
                "Participants: ${expense.participantsIds.joinToString(", ") { "User $it" }}",
                maxLines = 2,
                overflow = TextOverflow.Ellipsis
            )

            Text(
                "Date: ${expense.date}",
                style = MaterialTheme.typography.caption,
                color = Color.Gray
            )
        }
    }
}


@Composable
fun TransactionItem(transaction: Transaction) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        backgroundColor = Color.LightGray.copy(alpha = 0.1f),
        elevation = 1.dp
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = "User ${transaction.payerId} owes",
                    style = MaterialTheme.typography.body1
                )
                Text(
                    text = "User ${transaction.receiverId}",
                    fontWeight = FontWeight.Bold
                )
            }
            Text(
                text = "${"%.2f".format(transaction.amount)} PLN",
                fontWeight = FontWeight.Bold,
                color = PrimaryColor
            )
        }
    }
}

@Composable
fun ThisTripScreen(
    navController: NavController,
    tripId: String?,
    apiKey: String
) {
    val groupViewModel: GroupViewModel = viewModel()
    val groupState = remember { mutableStateOf<Group?>(null) }
    val isLoading = remember { mutableStateOf(false) }
    val errorState = remember { mutableStateOf<String?>(null) }
    val context = LocalContext.current

    // Fetch group details when screen opens
    LaunchedEffect(tripId) {
        if (tripId != null) {
            isLoading.value = true
            try {
                // First load all groups
                groupViewModel.loadAllGroups(apiKey)

                // Then find the specific group
                val group = groupViewModel.getGroupById(tripId.toInt())

                if (group != null) {
                    groupState.value = group
                    // Calculate expenses and transactions
                    groupViewModel.calculateGroup(apiKey, tripId.toInt())
                } else {
                    errorState.value = "Group not found"
                }
            } catch (e: Exception) {
                errorState.value = "Network error: ${e.message}"
            } finally {
                isLoading.value = false
            }
        } else {
            errorState.value = "Invalid trip ID"
        }
    }
    if (groupState.value != null) {
        GroupDetailsContent(
            group = groupState.value!!,
            expenses = groupViewModel.expenses, // Pass expenses from ViewModel
            transactions = groupViewModel.transactions,
            totalSpent = groupViewModel.totalSpent.value,
            onAddExpense = { expense ->
                if (tripId != null) {
                    groupViewModel.addExpense(
                        apiKey,
                        tripId.toInt(),
                        expense
                    )
                }
            }
        )
    }
    // Handle expense state changes
    LaunchedEffect(groupViewModel.addExpenseState.value) {
        when (val state = groupViewModel.addExpenseState.value) {
            is GroupViewModel.AddExpenseState.Success -> {
                Toast.makeText(context, state.message, Toast.LENGTH_SHORT).show()
                // Refresh calculations
                if (tripId != null) {
                    groupViewModel.calculateGroup(apiKey, tripId.toInt())
                }
            }
            is GroupViewModel.AddExpenseState.Error -> {
                Toast.makeText(context, state.message, Toast.LENGTH_LONG).show()
            }
            else -> {}
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Trip Details") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.Default.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            when {
                isLoading.value -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator()
                    }
                }

                errorState.value != null -> {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(16.dp),
                        verticalArrangement = Arrangement.Center,
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        Text(
                            text = "Error Loading Group",
                            style = MaterialTheme.typography.h6,
                            color = Color.Red
                        )
                        Spacer(modifier = Modifier.height(8.dp))
                        Text(errorState.value ?: "Unknown error")
                        Spacer(modifier = Modifier.height(16.dp))
                        Text("Trip ID: ${tripId ?: "N/A"}")
                    }
                }

                groupState.value != null -> {
                    GroupDetailsContent(
                        group = groupState.value!!,
                        expenses = groupViewModel.expenses,
                        transactions = groupViewModel.transactions,
                        totalSpent = groupViewModel.totalSpent.value,
                        onAddExpense = { expense ->
                            if (tripId != null) {
                                groupViewModel.addExpense(
                                    apiKey,
                                    tripId.toInt(),
                                    expense
                                )
                            }
                        }
                    )
                }

                else -> {
                    Box(
                        modifier = Modifier.fillMaxSize(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text("Group not found", color = Color.Gray)
                    }
                }
            }
        }
    }
}


@Composable
fun GroupDetailsContent(
    group: Group,
    expenses: List<Expense>, // Passed from ViewModel
    transactions: List<Transaction>,
    totalSpent: Double,
    onAddExpense: (Expense) -> Unit
) {
    var showExpenseDialog by remember { mutableStateOf(false) }
    var expenseTitle by remember { mutableStateOf("") }
    var expenseAmount by remember { mutableStateOf("") }
    var selectedPayer by remember { mutableStateOf<Int?>(null) }
    var selectedParticipants by remember { mutableStateOf<List<Int>>(emptyList()) }
    val context = LocalContext.current

    LazyColumn(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Group Header
        item {
            Column {
                Text(
                    text = group.name ?: "Unnamed Trip",
                    style = MaterialTheme.typography.h4,
                    color = PrimaryColor,
                    modifier = Modifier.padding(bottom = 8.dp)
                )

                // Location
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.LocationOn,
                        contentDescription = "Location",
                        tint = PrimaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = group.location ?: "No location specified",
                        style = MaterialTheme.typography.body1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Date Range
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Default.DateRange,
                        contentDescription = "Dates",
                        tint = PrimaryColor
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = formatDateRange(group.groupStartDate, group.groupEndDate),
                        style = MaterialTheme.typography.body1
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Description
                Text(
                    text = "Description",
                    style = MaterialTheme.typography.h6,
                    color = PrimaryColor
                )
                Text(
                    text = group.description ?: "No description available",
                    style = MaterialTheme.typography.body1,
                    modifier = Modifier.padding(vertical = 8.dp)
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Members section
                Text(
                    text = "Members",
                    style = MaterialTheme.typography.h6,
                    color = PrimaryColor
                )
                Spacer(modifier = Modifier.height(8.dp))

                group.membersIds?.forEach { memberId ->
                    Text(
                        text = "User ID: $memberId",
                        style = MaterialTheme.typography.body1,
                        modifier = Modifier.padding(vertical = 4.dp)
                    )
                }

                Spacer(modifier = Modifier.height(24.dp))
            }
        }

        // Expenses Section
        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "Expenses (Total: ${"%.2f".format(totalSpent)} PLN)",
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
            Spacer(modifier = Modifier.height(16.dp))
        }

        // Expense List
        if (expenses.isEmpty()) {
            item {
                Text("No expenses recorded yet", modifier = Modifier.padding(8.dp))
            }
        } else {
            items(expenses) { expense ->
                ExpenseItem(expense = expense)
            }
        }

        // Transactions Header
        item {
            Spacer(modifier = Modifier.height(24.dp))
            Text(
                text = "Settlement Transactions",
                style = MaterialTheme.typography.h6,
                color = PrimaryColor
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        // Transactions List
        if (transactions.isEmpty()) {
            item {
                Text("No transactions needed", modifier = Modifier.padding(8.dp))
            }
        } else {
            items(transactions) { transaction ->
                TransactionItem(transaction = transaction)
            }
        }
    }

    // Expense Dialog
    if (showExpenseDialog) {
        AlertDialog(
            onDismissRequest = { showExpenseDialog = false },
            title = { Text("Record New Expense", color = PrimaryColor) },
            text = {
                Column {
                    TextField(
                        value = expenseTitle,
                        onValueChange = { expenseTitle = it },
                        label = { Text("Expense Title*") },
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    TextField(
                        value = expenseAmount,
                        onValueChange = { expenseAmount = it },
                        label = { Text("Amount (PLN)*") },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        modifier = Modifier.fillMaxWidth()
                    )

                    Spacer(modifier = Modifier.height(16.dp))

                    Text("Payer:", style = MaterialTheme.typography.subtitle1)

                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                            .horizontalScroll(rememberScrollState())
                    ) {
                        group.membersIds?.forEach { memberId ->
                            val isSelected = selectedPayer == memberId
                            Button(
                                onClick = { selectedPayer = memberId },
                                modifier = Modifier
                                    .padding(end = 8.dp)
                                    .border(
                                        width = if (isSelected) 2.dp else 1.dp,
                                        color = if (isSelected) PrimaryColor else Color.Gray,
                                        shape = RoundedCornerShape(8.dp)
                                    ),
                                colors = ButtonDefaults.buttonColors(
                                    backgroundColor = if (isSelected) PrimaryColor.copy(alpha = 0.1f) else Color.Transparent,
                                    contentColor = if (isSelected) PrimaryColor else Color.Black
                                ),
                                elevation = ButtonDefaults.elevation(defaultElevation = 0.dp)
                            ) {
                                Text("User $memberId")
                            }
                        }
                    }

                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Participants:", style = MaterialTheme.typography.subtitle1)

                    // Participants selection
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp)
                    ) {
                        group.membersIds?.forEach { memberId ->
                            Row(
                                verticalAlignment = Alignment.CenterVertically,
                                modifier = Modifier.padding(vertical = 4.dp)
                            ) {
                                Checkbox(
                                    checked = selectedParticipants.contains(memberId),
                                    onCheckedChange = { isChecked ->  // Fixed parameter name
                                        selectedParticipants = if (isChecked) {
                                            selectedParticipants + memberId
                                        } else {
                                            selectedParticipants - memberId
                                        }
                                    }
                                )
                                Text("User $memberId", modifier = Modifier.padding(start = 8.dp))
                            }
                        }
                    }
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        val amount = expenseAmount.toDoubleOrNull()
                        if (expenseTitle.isNotBlank() && amount != null && selectedPayer != null && selectedParticipants.isNotEmpty()) {
                            val newExpense = Expense(
                                id = 0, // Will be generated by backend
                                description = expenseTitle,
                                amount = amount,
                                payerId = selectedPayer!!,
                                participantsIds = selectedParticipants,
                                date = LocalDate.now().toString()
                            )
                            onAddExpense(newExpense)

                            // Reset form
                            expenseTitle = ""
                            expenseAmount = ""
                            selectedPayer = null
                            selectedParticipants = emptyList()

                            showExpenseDialog = false
                        } else {
                            Toast.makeText(
                                context,
                                "Please fill all fields correctly",
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    },
                    colors = ButtonDefaults.buttonColors(
                        backgroundColor = PrimaryColor,
                        contentColor = Color.White
                    )
                ) {
                    Text("Add Expense")
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

private fun formatDateRange(start: String?, end: String?): String {
    if (start == null || end == null) return "Dates not set"

    return try {
        // Parse with java.time API (recommended for API 26+)
        val startDate = LocalDate.parse(start.substringBefore("T"))
        val endDate = LocalDate.parse(end.substringBefore("T"))

        val formatter = DateTimeFormatter.ofPattern("MMM dd, yyyy")
        "${startDate.format(formatter)} - ${endDate.format(formatter)}"
    } catch (e: Exception) {
        try {
            // Fallback to SimpleDateFormat
            val parser = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
            val formatter = SimpleDateFormat("MMM dd", Locale.getDefault())

            val startDate = parser.parse(start.substringBefore("T"))
            val endDate = parser.parse(end.substringBefore("T"))

            "${formatter.format(startDate)} - ${formatter.format(endDate)}"
        } catch (e2: Exception) {
            "Invalid dates"
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
fun ProfileScreenWrapper(
    onLogout: () -> Unit,
    context: Context = LocalContext.current
) {
    val token = remember { AuthStateManager.getToken(context) }
    val userEmail = remember { AuthStateManager.getUserEmail(context) }
    val userName = remember { AuthStateManager.getUserName(context) }

    if (token != null && userEmail != null) {
        ProfileScreen(
            email = userEmail,
            name = userName ?: "User",
            onLogout = {
                AuthStateManager.clearAuthState(context)
                onLogout()
            }
        )
    } else {
        LoginScreen(
            onLoginSuccess = onLogout, // After login, we want to show profile
            context = context
        )
    }
}

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    context: Context = LocalContext.current
) {
    var name by remember { mutableStateOf("") }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var isRegistering by remember { mutableStateOf(false) }
    val authViewModel: AuthViewModel = viewModel()

    LaunchedEffect(authViewModel.authState.value) {
        when (val state = authViewModel.authState.value) {
            is AuthViewModel.AuthState.Success -> {
                onLoginSuccess()
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

        OutlinedTextField(
            value = name,
            onValueChange = { name = it },
            label = { Text("Name") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

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
                    authViewModel.registerUser(context, name, email, password)
                } else {
                    authViewModel.loginUser(context, name, email, password)
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


@Composable
fun ProfileScreen(
    email: String,
    name: String,
    onLogout: () -> Unit
) {
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
            text = name,
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

//@Composable
//fun ProfileOption(optionText: String) {
//    Column(
//        modifier = Modifier
//            .fillMaxWidth()
//            .padding(vertical = 8.dp)
//    ) {
//        Text(text = optionText, fontSize = 18.sp, color = Color.Black)
//        Divider(color = Color.LightGray, thickness = 1.dp)
//    }
//}

@Preview(showBackground = true)
@Composable
fun ProfileScreenPreview() {
    // Create a dummy context for preview
    val context = LocalContext.current

    // Create a mock SharedPreferences for preview
    val mockPrefs = context.getSharedPreferences("mock_prefs", Context.MODE_PRIVATE)
    mockPrefs.edit()
        .putString("auth_token", "dummy_token")
        .putInt("user_id", 1)
        .putString("user_email", "user@example.com")
        .putString("user_name", "John Doe")
        .apply()

    ProfileScreenWrapper(
        onLogout = {},
        context = context
    )
}

@Preview(showBackground = true)
@Composable
fun ProfileScreenLoggedOutPreview() {
    // Create a dummy context for preview
    val context = LocalContext.current

    // Clear any mock preferences
    val mockPrefs = context.getSharedPreferences("mock_prefs", Context.MODE_PRIVATE)
    mockPrefs.edit().clear().apply()

    ProfileScreenWrapper(
        onLogout = {},
        context = context
    )
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

