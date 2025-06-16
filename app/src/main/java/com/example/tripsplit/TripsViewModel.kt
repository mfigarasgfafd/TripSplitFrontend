package com.example.tripsplit

import android.util.Log
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch


class TripsViewModel : ViewModel() {
    private val apiService = NetworkClient.apiService

    val trips = mutableStateListOf<Trip>()
    val isLoading = mutableStateOf(false)
    val errorState = mutableStateOf<String?>(null)

    fun loadUserTrips(userId: Int, apiKey: String) {
        if (isLoading.value) return

        viewModelScope.launch {
            isLoading.value = true
            errorState.value = null

            try {
                trips.clear()

                // Fetch groups from backend
                val groupsResponse = apiService.getGroups(apiKey)
                if (groupsResponse.isSuccessful) {
                    val groups = groupsResponse.body() ?: emptyList()

                    // Filter groups for this user
                    val userGroups = groups.filter { group ->
                        group.owner == userId || userId in group.members
                    }

                    // Convert groups to trips
                    userGroups.forEach { group ->
                        trips.add(convertGroupToTrip(group))
                    }

                    // If no groups, add mock trips
                    if (trips.isEmpty()) {
                        trips.addAll(getMockTrips())
                    }
                } else {
                    errorState.value = "Failed to load groups: ${groupsResponse.message()}"
                }
            } catch (e: Exception) {
                errorState.value = "Network error: ${e.message}"
                Log.e("TripsViewModel", "Error loading trips", e)
            } finally {
                isLoading.value = false
            }
        }
    }

    private fun convertGroupToTrip(group: Group): Trip {
        // Generate placeholder data
        val dateRange = when (group.id % 3) {
            0 -> "Jul 15 - Aug 2"
            1 -> "Dec 1 - Jan 15"
            else -> "Feb 10 - Feb 12"
        }

        val nextActivity = when (group.id % 4) {
            0 -> "Flight booking"
            1 -> "Visa applications"
            2 -> "Equipment rental"
            else -> "Hotel reservation"
        }

        val progress = when (group.id % 5) {
            0 -> 0.2f
            1 -> 0.4f
            2 -> 0.6f
            3 -> 0.8f
            else -> 1.0f
        }

        val imageResId = when (group.id % 3) {
            0 -> R.drawable.europe
            1 -> R.drawable.zhongnahai
            else -> R.drawable.ski
        }

        return Trip(
            id = group.id.toString(),
            name = group.name,
            dateRange = dateRange,
            nextActivity = nextActivity,
            progress = progress,
            imageResId = imageResId,
            isFromBackend = true
        )
    }

    private fun getMockTrips(): List<Trip> {
        return listOf(
            Trip("1", "Summer Europe Trip", "Jul 15 - Aug 2", "Flight booking", 0.4f, R.drawable.europe),
            Trip("2", "Asia Backpacking", "Dec 1 - Jan 15", "Visa applications", 0.2f, R.drawable.zhongnahai),
            Trip("3", "Weekend Ski Trip", "Feb 10 - Feb 12", "Equipment rental", 0.8f, R.drawable.ski)
        )
    }
}