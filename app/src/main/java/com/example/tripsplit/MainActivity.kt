package com.example.tripsplit

import android.os.Bundle
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
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController


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
    Column(
        modifier = Modifier.fillMaxSize()
    ) {
        // Header Section
        Text(
            text = "Join with a code",
            modifier = Modifier.padding(16.dp),
            style = MaterialTheme.typography.h6
        )

        // Filters Section
        Row(
            modifier = Modifier.fillMaxWidth().padding(8.dp),
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
            modifier = Modifier.fillMaxSize().padding(8.dp)
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
            composable("my_profile") { MyProfileScreen() }
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
            icon = { Icon(Icons.Filled.Favorite, contentDescription = "This Trip") },
            label = { Text("This Trip") },
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
        modifier = Modifier.fillMaxSize().padding(16.dp)
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
fun MyProfileScreen() {
    // Empty Profile screen
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("My Profile")
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
    Column(modifier = Modifier.fillMaxWidth().padding(16.dp)) {
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
        onClick = { /* Handle Filter click */ },
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
            .clickable { /* Handle item click */ },
        elevation = 4.dp
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            // grey box placeholder for image
            Box(
                modifier = Modifier
                    .size(120.dp, 60.dp)
                    .background(Color.Gray),
                contentAlignment = Alignment.Center
            ) {
                // design space for later
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
