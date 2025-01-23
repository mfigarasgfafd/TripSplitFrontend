package com.example.tripsplit

import android.R.attr.data
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.painterResource
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation


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

    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        Button(
            onClick = { showJoinDialog = true },
            modifier = Modifier.padding(16.dp)
        ) {
            Text(text = "Join with a code")
        }

        if (showJoinDialog) {
            JoinCodeDialog(
                onDismiss = { showJoinDialog = false },
                onCodeEntered = { code ->
                    showJoinDialog = false
                     Log.d("TUTAJ","Entered code: $code")

                }
            )
        }
        // Filters Section
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Button(onClick = { /* Filter0 action */ }) { Text("Filter0") }
            Button(onClick = { /* Filter1 action */ }) { Text("Filter1") }
            Button(onClick = { /* Filter2 action */ }) { Text("Filter2") }
            Button(onClick = { /* Filter3 action */ }) { Text("Filter3") }
        }

        // Sorting (later..)
        Text(
            text = "Sorting by Name",
            modifier = Modifier.padding(start = 16.dp)
        )

        // List of Trips
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(8.dp)
        ) {
            items(listOf("Joined Trip 1", "Joined Trip 2", "Joined Trip 3")) { trip ->
                TripItem(tripName = trip, onTripClick = {
                    tripsViewModel.selectTrip(it) // Set elected trip
                    navController.navigate("this_trip") // "This Trip" screen
                })
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
        onDismissRequest = { onDismiss() },
        title = { Text(text = "Enter Code") },
        text = {
            OutlinedTextField(
                value = code,
                onValueChange = { code = it },
                label = { Text("Code") },
                singleLine = true
            )
        },
        confirmButton = {
            TextButton(
                onClick = {
                    onCodeEntered(code.text) // Pass the entered code to the callback
                }
            ) {
                Text("OK")
            }
        },
        dismissButton = {
            TextButton(
                onClick = { onDismiss() }
            ) {
                Text("Cancel")
            }
        }
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
    val tripsViewModel: TripsViewModel = viewModel() // Get ViewModel

    Scaffold(
        bottomBar = { BottomNavBar(navController, tripsViewModel) }
    ) { innerPadding ->
        NavHost(navController = navController, startDestination = "my_trips", Modifier.padding(innerPadding)) {
            composable("my_trips") { MyTripsScreen(navController, tripsViewModel) }  // Pass ViewModel
            composable("this_trip") { ThisTripScreen(tripsViewModel) }  // Pass ViewModel to "This Trip"
            composable("my_profile") { ProfileScreenWrapper() }
        }
    }
}


@Composable
fun BottomNavBar(navController: NavHostController, tripsViewModel: TripsViewModel) {
    BottomNavigation(
        backgroundColor = Color(0xFFF0F0F0),
        contentColor = Color.Black
    ) {
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Home, contentDescription = "My Trips") },
            label = { Text("My Trips") },
            selected = navController.currentBackStackEntry?.destination?.route == "my_trips",
            onClick = {
                navController.navigate("my_trips") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "Calendar") }, // implement calendar instead of "this trip" screen
            label = { Text("Calendar") },
            selected = navController.currentBackStackEntry?.destination?.route == "this_trip",
            onClick = {
                navController.navigate("this_trip") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
        BottomNavigationItem(
            icon = { Icon(Icons.Filled.Person, contentDescription = "My Profile") },
            label = { Text("My Profile") },
            selected = navController.currentBackStackEntry?.destination?.route == "my_profile",
            onClick = {
                navController.navigate("my_profile") {
                    popUpTo(navController.graph.startDestinationId)
                    launchSingleTop = true
                }
            }
        )
    }
}



@Composable
fun ThisTripScreen(tripsViewModel: TripsViewModel) {
    // Observe currently selected trip
    val selectedTrip by tripsViewModel.selectedTrip.observeAsState("Most Recent Trip")

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        // Example layout based on your screenshot
        Text(text = selectedTrip, style = MaterialTheme.typography.h4)

        Text(text = "Amazing Trip Group", style = MaterialTheme.typography.h6)
        Text(text = "Trip group description", style = MaterialTheme.typography.body2)

        // later: add other elements like trip details, people involved, budget, calendar etc.
        LazyColumn {
            items(listOf("Person1", "Person2", "Person3")) { person ->
                PersonItem(person)
            }
        }
    }
}

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
    // track login session and user email
    var isLoggedIn by remember { mutableStateOf(false) }
    var userEmail by remember { mutableStateOf("") }

    if (isLoggedIn) {
        // Show profile screen with the logged in email
        ProfileScreen(email = userEmail)
    } else {
        // Show the Login screen if not logged in
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
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Log In", fontSize = 24.sp, modifier = Modifier.padding(bottom = 24.dp))

        // Email Field
        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Password Field
        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            modifier = Modifier.fillMaxWidth(),
            visualTransformation = PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Login Button
        Button(
            onClick = {
                onLogin(email) // Pass the email back to the parent
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(text = "Log In")
        }
    }
}

@Composable
fun ProfileScreen(email: String) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Top
    ) {
        // Profile Picture
        Image(
            painter = painterResource(id = R.drawable.placeholder), // Replace with actual profile image resource
            contentDescription = "Profile Picture",
            modifier = Modifier
                .size(100.dp)
                .clip(CircleShape)
        )

        Spacer(modifier = Modifier.height(16.dp))

        // temp profile name
        Text(text = "Ed Wood", fontSize = 24.sp, color = Color.Black)

        // Profile Email (Dynamic)
        Text(text = email, fontSize = 16.sp, color = Color.Gray)

        Spacer(modifier = Modifier.height(24.dp))

        // Options Section
        ProfileOption("Account Settings")
        ProfileOption("Notification Preferences")
        ProfileOption("Privacy & Security")
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