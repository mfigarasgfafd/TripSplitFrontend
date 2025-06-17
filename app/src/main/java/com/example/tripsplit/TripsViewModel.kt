package com.example.tripsplit

import android.util.Log
import androidx.compose.runtime.State
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.Locale


class TripsViewModel : ViewModel() {
    private val _trips = mutableStateListOf<Trip>()
    val trips: List<Trip> = _trips

    private val _isLoading = mutableStateOf(false)
    val isLoading: State<Boolean> = _isLoading

    private val _errorState = mutableStateOf<String?>(null)
    val errorState: State<String?> = _errorState

    fun loadUserTrips(apiKey: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorState.value = null
            try {
                val response = NetworkClient.apiService.getUserGroups(apiKey)
                if (response.isSuccessful) {
                    val groups = response.body() ?: emptyList()
                    _trips.clear()
                    _trips.addAll(groups.map { it.toTrip() })
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    _errorState.value = "Error ${response.code()}: $errorBody"
                    Log.e("TripsViewModel", "API error: $errorBody")
                }
            } catch (e: Exception) {
                _errorState.value = "Network error: ${e.message}"
                Log.e("TripsViewModel", "Network error", e)
            } finally {
                _isLoading.value = false
            }
        }
    }

    // Helper function to convert Group to Trip
    private fun Group.toTrip(): Trip {
        return Trip(
            id = id?.toString() ?: "0",
            name = name ?: "Unnamed Trip",
            dateRange = formatDateRange(groupStartDate, groupEndDate),
            nextActivity = "Manage expenses", // Default value
            progress = 0.5f, // Default progress
            imageResId = getRandomPlaceholderImage(),
            isFromBackend = true
        )
    }

    private fun formatDateRange(start: String?, end: String?): String {
        if (start == null || end == null) return "Dates not set"

        return try {
            // Parse with java.time API (recommended for API 26+)
            val startDate = LocalDate.parse(start.substringBefore("T"))
            val endDate = LocalDate.parse(end.substringBefore("T"))

            val formatter = DateTimeFormatter.ofPattern("MMM dd", Locale.getDefault())
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

    private fun getRandomPlaceholderImage(): Int {
        val placeholders = listOf(
            R.drawable.europe,
            R.drawable.zhongnahai,
            R.drawable.ski
        )
        return placeholders.random()
    }
}