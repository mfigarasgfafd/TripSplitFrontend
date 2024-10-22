package com.example.tripsplit

import androidx.lifecycle.ViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.LiveData

class TripsViewModel : ViewModel() {
    // LiveData to hold the currently selected trip
    private val _selectedTrip = MutableLiveData<String>()
    val selectedTrip: LiveData<String> get() = _selectedTrip

    // For demo purposes, set a default "most recent" trip
    init {
        _selectedTrip.value = "Most Recent Trip"
    }

    // Function to set the current trip
    fun selectTrip(tripName: String) {
        _selectedTrip.value = tripName
    }
}
